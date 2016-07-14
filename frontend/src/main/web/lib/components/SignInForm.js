import React, {PropTypes} from 'react'
import Select from './form/Select'
import {objectToKeysAndValuesArray} from '../utils/general'

// represents user has not yet selected a zanata server
const NO_SELECTION_OPT = '...'

export default React.createClass(
  {
    propTypes:{
      zanataUser: PropTypes.object,
      isSessionLoggedIn: PropTypes.func.isRequired,
      zanataOAuthUrls: PropTypes.object.isRequired,
      serverReturnUnauthorized: PropTypes.bool.isRequired
    },

    getInitialState() {
      return {
        zanataOAuthUrl: ''
      }
    },

    _signInWithZanata(e) {
      window.location = this.state.zanataOAuthUrl
      return false
    },

    _changeZanataServer(url) {
       this.setState({
         zanataOAuthUrl: url
       })
    },

    componentWillMount() {
      // check whether server session is still logged in
      this.props.isSessionLoggedIn()
    },

    render() {
      const {zanataUser, serverReturnUnauthorized} = this.props

      if (zanataUser && !serverReturnUnauthorized) {
        return <div>Welcome {zanataUser.name}</div>
      }

      const zanataOAuthUrls = {[NO_SELECTION_OPT]: NO_SELECTION_OPT, ...this.props.zanataOAuthUrls}
      const {keys, values} = objectToKeysAndValuesArray(zanataOAuthUrls)
      const zanataServerUrls = keys
      const oauthUrls = values

      const selectedIndex = oauthUrls.findIndex(oauth => oauth === this.state.zanataOAuthUrl)
      const chosenZanata = zanataServerUrls[selectedIndex]

      // TODO if zanataServerUrls has only one option, don't use selection
      return (
        <div>
          <form className="form-horizontal">
            <Select name='zanataServer' label='Zanata Servers'
              onChange={this._changeZanataServer}
              options={oauthUrls}
              optionsDesc={zanataServerUrls}
              selected={this.state.zanataOAuthUrl}
            />
            <div className="form-group">
              <div className="col-md-4 col-md-offset-3">
                <button type="button" className="btn btn-primary"
                  onClick={this._signInWithZanata}
                  disabled={!this.state.zanataOAuthUrl || this.state.zanataOAuthUrl === NO_SELECTION_OPT}>
                  Sign in to {chosenZanata || NO_SELECTION_OPT}
                </button>
              </div>
            </div>
          </form>
        </div>
      )
    }
  }
)
