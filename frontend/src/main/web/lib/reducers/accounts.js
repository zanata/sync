import { handleActions } from 'redux-actions'
import {
  GET_ZANATA_ACCOUNT_SUCCESS
} from '../actions/accountAction'

const defaultState = {
  zanataAccount: null,
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
