import {isUnauthorized, extractErrorMessage} from '../utils/errorResponse'
import {API_DONE, API_ERROR, API_IN_PROGRESS} from '../constants/commonStateKeys'
import {createNotification} from '../actions'


function dispatchErrorNotification(state, reducer, next) {
  const reducerState = state[reducer]
  const error = reducerState[API_ERROR]
  const notification = state.global.notification

  const notified = notification.source === reducer &&
    notification.reason == error
  // check to see if we have any API call that returns error and has not been
  // notified
  if (error && !notified) {
    const unauthorized= isUnauthorized(error)
    const message = unauthorized ?
      'You are not authorized to access this resource!' :
      extractErrorMessage(error);
    next(createNotification({
      source: reducer,
      isError: true,
      isUnauthorized: unauthorized,
      reason: error,
      message
    }))
  }
}

function dispatchSuccessNotification(state, reducer, next) {
  const reducerState = state[reducer]
  const done = reducerState[API_DONE]
  const notification = state.global.notification
  const notified = notification.source === reducer &&
    notification.reason == done
  console.log('======== here')
  if (done && !notified) {
    // FIXME this may override an error (unauthorized response)
    // FIXME this will only display save successful for the first time but not the subsequent save
    next(createNotification({
      source: reducer,
      isError: false,
      isUnauthorized: false,
      reason: done,
      message: 'Successful'
    }))
  }
}

export default store => next => action => {

  let result = next(action)
  const state = store.getState()

  // here we assume only two level of nesting in state
  // (e.g. every reducer create a simple object without nesting)
  let reducer, reducerInProgress, reducerWithError, reducerIsDone
  for (reducer in state) {
    const reducerState = state[reducer]
    if (reducerState[API_ERROR]) {
      reducerWithError = reducer
    }
    if (reducerState[API_DONE]) {
      reducerIsDone = reducer
    }
    if (reducerState[API_IN_PROGRESS]) {
      reducerInProgress = reducer
    }
  }

  // NOTE: this assumes we only have one notification at a time
  // (implication: two ajax calls and the later one will override the earlier
  //  one)
  if (reducerWithError) {
    dispatchErrorNotification(state, reducerWithError, next)
  }
  if (reducerIsDone) {
    dispatchSuccessNotification(state, reducerIsDone, next)
  }

  return result
}
