import React, { useEffect, useRef } from 'react';
import kurentoUtils from 'kurento-utils';
import './App.css';


const PARTICIPANT_MAIN_CLASS = 'participant main';
const PARTICIPANT_CLASS = 'participant';

class Participant {
  
  constructor(name, sendMessage) {
    this.name = name;
    this.container = document.createElement('div');
    this.container.className = this.isPresentMainParticipant() ? PARTICIPANT_CLASS : PARTICIPANT_MAIN_CLASS;
    this.container.id = name;
    this.span = document.createElement('span');
    this.video = document.createElement('video');
    this.rtcPeer = null;
    this.sendMessage = sendMessage;
    this.onIceCandidate = this.onIceCandidate.bind(this);


    this.container.appendChild(this.video);
    this.container.appendChild(this.span);
    this.container.onclick = this.switchContainerClass.bind(this);
    document.getElementById('participants').appendChild(this.container);

    this.span.appendChild(document.createTextNode(name));

    this.video.id = 'video-' + name;
    this.video.autoplay = true;
    this.video.controls = false;
  }

  getElement() {
    return this.container;
  }

  getVideoElement() {
    return this.video;
  }

  switchContainerClass() {
    if (this.container.className === PARTICIPANT_CLASS) {
      var elements = Array.prototype.slice.call(document.getElementsByClassName(PARTICIPANT_MAIN_CLASS));
      elements.forEach(function (item) {
        item.className = PARTICIPANT_CLASS;
      });

      this.container.className = PARTICIPANT_MAIN_CLASS;
    } else {
      this.container.className = PARTICIPANT_CLASS;
    }
  }

  isPresentMainParticipant() {
    return document.getElementsByClassName(PARTICIPANT_MAIN_CLASS).length !== 0;
  }

  offerToReceiveVideo(error, offerSdp, wp) {
    if (error) return console.error('sdp offer error');
    console.log('Invoking SDP offer callback function');
    var msg = {
      id: 'receiveVideoFrom',
      sender: this.name,
      sdpOffer: offerSdp,
    };
    this.sendMessage(msg);
  }

  onIceCandidate(candidate, wp) {
    console.log('Local candidate' + JSON.stringify(candidate));

    var message = {
      id: 'onIceCandidate',
      candidate: candidate,
      name: this.name,
    };
    this.sendMessage(message);
  }

  dispose() {
    console.log('Disposing participant ' + this.name);
    if (this.rtcPeer) {
      this.rtcPeer.dispose();
    }
    this.container.parentNode.removeChild(this.container);
  }
}

const App = () => {
  const ws = useRef(null);
  const participants = {};
  const name = useRef(null);
  const room = useRef(null);

  useEffect(() => {
    ws.current = new WebSocket('wss://focusing.site:8081/signal');
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

    return () => {
      if (ws.current) {
        ws.current.close();
      }
    };
  }, []);

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

  const onNewParticipant = (request) => {
    receiveVideo(request.name);
  };


  const receiveVideoResponse = (result) => {
    participants[result.name].rtcPeer.processAnswer(result.sdpAnswer, function (error) {
      if (error) return console.error(error);
    });
  };
  
  function onExistingParticipants(msg) {
    var constraints = {
      audio: true,
      video: {
        mandatory: {
          maxWidth: 320,
          maxFrameRate: 15,
          minFrameRate: 15,
        },
      },
    };
    console.log(name.current + ' registered in room ' + room.current);
    var participant = new Participant(name.current, sendMessage);
    participants[name.current] = participant;
    var video = participant.getVideoElement();

    console.log(participant.video)
  
    var options = {
      localVideo: video,
      mediaConstraints: constraints,
      onicecandidate: participant.onIceCandidate.bind(participant),
    };
    participant.rtcPeer = new kurentoUtils.WebRtcPeer.WebRtcPeerSendonly(options, function (error) {
      if (error) {
        return console.error(error);
      }
      this.generateOffer(participant.offerToReceiveVideo.bind(participant));
    });
  
    msg.data.forEach(receiveVideo);
  }
  
  function leaveRoom() {

    document.getElementById('container').style.visibility = 'visible';
    document.getElementById('leaveBtn').style.visibility = 'hidden';
    
    sendMessage({
      id: 'exit',
    });


    window.location.reload();
  }
  
  function receiveVideo(sender) {
    var participant = new Participant(sender, sendMessage);
    participants[sender] = participant;
    var video = participant.getVideoElement();
    console.log(participant.getVideoElement())
  
    var options = {
      remoteVideo: video,
      onicecandidate: participant.onIceCandidate.bind(participant),
    };
  
    participant.rtcPeer = new kurentoUtils.WebRtcPeer.WebRtcPeerRecvonly(options, function (error) {
      if (error) {
        return console.error(error);
      }

      console.log(participant)
      console.log(participants)
      this.generateOffer(participant.offerToReceiveVideo.bind(participant));
    });
  }
  
  function onParticipantLeft(request) {
    console.log('Participant ' + request.name + ' left');
    var participant = participants[request.name];
    participant.dispose();
    delete participants[request.name];
  }
  
  function sendMessage(message) {
    var jsonMessage = JSON.stringify(message);
    console.log('Sending message: ' + jsonMessage);
    if (ws.current && ws.current.readyState === WebSocket.OPEN) {
      ws.current.send(jsonMessage);
    }
  }

  return (
    <div>
      <div id='container'>
        <div className='title'>😁FACE OUT😁</div>
        <input type="text" id="name" placeholder="Enter your name" />
        <input type="text" id="roomName" placeholder="Enter room name" />
        <button id="registerBtn" onClick={register}>🔑Enter🔑</button>
      </div>
      <button id="leaveBtn"onClick={leaveRoom}>🙌Leave🙌</button>
      <div id='participants'>
	        {Object.values(participants).map((participant) => (
        <div key={participant.name}>
          {participant.getVideoElement()} {/* 비디오 요소 사용 */}
          <span>{participant.name}</span>
        </div>
      ))}
      </div>
    </div>
  );
};

export default App;
