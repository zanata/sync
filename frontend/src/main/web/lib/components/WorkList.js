import React, {PropTypes} from 'react'
import WorkSummary from './WorkSummary'
import SessionTimedOut from './SessionTimedOut'
import {route} from '../utils'
import { isUnauthorized, extractErrorMessage } from '../utils/errorResponse'

export default React.createClass({
  propTypes: {
    workSummaries: PropTypes.arrayOf(PropTypes.object).isRequired,
    zanataUsername: PropTypes.string,
    loadWorkSummaries: PropTypes.func.isRequired,
    runJob: PropTypes.func.isRequired,
    runningJobs: PropTypes.object.isRequired
  },

  // ask for `router` from context
  contextTypes: {
    router: React.PropTypes.object
  },

  componentWillMount() {
    const {zanataUsername, loadWorkSummaries} = this.props
    if (zanataUsername) {
      loadWorkSummaries(zanataUsername)
    } else {
      route.redirectToSignIn(this.context.router)
    }
  },

  render() {
    const {runJob, workSummaries, runningJobs, error} = this.props
    let errorMessage = null
    if (error) {
      if (isUnauthorized(error)) {
        return (<SessionTimedOut />)
      }
      errorMessage = (<h3 className='bg-danger'>{extractErrorMessage(error)}</h3>)
    }
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
        {errorMessage}
        {summaries}
      </div>
    )
  }
})
