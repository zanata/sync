import { handleActions } from 'redux-actions'
import {SHOW_NOTIFICATION, CLOSE_NOTIFICATION} from '../actions'

const defaultState = {
  notification: {
    source: null,
    reason: null,
    isError: false,
    message: ''
  },
  isUnauthorized: false
}


/**
 * Global shared state
 */
export default handleActions(
  {
    [SHOW_NOTIFICATION]: (state, action) => {
      // see ../middleware/apiCallStateChecker.js
      const {source, reason, message, isError, isUnauthorized} = action.payload
      return {
        ...state,
        notification: {
          source,
          reason,
          isError,
          message
        },
        isUnauthorized
      }
    },
    [CLOSE_NOTIFICATION]: (state, action) => {
      const {source, reason} = state.notification
      return {
        ...state,
        notification: {
          source,
          reason,
          isError: false,
          message: ''
        }
      }
    }
  },
  defaultState
)
