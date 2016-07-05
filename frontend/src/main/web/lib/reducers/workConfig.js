import { handleActions } from 'redux-actions'
import { NEW_WORK_REQUEST, NEW_WORK_SUCCESS, NEW_WORK_FAILURE,
  LOAD_WORK_REQUEST, LOAD_WORK_SUCCESS, LOAD_WORK_FAILURE,
  DELETE_WORK_REQUEST, DELETE_WORK_SUCCESS, DELETE_WORK_FAILURE
} from '../actions'
import {reducer} from '../utils'

const defaultState = {
  creating: false,
  created: false,
  error: null,
  workDetail: null
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
    },
    [LOAD_WORK_SUCCESS]: (state, action) => {
      return {
        ...state,
        workDetail: action.payload
      }
    },
    [LOAD_WORK_FAILURE]: (state, action) => {
      return {
        ...state,
        error: action.payload
      }
    },
    [DELETE_WORK_SUCCESS]: (state, action) => {
      return {
        ...state,
        error: null,
        workDetail: null
      }
    },
    [DELETE_WORK_FAILURE]: (state, action) => {
      return {
        ...state,
        error: action.payload
      }
    }
  },
  defaultState
)
