import React from 'react'
import Label from './Label'

export default React.createClass({
  propTypes: {
    name: React.PropTypes.string.isRequired,
    onChange: React.PropTypes.func.isRequired,
    inputValue: React.PropTypes.string,
    label: React.PropTypes.string,
    placeholder: React.PropTypes.string,
    lines: React.PropTypes.number,
    isSecret: React.PropTypes.bool
  },

  render() {
    const name = this.props.name
    const id = `${name}-input`
    const label = this.props.label || name
    const lines = this.props.lines || 1
    const onChangeFunc = this.props.onChange
    const onChangeCallback = (e) => onChangeFunc(e.target.value)

    let inputElm
    if (lines > 1) {
      inputElm = (
        <textarea id={id} rows={lines} className="form-control"
          name={name}
          onChange={onChangeCallback}
          value={this.props.inputValue}
        />
      )
    } else {
      const type = this.props.isSecret ? 'password' : 'text'
      inputElm = (
        <input id={id} className="form-control" type={type}
          name={name}
          onChange={onChangeCallback}
          value={this.props.inputValue}
          placeholder={this.props.placeholder}
        />
      )
    }
    return (
      <div className="form-group">
        <Label forId={id} label={label} />
        <div className="col-md-7">
          {inputElm}
        </div>
      </div>
    )
  }


})
