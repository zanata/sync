import React from 'react'
import {PropTypes} from 'react'

function description(label, value) {
  return (
    <div className='row'>
      <div className='col-sm-4 text-info'>{label}</div>
      <div className='col-sm-8'>{value}</div>
    </div>
  )
}

export default React.createClass({
  propTypes: {
    workId: PropTypes.number.isRequired,
    jobType: PropTypes.string.isRequired,
    runJob: PropTypes.func.isRequired,
    lastJobStatus: PropTypes.shape({
      status: PropTypes.string.isRequired
    }),
    running: PropTypes.bool.isRequired,
    pollJobStatus: PropTypes.func.isRequired
  },
  render() {
    const {workId, jobType, runJob, lastJobStatus,
      running, pollJobStatus} = this.props
    const runJobCallback = (e) => runJob(workId, jobType)
    const jobDescription = (jobType === 'REPO_SYNC') ? 'Sync to source repo' : 'Sync to Zanata'

    if (running) {
      // poll job status every certain seconds
      setTimeout(() => pollJobStatus(workId, jobType), 1500)
    }

    const status = (lastJobStatus && (
        <div className='row'>
          {description('started', lastJobStatus.startTime)}
          {description('ended', lastJobStatus.endTime)}
          {description('next', lastJobStatus.nextStartTime)}
          {description('status', lastJobStatus.status)}
        </div>

      )) || (
        'Not available'
      )


    // TODO handle job run failure
    return (
      <div>
        <div className='clearfix'>
          <span className='text-info'>{jobDescription}</span>
          <button type="button" className="btn btn-primary btn-sm pull-right"
            disabled={running}
            onClick={runJobCallback}>Run now</button>
        </div>
        <div className='container-fluid'>
          <div className='row'><span className='text-info'>Last run status:</span></div>
          {status}
        </div>
      </div>
    )
  }
})
