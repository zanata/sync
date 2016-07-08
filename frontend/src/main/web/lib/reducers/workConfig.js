import { handleActions } from 'redux-actions'
import { 
  NEW_WORK_REQUEST, NEW_WORK_SUCCESS, NEW_WORK_FAILURE,
  LOAD_WORK_SUCCESS,
  DELETE_WORK_SUCCESS
} from '../actions'

const defaultState = {
  saving: false,
  saveFailed: false,
  workDetail: null,
  deleted: false
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
    [DELETE_WORK_SUCCESS]: (state, action) => {
      return {
        ...state,
        workDetail: null,
        deleted: true
      }
    }
  },
  defaultState
)
