import { combineReducers } from 'redux'
import { routerReducer as routing } from 'react-router-redux'
import invariant from 'invariant'
import workConfig from './workConfig'
import security from './security'
import myWorks from './myWorks'
import accounts from './accounts'
import general from './general'
import {reducer as toastrReducer} from 'react-redux-toastr'

export function createReducersWithConfigs(configs) {
  const {zanataOAuthUrls, srcRepoPlugins, pollInterval, maxPollTimeout} = configs
  invariant(pollInterval, 'pollInterval must be greater than 0 (in milliseconds)')
  invariant(maxPollTimeout, 'maxPollTimeout must be defined (in milliseconds and greater than pollInterval)')
  invariant(zanataOAuthUrls && Object.keys(zanataOAuthUrls).length, 'you need to supply zanataOAuthUrls')
  invariant(srcRepoPlugins && srcRepoPlugins.length > 0, 'you need to provide at least one source repo plugin (in main-content element as attribute data-src-repo-plugins')

  return combineReducers({
    routing,
    workConfig,
    security,
    configs: () => configs,
    myWorks: myWorks(configs.maxPollCount),
    toastr: toastrReducer,
    accounts,
    general
  })
}

export default createReducersWithConfigs

