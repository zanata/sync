import { handleActions } from 'redux-actions'
import { NEW_WORK_REQUEST, NEW_WORK_SUCCESS, NEW_WORK_FAILURE } from '../actions'

const defaultState = {
  creating: false,
  created: false,
  error: null
}

export default handleActions(
  {
    [NEW_WORK_REQUEST]: (state, action) => {
      // TODO use immutable js
      let newState = Object.assign({}, state)
      newState.creating = true
      newState.error = null
      return newState
    },
    [NEW_WORK_SUCCESS]: (state, action) => {
      let newState = Object.assign({}, state)
      newState.creating = false
      newState.created = true
      newState.error = null
      return newState
    },
    [NEW_WORK_FAILURE]: (state, action) => {
      return {
        ...state,
        creating: false,
        created: false,
        error: action.payload
      }
    }
  },
  defaultState
)
