import React from 'react'
import cx from 'classnames'

export default React.createClass({
  propTypes: {
    forId: React.PropTypes.string,
    label: React.PropTypes.string.isRequired
  },
  render() {
    const classNames = cx(
      'col-md-3',
      'control-label',
      'text-capitalize'
    )
    return (
      <label className={classNames} htmlFor={this.props.forId}>
        {this.props.label}
      </label>
    )
  }
})
