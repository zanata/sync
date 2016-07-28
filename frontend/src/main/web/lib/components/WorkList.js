import React, {PropTypes} from 'react'
import WorkSummary from './WorkSummary'
import {redirectToSignIn} from '../utils/route'
import {startWebSocket} from '../utils/startWebSocket'

// TODO this is to cater different scenarios: in dev machine and in ITOS... make this a bit better
// openshift uses a different port for websocket
// see http://stackoverflow.com/a/19952072/345718
// ITOS firewall blocked 8000 but 8443 is open
const webSocketPort = (protocol, webSocketPort) => {
  if (webSocketPort) {
    return `:${webSocketPort}`
  } else {
    const currentPort = location.port
    if (protocol === 'ws') {
      return currentPort ? `:${currentPort}` : ''
    }
    // we use the default port
    return ''
  }
}
const webSocketProtocol = (webSocketPort) => {
  if (webSocketPort == 8443 || webSocketPort == 443) {
    return 'wss'
  }
  return 'ws'
}

export default React.createClass({
  propTypes: {
    workSummaries: PropTypes.arrayOf(PropTypes.object).isRequired,
    user: PropTypes.object,
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
    const {websocketPort, onJobStatusUpdate} = this.props
    const protocol = webSocketProtocol(websocketPort)
    const port = webSocketPort(protocol, websocketPort)

    const websocketEndpoint = `${protocol}://${location.hostname}${port}${location.pathname}websocket/jobStatus`
    this.websocket = startWebSocket(websocketEndpoint, onJobStatusUpdate)
  },

  componentWillMount() {
    const {user, loadWorkSummaries} = this.props
    if (user) {
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
          supportWebSocket={!!this.websocket}
          {...this.props}
        />
      )
    })
    return (
      <div className='list-group'>
        {summaries}
      </div>
    )
  }
})
