import React from 'react'

export default React.createClass({
  getInitialState() {
    return {
      show: false
    }
  },
  _clickCallback(e) {
    e.stopPropagation()
    this.setState({
      show: !this.state.show
    })
  },
  render() {
    const text = this.state.show ? this.props.text : '****'
    return (
      <abbr title={this.props.text} onClick={this._clickCallback}>
        {text}
      </abbr>
    )
  }
})
