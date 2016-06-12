import { createAction } from 'redux-actions'
import { CALL_API } from 'redux-api-middleware'
import Configs from '../constants/Configs'

export const NEW_WORK_REQUEST = 'NEW_WORK_REQUEST'
export const NEW_WORK_SUCCESS = 'NEW_WORK_SUCCESS'
export const NEW_WORK_FAILURE = 'NEW_WORK_FAILURE'

// const newWorkAction = createAction(NEW_WORK_SUBMIT)

export function submitNewWork(payload) {
  // console.log('======== new work button clicked:' + payload)
  const entity = {
    name: payload.name,
    description: payload.description,
    syncToZanataEnabled: payload.syncToZanataEnabled,
    syncToRepoEnabled: payload.syncToRepoEnabled
  }
  if (payload.syncToZanataEnabled) {
    entity.syncToZanataCron = payload.syncToZanataCron
    entity.syncOption = payload.syncOption
    // entity.transServerPluginConfig = {
    //   cron: payload.syncToZanataCron
    // }
  }
  if (payload.syncToRepoEnabled) {
    entity.srcRepoPluginConfig = {
      url: payload.repoUrl,
      username: payload.repoUsername,
      apiKey: payload.repoSecret,
      branch: payload.repoBranch
    }
  }

  return {
    [CALL_API]: {
      endpoint: `${Configs.basename}/api/work`,
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
      endpoint: `${Configs.basename}/api/oauth/url?z=${zanataUrl}`,
      method: 'GET',
      types: [SELECT_ZANATA_REQUEST, SELECT_ZANATA_SUCCESS, SELECT_ZANATA_FAILURE]
    }
  }
}

// ========== get zanata server url list =========
export const GET_ZANATA_SERVERS_REQUEST = 'GET_ZANATA_SERVERS_REQUEST'
export const GET_ZANATA_SERVERS_SUCCESS = 'GET_ZANATA_SERVERS_SUCCESS'
export const GET_ZANATA_SERVERS_FAILURE = 'GET_ZANATA_SERVERS_FAILURE'
export function getZanataServerUrls() {
  return {
    [CALL_API]: {
      endpoint: `${Configs.basename}/api/oauth`,
      method: 'GET',
      headers: {'Content-Type': 'application/json'},
      types: [GET_ZANATA_SERVERS_REQUEST, GET_ZANATA_SERVERS_SUCCESS, GET_ZANATA_SERVERS_FAILURE]
    }
  }
}
