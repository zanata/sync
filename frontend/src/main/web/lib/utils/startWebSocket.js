import {w3cwebsocket as W3CWebSocket} from 'websocket'
import invariant from 'invariant'

export function startWebSocket (endpoint, onMessageCallback) {
  invariant(typeof onMessageCallback === 'function', 'you need to pass in a function(obj) in second argument as callback on websocket message')
  console.log(`==== ${endpoint}`)

  let ws
  try{
    ws = new W3CWebSocket(endpoint)
  } catch (err) {
    console.error('error creating WebSocket', err)
    return null
  }

  ws.onerror = error => {
    console.error('Connect Error:', error);
    ws = null
  }

  ws.onopen = () => {
    console.info("======== websocket connected")
  }

  ws.onclose = () => {
    console.info("======== websocket disconnected")
  }

  ws.onmessage = e => {
    if (typeof e.data === 'string') {
      const message = JSON.parse(e.data)
      onMessageCallback(message)
    }
  }

  return ws
}


