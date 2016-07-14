import { handleActions } from 'redux-actions'
import {
  LOGOUT_SUCCESS,
  UNAUTHORIZED
} from '../actions'

const defaultState = {
  loggedOut: false,
  serverReturnUnauthorized: false
}

export default handleActions(
  {
    [LOGOUT_SUCCESS]: (state, action) => {
      return {
        ...state,
        loggedOut: true
      }
    },
    [UNAUTHORIZED]: (state, action) => {
      // see ../middleware/apiCallStateChecker.js
      return {
        ...state,
        serverReturnUnauthorized: true
      }
    }
  },
  defaultState
)
