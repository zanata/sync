import React from 'react'
import { connect } from 'react-redux'
import WorkList from '../components/WorkList'
import { loadWorkSummaries, runJob,
  updateJobStatus, getLatestJobStatus } from '../actions'

const mapStateToProps = (state) => {
  const user = state.configs.user
  const {workSummaries, runningJobs} = state.myWorks
  const websocketPort = state.configs.websocketPort
  const pollInterval = state.configs.pollInterval
  return {
    user,
    workSummaries,
    runningJobs,
    websocketPort,
    pollInterval
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
    },
    pollJobStatus: (workId, jobType) => {
      dispatch(getLatestJobStatus(workId, jobType))
    }
    // TODO add a prop to reload work summary?
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(WorkList)
