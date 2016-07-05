import { createAction } from 'redux-actions'
import { CALL_API } from 'redux-api-middleware'

export const NEW_WORK_REQUEST = 'NEW_WORK_REQUEST'
export const NEW_WORK_SUCCESS = 'NEW_WORK_SUCCESS'
export const NEW_WORK_FAILURE = 'NEW_WORK_FAILURE'

// const newWorkAction = createAction(NEW_WORK_SUBMIT)

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
        types: [CHECK_SESSION_REQUEST, CHECK_SESSION_SUCCESS, CHECK_SESSION_FAILURE]
      }
    })
  }
}

export function submitNewWork(payload) {
  console.log('======== new work button clicked:', payload)
  const entity = {
    name: payload.name,
    description: payload.description,
    syncToZanataEnabled: payload.syncToZanataEnabled,
    syncToRepoEnabled: payload.syncToRepoEnabled
  }
  if (payload.syncToZanataEnabled) {
    entity.syncToZanataCron = payload.syncToZanataCron
    entity.syncOption = payload.syncOption
  }
  if (payload.syncToRepoEnabled) {
    entity.srcRepoPluginName = payload.selectedRepoPluginName
    const regex = new RegExp('^' + payload.selectedRepoPluginName + '(.+)$')
    entity.srcRepoPluginConfig = {}
    // see lib/components/WorkForm.js
    Object.keys(payload).forEach(key => {
      const match = regex.exec(key)
      if (match && match.length === 2) {
        //console.log('found ' + key + ' with value ' + payload[key])
        entity.srcRepoPluginConfig[match[1]] = payload[key]
      }
    })
    entity.syncToRepoCron = payload.syncToRepoCron
  }

  console.log('==== entity for new work', entity)

  return (dispatch, getState) => {
    dispatch({
      [CALL_API]: {
        endpoint: `${getState().configs.apiUrl}/api/work`,
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        credentials: 'include',
        body: JSON.stringify(entity),
        types: [NEW_WORK_REQUEST, NEW_WORK_SUCCESS, NEW_WORK_FAILURE]
      }
    })
  }
}

// ========== selected a Zanata server for OAuth provider
export const SELECT_ZANATA_REQUEST = 'SELECT_ZANATA_REQUEST'
export const SELECT_ZANATA_SUCCESS = 'SELECT_ZANATA_SUCCESS'
export const SELECT_ZANATA_FAILURE = 'SELECT_ZANATA_FAILURE'
export function selectZanataServer(zanataUrl) {
  return (dispatch, getState) => {
    dispatch({
      [CALL_API]: {
        endpoint: `${getState().configs.apiUrl}/api/oauth/url?z=${zanataUrl}`,
        method: 'GET',
        credentials: 'include',
        types: [SELECT_ZANATA_REQUEST, SELECT_ZANATA_SUCCESS, SELECT_ZANATA_FAILURE]
      }
    })
  }
}

// =========== load work summaries
export const LOAD_WORKS_REQUEST = 'LOAD_WORKS_REQUEST'
export const LOAD_WORKS_SUCCESS = 'LOAD_WORKS_SUCCESS'
export const LOAD_WORKS_FAILURE = 'LOAD_WORKS_FAILURE'
export function loadWorkSummaries(username) {
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
        types: [RUN_JOB_REQUEST, RUN_JOB_SUCCESS, RUN_JOB_FAILURE]
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
