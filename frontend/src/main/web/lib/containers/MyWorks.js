import React from 'react'
import { connect } from 'react-redux'
import WorkList from '../components/WorkList'
import { loadWorkSummaries, runJob, getLatestJobStatus } from '../actions'

const mapStateToProps = (state) => {
  const username = state.zanata.user ? state.zanata.user.username : undefined
  const {workSummaries, error, loading, runningJobs} = state.myWorks
  return {
    zanataUsername: username,
    workSummaries,
    error,
    loading,
    runningJobs
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    loadWorkSummaries: (username) => {
      dispatch(loadWorkSummaries(username))
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
