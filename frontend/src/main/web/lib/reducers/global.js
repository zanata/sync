import { handleActions } from 'redux-actions'
import {SHOW_NOTIFICATION} from '../actions'

const defaultState = {
  notification: {
    source: null,
    isError: false,
    reason: null,
    message: ''
  },
  isUnauthorized: false
}


/**
 * Global shared state
 */
export default handleActions(
  {
    [SHOW_NOTIFICATION] : (state, action) => {
      // see ../middleware/apiCallStateChecker.js
      const {source, reason, message, isError, isUnauthorized} = action.payload
      return {
        ...state,
        notification: {
          isError,
          isUnauthorized,
          message
        },
        isUnauthorized
      }
    }
  },
  defaultState
)
