import React, {PropTypes} from 'react'
import NavBanner from '../components/NavBanner'
import { logout } from '../actions'
import { connect } from 'react-redux'

// this represents the root of the app
const App = React.createClass({
  propTypes: {
    zanataUser: PropTypes.object,
    onLogout: PropTypes.func.isRequired
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
        <div className="container-fluid container-cards-pf">
          <div className="row">
            <div className="col-sm-6 col-md-8 col-sm-push-3 col-md-push-2">
              <h2>Zanata Sync {message}</h2>
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
    zanataUser: state.zanata.user
  }
}

const mapDispatcherToProps = (dispatch) => {
  return {
    onLogout: () => dispatch(logout())
  }
}

export default connect(mapStateToProps, mapDispatcherToProps)(App)
