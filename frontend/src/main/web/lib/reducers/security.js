import { handleActions } from 'redux-actions'
import {
  SELECT_ZANATA_REQUEST, SELECT_ZANATA_SUCCESS, SELECT_ZANATA_FAILURE,
  LOGOUT_REQUEST, LOGOUT_SUCCESS, LOGOUT_FAILURE
} from '../actions'
import {reducer} from '../utils'

const defaultState = {
  error: undefined,
  zanataOAuthUrl: null,
  loggedOut: false,
  loading: false
}

const {requestHandler, errorHandler} = reducer

export default handleActions(
  {
    [SELECT_ZANATA_REQUEST]: requestHandler,
    [SELECT_ZANATA_SUCCESS]: (state, action) => {
      const oauthUrl = action.payload.data
      return {
        ...state,
        zanataOAuthUrl: oauthUrl,
        loading: false
      }
    },
    [SELECT_ZANATA_FAILURE]: errorHandler,
    [LOGOUT_REQUEST]: requestHandler,
    [LOGOUT_SUCCESS]: (state, action) => {
      return {
        ...state,
        loggedOut: true,
        loading: false
      }
    },
    [LOGOUT_FAILURE]: errorHandler
  },
  defaultState
)
