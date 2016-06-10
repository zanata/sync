import { createAction } from 'redux-actions'
import { CALL_API } from 'redux-api-middleware'

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
      endpoint: `/api/work`,
      method: 'POST',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify(entity),
      types: [NEW_WORK_REQUEST, NEW_WORK_SUCCESS, NEW_WORK_FAILURE]
    }
  }
}
