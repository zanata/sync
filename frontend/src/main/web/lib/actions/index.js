import { createAction } from 'redux-actions'
import { CALL_API } from 'redux-api-middleware'


export const UNAUTHORIZED = 'UNAUTHORIZED'
export const becomeUnauthorized = createAction(UNAUTHORIZED)

// =========== check whether server session is still logged in
export const CHECK_SESSION_REQUEST = 'CHECK_SESSION_REQUEST'
export const CHECK_SESSION_SUCCESS = 'CHECK_SESSION_SUCCESS'
export const CHECK_SESSION_FAILURE = 'CHECK_SESSION_FAILURE'
export function checkSession() {
  return (dispatch, getState) => {
    dispatch({
      [CALL_API]: {
        endpoint: `${getState().configs.apiUrl}/api/work`,
        method: 'HEAD',
        headers: {'Content-Type': 'application/json'},
        credentials: 'include',
        types: [CHECK_SESSION_REQUEST, {
          type: CHECK_SESSION_SUCCESS,
          payload: (action, state, res) => {
            if (res) {
              return {
                status: res.status
              }
            } else {
              return {
                status: 'Network request failed'
              }
            }
          }
        }, CHECK_SESSION_FAILURE]
      }
    })
  }
}

// ============ submit/save a new work config
export const NEW_WORK_REQUEST = 'NEW_WORK_REQUEST'
export const NEW_WORK_SUCCESS = 'NEW_WORK_SUCCESS'
export const NEW_WORK_FAILURE = 'NEW_WORK_FAILURE'
export function submitNewWork(payload) {
  console.log('======== new work button clicked:', payload)
  const entity = {
    name: payload.name,
    description: payload.description,
    syncToZanataEnabled: payload.syncToZanataEnabled,
    syncToRepoEnabled: payload.syncToRepoEnabled,
    srcRepoUrl: payload.srcRepoUrl,
    srcRepoBranch: payload.srcRepoBranch,
    srcRepoAccountId: payload.srcRepoAccountId,
    projectConfigs: payload.projectConfigs
  }
  if (payload.syncToZanataEnabled) {
    entity.syncToZanataCron = payload.syncToZanataCron
    entity.syncOption = payload.syncOption
  }

  if (payload.syncToRepoEnabled) {
    entity.syncToRepoCron = payload.syncToRepoCron
  }
  entity.zanataWebHookSecret = payload.zanataWebHookSecret

  console.log('==== entity for new work', entity)

  return (dispatch, getState) => {
    dispatch({
      [CALL_API]: {
        endpoint: `${getState().configs.apiUrl}/api/work`,
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        credentials: 'include',
        body: JSON.stringify(entity),
        types: [NEW_WORK_REQUEST, {
          type: NEW_WORK_SUCCESS,
          meta: {
            notification: 'Saved successfully'
          }
        }, NEW_WORK_FAILURE]
      }
    })
  }
}

// =========== load work summaries
export const LOAD_WORKS_REQUEST = 'LOAD_WORKS_REQUEST'
export const LOAD_WORKS_SUCCESS = 'LOAD_WORKS_SUCCESS'
export const LOAD_WORKS_FAILURE = 'LOAD_WORKS_FAILURE'
export function loadWorkSummaries() {
  return (dispatch, getState) => {
    dispatch({
      [CALL_API]: {
        endpoint: `${getState().configs.apiUrl}/api/work/mine`,
        method: 'GET',
        credentials: 'include',
        types: [LOAD_WORKS_REQUEST, LOAD_WORKS_SUCCESS, LOAD_WORKS_FAILURE]
      }
    })
  }
}

