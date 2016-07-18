import React, {PropTypes} from 'react'
import cx from 'classnames'



export default React.createClass({
  propTypes: {
    id: PropTypes.string.isRequired,
    okBtnText: PropTypes.string,
    cancelBtnText: PropTypes.string,
    title: PropTypes.string.isRequired,
    content: PropTypes.any.isRequired,
    show: PropTypes.bool.isRequired,
    onClose: PropTypes.func.isRequired,
    onConfirm: PropTypes.func.isRequired
  },

  render() {
    const {title, content, onClose, id, onConfirm, show} = this.props

    const classes = cx('modal', 'fade', {
      'in': show
    })
    const style = {
      display: show ? 'block': 'none'
    }

    const onClockCallback = (e) => onClose(id)
    const onConfirmCallback = (e) => onConfirm(id)
    const okBtnText = this.props.okBtnText || 'OK'
    const cancelBtnText = this.props.cancelBtnText || 'Cancel'
    return (
      <div id={id} className={classes} tabIndex="-1"
        role="dialog" aria-label={title} style={style}>
        <div className="modal-dialog" role="document">
          <div className="modal-content">
            <div className="modal-header">
              <button type="button" className="close" aria-label="Close"
                onClick={onClockCallback}>
                <span aria-hidden="true">&times;</span>
              </button>
              <h4 className="modal-title">{title}</h4>
            </div>
            <div className="modal-body">
              {content}
            </div>
            <div className="modal-footer">
              <button type="button" className="btn btn-default"
                onClick={onClockCallback}>{cancelBtnText}</button>
              <button type="button" className="btn btn-primary"
                onClick={onConfirmCallback}>{okBtnText}</button>
            </div>
          </div>
        </div>
      </div>
    )
  }
})
