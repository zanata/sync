import React from 'react'
import NavBanner from './NavBanner'

export default React.createClass({
  render() {
    return (
      <div className="container-fluid container-cards-pf">
        <div className="row">
          <div className="col-sm-6 col-md-8 col-sm-push-3 col-md-push-2">
            <NavBanner />
            <h1>Zanata Sync</h1>

            {/* this is passed down by nested react router */}
            {this.props.children}
          </div>
        </div>
      </div>
    )
  }
})
