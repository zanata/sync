import { handleActions } from 'redux-actions'
import {
  SELECT_ZANATA_REQUEST, SELECT_ZANATA_SUCCESS, SELECT_ZANATA_FAILURE,
  GET_ZANATA_SERVERS_REQUEST, GET_ZANATA_SERVERS_SUCCESS, GET_ZANATA_SERVERS_FAILURE
} from '../actions'
import Configs from '../constants/Configs'

const defaultState = {
  // TODo check if we need this here
  user: null,
  zanataServerUrls: ['----'],
  error: undefined,
  zanataOAuthUrl: null,
  loading: false
}

// generic request handler
const requestHandler = (state, action) => {
  return {
    ...state,
    loading: true
  }
}

// generic error handler
const errorHandler = (state, action) => {
  console.error('api error:', action.payload)
  return {
    ...state,
    error: action.payload.message,
    loading: false
  }
}

export default handleActions(
  {
    [GET_ZANATA_SERVERS_REQUEST]: requestHandler,
    [GET_ZANATA_SERVERS_SUCCESS]: (state, action) => {
      console.log(action)
      const urls = ['-- please select one --', ...action.payload]
      return {
        ...state,
        zanataServerUrls: urls,
        loading: false
      }
    },
    [GET_ZANATA_SERVERS_FAILURE]: errorHandler,
    [SELECT_ZANATA_REQUEST]: requestHandler,
    [SELECT_ZANATA_SUCCESS]: (state, action) => {
      console.log(action)
      const oauthUrl = action.payload.data
      return {
        ...state,
        zanataOAuthUrl: oauthUrl,
        loading: false
      }
    },
    [SELECT_ZANATA_FAILURE]: errorHandler
  },
  defaultState
)
