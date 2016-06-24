import React, {PropTypes} from 'react'
import NavLink from './NavLink'
import cx from 'classnames'

const NavHeader = React.createClass({
  getInitialState() {
    return {
      dropdownOpen: false
    }
  },

  _toggleDropdown(e) {
    e.preventDefault()
    const current = this.state.dropdownOpen
    this.setState({
      dropdownOpen: !current
    })
  },

  render() {
    const {name, zanataServer} = this.props
    if (!name || !zanataServer) {
      return null
    }
    // TODO click outside the dropdown won't close it
    const drowdownClass = cx('dropdown', {
      open: this.state.dropdownOpen
    })
    const ariaExpanded = this.state.dropdownOpen
    const logoutCallback = (e) => {
      e.preventDefault()
      this.props.onLogout()
    }
    return (
      <ul className="nav navbar-nav navbar-utility">
        <li className={drowdownClass}>
          <a href='#' className="dropdown-toggle" data-toggle="dropdown"
            aria-expanded={ariaExpanded}
            onClick={this._toggleDropdown}>
            <span className="pficon pficon-user" />
            {this.props.name}<b className="caret" />
          </a>
          <ul className="dropdown-menu">
            <li>
              <a href={this.props.zanataServer} target="_blank">Go to Zanata</a>
            </li>
            <li className="divider" />
            <li>
              <a href="#" onClick={logoutCallback}>Log out</a>
            </li>
          </ul>
        </li>
      </ul>
    )
  }
})

export default React.createClass(
  {
    propTypes: {
      name: PropTypes.string,
      zanataServer: PropTypes.string,
      onLogout: PropTypes.func.isRequired
    },

    getInitialState() {
      return {
        showNavMenu: false
      }
    },

    _toggleNav() {
      const current = this.state.showNavMenu
      this.setState({
        showNavMenu: !current
      })
    },

    render() {
      const ariaExpanded = this.state.showNavMenu
      const navMenuClass = cx('collapse', 'navbar-collapse', {
        show: this.state.showNavMenu,
        hidden: !this.state.showNavMenu
      })
      return (
        <nav className="navbar navbar-default navbar-pf" role="navigation">
          <div className="navbar-header">
            <button type="button" className="navbar-toggle"
              data-toggle="collapse" onClick={this._toggleNav}>
              <span className="sr-only">Toggle navigation</span>
              <span className="icon-bar"/>
              <span className="icon-bar"/>
              <span className="icon-bar"/>
            </button>
          </div>
          <div className={navMenuClass} aria-expanded={ariaExpanded}>
          <NavHeader {...this.props} />
            <ul className="nav navbar-nav navbar-primary">
              <NavLink to="/" onlyActiveOnIndex={true}>Home</NavLink>
              <NavLink to="/work/new">Create new work</NavLink>
              <NavLink to="/work/mine">View your works</NavLink>
            </ul>
          </div>
        </nav>
      )
    }
  }
)
