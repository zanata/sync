import { handleActions } from 'redux-actions'
import {
  LOAD_WORKS_REQUEST, LOAD_WORKS_SUCCESS, LOAD_WORKS_FAILURE
} from '../actions'
import {reducerUtil} from '../utils'

const defaultState = {
  error: undefined,
  loading: false,
  workSummaries: []
}

export default handleActions(
  {
    [LOAD_WORKS_REQUEST]: reducerUtil.requestHandler,
    [LOAD_WORKS_SUCCESS]: (state, action) => {
      console.log(action)
      const summaries = action.payload
      return {
        ...state,
        workSummaries: summaries,
        loading: false
      }
    },
    [LOAD_WORKS_FAILURE]: reducerUtil.errorHandler
  },
  defaultState
)

