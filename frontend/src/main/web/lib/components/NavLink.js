import React from 'react'
import {Link} from 'react-router'
import cx from 'classnames'

// copied from react-router/Link.js
function createLocationDescriptor(to, _ref) {
  var query = _ref.query;
  var hash = _ref.hash;
  var state = _ref.state;

  if (query || hash || state) {
    return { pathname: to, query: query, hash: hash, state: state };
  }

  return to;
}

export default React.createClass(
  {
    // ask for `router` from context
    contextTypes: {
      router: React.PropTypes.object
    },
    render() {
      const {to, query, hash, state, onlyActiveOnIndex} = this.props
      const router = this.context.router
      const location = createLocationDescriptor(to, { query: query, hash: hash, state: state })

      const isActive = router.isActive(location, onlyActiveOnIndex)
      const liClass = cx({
        active: isActive
      })
      return (
        <li className={liClass}>
          <Link {...this.props} activeClassName='active' />
        </li>
      )
    }
  }
)
