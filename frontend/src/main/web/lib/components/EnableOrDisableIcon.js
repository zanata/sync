import React, {PropTypes} from 'react'

const Component = (props) => {
  if (props.enabled) {
    return (<span className="glyphicon glyphicon-ok text-success" aria-hidden="true"/>)
  } else {
    return (<span className="glyphicon glyphicon-remove text-warning" aria-hidden="true"/>)
  }
}

Component.propTypes = {
  enabled: PropTypes.bool.isRequired
}

export default Component