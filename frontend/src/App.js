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

  return (
    <div className="App">

    </div>
  );
}

export default App;
