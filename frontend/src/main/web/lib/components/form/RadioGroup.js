import React from 'react'
import Label from './Label'

export default React.createClass({
  propTypes: {
    name: React.PropTypes.string.isRequired,
    onChange: React.PropTypes.func.isRequired,
    label: React.PropTypes.string,
    options: React.PropTypes.arrayOf(React.PropTypes.string).isRequired,
    optionsDesc: React.PropTypes.arrayOf(React.PropTypes.string),
    selected: React.PropTypes.string

  },

  render() {
    const name = this.props.name
    const label = this.props.label || name
    const optsDesc = this.props.optionsDesc || this.props.options
    const changeCallback = e => this.props.onChange(e.target.value)
    const selected = this.props.selected || this.props.options[0]
    const options = this.props.options.map((opt, i) => {
      return (
        <div className="radio-inline" key={i}>
          <label>
            <input type="radio" name={name} id={opt} value={opt}
              checked={opt === selected}
              onChange={changeCallback}/>
            {optsDesc[i]}
          </label>
        </div>
      )
    })
    return (
      <div className="form-group">
        <Label label={label} />
        <div className="col-md-7">
          {options}
        </div>
      </div>
    )
  }
})
