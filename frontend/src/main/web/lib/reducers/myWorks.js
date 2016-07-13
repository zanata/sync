import { handleActions } from 'redux-actions'
import {
  LOAD_WORKS_SUCCESS,
  RUN_JOB_REQUEST, RUN_JOB_SUCCESS,
  UPDATE_JOB_STATUS
} from '../actions'
import { toJobSummaryKeyName } from '../constants/Enums'

const defaultState = {
  workSummaries: [],
  runningJobs: {}
}

export default (maxPollCount) => handleActions(
  {
    [LOAD_WORKS_SUCCESS]: (state, action) => {
      return {
        ...state,
        workSummaries: action.payload
      }
    },
    [RUN_JOB_REQUEST]: (state, action) => {
      const {workId, jobType} = action.meta
      // we use work id and job type as identifier for a job...
      // see JobResource.java
      const runningJobs = Object.assign({}, state.runningJobs)
      runningJobs[workId + jobType] = true
      return {
        ...state,
        runningJobs
      }
    },
    [UPDATE_JOB_STATUS]: (state, action) => {
      const {workId, jobType, status} = action.payload
      const runningJobs = Object.assign({}, state.runningJobs)
      const runningJobKey = workId + jobType

      delete runningJobs[runningJobKey]

      const workSummary = state.workSummaries.find(work => work.id === workId)
      const job = toJobSummaryKeyName(jobType)
      workSummary[job].lastJobStatus = action.payload

      return {
        ...state,
        runningJobs
      }
    }
  },
  defaultState
)
