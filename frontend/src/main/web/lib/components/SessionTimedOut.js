import React from 'react'
import {Link} from 'react-router'

export default React.createClass({

  render() {
    return (
      (
        <div>
          <h3 className="bg-danger">You session has timed out</h3>
          Go to <Link to="/">home</Link> to sign in again.
        </div>
      )
    )
  }
})
