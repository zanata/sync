import { handleActions } from 'redux-actions'
import {
  NEW_WORK_REQUEST, NEW_WORK_SUCCESS, NEW_WORK_FAILURE,
  LOAD_WORK_SUCCESS,
  TOGGLE_DELETE_CONFIRMATION,
  DELETE_WORK_SUCCESS
} from '../actions'

const defaultState = {
  saving: false,
  saveFailed: false,
  workDetail: null,
  deleted: false,
  showDeleteConfirmation: false
}

export default handleActions(
  {
    [NEW_WORK_REQUEST]: (state, action) => {
      return {
        ...state,
        saving: true,
        saveFailed: false
      }
    },
    [NEW_WORK_SUCCESS]: (state, action) => {
      return {
        ...state,
        saving: false,
        saveFailed: false
      }
    },
    [NEW_WORK_FAILURE]: (state, action) => {
      return {
        ...state,
        saving: false,
        saveFailed: true
      }
    },

    [LOAD_WORK_SUCCESS]: (state, action) => {
      return {
        ...state,
        workDetail: action.payload
      }
    },
    [TOGGLE_DELETE_CONFIRMATION]: (state, action) => {
      return {
        ...state,
        showDeleteConfirmation: action.payload.show
      }
    },
    [DELETE_WORK_SUCCESS]: (state, action) => {
      return {
        ...state,
        workDetail: null,
        showDeleteConfirmation: false,
        deleted: true
      }
    }
  },
  defaultState
)
