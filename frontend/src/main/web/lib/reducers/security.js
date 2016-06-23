import { handleActions } from 'redux-actions'
import {
  SELECT_ZANATA_REQUEST, SELECT_ZANATA_SUCCESS, SELECT_ZANATA_FAILURE
} from '../actions'
import {reducer} from '../utils'

const defaultState = {
  error: undefined,
  zanataOAuthUrl: null,
  loading: false
}

export default handleActions(
  {
    [SELECT_ZANATA_REQUEST]: reducer.requestHandler,
    [SELECT_ZANATA_SUCCESS]: (state, action) => {
      console.log(action)
      const oauthUrl = action.payload.data
      return {
        ...state,
        zanataOAuthUrl: oauthUrl,
        loading: false
      }
    },
    [SELECT_ZANATA_FAILURE]: reducer.errorHandler
  },
  defaultState
)
