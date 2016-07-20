import React, {PropTypes} from 'react'
import WorkSummary from './WorkSummary'
import {redirectToSignIn} from '../utils/route'
import {startWebSocket, isITOS} from '../utils/startWebSocket'

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

  _connectWebSocket() {
    if (isITOS()) {
      console.info('deployed on ITOS. No websocket support!!')
      return
    }
    const {websocketPort, onJobStatusUpdate} = this.props
    const port = websocketPort ? websocketPort : location.port
    // openshift uses a different port for websocket
    // see http://stackoverflow.com/a/19952072/345718
    // TODO if we are on https, we should use wss:// instead
    const websocketEndpoint = `ws://${location.hostname}:${port}${location.pathname}websocket/jobStatus`
    this.websocket = startWebSocket(websocketEndpoint, onJobStatusUpdate)
  },

  componentWillMount() {
    const {zanataUsername, loadWorkSummaries} = this.props
    if (zanataUsername) {
      loadWorkSummaries()
      this._connectWebSocket()
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
