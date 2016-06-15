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
    // entity.transServerPluginConfig = {
    //   cron: payload.syncToZanataCron
    // }
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
