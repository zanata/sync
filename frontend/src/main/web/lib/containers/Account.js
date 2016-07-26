import React from 'react'
import { connect } from 'react-redux'
import AccountForm from '../components/AccountForm'
import {getZanataAccount, saveZanataAccount,
  SAVE_ZANATA_ACCOUNT_REQUEST} from '../actions/accountAction'

const mapStateToProps = (state) => {
  const user = state.configs.user
  const {zanataAccount} = state.accounts
  const saving = !!state.general.requesting[SAVE_ZANATA_ACCOUNT_REQUEST]
  return {
    user,
    zanataAccount,
    saving
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
