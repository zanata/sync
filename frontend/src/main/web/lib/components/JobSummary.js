import React, {PropTypes} from 'react'
import ProgressBar from './ProgressBar'
import { toJobDescription } from '../constants/Enums'
import { formatDate } from '../utils/DateTime'

const RunDuration = (props) => {
  const {startTime, endTime} = props
  if (startTime) {
    return (
      <div className='col-sm-6'>
        From {formatDate(startTime)} to {formatDate(endTime)}
      </div>
    )
  }
  return null
}

const NextFireTime = (props) => {
  const {startTime, endTime, nextStartTime} = props
  if (nextStartTime && (nextStartTime != startTime && nextStartTime != endTime)) {
    return (
      <div className='col-sm-3'>
        <span className='glyphicon glyphicon-time' aria-hidden="true" alt="next run time"/>
        {formatDate(nextStartTime)}
      </div>
    )
  }
  return null
}

const Status = (props) => {
  return (
    <div className='col-sm-3 text-info'>{props.status}</div>
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
    pollJobStatus: PropTypes.func.isRequired,
    supportWebSocket: PropTypes.bool.isRequired
  },
  render() {
    const {workId, jobType, runJob, lastJobStatus,
      running, pollJobStatus, pollInterval, supportWebSocket} = this.props
    const runJobCallback = (e) => runJob(workId, jobType)
    const jobDescription = toJobDescription(jobType)

    if (running && !supportWebSocket) {
      // poll job status every certain seconds
      setTimeout(() => pollJobStatus(workId, jobType), pollInterval)
    }

    let statusDisplay = null
    if (lastJobStatus && !running) {
      const {startTime, endTime, nextStartTime, status} = lastJobStatus
      statusDisplay = (
        <div className='row'>
          <Status status={status}/>
          <RunDuration startTime={startTime} endTime={endTime}/>
          <NextFireTime startTime={startTime} endTime={endTime}
            nextStartTime={nextStartTime}/>
        </div>
      )
    }
    const loadingBar = running && (
        <div className='row'><ProgressBar loading/></div>
      )

    return (
      <div className='panel panel-default'>
        <div className='panel-heading'>
          <span className='text-info'>{jobDescription}</span>
          <button type="button" className="btn btn-primary btn-sm pull-right"
            disabled={running}
            onClick={runJobCallback}>
            <span className='hidden-xs'>Run now</span>
            <span className='glyphicon glyphicon-play-circle visible-xs-inline'
              aria-hidden="true" />
          </button>
        </div>
        <div className='panel-body'>
          <div className='container-fluid'>
            {loadingBar}
            {statusDisplay}
          </div>
        </div>
      </div>
    )
  }
})
