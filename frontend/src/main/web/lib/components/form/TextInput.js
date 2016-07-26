import React, {PropTypes} from 'react'
import Label from './Label'

export default React.createClass({
  propTypes: {
    name: PropTypes.string.isRequired,
    onChange: PropTypes.func.isRequired,
    inputValue: PropTypes.string,
    label: PropTypes.string,
    placeholder: PropTypes.string,
    lines: PropTypes.number,
    isSecret: PropTypes.bool
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
