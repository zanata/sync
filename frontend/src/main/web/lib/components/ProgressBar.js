import React, {PropTypes} from 'react'

const ProgressBar = (props) => {
  if (props.loading) {
    const currentPercent = props.currentPercent || 100
    const style = {
      width: `${currentPercent}%`
    }
    const srTxt = currentPercent == 100 ? 'loading' : `${currentPercent}% complete`
    return (
      <div className="progress">
        <div className="progress-bar progress-bar-striped active"
          role="progressbar" aria-valuenow={currentPercent} aria-valuemin="0"
          aria-valuemax="100" style={style}>
          <span className="sr-only">{srTxt}</span>
        </div>
      </div>
    )
  }
  return null
}

ProgressBar.propTypes = {
  loading: PropTypes.bool.isRequired,
  currentPercent: PropTypes.number
}

export default ProgressBar
