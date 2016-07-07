/**
 * Method to convert a failed API (redux-api-middleware) call action payload
 * @param errorPayload
 */
export function extractErrorMessage(errorPayload) {
  if (errorPayload.response) {
    // TODO properly display error response
    return JSON.stringify(errorPayload.response)
  } else if (errorPayload.message) {
    return errorPayload.message
  }
  return JSON.stringify(errorPayload)
}

export function isUnauthorized(errorPayload) {
  return errorPayload && errorPayload.status && errorPayload.status === 401
}
