import {w3cwebsocket as W3CWebSocket} from 'websocket'
import {updateJobStatus} from '../actions'


export default function (store) {
  const server = `ws://${location.host}${location.pathname}websocket/jobStatus`
  console.log(`==== ${server}`)
  const ws = new W3CWebSocket(server)

  ws.onerror = error => {
    console.error(`Connect Error: ${error.toString()}`);
  }

  ws.onopen = () => {
    console.info("======== websocket connected")
  }

  ws.onclose = () => {
    console.info("======== websocket disconnected")
  }

  ws.onmessage = e => {
    if (typeof e.data === 'string') {
      const jobStatus = JSON.parse(e.data)
      store.dispatch(updateJobStatus(jobStatus));
    }
  }

}
