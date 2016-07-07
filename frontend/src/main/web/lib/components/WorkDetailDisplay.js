import React, {PropTypes} from 'react'
import ProgressBar from './ProgressBar'
import EnableOrDisableIcon from './EnableOrDisableIcon'
import cx from 'classnames'
import { isError, isSuccess, isRunning } from '../constants/Enums'
import { duration, datePropValidator, formatDate } from '../utils/DateTime'

const ToggleReveal = React.createClass({
  getInitialState() {
    return {
      show: false
    }
  },
  _clickCallback(e) {
    e.stopPropagation()
    this.setState({
      show: !this.state.show
    })
  },
  render() {
    const text = this.state.show ? this.props.text : '****'
    return (
      <abbr title={this.props.text} onClick={this._clickCallback}>
        {text}
      </abbr>
    )
  }
})

export default React.createClass({
  propTypes: {
    loadWorkDetail: PropTypes.func.isRequired,
    workDetail: PropTypes.shape({
      id: PropTypes.number.isRequired,
      name: PropTypes.string.isRequired,
      createdDate: PropTypes.string.isRequired,
      srcRepoPluginName: PropTypes.string.isRequired,
      srcRepoPluginConfig: PropTypes.object.isRequired,
      syncToServerEnabled: PropTypes.bool.isRequired,
      syncToRepoEnabled: PropTypes.bool.isRequired,
      jobRunHistory: PropTypes.arrayOf(PropTypes.shape({
        id: PropTypes.string.isRequired,
        status: PropTypes.string.isRequired,
        startTime: datePropValidator,
        endTime: datePropValidator
      }))
    }),
    deleteWork: PropTypes.func.isRequired
  },

  componentDidMount() {
    this.props.loadWorkDetail(this.props.routeParams.id)
  },

  render() {
    const {workDetail} = this.props

    if (!workDetail) {
      return null
    }

    const {name, description, createdDate, srcRepoPluginName,
      srcRepoPluginConfig,
      syncToServerEnabled, syncToRepoEnabled,
      syncToZanataCron, syncToRepoCron, jobRunHistory
    } = workDetail

    const descDisplay = description ? (<p>{description}</p>) : null

    const history = jobRunHistory.map(status => {
      const className = cx({
        'success': isSuccess(status.status),
        'danger': isError(status.status),
        'warning': isRunning(status.status)
      })
      const durationDisplay = duration(status.startTime, status.endTime)
      return (
        <tr key={status.id}>
            <td className={className}>{status.status}</td>
            <td>{formatDate(status.startTime)}</td>
            <td>{durationDisplay}</td>
        </tr>
      )
    })

    const deleteCallback = (e) => this.props.deleteWork(this.props.routeParams.id)

    return (
      <div className='container-fluid'>
        <div className='row'>
          <div className='panel panel-info'>
            <div className='panel-heading'>
              <h3 className='panel-title text-center'>
                {name}
                <button type="button" onClick={deleteCallback}
                  className='pull-right btn btn-danger btn-xs'>Delete
                </button>
              </h3>
            </div>
            <div className='panel-body'>
              <blockquote>
                {descDisplay}
                <footer>created at {formatDate(createdDate)}</footer>
              </blockquote>
              <ul className="list-group">
                <li className="list-group-item">
                  <span className='label label-info'>{srcRepoPluginName}</span>source repo: {srcRepoPluginConfig.username}:<ToggleReveal text={srcRepoPluginConfig.secret} />@{srcRepoPluginConfig.url}
                </li>
                <li className='list-group-item'>
                  <ul className="list-inline">
                    <li>{syncToZanataCron} Sync to Zanata <EnableOrDisableIcon enabled={syncToServerEnabled}/></li>
                    <li>{syncToRepoCron} Sync to repo <EnableOrDisableIcon enabled={syncToRepoEnabled} /></li>
                  </ul>
                </li>
              </ul>
            </div>
          </div>
        </div>
        <div className='row'>
          <table className='table table-striped'>
            <thead>
              <tr>
                <th>Status</th>
                <th>Start time</th>
                <th>Duration</th>
              </tr>
            </thead>
            <tbody>
              {history}
            </tbody>
          </table>
        </div>
      </div>
    )
  }
})
