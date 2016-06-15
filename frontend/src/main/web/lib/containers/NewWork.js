import { submitNewWork } from '../actions'
import { connect } from 'react-redux'
import WorkForm from '../components/WorkForm'

const mapStateToProps = (state) => {
  return {
    creating: state.workConfig.creating,
    created: state.workConfig.created,
    error: state.workConfig.error,
    srcRepoPlugins: state.zanata.srcRepoPlugins,
    loggedIn: !!state.zanata.user
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    onSaveNewWork: (workForm) => dispatch(submitNewWork(workForm))
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(WorkForm)
