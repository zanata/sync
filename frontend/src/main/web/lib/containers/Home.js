import React from 'react'
import Configs from '../constants/Configs'
import { connect } from 'react-redux'
import SignInForm from '../components/SignInForm'
import { selectZanataServer, getZanataServerUrls } from '../actions'

const mapStateToProps = (state) => {
  return {
    zanataServerUrls: state.security.zanataServerUrls,
    zanataOAuthUrl: state.security.zanataOAuthUrl
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    onSignIn: (zanataUrl) => {
      dispatch(selectZanataServer(zanataUrl))
    },

    getZanataServerUrls: () => {
      dispatch(getZanataServerUrls())
    }
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(SignInForm)

