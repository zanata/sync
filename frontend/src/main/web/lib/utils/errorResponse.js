import {InternalError, InvalidRSAA, RequestError, ApiError, getJSON} from 'redux-api-middleware'

/**
 * Method to convert a failed API (redux-api-middleware) call action payload
 * @param errorPayload
 */
export function extractErrorMessage(errorPayload) {
  const isApiError = errorPayload instanceof ApiError
  const isOtherError = errorPayload instanceof InternalError 
    || errorPayload instanceof InvalidRSAA 
    || errorPayload instanceof RequestError
  if (isApiError && errorPayload.response) {
    // TODO properly display error response
    return JSON.stringify(errorPayload.response)
  } else if (isOtherError) {
    return `${errorPayload.name} - ${errorPayload.message}`
  }
  return JSON.stringify(errorPayload)
}

export function isUnauthorized(errorPayload) {
  return errorPayload && errorPayload.status && errorPayload.status === 401
}
