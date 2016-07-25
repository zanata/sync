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

// ======= save zanata account to current user
export const SAVE_ZANATA_ACCOUNT_REQUEST = 'SAVE_ZANATA_ACCOUNT_REQUEST'
export const SAVE_ZANATA_ACCOUNT_SUCCESS = 'SAVE_ZANATA_ACCOUNT_SUCCESS'
export const SAVE_ZANATA_ACCOUNT_FAILURE = 'SAVE_ZANATA_ACCOUNT_FAILURE'
export function saveZanataAccount(zanataAccount) {
  const entity = {
    username: zanataAccount.zanataUsername,
    zanataServer: zanataAccount.zanataServer,
    apiKey: zanataAccount.zanataSecret
  }
  return (dispatch, getState) => {
    dispatch({
      [CALL_API]: {
        endpoint: `${getState().configs.apiUrl}/api/account/zanata`,
        method: 'PUT',
        headers: {'Content-Type': 'application/json'},
        credentials: 'include',
        body: JSON.stringify(entity),
        types: [SAVE_ZANATA_ACCOUNT_REQUEST, SAVE_ZANATA_ACCOUNT_SUCCESS, SAVE_ZANATA_ACCOUNT_FAILURE]
      }
    })
  }
}
