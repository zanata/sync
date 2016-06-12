import { combineReducers } from 'redux'
import { routerReducer as routing } from 'react-router-redux'
import workConfig from './workConfig'
import security from './security'
// import common from './common'

// Add the reducer to your store on the `routing` key
const rootReducer = combineReducers({
  routing,
  workConfig,
  security
  // explore,
  // glossary,
  // common
})

export default rootReducer

