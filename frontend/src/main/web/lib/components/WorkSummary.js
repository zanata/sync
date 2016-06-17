import React from 'react'
import {PropTypes} from 'react'
import JobSummary from './JobSummary'

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
    runJob: PropTypes.func.isRequired
  },

  render() {
    const progress = (
      <div className="progress-container progress-description-left progress-label-right">
        <div className="progress-description">
          CPU
        </div>
        <div className="progress">
          <div className="progress-bar" role="progressbar" aria-valuenow="25" aria-valuemin="0" aria-valuemax="100" style="width: 25%;" data-toggle="tooltip" title="" data-original-title="25% Used">
            <span><strong>115 of 460</strong> MHz</span>
          </div>
          <div className="progress-bar progress-bar-remaining" role="progressbar" aria-valuenow="75" aria-valuemin="0" aria-valuemax="100" style="width: 75%;" data-toggle="tooltip" title="" data-original-title="75% Available">
            <span className="sr-only">75% Available</span>
          </div>
        </div>
      </div>
    )
    const {id, name, description, runJob, syncToRepoJob, syncToTransServerJob} = this.props

    const syncToRepoSummary = (
      <JobSummary workId={id} jobKey={syncToRepoJob.jobKey}
        jobType={syncToRepoJob.type} runJob={runJob}
        lastJobStatus={syncToRepoJob.lastJobStatus}
      />
    )

    const syncToZanataSummary = (
      <JobSummary workId={id} jobKey={syncToTransServerJob.jobKey}
        jobType={syncToTransServerJob.type} runJob={runJob}
        lastJobStatus={syncToTransServerJob.lastJobStatus}
      />
    )

    const cardHeight = 280
    const cardStyle = {height: cardHeight + 'px'}
    const cardBodyStyle = {height: cardHeight * 0.8 + 'px'}
    return (
      <div className="col-xs-12 col-sm-6 col-md-4">
        <div className="card-pf" style={cardStyle}>
          <div className="card-pf-heading">
            <h4 className="card-pf-title">{name}</h4>
            <h4 className='small'>{description}</h4>
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
