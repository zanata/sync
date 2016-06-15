import React from 'react'
import Select from './form/Select'
import TextInput from './form/TextInput'
import RadioGroup from './form/RadioGroup'
import ToggleFieldSet from './form/ToggleFieldSet'
import cx from 'classnames'
import Configs from '../constants/Configs'
import { push } from 'react-router-redux'

export default React.createClass({
  propTypes: {
    onSaveNewWork: React.PropTypes.func.isRequired,
    creating: React.PropTypes.bool.isRequired,
    created: React.PropTypes.bool.isRequired,
    // TODO use shape to be more specific
    srcRepoPlugins: React.PropTypes.arrayOf(React.PropTypes.object).isRequired
  },
  getInitialState() {
    return {
      name: '',
      description: '',
      syncToZanataEnabled: true,
      syncOption: 'SOURCE',
      syncToZanataCron: 'MANUAL',
      syncToRepoEnabled: true,
      // srcRepoPlugins will never change so this is not an anti-pattern
      // TODO check whether there are any plugins (do it in index.js)
      selectedRepoPluginName: this.props.srcRepoPlugins[0].name,
      syncToRepoCron: 'MANUAL'
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
    console.log('Reset')
    this.setState({myinput: ''})
  },

  componentWillMount() {
    if (!this.props.loggedIn) {
      // TODO use props not Configs
      const path = `${Configs.basename}`;
      console.info('redirect to home page for sign in:' + path)
      this.context.router.push({
        pathname: path,
        // query: { modal: true },
        state: { needSignIn: true }
      })
    }
  },

  render() {
    const callbackFor = (field) => {
      return this._handleChange.bind(this, field)
    }

    const saveCallback = e => this.props.onSaveNewWork(this.state)

    const saveBtnText = this.props.creating ? 'Saving...' : 'Save'
    const saveBtnDisabled = this.props.creating
    const msgClass = cx('col-md-3', 'text-right', {
      'bg-danger': this.props.error,
      'bg-success': this.props.created,
    })
    let msgContent = this.props.error ? this.props.error.message : undefined
    if (this.props.created) {
      msgContent = 'Saved successfully'
    }

    const srcRepoPluginsName = this.props.srcRepoPlugins.map(plugin => plugin.name)

    const selectedPluginName = this.state.selectedRepoPluginName

    const selectedPlugin = this.props.srcRepoPlugins.filter(
      plugin => plugin.name === selectedPluginName
    )[0]


    const selectedPluginFields = Object.keys(selectedPlugin.fields).map(key => {
      // TODO field tooltip
      const field = selectedPlugin.fields[key]
      return (
        <TextInput key={field.key} name={field.key} label={field.label}
          onChange={callbackFor(`${selectedPluginName}${field.key}`)}
          placeholder={field.placeholder} isSecret={field.masked}/>
      )
    })

    return (
      <div>
        <form className="form-horizontal">
          <TextInput name='name' onChange={callbackFor('name')}
            placeholder='work name' inputValue={this.state.name}/>
          <TextInput name='description' onChange={callbackFor('description')}
            lines={3} inputValue={this.state.description}/>

          <ToggleFieldSet name='syncToZanataEnabled'
            legend='Sync to Zanata server settings'
            label='Enable sync to Zanata server'
            onChange={callbackFor('syncToZanataEnabled')}
            enabled={this.state.syncToZanataEnabled}>
            <RadioGroup name='syncOption' label='Synchronization option'
              options={['SOURCE', 'TRANSLATION', 'BOTH']}
              onChange={callbackFor('syncOption')}
              selected={this.state.syncOption}
            />
            <Select name='syncToZanataCron' label='Runs'
              onChange={callbackFor('syncToZanataCron')}
              options={['MANUAL', 'ONE_HOUR', 'TWO_HOUR', 'SIX_HOUR']}
              optionsDesc={['Manually', 'Every hour', 'Every two hour', 'Every six hour']}
              selected={this.state.syncToZanataCron}
            />
          </ToggleFieldSet>

          <ToggleFieldSet name='syncToRepoEnabled'
            legend='Source repository settings'
            label='Enable sync to source repository'
            onChange={callbackFor('syncToRepoEnabled')}
            enabled={this.state.syncToRepoEnabled}>
            <Select name='syncToRepoCron' label='Runs'
              onChange={callbackFor('syncToRepoCron')}
              options={['MANUAL', 'ONE_HOUR', 'TWO_HOUR', 'SIX_HOUR']}
              optionsDesc={['Manually', 'Every hour', 'Every two hour', 'Every six hour']}
              selected={this.state.syncToRepoCron}
            />
            <Select name='srcRepoPlugin' label='Source repository plugin'
              onChange={callbackFor('srcRepoPlugin')}
              options={srcRepoPluginsName}
              selected={this.state.selectedRepoPluginName}
            />
            {selectedPluginFields}

          </ToggleFieldSet>

          <div className="form-group">
            <div className={msgClass}>{msgContent}</div>
            <div className="col-md-7 ">
              <button type="button" className="btn btn-primary"
                onClick={saveCallback}
                disabled={saveBtnDisabled}>
                {saveBtnText}
              </button>
              <button type="button" className="btn btn-default">Cancel</button>
            </div>
          </div>
        </form>

      </div>
    )
  }
})

