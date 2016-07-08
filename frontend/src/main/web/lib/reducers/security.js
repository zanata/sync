import { handleActions } from 'redux-actions'
import {
  CHECK_SESSION_REQUEST, CHECK_SESSION_SUCCESS, CHECK_SESSION_FAILURE,
  SELECT_ZANATA_REQUEST, SELECT_ZANATA_SUCCESS, SELECT_ZANATA_FAILURE,
  LOGOUT_REQUEST, LOGOUT_SUCCESS, LOGOUT_FAILURE,
  UNAUTHORIZED
} from '../actions'

const defaultState = {
  zanataOAuthUrl: null,
  loggedOut: false,
  serverReturnUnauthorized: false
}

export default handleActions(
  {
    // [CHECK_SESSION_FAILURE] :errorHandler,
    // [SELECT_ZANATA_REQUEST]: requestHandler,
    [SELECT_ZANATA_SUCCESS]: (state, action) => {
      const oauthUrl = action.payload.data
      return {
        ...state,
        zanataOAuthUrl: oauthUrl
      }
    },
    // [SELECT_ZANATA_FAILURE]: errorHandler,
    // [LOGOUT_REQUEST]: requestHandler,
    [LOGOUT_SUCCESS]: (state, action) => {
      return {
        ...state,
        loggedOut: true
      }
    },
    // [LOGOUT_FAILURE]: errorHandler,
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
