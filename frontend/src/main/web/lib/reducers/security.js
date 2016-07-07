import { handleActions } from 'redux-actions'
import {
  CHECK_SESSION_REQUEST, CHECK_SESSION_SUCCESS, CHECK_SESSION_FAILURE,
  SELECT_ZANATA_REQUEST, SELECT_ZANATA_SUCCESS, SELECT_ZANATA_FAILURE,
  LOGOUT_REQUEST, LOGOUT_SUCCESS, LOGOUT_FAILURE
} from '../actions'
import {requestHandler, errorHandler, commonSuccessState} from '../utils/reducer'
import { API_DONE, API_ERROR, API_IN_PROGRESS} from '../constants/commonStateKeys'

const defaultState = {
  [API_ERROR]: null,
  [API_IN_PROGRESS]: false,
  [API_DONE]: false,
  zanataOAuthUrl: null,
  loggedOut: false
}

export default handleActions(
  {
    [CHECK_SESSION_FAILURE] :errorHandler,
    [SELECT_ZANATA_REQUEST]: requestHandler,
    [SELECT_ZANATA_SUCCESS]: (state, action) => {
      const oauthUrl = action.payload.data
      const successState = commonSuccessState(state)
      return {
        ...successState,
        zanataOAuthUrl: oauthUrl
      }
    },
    [SELECT_ZANATA_FAILURE]: errorHandler,
    [LOGOUT_REQUEST]: requestHandler,
    [LOGOUT_SUCCESS]: (state, action) => {
      const successState = commonSuccessState(state)
      return {
        ...successState,
        loggedOut: true
      }
    },
    [LOGOUT_FAILURE]: errorHandler
  },
  defaultState
)
