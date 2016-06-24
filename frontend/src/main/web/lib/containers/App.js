import React from 'react'
import NavBanner from '../components/NavBanner'
import { connect } from 'react-redux'

// this represents the root of the app
const App = React.createClass({
  render() {
    const {zanataUser, zanataServer} = this.props
    let message = 'Please sign in to a Zanata server'
    if (zanataUser && zanataServer) {
       message = `${zanataUser.name}@${zanataServer}`
    }
    message = message && (<span className='small'>{message}</span>)
    return (
      <div className="container-fluid container-cards-pf">
        <div className="row">
          <div className="col-sm-6 col-md-8 col-sm-push-3 col-md-push-2">
            <NavBanner />
            <h2>Zanata Sync {message}</h2>
            {/* this is passed down by nested react router */}
            {this.props.children}
          </div>
        </div>
      </div>
    )
  }
})

const mapStateToProps = (state) => {
  return {
    zanataUser: state.zanata.user,
    zanataServer: state.zanata.url
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    onSignIn: (zanataUrl) => {
      dispatch(selectZanataServer(zanataUrl))
    }
  }
}

export default connect(mapStateToProps, null)(App)
