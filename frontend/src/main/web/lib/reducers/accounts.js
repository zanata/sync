import { handleActions } from 'redux-actions'
import {
  GET_ZANATA_ACCOUNT_SUCCESS,
  SAVE_ZANATA_ACCOUNT_SUCCESS
} from '../actions/accountAction'

const defaultState = {
  zanataAccount: {
    username: '',
    apiKey: '',
    zanataServer: ''
  },
  repoAccounts: []
}

export default handleActions(
  {
    [GET_ZANATA_ACCOUNT_SUCCESS]: (state, action) => {
      return {
        ...state,
        zanataAccount: action.payload
      }
    }
  },
  defaultState
)
