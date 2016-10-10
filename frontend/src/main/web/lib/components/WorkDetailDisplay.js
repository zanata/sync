import React, {PropTypes} from 'react'
import ProgressBar from './ProgressBar'
import EnableOrDisableIcon from './EnableOrDisableIcon'
import RepoAccountLineItem from './RepoAccountLineItem'
import cx from 'classnames'
import { isError, isSuccess, isRunning, toJobDescription } from '../constants/Enums'
import { duration, datePropValidator, formatDate } from '../utils/DateTime'

const syncWebhookURL = (apiUrl, id) => {
  return `${location.protocol}//${location.host}${apiUrl}/api/work/${id}/translation/changed`
}

export default React.createClass({
  propTypes: {
    loadWorkDetail: PropTypes.func.isRequired,
    apiUrl: PropTypes.string.isRequired,
    workDetail: PropTypes.shape({
      id: PropTypes.number.isRequired,
      name: PropTypes.string.isRequired,
      createdDate: PropTypes.string.isRequired,
      repoAccount: PropTypes.shape({
        id: PropTypes.number.isRequired,
        repoType: PropTypes.string.isRequired,
        username: PropTypes.string,
        secret: PropTypes.string
      }).isRequired,
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

  // ask for `router` from context
  contextTypes: {
    router: React.PropTypes.object
  },

  render() {
    const {workDetail, apiUrl} = this.props

    if (!workDetail || workDetail.id !== parseInt(this.props.routeParams.id)) {
      return <ProgressBar loading={true}/>
    }

    if (workDetail.deleted && workDetail.id == parseInt(this.props.routeParams.id)) {
      this.context.router.push({
        pathname: '/work/mine'
      })
      return <div>deleted</div>
    }


    const {name, description, createdDate, syncToServerEnabled, syncToRepoEnabled,
      repoAccount, srcRepoUrl, srcRepoBranch, syncToRepoCron,
      syncToZanataOption, syncToZanataCron,
      jobRunHistory
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
            <td>{toJobDescription(status.jobType)}</td>
            <td className={className}>{status.status}</td>
            <td>{formatDate(status.startTime)}</td>
            <td>{durationDisplay}</td>
        </tr>
      )
    })

    const deleteCallback = (e) => this.props.deleteWork()

    const webhookURL = syncToRepoEnabled && syncToRepoCron === 'WEBHOOK' ?
      (<span className='text-info'>Sync to repo webhook URL: {syncWebhookURL(apiUrl, workDetail.id)}</span>) : undefined

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
                <RepoAccountLineItem url={srcRepoUrl} username={repoAccount.username}
                  secret={repoAccount.secret} branch={srcRepoBranch}
                  repoType={repoAccount.repoType} />
                <li className='list-group-item'>
                  <ul className="list-inline">
                    <li>Sync <strong className='text-info'>{syncToZanataOption}</strong> to Zanata {syncToZanataCron} <EnableOrDisableIcon enabled={syncToServerEnabled}/></li>
                    <li>Sync to repo {syncToRepoCron} <EnableOrDisableIcon enabled={syncToRepoEnabled} /> {webhookURL}</li>
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
                <th>Sync Job Type</th>
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
