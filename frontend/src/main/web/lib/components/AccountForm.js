import React, {PropTypes} from 'react'
import TextInput from './form/TextInput'
import {redirectToSignIn} from '../utils/route'

export default React.createClass({
  propTypes: {
    user: PropTypes.shape({
      username: PropTypes.string.isRequired
    }),
    onSaveZanataAccount: PropTypes.func.isRequired,
    getZanataAccount: PropTypes.func.isRequired,
    zanataAccount: PropTypes.object
  },

  getInitialState() {
    return {
      zanataUsername: '',
      zanataSecret: '',
      zanataServer: ''
    }
  },

  // ask for `router` from context
  contextTypes: {
    router: React.PropTypes.object
  },

  _handleChange(field, newValue) {
    let newState = Object.assign({}, this.state)
    newState[field] = newValue
    this.setState(newState)
  },

  _handleReset() {
    this.replaceState(this.getInitialState())
  },

  componentWillMount() {
    const user = this.props.user
    if (!user) {
      redirectToSignIn(this.context.router)
    } else {
      this.props.getZanataAccount()
    }
  },

  render() {
    const callbackFor = (field) => {
      return this._handleChange.bind(this, field)
    }

    const {saving, saveFailed, user} = this.props

    const saveCallback = e => this.props.onSaveZanataAccount(this.state)


    const zanataAccount = this.props.zanataAccount || {}

    const saveBtnText = saving ? 'Saving...' : 'Save'
    const saveBtnDisabled = saving


    return (
      <form className="form-horizontal">
        <fieldset>
          <legend>Associated Zanata Account</legend>
          <TextInput name='zanataServer' label='Server URL'
            onChange={callbackFor('zanataServer')}
            placeholder='http://translate.zanata.org'
            inputValue={zanataAccount.server}/>
          <TextInput name='zanataUsername' label='Username'
            onChange={callbackFor('zanataUsername')}
            placeholder='username'
            inputValue={zanataAccount.username}/>
          <TextInput name='zanataSecret' label='Secret'
            onChange={callbackFor('zanataSecret')}
            inputValue={zanataAccount.secret}
            isSecret
          />

        </fieldset>


        <div className="form-group">
          <div className='col-md-3'></div>
          <div className="col-md-7 ">
            <button type="button" className="btn btn-primary"
              onClick={saveCallback}
              disabled={saveBtnDisabled}>
              {saveBtnText}
            </button>
            <button type="button" className="btn btn-default"
              onClick={this._handleReset}>Cancel</button>
          </div>
        </div>
      </form>
    )
  }
})
