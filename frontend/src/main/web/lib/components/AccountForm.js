import React, {PropTypes} from 'react'
import TextInput from './form/TextInput'
import Select from './form/Select'
import {redirectToSignIn} from '../utils/route'
import RepoAccountLineItem from './RepoAccountLineItem'


export default React.createClass({
  propTypes: {
    user: PropTypes.shape({
      username: PropTypes.string.isRequired
    }),
    onSaveZanataAccount: PropTypes.func.isRequired,
    zanataAccount: PropTypes.shape({
      zanataServer: PropTypes.string,
      username: PropTypes.string,
      apiKey: PropTypes.string,
      repoAccounts: PropTypes.arrayOf(PropTypes.shape({
        id: PropTypes.number.isRequired,
        repoType: PropTypes.string.isRequired,
        repoHostname: PropTypes.string.isRequired,
        username: PropTypes.string,
        secret: PropTypes.string
      }))
    }),
    savingZanataAccount: PropTypes.bool.isRequired,
    savingRepoAccount: PropTypes.bool.isRequired,
    onSaveRepoAccount: PropTypes.func.isRequired,
    srcRepoTypes: PropTypes.arrayOf(PropTypes.string).isRequired
  },

  getInitialState() {
    const {zanataServer, username, apiKey} = this.props.zanataAccount
    return {
      zanataUsername: username || '',
      zanataSecret: apiKey || '',
      zanataServer: zanataServer || '',

      repoId: -1,
      repoType: this.props.srcRepoTypes[0],
      repoHostname: '',
      repoUsername: '',
      repoSecret: ''
    }
  },

  componentWillReceiveProps(nextProps) {
    if (nextProps.zanataAccount) {
      const {zanataServer, username, apiKey} = nextProps.zanataAccount
      this.setState({
        zanataUsername: username,
        zanataServer,
        zanataSecret: apiKey
      })
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

  _callbackFor(field) {
    return this._handleChange.bind(this, field)
  },

  _handleResetZanata() {
    this.setState({
      zanataUsername: '',
      zanataSecret: '',
      zanataServer: ''
    })
  },

  _selectRepoAccount(account) {
    // setState will merge old state with new one
    this.setState({
      repoId: account.id,
      repoUsername: account.username,
      repoSecret: account.secret,
      repoHostname: account.repoHostname,
      repoType: account.repoType
    })
  },

  _addNewRepoAccount() {
    const {repoType} = this.getInitialState()
    this.setState({
      repoId: -1,
      repoUsername: '',
      repoSecret: '',
      repoHostname: '',
      repoType
    })
  },

  componentWillMount() {
    const user = this.props.user
    if (!user) {
      redirectToSignIn(this.context.router)
    }
  },

  render() {
    const {savingZanataAccount, savingRepoAccount, zanataAccount} = this.props

    const saveZanataAccountCallback = e => this.props.onSaveZanataAccount(this.state)

    // repo accounts
    const repoAccountItems = zanataAccount.repoAccounts.map(acc => {
      return (
        <RepoAccountLineItem key={acc.id} url={acc.repoHostname}
          username={acc.username} secret={acc.secret} repoType={acc.repoType}
          classNames={{active: acc.id === this.state.repoId}}
          onLineItemClick={() => this._selectRepoAccount(acc)}
        />
      )
    })

    // selected repo account
    const saveRepoAccountCallback = e => this.props.onSaveRepoAccount(this.state)
    let saveBtnText
    if (savingRepoAccount) {
      saveBtnText = 'Saving...'
    } else if (this.state.repoId < 0) {
      saveBtnText = 'Save New Account'
    } else {
      saveBtnText = 'Update Account'
    }

    return (
      <div className="form-horizontal">
        <fieldset>
          <legend>Associated Zanata Account</legend>
          <TextInput name='zanataServer' label='Server URL'
            onChange={this._callbackFor('zanataServer')}
            placeholder='http://translate.zanata.org'
            inputValue={this.state.zanataServer}/>
          <TextInput name='zanataUsername' label='Username'
            onChange={this._callbackFor('zanataUsername')}
            placeholder='username'
            inputValue={this.state.zanataUsername}/>
          <TextInput name='zanataSecret' label='Secret'
            onChange={this._callbackFor('zanataSecret')}
            inputValue={this.state.zanataSecret} isSecret
          />

          <div className="form-group">
            <div className='col-md-3'></div>
            <div className="col-md-7 ">
              <button type="button" className="btn btn-primary"
                onClick={saveZanataAccountCallback}
                disabled={savingZanataAccount}>
                {savingZanataAccount ? 'Saving...' : 'Save'}
              </button>
              <button type="button" className="btn btn-default"
                onClick={this._handleResetZanata}>Reset</button>
            </div>
          </div>
        </fieldset>

        <fieldset>
          <legend>Associated Source Repository Accounts</legend>
          <div className="list-group col-md-7 col-md-offset-3">
            {repoAccountItems}
          </div>

          <div className="form-group">
            <div className="col-md-7 col-md-offset-3">
              <button type="button" className="btn btn-danger btn-lg"
                onClick={this._addNewRepoAccount}> + </button>
            </div>
          </div>

          <Select name='srcRepoType' label='Source repository type'
            onChange={this._callbackFor('repoType')}
            options={this.props.srcRepoTypes}
            selected={this.state.repoType}
          />
          <TextInput name='repoHostname' label='Repo Host URL'
            onChange={this._callbackFor('repoHostname')}
            placeholder='https://github.com'
            inputValue={this.state.repoHostname}/>
          <TextInput name='repoUsername' label='Username'
            onChange={this._callbackFor('repoUsername')}
            placeholder='username'
            inputValue={this.state.repoUsername}/>
          <TextInput name='repoSecret' label='Secret'
            onChange={this._callbackFor('repoSecret')}
            inputValue={this.state.repoSecret} isSecret
          />

          <div className="form-group">
            <div className='col-md-3'></div>
            <div className="col-md-7 ">
              <button type="button" className="btn btn-primary"
                onClick={saveRepoAccountCallback}
                disabled={savingRepoAccount}>
                {saveBtnText}
              </button>
            </div>
          </div>

        </fieldset>
      </div>
    )
  }
})
