import React, {PropTypes} from 'react'

const FieldSet = (props) => {
  return (
    <fieldset>
      <legend>{props.legend}</legend>
      {props.children}
    </fieldset>
  )
}

FieldSet.propTypes = {
  legend: React.PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.element
  ]).isRequired
}

export default FieldSet
