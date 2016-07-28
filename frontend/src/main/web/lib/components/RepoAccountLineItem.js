import React, {PropTypes} from 'react'
import ToggleReveal from './ToggleReveal'
import cx from 'classnames'

const component = (props) => {
  const {repoType, branch, username, secret, url,
    classes, onLineItemClick} = props

  const repoTypeDisplay = (<span className='label label-info'>{repoType}</span>)
  const branchDisplay = branch && (
      <span className="text-muted glyphicon glyphicon-tag" title='Branch' aria-hidden="true">{branch}</span>
    )
  const credentialDisplay = username && secret && (
      <span>{username}:<ToggleReveal text={secret}/>@</span>
    )

  const classNames = cx('list-group-item', classes)

  if (onLineItemClick) {
    return (
      <button className={classNames} onClick={onLineItemClick}>
        {repoTypeDisplay} {credentialDisplay}{url}   {branchDisplay}
      </button>
    )
  } else {
    return (
      <li className={classNames}>
        {repoTypeDisplay} {credentialDisplay}{url}   {branchDisplay}
      </li>
    )
  }

}

component.propTypes = {
  repoType: PropTypes.string.isRequired,
  branch: PropTypes.string,
  url: PropTypes.string.isRequired,
  username: PropTypes.string,
  secret: PropTypes.string,
  onLineItemClick: PropTypes.func,
  classNames: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.arrayOf(PropTypes.string),
    PropTypes.object
  ])
}

export default component
