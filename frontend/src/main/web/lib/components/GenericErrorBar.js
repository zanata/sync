import React, {PropTypes} from 'react'
import {extractErrorMessage} from '../utils/errorResponse'

const GenericErrorBar = (props) => {
  return (<h3 className='bg-danger'>{extractErrorMessage(props.error)}</h3>)
}

GenericErrorBar.propTypes = {
  error: PropTypes.object.isRequired
}

export default GenericErrorBar
