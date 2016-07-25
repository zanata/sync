import React from 'react'
import { Route, IndexRoute } from 'react-router'

import Home from './containers/Home'
import App from './containers/App'
import NewWork from './containers/NewWork'
import Account from './containers/Account'
import MyWorks from './containers/MyWorks'
import WorkDetail from './containers/WorkDetail'

const routes = (
  <Route path='/' component={App}>
    <IndexRoute component={Home} />
    <Route path='work/new' component={NewWork} />
    <Route path='work/mine' component={MyWorks} />
    <Route path='account' component={Account} />
    <Route path='work/:id' component={WorkDetail} />
  </Route>
)

export default routes

