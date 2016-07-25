import React from 'react'
import { connect } from 'react-redux'
import AccountForm from '../components/AccountForm'
import {getZanataAccount, saveZanataAccount} from '../actions/accountAction'

const mapStateToProps = (state) => {
  const user = state.configs.user
  const {zanataAccount} = state.accounts
  return {
    user,
    zanataAccount
  }
}

const mapDispatchToProps = (dispatch) => {

  return {
    onSaveZanataAccount: (zanataAccount) => {
      dispatch(saveZanataAccount(zanataAccount))
    },
    getZanataAccount: () => dispatch(getZanataAccount())
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(AccountForm)
