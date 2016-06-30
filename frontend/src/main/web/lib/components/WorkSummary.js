import React from 'react'
import JobSummary from './JobSummary'

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

    const cardHeight = 460
    const cardStyle = {height: cardHeight + 'px'}
    const cardBodyStyle = {height: cardHeight * 0.8 + 'px'}
    return (
      <div className="col-xs-12 col-sm-6 col-md-4">
        <div className="card-pf" style={cardStyle}>
          <div className="card-pf-heading">
            <h4 className="card-pf-title">{name}</h4>
          </div>
          <div className="card-pf-body" style={cardBodyStyle}>
            {syncToRepoSummary}
            {syncToZanataSummary}
          </div>
        </div>

      </div>
    )
  }
})
