import { combineReducers } from 'redux'
import { routerReducer as routing } from 'react-router-redux'
import invariant from 'invariant'
import workConfig from './workConfig'
import security from './security'
import myWorks from './myWorks'

export function createReducersWithConfigs(configs) {
  const {zanataServerUrls, srcRepoPlugins, pollInterval, maxPollTimeout} = configs
  invariant(zanataServerUrls && zanataServerUrls.length, 'you need to supply zanataServerUrls')
  invariant(pollInterval, 'pollInterval must be greater than 0 (in milliseconds)')
  invariant(maxPollTimeout, 'maxPollTimeout must be defined (in milliseconds and greater than pollInterval)')
  invariant(srcRepoPlugins && srcRepoPlugins.length > 0, 'you need to provide at least one source repo plugin (in main-content element as attribute data-src-repo-plugins')

  return combineReducers({
    routing,
    workConfig,
    security,
    configs: () => configs,
    myWorks: myWorks(configs.maxPollCount)
  })
}

export default createReducersWithConfigs

