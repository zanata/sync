import {isUnauthorized, extractErrorMessage} from '../utils/errorResponse'
import {becomeUnauthorized, NEW_WORK_SUCCESS, CHECK_SESSION_FAILURE } from '../actions'
import {InternalError, InvalidRSAA, RequestError, ApiError} from 'redux-api-middleware'

import {bindActionCreators} from 'redux'
import {actions as toastrActions} from 'react-redux-toastr'

export default store => next => action => {

  let result = next(action)
  const toastr = bindActionCreators(toastrActions, next)
  
  const {type, payload} = action
  // this assume we use redux-api-middleware so the error action may come in
  // several form. See https://www.npmjs.com/package/redux-api-middleware
  if (action.error && type !== CHECK_SESSION_FAILURE) {
    const unauthorized= isUnauthorized(payload)
    let message
    if (unauthorized) {
      message = 'You are not authorized to access this resource!'
      next(becomeUnauthorized())
    } else {
      message = extractErrorMessage(payload);
    }
    toastr.error(message)
  } else if (type === NEW_WORK_SUCCESS) {
    toastr.success('Saved successfully', {
      timeOut: 1000
    })
  }

  return result
}
