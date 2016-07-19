import React, {PropTypes} from 'react'
import WorkSummary from './WorkSummary'
import {redirectToSignIn} from '../utils/route'
import startWebSocket from '../utils/startWebSocket'

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
    const {zanataUsername, loadWorkSummaries, onJobStatusUpdate,
      websocketPort} = this.props
    const port = websocketPort ? websocketPort : location.port
    // openshift uses a different port for websocket
    // see http://stackoverflow.com/a/19952072/345718
    const websocketEndpoint = `ws://${location.hostname}:${port}${location.pathname}websocket/jobStatus`
    if (zanataUsername) {
      loadWorkSummaries()
      this.websocket = startWebSocket(websocketEndpoint, onJobStatusUpdate)
    } else {
      redirectToSignIn(this.context.router)
    }
  },

  componentWillUnmount() {
    if (this.websocket) {
      this.websocket.close()
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
