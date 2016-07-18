import React, {PropTypes} from 'react'
import NavBanner from '../components/NavBanner'
import { logout, deleteWork, toggleDeleteConfirmation } from '../actions'
import { connect } from 'react-redux'
import SessionTimedOut from '../components/SessionTimedOut'
import DeleteConfirmation from '../components/modal/Confirmation'

// this represents the root of the app
const App = React.createClass({
  propTypes: {
    zanataUser: PropTypes.object,
    loggedOut: PropTypes.bool,
    onLogout: PropTypes.func.isRequired,
    serverReturnUnauthorized: PropTypes.bool.isRequired,
    showDeleteConfirmation: PropTypes.bool.isRequired,
    selectedConfig: PropTypes.object,
    onCloseDeleteConfirmation: PropTypes.func.isRequired,
    onConfirmDeleteConfig: PropTypes.func.isRequired
  },

  componentWillReceiveProps(nextProps) {
    if (nextProps.loggedOut) {
      const {origin, pathname} = window.location
      window.location = `${origin}${pathname}`
    }
  },

  render() {
    const {serverReturnUnauthorized, zanataUser, location,
      children, selectedConfig, showDeleteConfirmation,
      onCloseDeleteConfirmation, onConfirmDeleteConfig} = this.props
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
    const routeComponent = serverReturnUnauthorized && !isIndexRoute ?
      (<SessionTimedOut />) : children

    const selectedConfigName = (selectedConfig && selectedConfig.name)

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
              {routeComponent}
            </div>
          </div>
        </div>
        <DeleteConfirmation id="deleteWorkConfirm" okBtnText="Delete"
          title={`Delete ${selectedConfigName}?`}
          content={`Are you sure you want to delete ${selectedConfigName}?`}
          show={showDeleteConfirmation}
          onClose={onCloseDeleteConfirmation}
          onConfirm={onConfirmDeleteConfig}
        />
      </div>
    )
  }
})

const mapStateToProps = (state) => {
  return {
    zanataUser: state.configs.user,
    loggedOut: state.security.loggedOut,
    serverReturnUnauthorized: state.security.serverReturnUnauthorized,
    showDeleteConfirmation: state.workConfig.showDeleteConfirmation,
    selectedConfig: state.workConfig.workDetail
  }
}

const mapDispatcherToProps = (dispatch) => {
  return {
    onLogout: () => dispatch(logout()),
    onCloseDeleteConfirmation: (id) => dispatch(toggleDeleteConfirmation({
      show: false
    })),
    onConfirmDeleteConfig: (id) => dispatch(deleteWork())
  }
}

export default connect(mapStateToProps, mapDispatcherToProps)(App)
