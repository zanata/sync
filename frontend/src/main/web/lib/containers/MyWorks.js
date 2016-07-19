import React from 'react'
import { connect } from 'react-redux'
import WorkList from '../components/WorkList'
import { loadWorkSummaries, runJob, updateJobStatus } from '../actions'

const mapStateToProps = (state) => {
  const username = state.configs.user ? state.configs.user.username : undefined
  const {workSummaries, runningJobs} = state.myWorks
  const websocketPort = state.configs.websocketPort
  return {
    zanataUsername: username,
    workSummaries,
    runningJobs,
    websocketPort
  }
}

const mapDispatchToProps = (dispatch) => {

  return {
    loadWorkSummaries: () => {
      dispatch(loadWorkSummaries())
    },
    runJob: (workId, jobType) => {
      dispatch(runJob(workId, jobType))
    },
    onJobStatusUpdate: (jobStatus) => {
      dispatch(updateJobStatus(jobStatus));
    }
    // TODO add a prop to reload work summary?
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(WorkList)
