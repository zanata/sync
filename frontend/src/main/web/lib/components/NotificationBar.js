import React, {PropTypes} from 'react'
import cx from 'classnames'

const NotificationBar = (props) => {
  const className = cx({
    'bg-danger': props.isError,
    'bg-info': !props.isError
  })
  return (<h3 className={className}>{props.message}</h3>)
}

NotificationBar.propTypes = {
  isError: PropTypes.bool,
  message: PropTypes.string.isRequired
}

export default NotificationBar
