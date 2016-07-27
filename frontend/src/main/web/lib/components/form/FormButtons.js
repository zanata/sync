import React, {PropTypes} from 'react'

const FormButtons = (props) => {
  const {onSave, saving, saveBtnText, cancelBtnText, onCancel} = props

  const saveText = saving ? 'Saving...' : (saveBtnText || 'Save')

  const cancelBtn = onCancel && (
    <button type="button" className="btn btn-default"
      onClick={onCancel}>{cancelBtnText || 'Cancel'}</button>
    )
  return (
    <div className="form-group">
      <div className='col-md-3'></div>
      <div className="col-md-7 ">
        <button type="button" className="btn btn-primary"
          onClick={onSave} disabled={saving}>
          {saveText}
        </button>
        {cancelBtn}
      </div>
    </div>
  )
}

FormButtons.propTypes = {
  onSave: PropTypes.func.isRequired,
  saving: PropTypes.bool.isRequired,
  saveBtnText: PropTypes.string,
  cancelBtnText: PropTypes.string,
  onCancel: PropTypes.func
}

export default FormButtons
