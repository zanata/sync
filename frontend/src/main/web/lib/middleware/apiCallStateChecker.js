import {isUnauthorized, extractErrorMessage} from '../utils/errorResponse'
import {API_DONE, API_ERROR, API_IN_PROGRESS} from '../constants/commonStateKeys'
import {showNotification} from '../actions'

export default store => next => action => {

  let result = next(action)
  const state = store.getState()

  // here we assume only two level of nesting in state
  // (e.g. every reducer create a simple object without nesting)
  // const reducerStates = Object.keys(state).map(key => state[key])
  Object.keys(state).forEach(reducer => {
    const reducerState = state[reducer]
    const error = reducerState[API_ERROR]
    const notification = state.global
    // this assumes we only have one notification at a time
    // (implication: two ajax calls and the later one will override the earlier
    //  one)
    const notified = notification.source === reducer &&
      notification.reason == error
    // check to see if we have any API call that returns error and has not been
    // notified
    if (error && !notified) {
      const unauthorized= isUnauthorized(error)
      const message = unauthorized ?
        'You are not authorized to access this resource!' :
        extractErrorMessage(error);
      next(showNotification({
        source: [reducer],
        isError: true,
        isUnauthorized: unauthorized,
        reason: error,
        message
      }))
    }
  })

  return result
}
