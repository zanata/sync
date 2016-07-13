import React, {PropTypes} from 'react'
import WorkSummary from './WorkSummary'
import {redirectToSignIn} from '../utils/route'
import startWebSocket from '../utils/startWebSocket'

const websocketEndpoint = `ws://${location.host}${location.pathname}websocket/jobStatus`

export default React.createClass({
  propTypes: {
    workSummaries: PropTypes.arrayOf(PropTypes.object).isRequired,
    zanataUsername: PropTypes.string,
    loadWorkSummaries: PropTypes.func.isRequired,
    runJob: PropTypes.func.isRequired,
    runningJobs: PropTypes.object.isRequired,
    onJobStatusUpdate: PropTypes.func.isRequired
  },

  // ask for `router` from context
  contextTypes: {
    router: React.PropTypes.object
  },

  componentWillMount() {
    const {zanataUsername, loadWorkSummaries, onJobStatusUpdate} = this.props
    if (zanataUsername) {
      loadWorkSummaries()
      startWebSocket(websocketEndpoint, onJobStatusUpdate)
    } else {
      redirectToSignIn(this.context.router)
    }
  },

  render() {
    const {runJob, workSummaries, runningJobs} = this.props
    const summaries = workSummaries.map(work => {
      return (
        <WorkSummary key={work.id} id={work.id} name={work.name}
          description={work.description}
          syncToRepoJob={work.syncToRepoJob}
          syncToTransServerJob={work.syncToTransServerJob}
          runJob={runJob} runningJobs={runningJobs}
          {...this.props}
        />
      )
    })
    return (
      <div className='row row-cards-pf'>
        {summaries}
      </div>
    )
  }
})
