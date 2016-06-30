import React from 'react';
import { render } from 'react-dom'
import { Router, useRouterHistory, hashHistory, browserHistory } from 'react-router'
import { createHistory } from 'history'
import Configs from './lib/constants/Configs'
import routes from './lib/routes'
import { createStore, applyMiddleware } from 'redux'
import { Provider } from 'react-redux'
import { syncHistoryWithStore } from 'react-router-redux'
import { apiMiddleware } from 'redux-api-middleware'
import createLogger from 'redux-logger'
import rootReducer from './lib/reducers'

// import _ from 'lodash';
// import 'zanata-ui/lib/styles/index.css'
import 'patternfly/dist/css/patternfly.min.css'
import 'patternfly/dist/css/patternfly-additions.min.css'

/**
 * Process attributes in dom element:id='main-content'
 *
 * base-url - base url for rest api
 * TODO update comment
 * user - json object of user information. See {@link org.zanata.rest.editor.dto.User}
 * data - json object of any information to be included. e.g Permission {@link org.zanata.rest.editor.dto.Permission}, and View
 * dev - If 'dev' attribute exist, all api data will be retrieve from .json file in test directory.
 */
const mountNode = document.getElementById('main-content')
const dataUser = mountNode.getAttribute('data-user')
const apiUrl = mountNode.getAttribute('data-api-url')
const basename = mountNode.getAttribute('data-app-basename')
const zanataServerUrls = JSON.parse(mountNode.getAttribute('data-zanata-server-urls'))
const srcRepoPlugins = JSON.parse(mountNode.getAttribute('data-src-repo-plugins'))
  // user = JSON.parse(mountNode.getAttribute('user')),
const data = JSON.parse(mountNode.getAttribute('data'))
const dev = data.dev;

const user = dataUser ? JSON.parse(dataUser): undefined
// const user = undefined

function secondsToMilli(num) {
  return num * 1000
}

function minutesToMilli(num) {
  return num * 1000 * 60
}

// base rest url, e.g http://localhost:8080/rest
Configs.apiUrl = apiUrl || ''
Configs.basename = basename || ''
Configs.pollInterval = secondsToMilli(2)
Configs.maxPollTimeout = minutesToMilli(5)
Configs.maxPollCount = Configs.maxPollTimeout / Configs.pollInterval
// TODO doe we use these?
Configs.data = data;
//append with .json extension in 'dev' environment
Configs.urlPostfix = dev ? '' : '.json?'
// see org.zanata.rest.editor.dto.User
// Configs.user = user;

console.log('Configs', Configs)

const loggerOption = {
  // level = 'log': 'log' | 'console' | 'warn' | 'error' | 'info', // console's level
  // duration = false: Boolean, // Print the duration of each action?
  // timestamp = true: Boolean, // Print the timestamp with each action?
  // colors: ColorsObject, // Object with color getters. See the ColorsObject interface.
  // logger = console: LoggerObject, // Implementation of the `console` API.
  // logErrors = true: Boolean, // Should the logger catch, log, and re-throw errors?
  // collapsed, // Takes a boolean or optionally a function that receives `getState` function for accessing current store state and `action` object as parameters. Returns `true` if the log group should be collapsed, `false` otherwise.
  // predicate, // If specified this function will be called before each action is processed with this middleware.
  // stateTransformer, // Transform state before print. Eg. convert Immutable object to plain JSON.
  // actionTransformer, // Transform state before print. Eg. convert Immutable object to plain JSON.
  // errorTransformer, // Transform state before print. Eg. convert Immutable object to plain JSON.
  // diff = false: Boolean, // Show diff between states.
  // diffPredicate // Filter function for showing states diff.'
}
const logger = createLogger(loggerOption)

// TODO put everything in Configs and remove the Configs.js file (nowhere should reference it)
const store = createStore(
  rootReducer(user, zanataServerUrls, srcRepoPlugins, Configs),
  applyMiddleware(
    apiMiddleware,
    logger
  )
)

// Run our app under the /base URL.
// TODO this is not working. Will need to hard code full path in routes.js or make this work
const basenameAwareHistory = useRouterHistory(createHistory)({
  basename: `${Configs.basename}`
})
// Create an enhanced history that syncs navigation events with the store
const history = syncHistoryWithStore(hashHistory, store)

render(
  <Provider store={store}>
    <Router routes={routes} history={history} />
  </Provider>, mountNode
)





