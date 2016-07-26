import { handleActions } from 'redux-actions'
import {
  API_REQUESTING
} from '../actions/index'

const defaultState = {
  requesting: {}
}

export default handleActions(
  {
    [API_REQUESTING]: (state, action) => {
      const {type, requesting} = action.payload
      return {
        ...state,
        requesting: {
          [type]: requesting
        }
      }
    }
  },
  defaultState
)
