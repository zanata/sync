import React, {PropTypes} from 'react'
import ProgressBar from './ProgressBar'
import { toJobDescription, isJobFinished } from '../constants/Enums'
import { formatDate } from '../utils/DateTime'

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
    pollInterval: PropTypes.number.isRequired,
    pollJobStatus: PropTypes.func.isRequired
  },
  render() {
    const {workId, jobType, runJob, lastJobStatus,
      running, pollJobStatus, pollInterval} = this.props
    const runJobCallback = (e) => runJob(workId, jobType)
    const jobDescription = toJobDescription(jobType)

    if (running) {
      // poll job status every certain seconds
      setTimeout(() => pollJobStatus(workId, jobType), pollInterval)
    }

    let statusDisplay = null
    if (lastJobStatus && !running) {
      statusDisplay = (
        <div className='container-fluid'>
          {description('status', lastJobStatus.status)}
          {description('started', formatDate(lastJobStatus.startTime))}
          {description('ended', formatDate(lastJobStatus.endTime))}
          {description('next', formatDate(lastJobStatus.nextStartTime))}
        </div>
      )
    }
    let loadingBar = null
    if (running) {
      loadingBar = (
        <div>
          <ProgressBar loading={running}/>
          {/* placeholder so that it won't shift the panel up and down */}
          <div className='container-fluid'>
            <div className='row'>&nbsp;</div>
            <div className='row'>&nbsp;</div>
          </div>
        </div>
      )
    }

    return (
      <div className='panel panel-default'>
        <div className='panel-heading'>
          <span className='text-info'>{jobDescription}</span>
          <button type="button" className="btn btn-primary btn-sm pull-right"
            disabled={running}
            onClick={runJobCallback}>Run now</button>
        </div>
        <div className='panel-body'>
          {loadingBar}
          {statusDisplay}
        </div>
      </div>
    )
  }
})
