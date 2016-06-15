import { combineReducers } from 'redux'
import { routerReducer as routing } from 'react-router-redux'
import workConfig from './workConfig'
import security from './security'
import invariant from 'invariant'


// the returned reducer function always return the given zanata info
const zanataReducer = (user, zanata, zanataServerUrls, srcRepoPlugins) => {
  return (state, action) => {
     return {
       url: zanata,
       user,
       zanataServerUrls,
       srcRepoPlugins
     }
  }
}

export function withZanataInfo(user, zanataUrl, zanataServerUrls, srcRepoPlugins) {
  invariant(arguments.length == 4, 'you need to supply user, zanataUrl, zanataServerUrls and srcRepoPlugins as arguments')
  invariant(srcRepoPlugins.length > 0, 'you need to provide at least one source repo plugin (in main-content element as attribute data-src-repo-plugins')
  return combineReducers({
    routing,
    workConfig,
    security,
    zanata: zanataReducer(user, zanataUrl, zanataServerUrls, srcRepoPlugins)
  })
}

export default withZanataInfo

