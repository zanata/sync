import { handleActions } from 'redux-actions'
import {
  LOAD_WORKS_SUCCESS,
  RUN_JOB_REQUEST, RUN_JOB_SUCCESS,
  GET_JOB_STATUS_SUCCESS,
  UPDATE_JOB_STATUS
} from '../actions'
import { toJobSummaryKeyName, isJobFinished } from '../constants/Enums'

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
    [UPDATE_JOB_STATUS]: (state, action) => {
      const {workId, jobType, status} = action.payload
      const runningJobs = Object.assign({}, state.runningJobs)
      const runningJobKey = workId + jobType

      if (isJobFinished(status)) {
        delete runningJobs[runningJobKey]
      }

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
