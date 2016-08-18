import React, {PropTypes} from 'react'
import Select from './form/Select'
import TextInput from './form/TextInput'
import FormButtons from './form/FormButtons'
import Checkbox from './form/Checkbox'
import RadioGroup from './form/RadioGroup'
import FieldSet from './form/FieldSet'
import {redirectToSignIn} from '../utils/route'
import {objectToKeysAndValuesArray} from '../utils/general'

const ZanataAccountDisplay = (props) => {
  const acc = props.zanataAccount
  return (<span className='text-muted'>{` ${acc.username}@${acc.zanataServer}`}</span>)
}

export default React.createClass({
  propTypes: {
    onSaveNewWork: PropTypes.func.isRequired,
    saving: PropTypes.bool.isRequired,
    srcRepoPlugins: PropTypes.arrayOf(PropTypes.string).isRequired,
    cronOptions: PropTypes.object.isRequired,
    user: PropTypes.object,
    zanataAccount: PropTypes.object
  },

  _getDefaultRepoAccountId(props) {
    const zAcc = props.zanataAccount
    // got accounts from the server, we will set it to be the first one
    if (zAcc && zAcc.repoAccounts && zAcc.repoAccounts.length > 0) {
      return zAcc.repoAccounts[0].id
    }
    return null
  },

  getInitialState() {
    const srcRepoAccountId = this._getDefaultRepoAccountId(this.props)
    return {
      name: '',
      description: '',
      syncToZanataEnabled: true,
      syncOption: 'Source',
      syncToZanataCron: 'MANUAL',
      syncToRepoEnabled: true,
      // srcRepoPlugins and cronOptions will never change so this is not an anti-pattern
      selectedRepoPluginName: this.props.srcRepoPlugins[0],
      syncToRepoCron: 'MANUAL',
      srcRepoAccountId,
      srcRepoUrl: '',
      srcRepoBranch: '',
      zanataWebHookSecret: ''
    }
  },

  componentWillReceiveProps(nextProps) {
    const zAcc = nextProps.zanataAccount
    // if we don't have a default srcRepoAccountId and we have received the
    // accounts from the server, we will set it to be the first one
    if (!this.state.srcRepoAccountId) {
      this.setState({
        srcRepoAccountId: this._getDefaultRepoAccountId(nextProps)
      })
    }
  },

  _handleChange(field, newValue) {
    // this.setState({: event.target.value})
    // TODO use immutable js for better performance
    // console.log(field + '====' + newValue)
    let newState = Object.assign({}, this.state)
    newState[field] = newValue
    this.setState(newState)
  },

  _handleReset() {
    this.setState(this.getInitialState())
  },

  componentWillMount() {
    if (!this.props.user) {
      redirectToSignIn()
    }
  },

  render() {
    const callbackFor = (field) => {
      return this._handleChange.bind(this, field)
    }

    const saveCallback = e => this.props.onSaveNewWork(this.state)

    const {saving, zanataAccount} = this.props

    const repoAccounts = (zanataAccount.repoAccounts || [])
      .filter(acc => acc.repoType === this.state.selectedRepoPluginName)

    // filter repoAccount based on repoURL
    const repoUrl = this.state.srcRepoUrl
    const repoAccountFilter = acc => !repoUrl || repoUrl.startsWith(acc.repoHostname)

    const repoAccountOptions = repoAccounts.filter(repoAccountFilter).map(acc => acc.id)
    const repoAccountLabels = repoAccounts.filter(repoAccountFilter).map(acc => `${acc.username}@${acc.repoHostname}`)

    let repoAccountSelect
    if (repoAccountOptions.length === 0) {
      repoAccountSelect = (<div className="text-danger">Click Account in the menu to create one</div>)
    } else {
      repoAccountSelect = (
        <Select name='srcRepoAccount' label='Repo Account'
          onChange={callbackFor('srcRepoAccountId')}
          options={repoAccountOptions}
          optionsDesc={repoAccountLabels}
          selected={this.state.srcRepoAccountId}
        />
      )
    }

    const {keys: cronDisplays, values: cronValues} = objectToKeysAndValuesArray(this.props.cronOptions)

    const srcRepoSettingsLegend = (
      <span>
        Sync to Zanata server settings
        <ZanataAccountDisplay zanataAccount={zanataAccount}/>
      </span>
    )

    const useZanataWebHook = this.state.syncToRepoCron === 'WEBHOOK'

    const webhookSecretInput = useZanataWebHook && (
        <TextInput name='zanataWebHookSecret' label='Zanata Web Hook Secret (optional)'
          onChange={callbackFor('zanataWebHookSecret')}
          inputValue={this.state.zanataWebHookSecret} isSecret
        />
      )
    return (
      <div>
        <form className="form-horizontal">
          <TextInput name='name' onChange={callbackFor('name')}
            placeholder='work name' inputValue={this.state.name}/>
          <TextInput name='description' onChange={callbackFor('description')}
            lines={3} inputValue={this.state.description}/>

          <FieldSet legend={srcRepoSettingsLegend}>
            <Checkbox name='syncToZanataEnabled'
              label='Enable sync to Zanata server'
              onChange={callbackFor('syncToZanataEnabled')}
              checked={this.state.syncToZanataEnabled}
            />
            <RadioGroup name='syncOption' label='Synchronization option'
              options={['Source', 'Translation', 'Both']}
              onChange={callbackFor('syncOption')}
              selected={this.state.syncOption}
            />
            <Select name='syncToZanataCron' label='Runs'
              onChange={callbackFor('syncToZanataCron')}
              options={cronValues}
              optionsDesc={cronDisplays}
              selected={this.state.syncToZanataCron}
            />
          </FieldSet>

          <FieldSet legend='Source repository settings'>
            <Checkbox name="syncToRepoEnabled"
              label='Enable sync to source repository'
              onChange={callbackFor('syncToRepoEnabled')}
              checked={this.state.syncToRepoEnabled}
            />
            <Select name='syncToRepoCron' label='Runs'
              onChange={callbackFor('syncToRepoCron')}
              options={cronValues}
              optionsDesc={cronDisplays}
              selected={this.state.syncToRepoCron}
            />
            {webhookSecretInput}
            <Select name='srcRepoPlugin' label='Repo type'
              onChange={callbackFor('srcRepoPlugin')}
              options={this.props.srcRepoPlugins}
              selected={this.state.selectedRepoPluginName}
            />
            {repoAccountSelect}
            <TextInput name='srcRepoUrl' label='URL'
              onChange={callbackFor('srcRepoUrl')}
              placeholder='https://github.com/zanata/zanata-server.git'
              inputValue={this.state.srcRepoUrl}/>
            <TextInput name='srcRepoBranch' label='Branch'
              onChange={callbackFor('srcRepoBranch')}
              placeholder='master'
              inputValue={this.state.srcRepoBranch}/>
          </FieldSet>

          <FormButtons onSave={saveCallback} saving={saving}
            onCancel={this._handleReset}/>
        </form>

      </div>
    )
  }
})

