import React, {PropTypes} from 'react'
import NavBanner from '../components/NavBanner'
import { logout } from '../actions'
import { connect } from 'react-redux'
import SessionTimedOut from '../components/SessionTimedOut'
import NotificationBar from '../components/NotificationBar'

// this represents the root of the app
const App = React.createClass({
  propTypes: {
    zanataUser: PropTypes.object,
    loggedOut: PropTypes.bool,
    onLogout: PropTypes.func.isRequired,
    isUnauthorized: PropTypes.bool.isRequired,
    notification: PropTypes.object
  },

  componentWillReceiveProps(nextProps) {
    if (nextProps.loggedOut) {
      const {origin, pathname} = window.location
      window.location = `${origin}${pathname}`
    }
  },

  render() {
    const {isUnauthorized, zanataUser, location, children, notification} = this.props
    let message = 'Please sign in to a Zanata server'
    if (zanataUser) {
       message = `${zanataUser.name}@${zanataUser.zanataServer}`
    }
    message = message && (<span className='small'>{message}</span>)
    // check current route, see if it's indexRoute (which has sign in form)
    const isIndexRoute = location.pathname === '/'
    // this.props.children is passed down by nested react router.
    // if unauthorized and is not index route, then replace children to
    // SessionTimedOut
    const routeComponent = isUnauthorized && !isIndexRoute ?
      (<SessionTimedOut />) : children

    // display any notification
    let notificationBar = null
    if (notification.message) {
      notificationBar = (<NotificationBar isError={notification.isError} message={notification.message} />)
    }
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
              {notificationBar}
            </div>
          </div>
          <div className="row">
            <div className="col-sm-6 col-md-8 col-sm-push-3 col-md-push-2">
              {/* this is passed down by nested react router */}
              {routeComponent}
            </div>
          </div>
        </div>
      </div>
    )
  }
})

const mapStateToProps = (state) => {
  return {
    zanataUser: state.configs.user,
    loggedOut: state.security.loggedOut,
    isUnauthorized: state.global.isUnauthorized,
    notification: state.global.notification
  }
}

const mapDispatcherToProps = (dispatch) => {
  return {
    onLogout: () => dispatch(logout())
  }
}

export default connect(mapStateToProps, mapDispatcherToProps)(App)
