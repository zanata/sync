import React from 'react'
import { connect } from 'react-redux'
import WorkList from '../components/WorkList'
import { loadWorkSummaries, runJob, getLatestJobStatus } from '../actions'

const mapStateToProps = (state) => {
  const username = state.configs.user ? state.configs.user.username : undefined
  const {workSummaries, error, loading, runningJobs} = state.myWorks
  const pollInterval = state.configs.pollInterval
  return {
    zanataUsername: username,
    workSummaries,
    error,
    loading,
    runningJobs,
    pollInterval
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {

  return {
    loadWorkSummaries: (username) => {
      dispatch(loadWorkSummaries(ownProps.apiUrl))
    },
    runJob: (workId, jobType) => {
      dispatch(runJob(workId, jobType))
    },
    pollJobStatus: (workId, jobType) => {
      dispatch(getLatestJobStatus(workId, jobType))
    }

    // TODO add a prop to reload work summary?
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(WorkList)
