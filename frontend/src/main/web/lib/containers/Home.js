import React from 'react'
import { connect } from 'react-redux'
import SignInForm from '../components/SignInForm'
import { selectZanataServer, checkSession } from '../actions'

const mapStateToProps = (state) => {
  return {
    zanataServerUrls: state.configs.zanataServerUrls,
    zanataOAuthUrl: state.security.zanataOAuthUrl,
    zanataUser: state.configs.user,
    error: state.security.error
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    onSignIn: (zanataUrl) => {
      dispatch(selectZanataServer(zanataUrl))
    },
    isSessionLoggedIn: () => {
      dispatch(checkSession())
    }
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(SignInForm)

