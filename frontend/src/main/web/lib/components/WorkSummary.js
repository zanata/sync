import React from 'react'
import JobSummary from './JobSummary'
import {Link} from 'react-router'

const {PropTypes} = React

const jobSummaryShape = {
  jobKey: PropTypes.string.isRequired,
  type: PropTypes.string.isRequired,
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

  render() {
    const {id, name, description, runJob, syncToRepoJob, syncToTransServerJob,
      runningJobs} = this.props

    // TODO make this an enum
    const syncToRepoRunning = !!runningJobs[id + 'REPO_SYNC']
    const syncToRepoSummary = (
      <JobSummary workId={id} jobKey={syncToRepoJob.jobKey}
        jobType={syncToRepoJob.type} runJob={runJob}
        lastJobStatus={syncToRepoJob.lastJobStatus}
        running={syncToRepoRunning}
        {...this.props}
      />
    )

    const syncToZanataRunning = !!runningJobs[id + 'SERVER_SYNC']
    const syncToZanataSummary = (
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
    return (
      <div className="list-group-item">
        <h4 className="list-group-item-heading">{name} <Link
          className='text-muted pull-right' to={detailLink}>more detail...</Link></h4>
        <div className="list-group-item-text">
          {syncToRepoSummary}
          {syncToZanataSummary}
        </div>
      </div>
    )
  }
})
