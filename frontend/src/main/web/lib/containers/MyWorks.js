import React from 'react'
import { connect } from 'react-redux'
import WorkList from '../components/WorkList'
import { loadWorkSummaries, runJob } from '../actions'

const mapStateToProps = (state) => {
  const username = state.zanata.user ? state.zanata.user.username : undefined
  return {
    zanataUsername: username,
    workSummaries: state.myWorks.workSummaries
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    loadWorkSummaries: (username) => {
      dispatch(loadWorkSummaries(username))
    },
    runJob: (workId, jobType) => {
      dispatch(runJob(workId, jobType))
    }
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(WorkList)
