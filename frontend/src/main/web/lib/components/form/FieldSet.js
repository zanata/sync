import React from 'react'
import Label from './Label'
import cx from 'classnames'

export default React.createClass({
  propTypes: {
    name: React.PropTypes.string.isRequired,
    onChange: React.PropTypes.func.isRequired,
    legend: React.PropTypes.string.isRequired,
    label: React.PropTypes.string,
    enabled: React.PropTypes.bool
  },

  render() {
    const name = this.props.name;
    const id = `${name}-input`
    const label = this.props.label || name
    const callback = e => this.props.onChange(e.target.checked)
    const enabled = this.props.enabled

    return (
      <fieldset>
        <legend>{this.props.legend}</legend>
        <div className="form-group">
          <Label forId={id} label={label}/>
          <div className="col-md-7">
            <input type="checkbox" id={id} name={name} onChange={callback}
              checked={enabled}/>
          </div>
        </div>
        <div>
          {this.props.children}
        </div>
      </fieldset>

    )
  }
})
