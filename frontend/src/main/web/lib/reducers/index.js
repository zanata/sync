import { combineReducers } from 'redux'
import { routerReducer as routing } from 'react-router-redux'
import workConfig from './workConfig'
import security from './security'


// the returned reducer function always return the given zanata url and user info
const zanataReducer = (user, zanata) => {
  return (state, action) => {
     return {
       url: zanata,
       user
     }
  }
}

export function withZanataInfo(user, zanataUrl) {
  return combineReducers({
    routing,
    workConfig,
    security,
    zanata: zanataReducer(user, zanataUrl)
  })
}

export default withZanataInfo

