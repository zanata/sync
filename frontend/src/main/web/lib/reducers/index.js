import { combineReducers } from 'redux'
import { routerReducer as routing } from 'react-router-redux'
import invariant from 'invariant'
import workConfig from './workConfig'
import security from './security'
import myWorks from './myWorks'


// the returned reducer function always return the given zanata info
const zanataReducer = (user, zanataServerUrls, srcRepoPlugins) => {
  return (state, action) => {
     return {
       url: (user && user.zanataServer) || '',
       user,
       zanataServerUrls,
       srcRepoPlugins
     }
  }
}

export function withZanataInfo(user, zanataServerUrls, srcRepoPlugins) {
  invariant(arguments.length == 3, 'you need to supply user, zanataServerUrls and srcRepoPlugins as arguments')
  invariant(srcRepoPlugins.length > 0, 'you need to provide at least one source repo plugin (in main-content element as attribute data-src-repo-plugins')

  return combineReducers({
    routing,
    workConfig,
    security,
    myWorks,
    zanata: zanataReducer(user, zanataServerUrls, srcRepoPlugins)
  })
}

export default withZanataInfo

