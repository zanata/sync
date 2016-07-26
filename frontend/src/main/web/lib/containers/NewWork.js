import { submitNewWork, NEW_WORK_REQUEST } from '../actions/index'
import { connect } from 'react-redux'
import WorkForm from '../components/WorkForm'


const mapStateToProps = (state) => {
  const user = state.configs.user
  const zanataAccount = state.accounts.zanataAccount
  return {
    saving: !!state.general.requesting[NEW_WORK_REQUEST],
    srcRepoPlugins: state.configs.srcRepoPlugins,
    cronOptions: state.configs.cronOptions,
    zanataAccount,
    user
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    onSaveNewWork: (workForm) => dispatch(submitNewWork(workForm))
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(WorkForm)
