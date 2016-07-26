import React, {PropTypes} from 'react'
import Select from './form/Select'
import TextInput from './form/TextInput'
import RadioGroup from './form/RadioGroup'
import FieldSet from './form/FieldSet'
import {redirectToSignIn} from '../utils/route'
import {objectToKeysAndValuesArray} from '../utils/general'

export default React.createClass({
  propTypes: {
    onSaveNewWork: PropTypes.func.isRequired,
    saving: PropTypes.bool.isRequired,
    srcRepoPlugins: PropTypes.arrayOf(PropTypes.string).isRequired,
    cronOptions: PropTypes.object.isRequired,
    user: PropTypes.object
  },
  getInitialState() {
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
      srcRepoUrl: '',
      srcRepoUsername: '',
      srcRepoSecret: '',
      srcRepoBranch: ''
    }
  },

  // ask for `router` from context
  contextTypes: {
    router: React.PropTypes.object
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
    this.replaceState(this.getInitialState())
  },

  componentWillMount() {
    if (!this.props.user) {
      redirectToSignIn(this.context.router)
    }
  },

  render() {
    const callbackFor = (field) => {
      return this._handleChange.bind(this, field)
    }

    const saveCallback = e => this.props.onSaveNewWork(this.state)

    const {saving} = this.props

    const saveBtnText = saving ? 'Saving...' : 'Save'

    const {keys: cronDisplays, values: cronValues} = objectToKeysAndValuesArray(this.props.cronOptions)

    return (
      <div>
        <form className="form-horizontal">
          <TextInput name='name' onChange={callbackFor('name')}
            placeholder='work name' inputValue={this.state.name}/>
          <TextInput name='description' onChange={callbackFor('description')}
            lines={3} inputValue={this.state.description}/>

          <FieldSet name='syncToZanataEnabled'
            legend='Sync to Zanata server settings'
            label='Enable sync to Zanata server'
            onChange={callbackFor('syncToZanataEnabled')}
            enabled={this.state.syncToZanataEnabled}>
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

          <FieldSet name='syncToRepoEnabled'
            legend='Source repository settings'
            label='Enable sync to source repository'
            onChange={callbackFor('syncToRepoEnabled')}
            enabled={this.state.syncToRepoEnabled}>
            <Select name='syncToRepoCron' label='Runs'
              onChange={callbackFor('syncToRepoCron')}
              options={cronValues}
              optionsDesc={cronDisplays}
              selected={this.state.syncToRepoCron}
            />
            <Select name='srcRepoPlugin' label='Source repository plugin'
              onChange={callbackFor('srcRepoPlugin')}
              options={this.props.srcRepoPlugins}
              selected={this.state.selectedRepoPluginName}
            />
            <TextInput name='srcRepoUrl' onChange={callbackFor('srcRepoUrl')}
              placeholder='https://github.com/zanata/zanata-server.git'
              inputValue={this.state.srcRepoUrl}/>
            <TextInput name='srcRepoUsername' onChange={callbackFor('srcRepoUsername')}
              inputValue={this.state.srcRepoUsername}/>
            <TextInput name='srcRepoSecret' onChange={callbackFor('srcRepoSecret')}
              inputValue={this.state.srcRepoSecret} isSecret
            />
            <TextInput name='srcRepoBranch' onChange={callbackFor('srcRepoBranch')}
              placeholder='master'
              inputValue={this.state.srcRepoBranch}/>
          </FieldSet>

          <div className="form-group">
            <div className='col-md-3'></div>
            <div className="col-md-7 ">
              <button type="button" className="btn btn-primary"
                onClick={saveCallback}
                disabled={saving}>
                {saveBtnText}
              </button>
              <button type="button" className="btn btn-default"
                onClick={this._handleReset}>Cancel</button>
            </div>
          </div>
        </form>

      </div>
    )
  }
})

