import React, {PropTypes} from 'react'
import cx from 'classnames'

const NotificationBar = (props) => {
  const className = cx("alert alert-fixed", {
    'alert-danger': props.isError,
    'alert-info': !props.isError
  })
  const closeCallback = e => {
    e.stopPropagation()
    props.onDismiss()
  }
  return (
    <div className={className} role="alert">
      <button type="button" className="close" data-dismiss="alert"
        aria-label="Close" onClick={closeCallback}>
        <span aria-hidden="true">&times;</span>
      </button>
      {props.message}
    </div>
  )
}

NotificationBar.propTypes = {
  isError: PropTypes.bool,
  message: PropTypes.string.isRequired,
  onDismiss: PropTypes.func.isRequired
}

export default NotificationBar
