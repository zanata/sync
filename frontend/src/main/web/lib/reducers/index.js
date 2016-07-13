import { combineReducers } from 'redux'
import { routerReducer as routing } from 'react-router-redux'
import invariant from 'invariant'
import workConfig from './workConfig'
import security from './security'
import myWorks from './myWorks'
import {reducer as toastrReducer} from 'react-redux-toastr'

export function createReducersWithConfigs(configs) {
  const {zanataServerUrls, srcRepoPlugins} = configs
  invariant(zanataServerUrls && zanataServerUrls.length, 'you need to supply zanataServerUrls')
  invariant(srcRepoPlugins && srcRepoPlugins.length > 0, 'you need to provide at least one source repo plugin (in main-content element as attribute data-src-repo-plugins')

  return combineReducers({
    routing,
    workConfig,
    security,
    configs: () => configs,
    myWorks: myWorks,
    toastr: toastrReducer
  })
}

export default createReducersWithConfigs

