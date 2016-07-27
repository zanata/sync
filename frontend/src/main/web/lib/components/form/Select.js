import React, {PropTypes} from 'react'
import Label from './Label'

export default React.createClass({
  propTypes: {
    name: PropTypes.string.isRequired,
    onChange: PropTypes.func.isRequired,
    options: PropTypes.arrayOf(PropTypes.any).isRequired,
    optionsDesc: PropTypes.arrayOf(PropTypes.string),
    label: PropTypes.string,
    selected: PropTypes.any

  },

  render() {
    const name = this.props.name
    const id = `${name}-select`
    const label = this.props.label || name
    const options = this.props.options
    const optionsDesc = this.props.optionsDesc || options
    const selected = this.props.selected
    const optionElms = options.map((opt, i) => {
      return ( <option key={i} value={opt}>{optionsDesc[i]}</option> )
    })
    const onChangeCallback = e => this.props.onChange(e.target.value)

    return (
      <div className="form-group">
        <Label forId={id} label={label} />
        <div className="col-md-7">
          <select id={id} className="form-control" name={name} value={selected}
            onChange={onChangeCallback}>
            {optionElms}
          </select>
        </div>
      </div>
    )
  }
})
