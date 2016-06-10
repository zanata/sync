import React from 'react'
import NavLink from './NavLink'

export default React.createClass(
  {
    render() {
      return (
        <nav className="navbar navbar-default navbar-pf" role="navigation">
          <div className="collapse navbar-collapse navbar-collapse-1">
            <ul className="nav navbar-nav navbar-primary">
              <NavLink to="/" onlyActiveOnIndex={true}>Home</NavLink>
              <NavLink to="/work/new">Create new work</NavLink>
              <NavLink to="/works">View your works</NavLink>
            </ul>
          </div>
        </nav>
      )
    }
  }
)
