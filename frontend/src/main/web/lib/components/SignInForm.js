import React from 'react'
import Select from './form/Select'

// represents user has not yet selected a zanata server
const NO_SELECTION_OPT = '...'

export default React.createClass(
  {
    propTypes:{
      zanataUser: React.PropTypes.object,
      zanataServer: React.PropTypes.string,
      onSignIn: React.PropTypes.func.isRequired,
      zanataServerUrls: React.PropTypes.arrayOf(React.PropTypes.string).isRequired,
      zanataOAuthUrl: React.PropTypes.string
    },

    getInitialState() {
      return {
        zanataUrl: ''
      }
    },

    _signInWithZanata(e) {
      // this.context.router.push({
      //   pathname: path,
      //   query: { modal: true }
      // })
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

    render() {
      const user = this.props.zanataUser
      const server = this.props.zanataServer
      if (user && server) {
        return <div>Welcome {user.name} from {user.username}@{server}</div>
      }

      const zanataServerUrls = [NO_SELECTION_OPT, ...this.props.zanataServerUrls];

      return (
        <div>
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
