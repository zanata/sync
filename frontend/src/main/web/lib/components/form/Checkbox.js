import React, {PropTypes} from 'react'
import Label from './Label'

const Checkbox = (props) => {
  const {name, checked, onChange} = props
  const id = `${name}-input`
  const label = props.label || name
  const callback = e => onChange(e.target.checked)

  return (
    <div className="form-group">
      <Label forId={id} label={label}/>
      <div className="col-md-7">
        <input type="checkbox" id={id} name={name} onChange={callback}
          checked={checked}/>
      </div>
    </div>
  )
}

Checkbox.propTypes = {
  name: React.PropTypes.string.isRequired,
  onChange: React.PropTypes.func.isRequired,
  label: React.PropTypes.string,
  checked: React.PropTypes.bool
}

export default Checkbox
