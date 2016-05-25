function enableSection(checkbox) {
  //add is-disabled to all input and select
  var enabled = checkbox.prop('checked');
  var section = checkbox.closest('div');

  section.find('input[type=text]').each(function () {
    enabledInput(this, enabled);
  });

  section.find('select').each(function () {
    enabledInput(this, enabled);
  });
}

function enabledInput(input, enabled) {
  if(enabled === true) {
    jQuery(input).removeClass('is-disabled');
    jQuery(input).removeAttr('disabled');
  } else {
    jQuery(input).addClass('is-disabled');
    jQuery(input).attr('disabled', true);
  }
}

