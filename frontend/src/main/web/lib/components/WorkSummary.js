import React from 'react'
import JobSummary from './JobSummary'
import {Link} from 'react-router'
import cx from 'classnames'

const {PropTypes} = React

const jobSummaryShape = {
  jobKey: PropTypes.string.isRequired,
  type: PropTypes.string.isRequired,
  enabled: PropTypes.bool.isRequired,
  lastJobStatus: PropTypes.object
}

export default React.createClass({
  propTypes: {
    id: PropTypes.number.isRequired,
    name: PropTypes.string.isRequired,
    description: PropTypes.string,
    syncToRepoJob: PropTypes.shape(jobSummaryShape).isRequired,
    syncToTransServerJob: PropTypes.shape(jobSummaryShape).isRequired,
    runJob: PropTypes.func.isRequired,
    runningJobs: PropTypes.object.isRequired
  },

  getInitialState() {
    return {
      active: false
    }
  },

  _setActiveState(active) {
    this.setState({
      active
    })
  },

  render() {
    const {id, name, description, runJob, syncToRepoJob, syncToTransServerJob,
      runningJobs} = this.props

    // TODO make this an enum
    const syncToRepoRunning = !!runningJobs[id + 'REPO_SYNC']
    const syncToRepoSummary = syncToRepoJob.enabled && (
      <JobSummary workId={id} jobKey={syncToRepoJob.jobKey}
        jobType={syncToRepoJob.type} runJob={runJob}
        lastJobStatus={syncToRepoJob.lastJobStatus}
        running={syncToRepoRunning}
        {...this.props}
      />
    )

    const syncToZanataRunning = !!runningJobs[id + 'SERVER_SYNC']
    const syncToZanataSummary = syncToTransServerJob.enabled && (
      <JobSummary workId={id} jobKey={syncToTransServerJob.jobKey}
        jobType={syncToTransServerJob.type} runJob={runJob}
        lastJobStatus={syncToTransServerJob.lastJobStatus}
        running={syncToZanataRunning}
        {...this.props}
      />
    )

    // TODO we don't display it as it may potentially break the layout. We want to turn this into a tooltip or popover
    const desc = (<h4 className='small'>{description}</h4>)

    const detailLink = `/work/${id}`
    const classNames = cx('list-group-item', {
      active: this.state.active
    })
    return (
      <div className={classNames}
        onMouseOver={() => this._setActiveState(true)}
        onMouseLeave={() => this._setActiveState(false)}
      >
        <h3 className="list-group-item-heading">{name}
          <Link
            className='btn btn-default btn-xs text-muted pull-right'
            to={detailLink}>more detail...</Link>
        </h3>
        <div className="list-group-item-text">
          {syncToRepoSummary}
          {syncToZanataSummary}
        </div>
      </div>
    )
  }
})
