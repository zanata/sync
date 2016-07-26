import {isUnauthorized, extractErrorMessage} from '../utils/errorResponse'
import {becomeUnauthorized, requesting,
  CHECK_SESSION_FAILURE } from '../actions/index'

import {bindActionCreators} from 'redux'
import {actions as toastrActions} from 'react-redux-toastr'

const actionTypeIsAPIRequest = (type) => type.match(/(\w|_)+_REQUEST/)
const actionTypeIsAPISuccess = (type) => type.match(/(\w|_)+_SUCCESS/)
const actionHasError = (action) => action.error

export default store => next => action => {

  const toastr = bindActionCreators(toastrActions, next)

  const {type, payload} = action
  // this assume we use redux-api-middleware so the error action may come in
  // several form. See https://www.npmjs.com/package/redux-api-middleware
  // TODO check session failure may happen when session times out. Will need to check whether state has already contain a user
  if (actionTypeIsAPIRequest(type) && !actionHasError(action)) {
    next(requesting({
      type: type,
      requesting: true
    }))
  } else if (actionHasError(action) && type !== CHECK_SESSION_FAILURE) {
    const unauthorized= isUnauthorized(payload)
    let message
    if (unauthorized) {
      message = 'You are not authorized to access this resource!'
      next(becomeUnauthorized())
    } else {
      message = extractErrorMessage(payload);
    }
    toastr.error(message)
    next(requesting({
      type: type,
      requesting: false
    }))
  } else if (actionTypeIsAPISuccess(type)) {
    if (action.meta && action.meta.notification) {
      toastr.success(action.meta.notification, {
        timeOut: 1000
      })
    }
    next(requesting({
      type: type,
      requesting: false
    }))
  }

  // continue the normal chain
  return next(action)
}
