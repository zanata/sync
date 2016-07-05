import React, {PropTypes} from 'react'
import Select from './form/Select'
import { isUnauthorized, extractErrorMessage } from '../utils/errorResponse'
import GenericErrorBar from './GenericErrorBar'

// represents user has not yet selected a zanata server
const NO_SELECTION_OPT = '...'

export default React.createClass(
  {
    propTypes:{
      zanataUser: PropTypes.object,
      onSignIn: PropTypes.func.isRequired,
      zanataServerUrls: PropTypes.arrayOf(React.PropTypes.string).isRequired,
      isSessionLoggedIn: PropTypes.func.isRequired,
      zanataOAuthUrl: PropTypes.string
    },

    getInitialState() {
      return {
        zanataUrl: ''
      }
    },

    _signInWithZanata(e) {
      return this.props.onSignIn(this.state.zanataUrl)
    },

    _changeZanataServer(url) {
       this.setState({
         zanataUrl: url
       })
    },

    componentWillReceiveProps(nextProps) {
       if (nextProps.zanataOAuthUrl) {
         console.log(nextProps.zanataOAuthUrl)
         window.location = nextProps.zanataOAuthUrl
       }
    },

    componentWillMount() {
      // check whether server session is still logged in
      this.props.isSessionLoggedIn()
    },

    render() {
      const {zanataUser, error} = this.props

      if (!error && zanataUser) {
        return <div>Welcome {zanataUser.name}</div>
      }

      let message = null
      if (error && !isUnauthorized(error)) {
        message = <GenericErrorBar error={error} />
      }

      const zanataServerUrls = [NO_SELECTION_OPT, ...this.props.zanataServerUrls];

      return (
        <div>
          {message}
          <form className="form-horizontal">
            <Select name='zanataServer' label='Zanata Servers'
              onChange={this._changeZanataServer}
              options={zanataServerUrls}
              selected={this.state.zanataUrl}
            />
            <div className="form-group">
              <div className="col-md-4 col-md-offset-3">
                <button type="button" className="btn btn-primary"
                  onClick={this._signInWithZanata} disabled={!this.state.zanataUrl || this.state.zanataUrl === NO_SELECTION_OPT}>
                  Sign in to {this.state.zanataUrl || NO_SELECTION_OPT}
                </button>
              </div>
            </div>
          </form>
        </div>
      )
    }
  }
)
