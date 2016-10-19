import { CALL_API } from 'redux-api-middleware'

// ========= get the associated zanata account
export const GET_ZANATA_ACCOUNT_REQUEST = 'GET_ZANATA_ACCOUNT_REQUEST'
export const GET_ZANATA_ACCOUNT_SUCCESS = 'GET_ZANATA_ACCOUNT_SUCCESS'
export const GET_ZANATA_ACCOUNT_FAILURE = 'GET_ZANATA_ACCOUNT_FAILURE'
export function getZanataAccount() {
  return (dispatch, getState) => {
    dispatch({
      [CALL_API]: {
        endpoint: `${getState().configs.apiUrl}/api/account/zanata`,
        method: 'GET',
        headers: {'Content-Type': 'application/json'},
        credentials: 'include',
        types: [GET_ZANATA_ACCOUNT_REQUEST, GET_ZANATA_ACCOUNT_SUCCESS, GET_ZANATA_ACCOUNT_FAILURE]
      }
    })
  }
}

// ========= save repo account to current user
export const SAVE_REPO_ACCOUNT_REQUEST = 'SAVE_REPO_ACCOUNT_REQUEST'
export const SAVE_REPO_ACCOUNT_SUCCESS = 'SAVE_REPO_ACCOUNT_SUCCESS'
export const SAVE_REPO_ACCOUNT_FAILURE = 'SAVE_REPO_ACCOUNT_FAILURE'
export function saveRepoAccount(repoAccount) {
  const entity = {
    id: repoAccount.repoId >= 0 ? repoAccount.repoId : null,
    username: repoAccount.repoUsername,
    repoHostname: repoAccount.repoHostname,
    secret: repoAccount.repoSecret,
    repoType: repoAccount.repoType
  }
  console.log('saving', entity)
  return (dispatch, getState) => {
    dispatch({
      [CALL_API]: {
        endpoint: `${getState().configs.apiUrl}/api/account/repo`,
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        credentials: 'include',
        body: JSON.stringify(entity),
        types: [SAVE_REPO_ACCOUNT_REQUEST,
          {
            type: SAVE_REPO_ACCOUNT_SUCCESS,
            meta: {
              notification: 'Repo Account Saved'
            }
          },
          SAVE_REPO_ACCOUNT_FAILURE]
      }
    })
  }
}