// =========== run a specific job
export const RUN_JOB_REQUEST = 'RUN_JOB_REQUEST'
export const RUN_JOB_SUCCESS = 'RUN_JOB_SUCCESS'
export const RUN_JOB_FAILURE = 'RUN_JOB_FAILURE'
export function runJob(workId, jobType) {
  return (dispatch, getState) => {
    dispatch({
      [CALL_API]: {
        endpoint: `${getState().configs.apiUrl}/api/job/start?id=${workId}&type=${jobType}`,
        method: 'POST',
        credentials: 'include',
        types: [
          {
            type: RUN_JOB_REQUEST,
            meta: {
              workId: workId,
              jobType: jobType
            }
          }, RUN_JOB_SUCCESS, RUN_JOB_FAILURE]
      }
    })
  }
}


// ============= get job status
export const GET_JOB_STATUS_REQUEST = 'GET_JOB_STATUS_REQUEST'
export const GET_JOB_STATUS_SUCCESS = 'GET_JOB_STATUS_SUCCESS'
export const GET_JOB_STATUS_FAILURE = 'GET_JOB_STATUS_FAILURE'
export function getLatestJobStatus(workId, jobType) {
  return (dispatch, getState) => {
    dispatch({
      [CALL_API]: {
        endpoint: `${getState().configs.apiUrl}/api/job/last/status?id=${workId}&type=${jobType}`,
        method: 'GET',
        credentials: 'include',
        types: [GET_JOB_STATUS_REQUEST, GET_JOB_STATUS_SUCCESS,
          GET_JOB_STATUS_FAILURE]
      }
    })
  }
}


// ============= web socket update job status
export const UPDATE_JOB_STATUS = 'UPDATE_JOB_STATUS'
export const updateJobStatus = createAction(UPDATE_JOB_STATUS)

// ============ log out
export const LOGOUT_REQUEST = 'LOGOUT_REQUEST'
export const LOGOUT_SUCCESS = 'LOGOUT_SUCCESS'
export const LOGOUT_FAILURE = 'LOGOUT_FAILURE'
export function logout() {
  return (dispatch, getState) => {
    dispatch({
      [CALL_API]: {
        endpoint: `${getState().configs.apiUrl}/api/oauth/logout`,
        method: 'POST',
        credentials: 'include',
        types: [LOGOUT_REQUEST, LOGOUT_SUCCESS, LOGOUT_FAILURE]
      }
    })
  }
}

// =========== load work detail
export const LOAD_WORK_REQUEST = 'LOAD_WORK_REQUEST'
export const LOAD_WORK_SUCCESS = 'LOAD_WORK_SUCCESS'
export const LOAD_WORK_FAILURE = 'LOAD_WORK_FAILURE'
export function getWorkDetail(id) {
  return (dispatch, getState) => {
    dispatch({
      [CALL_API]: {
        endpoint: `${getState().configs.apiUrl}/api/work/${id}`,
        method: 'GET',
        credentials: 'include',
        types: [LOAD_WORK_REQUEST, LOAD_WORK_SUCCESS, LOAD_WORK_FAILURE]
      }
    })
  }
}

export const TOGGLE_DELETE_CONFIRMATION = 'TOGGLE_DELETE_CONFIRMATION'
export const toggleDeleteConfirmation = createAction(TOGGLE_DELETE_CONFIRMATION)

// =========== delete a single work config
export const DELETE_WORK_REQUEST = 'DELETE_WORK_REQUEST'
export const DELETE_WORK_SUCCESS = 'DELETE_WORK_SUCCESS'
export const DELETE_WORK_FAILURE = 'DELETE_WORK_FAILURE'
export function deleteWork() {
  return (dispatch, getState) => {
    const selectedConfig = getState().workConfig.workDetail
    if (selectedConfig) {
      dispatch({
        [CALL_API]: {
          endpoint: `${getState().configs.apiUrl}/api/work/${selectedConfig.id}`,
          method: 'DELETE',
          credentials: 'include',
          types: [DELETE_WORK_REQUEST, DELETE_WORK_SUCCESS, DELETE_WORK_FAILURE]
        }
      })
    }
  }
}

// ============= generic API request action
export const API_REQUESTING = 'API_REQUESTING'
export const requesting = createAction(API_REQUESTING)
