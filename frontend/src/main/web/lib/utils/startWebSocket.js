import {w3cwebsocket as W3CWebSocket} from 'websocket'
import invariant from 'invariant'


// TODO internal ITOS does not support websocket!! yet! Question asked: https://mojo.redhat.com/thread/941903
export const isITOS = () => {
  return location.hostname.match(/.+\.itos\.redhat\.com.*/)
}

export function startWebSocket (endpoint, onMessageCallback) {
  invariant(typeof onMessageCallback === 'function', 'you need to pass in a function(obj) in second argument as callback on websocket message')
  console.log(`==== ${endpoint}`)

  const ws = new W3CWebSocket(endpoint)

  ws.onerror = error => {
    console.error('Connect Error:', error);
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


