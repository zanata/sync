import React from 'react'
import { connect } from 'react-redux'
import SignInForm from '../components/SignInForm'
import { checkSession } from '../actions'

const mapStateToProps = (state) => {
  return {
    zanataOAuthUrls: state.configs.zanataOAuthUrls,
    user: state.configs.user,
    serverReturnUnauthorized: state.security.serverReturnUnauthorized
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    isSessionLoggedIn: () => {
      dispatch(checkSession())
    }
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(SignInForm)

