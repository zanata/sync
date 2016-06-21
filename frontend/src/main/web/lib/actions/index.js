import { createAction } from 'redux-actions'
import { CALL_API } from 'redux-api-middleware'
import Configs from '../constants/Configs'

export const NEW_WORK_REQUEST = 'NEW_WORK_REQUEST'
export const NEW_WORK_SUCCESS = 'NEW_WORK_SUCCESS'
export const NEW_WORK_FAILURE = 'NEW_WORK_FAILURE'

// const newWorkAction = createAction(NEW_WORK_SUBMIT)

const restUrlRoot = () => {
  return `${Configs.apiUrl}${Configs.basename}`
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
    entity.transServerPluginConfig = {
      username: payload.zanataUsername,
      secret: payload.zanataSecret
    }
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

  return {
    [CALL_API]: {
      endpoint: `${restUrlRoot()}/api/work`,
      method: 'POST',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify(entity),
      types: [NEW_WORK_REQUEST, NEW_WORK_SUCCESS, NEW_WORK_FAILURE]
    }
  }
}

// ========== selected a Zanata server for OAuth provider
export const SELECT_ZANATA_REQUEST = 'SELECT_ZANATA_REQUEST'
export const SELECT_ZANATA_SUCCESS = 'SELECT_ZANATA_SUCCESS'
export const SELECT_ZANATA_FAILURE = 'SELECT_ZANATA_FAILURE'
export function selectZanataServer(zanataUrl) {
  return {
    [CALL_API]: {
      endpoint: `${restUrlRoot()}/api/oauth/url?z=${zanataUrl}`,
      method: 'GET',
      types: [SELECT_ZANATA_REQUEST, SELECT_ZANATA_SUCCESS, SELECT_ZANATA_FAILURE]
    }
  }
}

// =========== load work summaries
export const LOAD_WORKS_REQUEST = 'LOAD_WORKS_REQUEST'
export const LOAD_WORKS_SUCCESS = 'LOAD_WORKS_SUCCESS'
export const LOAD_WORKS_FAILURE = 'LOAD_WORKS_FAILURE'
export function loadWorkSummaries(username) {
  return {
    [CALL_API]: {
      endpoint: `${restUrlRoot()}/api/work/by/${username}`,
      method: 'GET',
      types: [LOAD_WORKS_REQUEST, LOAD_WORKS_SUCCESS, LOAD_WORKS_FAILURE]
    }
  }
}

// =========== run a specific job
export const RUN_JOB_REQUEST = 'RUN_JOB_REQUEST'
export const RUN_JOB_SUCCESS = 'RUN_JOB_SUCCESS'
export const RUN_JOB_FAILURE = 'RUN_JOB_FAILURE'
export function runJob(workId, jobType) {
  return {
    [CALL_API]: {
      endpoint: `${restUrlRoot()}/api/job/start?id=${workId}&type=${jobType}`,
      method: 'POST',
      types: [RUN_JOB_REQUEST, RUN_JOB_SUCCESS, RUN_JOB_FAILURE]
    }
  }
}

// ============= get job status
export const GET_JOB_STATUS_REQUEST = 'GET_JOB_STATUS_REQUEST'
export const GET_JOB_STATUS_SUCCESS = 'GET_JOB_STATUS_SUCCESS'
export const GET_JOB_STATUS_FAILURE = 'GET_JOB_STATUS_FAILURE'
export function getLatestJobStatus(workId, jobType) {
  return {
    [CALL_API]: {
      endpoint: `${restUrlRoot()}/api/job/status?id=${workId}&type=${jobType}`,
      method: 'GET',
      types: [GET_JOB_STATUS_REQUEST, GET_JOB_STATUS_SUCCESS,
        GET_JOB_STATUS_FAILURE]
    }
  }
}
