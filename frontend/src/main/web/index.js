import React from 'react';
import { render } from 'react-dom'
import {Router, browserHistory} from 'react-router';
import routes from './lib/routes';
import Configs from './lib/constants/Configs';
import { createStore, applyMiddleware } from 'redux'
import { Provider } from 'react-redux'
import { syncHistoryWithStore } from 'react-router-redux'
import { apiMiddleware } from 'redux-api-middleware'
import createLogger from 'redux-logger';
import rootReducer from './lib/reducers'

// import _ from 'lodash';
// import 'zanata-ui/lib/styles/index.css'
import 'patternfly/dist/css/patternfly.min.css'
import 'patternfly/dist/css/patternfly-additions.min.css'

/**
 * Process attributes in dom element:id='main-content'
 *
 * base-url - base url for rest api
 * user - json object of user information. See {@link org.zanata.rest.editor.dto.User}
 * data - json object of any information to be included. e.g Permission {@link org.zanata.rest.editor.dto.Permission}, and View
 * dev - If 'dev' attribute exist, all api data will be retrieve from .json file in test directory.
 */
const mountNode = document.getElementById('main-content'),
  baseUrl = mountNode.getAttribute('base-url'),
  user = JSON.parse(mountNode.getAttribute('user')),
  data = JSON.parse(mountNode.getAttribute('data')),
  dev = data.dev;

// base rest url, e.g http://localhost:8080/rest
Configs.baseUrl = baseUrl;
Configs.data = data;
//append with .json extension in 'dev' environment
Configs.urlPostfix = dev ? '' : '.json?';
// see org.zanata.rest.editor.dto.User
Configs.user = user;

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

const store = createStore(
  rootReducer,
  applyMiddleware(
    apiMiddleware,
    logger
  )
)

// Create an enhanced history that syncs navigation events with the store
const history = syncHistoryWithStore(browserHistory, store)

render(
  <Provider store={store}>
    <Router routes={routes} history={history} />
  </Provider>, mountNode
)





