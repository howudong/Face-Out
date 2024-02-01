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
  }, []);

  return (
    <div className="App">

    </div>
  );
}

export default App;
