import { handleActions } from 'redux-actions'
import {
  SELECT_ZANATA_SUCCESS,
  LOGOUT_SUCCESS,
  UNAUTHORIZED
} from '../actions'

const defaultState = {
  zanataOAuthUrl: null,
  loggedOut: false,
  serverReturnUnauthorized: false
}

export default handleActions(
  {
    [SELECT_ZANATA_SUCCESS]: (state, action) => {
      const oauthUrl = action.payload.data
      return {
        ...state,
        zanataOAuthUrl: oauthUrl
      }
    },
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
