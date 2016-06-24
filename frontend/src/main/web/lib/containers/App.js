import React, {PropTypes} from 'react'
import NavBanner from '../components/NavBanner'
import { logout } from '../actions'
import { connect } from 'react-redux'

// this represents the root of the app
const App = React.createClass({
  propTypes: {
    zanataUser: PropTypes.object,
    loggedOut: PropTypes.bool,
    onLogout: PropTypes.func.isRequired
  },

  componentWillReceiveProps(nextProps) {
    if (nextProps.loggedOut) {
      const {origin, pathname} = window.location
      window.location = `${origin}${pathname}`
    }
  },

  render() {
    const {zanataUser} = this.props
    let message = 'Please sign in to a Zanata server'
    if (zanataUser) {
       message = `${zanataUser.name}@${zanataUser.zanataServer}`
    }
    message = message && (<span className='small'>{message}</span>)
    return (
      <div>
        <NavBanner name={zanataUser && zanataUser.name}
          zanataServer={zanataUser && zanataUser.zanataServer}
          onLogout={this.props.onLogout}
        />
        <h2 className='text-center'>Zanata Sync {message}</h2>
        <div className="container-fluid container-cards-pf">
          <div className="row">
            <div className="col-sm-6 col-md-8 col-sm-push-3 col-md-push-2">
              {/* this is passed down by nested react router */}
              {this.props.children}
            </div>
          </div>
        </div>
      </div>
    )
  }
})

const mapStateToProps = (state) => {
  return {
    zanataUser: state.zanata.user,
    loggedOut: state.security.loggedOut
  }
}

const mapDispatcherToProps = (dispatch) => {
  return {
    onLogout: () => dispatch(logout())
  }
}

export default connect(mapStateToProps, mapDispatcherToProps)(App)
