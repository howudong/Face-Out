import React, { useEffect, useRef } from 'react';
import kurentoUtils from 'kurento-utils';
import './App.css';

const PARTICIPANT_MAIN_CLASS = 'participant main';
const PARTICIPANT_CLASS = 'participant';
const WEBSOCKET_URL = process.env.REACT_APP_API_URL

class Participant {

  constructor(name, sendMessage){

  }

}


function App() {
  const ws = useRef(null);
  const participants = {};
  const name = useRef(null);
  const room = useRef(null);


  useEffect(() => {
    ws.current = new WebSocket(WEBSOCKET_URL);
    ws.current.onopen = function () {
      console.log('WebSocket connection opened.');
    };
    ws.current.onmessage = function (message) {
      var parsedMessage = JSON.parse(message.data);
      console.info('Received message: ' + message.data);

      switch (parsedMessage.id) {
        case 'existingParticipants':
          onExistingParticipants(parsedMessage);
          break;
        case 'newParticipantArrived':
          onNewParticipant(parsedMessage);
          break;
        case 'receiveVideoAnswer':
          receiveVideoResponse(parsedMessage);
          break;
        case 'iceCandidate':
          participants[parsedMessage.name].rtcPeer.addIceCandidate(parsedMessage.candidate, function (error) {
            if (error) {
              console.error("Error adding candidate: " + error);
              return;
            }
          });
          break;
        case 'participantExit':
          onParticipantLeft(parsedMessage); 
          break;
        default:
          console.error('Unrecognized message', parsedMessage);
      }
    };
  }, []);

  //등록
  const register = () => {
    name.current = document.getElementById('name').value;
    room.current = '1';
    document.getElementById('container').style.visibility = 'hidden';
    document.getElementById('leaveBtn').style.visibility = 'visible';
    const message = {
      id: 'join',
      name: name.current,
      room: room.current,
    };
    sendMessage(message);
  };

  //종료
  function leaveRoom() {
    document.getElementById('container').style.visibility = 'visible';
    document.getElementById('leaveBtn').style.visibility = 'hidden';
    console.log("out 표시")
    sendMessage({
      id: 'exit',
    });
    window.location.reload();
  }

  function sendMessage(message) {
    var jsonMessage = JSON.stringify(message);
    console.log('Sending message: ' + jsonMessage);
    if (ws.current && ws.current.readyState === WebSocket.OPEN) {
      ws.current.send(jsonMessage);
    }
  }

  return (
    <div className="App">
      <div id='container'>
        <div className='title'>😁FACE OUT😁</div>
        <input type="text" id="name" placeholder="Enter your name" />
        <input type="text" id="roomName" placeholder="Enter room name" />
        <button id="registerBtn" onClick={register}>🔑Enter🔑</button>
      </div>
      <button id="leaveBtn"onClick={leaveRoom}>🙌Leave🙌</button>
    </div>
  );
}

export default App;
