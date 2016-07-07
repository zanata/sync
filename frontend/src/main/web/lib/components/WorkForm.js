import React, {PropTypes} from 'react'
import Select from './form/Select'
import TextInput from './form/TextInput'
import RadioGroup from './form/RadioGroup'
import ToggleFieldSet from './form/ToggleFieldSet'
import cx from 'classnames'
import {route} from '../utils'
import { API_DONE, API_ERROR, API_IN_PROGRESS} from '../constants/commonStateKeys'

export default React.createClass({
  propTypes: {
    onSaveNewWork: PropTypes.func.isRequired,
    created: PropTypes.bool.isRequired,
    // TODO use shape to be more specific
    srcRepoPlugins: PropTypes.arrayOf(React.PropTypes.object).isRequired,
    zanataUser: PropTypes.object
  },
  getInitialState() {
    return {
      name: '',
      description: '',
      syncToZanataEnabled: true,
      syncOption: 'SOURCE',
      syncToZanataCron: 'MANUAL',
      syncToRepoEnabled: true,
      // srcRepoPlugins, zanataUsername and zanataSecret will never change so this is not an anti-pattern
      selectedRepoPluginName: this.props.srcRepoPlugins[0].name,
      zanataUsername: this.props.zanataUsername,
      zanataSecret: this.props.zanataSecret,
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
    this.setState(this.getInitialState())
  },

  componentWillMount() {
    if (!this.props.zanataUser) {
      route.redirectToSignIn(this.context.router)
    }
  },

  render() {
    const callbackFor = (field) => {
      return this._handleChange.bind(this, field)
    }

    const saveCallback = e => this.props.onSaveNewWork(this.state)

    const saveBtnText = this.props[API_IN_PROGRESS] ? 'Saving...' : 'Save'
    const saveBtnDisabled = this.props[API_IN_PROGRESS]
    const msgClass = cx('col-md-3', 'text-right', {
      'bg-danger': this.props.error,
      'bg-success': this.props.created,
    })

    const error = this.props[API_ERROR]
    let msgContent

    if (this.props.created) {
      // TODO maybe put up a toastr message and then reset the form to initial state or redirect to other page?
      // TODO dispatch an action to reset state. e.g. created back to false and maybe re-route to somewhere else
      msgContent = 'Saved successfully'
    } else if (error) {
      msgContent = 'Save failed'
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
              <button type="button" className="btn btn-default"
                onClick={this._handleReset}>Cancel</button>
            </div>
          </div>
        </form>

      </div>
    )
  }
})

