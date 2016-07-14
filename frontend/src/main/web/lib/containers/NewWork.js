import { submitNewWork } from '../actions'
import { connect } from 'react-redux'
import WorkForm from '../components/WorkForm'


const mapStateToProps = (state) => {
  const user = state.configs.user
  return {
    saving: state.workConfig.saving,
    saveFailed: state.workConfig.saveFailed,
    srcRepoPlugins: state.configs.srcRepoPlugins,
    cronOptions: state.configs.cronOptions,
    zanataUser: user
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    onSaveNewWork: (workForm) => dispatch(submitNewWork(workForm))
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(WorkForm)
