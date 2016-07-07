import { submitNewWork, closeNotification } from '../actions'
import { connect } from 'react-redux'
import WorkForm from '../components/WorkForm'
import { API_DONE, API_IN_PROGRESS} from '../constants/commonStateKeys'


const mapStateToProps = (state) => {
  const user = state.configs.user
  return {
    saving: state.workConfig[API_IN_PROGRESS],
    notification: state.global.notification,
    srcRepoPlugins: state.configs.srcRepoPlugins,
    zanataUser: user
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    onSaveNewWork: (workForm) => dispatch(submitNewWork(workForm)),
    dismissNotification: () => dispatch(closeNotification())
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(WorkForm)
