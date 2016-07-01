import { submitNewWork } from '../actions'
import { connect } from 'react-redux'
import WorkForm from '../components/WorkForm'

const mapStateToProps = (state) => {
  const user = state.configs.user
  return {
    creating: state.workConfig.creating,
    created: state.workConfig.created,
    error: state.workConfig.error,
    srcRepoPlugins: state.configs.srcRepoPlugins,
    zanataUsername: user ? user.username : undefined,
    zanataSecret: user ? user.apiKey : undefined
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    onSaveNewWork: (workForm) => dispatch(submitNewWork(workForm))
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(WorkForm)
