import React from 'react'
import {PropTypes} from 'react'

export default React.createClass({
  propTypes: {
    workId: PropTypes.number.isRequired,
    jobKey: PropTypes.string.isRequired,
    jobType: PropTypes.string.isRequired,
    runJob: PropTypes.func.isRequired,
    lastJobStatus: PropTypes.shape({
      status: PropTypes.string.isRequired
    })
  },
  render() {
    const {workId, jobKey, jobType, runJob, lastJobStatus} = this.props
    const runJobCallback = (e) => runJob(workId, jobType)
    const jobDescription = (jobType === 'REPO_SYNC') ? 'Sync to source repo' : 'Sync to Zanata'
    // TODO while running the job, disable the button and show progress
    // TODO handle job run failure
    return (
      <div>
        <div className='clearfix'>
          <span className='text-info'>{jobDescription}</span>
          <button type="button" className="btn btn-primary btn-sm pull-right"
            onClick={runJobCallback}>Run now</button>
        </div>
        <div>
          <span className='text-info'>Last run status:</span>
          <p className='bg-warning'>{lastJobStatus && lastJobStatus.status}</p>
        </div>
      </div>
    )
  }
})
