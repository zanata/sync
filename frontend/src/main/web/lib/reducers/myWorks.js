import { handleActions } from 'redux-actions'
import {
  LOAD_WORKS_REQUEST, LOAD_WORKS_SUCCESS, LOAD_WORKS_FAILURE,
  RUN_JOB_REQUEST, RUN_JOB_SUCCESS, RUN_JOB_FAILURE,
  GET_JOB_STATUS_REQUEST, GET_JOB_STATUS_SUCCESS, GET_JOB_STATUS_FAILURE
} from '../actions'
import { reducer } from '../utils'
import { isJobFinished, toJobSummaryKeyName } from '../constants/Enums'

const defaultState = {
  error: undefined,
  loading: false,
  workSummaries: [],
  runningJobs: {}
}

export default (pollInterval, maxPollCount) => handleActions(
  {
    [LOAD_WORKS_REQUEST]: reducer.requestHandler,
    [LOAD_WORKS_SUCCESS]: (state, action) => {
      const summaries = action.payload
      return {
        ...state,
        workSummaries: summaries,
        loading: false
      }
    },
    [LOAD_WORKS_FAILURE]: reducer.errorHandler,
    // [RUN_JOB_REQUEST]: (state, action) => {
    //   // we do nothing... for now
    //   return {
    //     ...state
    //   }
    // },
    [RUN_JOB_SUCCESS]: (state, action) => {
      const {workId, jobType} = action.payload
      // we use work id and job type as identifier for a job...
      // see JobResource.java
      const runningJobs = Object.assign({}, state.runningJobs)
      runningJobs[workId + jobType] = 1
      return {
        ...state,
        runningJobs
      }
    },
    [RUN_JOB_FAILURE]: (state, action) => {
      console.error('run job failed', action)
      return {
        ...state,
        error: action.payload.message
      }
    },
    [GET_JOB_STATUS_REQUEST]: (state, action) => {
      // TODO ideally we want to show a progress thingy besides the job
      return {
        ...state
      }
    },
    [GET_JOB_STATUS_SUCCESS]: (state, action) => {
      const {workId, jobType, status, startTime, endTime} = action.payload
      const runningJobs = Object.assign({}, state.runningJobs)
      const runningJobKey = workId + jobType
      if (isJobFinished(status)) {
        delete runningJobs[runningJobKey]
      } else {
        // we increment the count of the running job by 1
        runningJobs[runningJobKey] += 1
        // TODO we need to do another poll but for now
        // temp hack: a new runningJobs object will cause a re-render which triggers another setTimeout
      }

      const workSummary = state.workSummaries.find(work => work.id === workId)
      const job = toJobSummaryKeyName(jobType)
      workSummary[job].lastJobStatus = action.payload

      // stop polling if we have reached the max poll count
      if (runningJobs[runningJobKey] >= maxPollCount) {
        delete runningJobs[runningJobKey]
        workSummary[job].lastJobStatus.status = 'Timeout polling result'
      }

      return {
        ...state,
        runningJobs
      }
    },
    [GET_JOB_STATUS_FAILURE]: (state, action) => {
      console.error('get job status failed', action)
      return {
        ...state,
        error: action.payload.message
      }
    }
  },
  {
    ...defaultState,
    pollInterval
  }
)

