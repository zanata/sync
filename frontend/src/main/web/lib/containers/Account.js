import React from 'react'
import { connect } from 'react-redux'
import AccountForm from '../components/AccountForm'
import {saveRepoAccount,
  SAVE_ZANATA_ACCOUNT_REQUEST,
  SAVE_REPO_ACCOUNT_REQUEST} from '../actions/accountAction'

const mapStateToProps = (state) => {
  const user = state.configs.user
  const {zanataAccount} = state.accounts
  const savingRepoAccount = !!state.general.requesting[SAVE_REPO_ACCOUNT_REQUEST]
  const srcRepoTypes = state.configs.srcRepoPlugins

  return {
    user,
    zanataAccount,
    savingRepoAccount,
    srcRepoTypes
  }
}

const mapDispatchToProps = (dispatch) => {

  return {
    onSaveRepoAccount: (repoAccount) => dispatch(saveRepoAccount(repoAccount))
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(AccountForm)
