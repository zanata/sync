import React from 'react'
import Select from './form/Select'
import TextInput from './form/TextInput'
import RadioGroup from './form/RadioGroup'
import ToggleFieldSet from './form/ToggleFieldSet'
import cx from 'classnames'

export default React.createClass({
  propTypes: {
    onSaveNewWork: React.PropTypes.func.isRequired,
    creating: React.PropTypes.bool.isRequired,
    created: React.PropTypes.bool.isRequired
  },
  getInitialState() {
    return {
      name: '',
      description: '',
      syncToZanataEnabled: true,
      syncOption: 'SOURCE',
      syncToZanataCron: 'MANUAL',
      syncToRepoEnabled: true,
      syncToRepoCron: 'MANUAL',
      repoUrl: '',
      repoUsername: '',
      repoSecret: '',
      repoBranch: ''
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
    console.log('Reset')
    this.setState({myinput: ''})
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
            <TextInput name='repoUrl' label='URL'
              onChange={callbackFor('repoUrl')}
              placeholder='file://path/to/your/repo'/>
            <TextInput name='repoBranch' label='Branch'
              onChange={callbackFor('repoBranch')} placeholder='master'/>
            <TextInput name='repoUsername' label='Username'
              onChange={callbackFor('repoUsername')}/>
            <TextInput name='repoSecret' label='Secret'
              onChange={callbackFor('repoSecret')} isSecret/>
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

