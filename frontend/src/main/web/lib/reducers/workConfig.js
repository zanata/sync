import { handleActions } from 'redux-actions'
import { NEW_WORK_REQUEST, NEW_WORK_SUCCESS, NEW_WORK_FAILURE,
  LOAD_WORK_REQUEST, LOAD_WORK_SUCCESS, LOAD_WORK_FAILURE,
  DELETE_WORK_REQUEST, DELETE_WORK_SUCCESS, DELETE_WORK_FAILURE
} from '../actions'
import {requestHandler, errorHandler, commonSuccessState} from '../utils/reducer'
import { API_DONE, API_ERROR, API_IN_PROGRESS} from '../constants/commonStateKeys'

const defaultState = {
  [API_ERROR]: null,
  [API_IN_PROGRESS]: false,
  [API_DONE]: false,
  newConfigSaved: false,
  workDetail: null
}

export default handleActions(
  {
    [NEW_WORK_REQUEST]: requestHandler,
    [NEW_WORK_SUCCESS]: (state, action) => {
      const successState = commonSuccessState(state)
      return {
        ...successState
      }
    },
    [NEW_WORK_FAILURE]: errorHandler,
    [LOAD_WORK_SUCCESS]: (state, action) => {
      const successState = commonSuccessState(state)
      return {
        ...successState,
        workDetail: action.payload
      }
    },
    [LOAD_WORK_FAILURE]: errorHandler,
    [DELETE_WORK_SUCCESS]: (state, action) => {
      const successState = commonSuccessState(state)
      return {
        ...successState,
        workDetail: null
      }
    },
    [DELETE_WORK_FAILURE]: errorHandler
  },
  defaultState
)
