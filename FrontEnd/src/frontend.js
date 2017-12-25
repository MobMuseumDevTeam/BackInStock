const STATUS_DIV_ID = "#stock-notification-status"
const NOTIFICATION_FORM_SKU_ID = "#stock-notification-sku";
const INPUT_SKU_ID = "#product-selector";
const PRODUCT_TYPE = "cell phone";

function onSubmit(form){
  // Copy required data into hidden form fields
  $(NOTIFICATION_FORM_SKU_ID).val($(INPUT_SKU_ID).val());
  var json = getFormDataAsJSON(form);
  if(isValidEmail(json['email'])) {
    $(STATUS_DIV_ID).text("Submitting...");
    console.log(json);
    submitNotification(json).then(function(response) {
      if(response['saved']) {
        $(STATUS_DIV_ID).html("Your notification has been saved. <a href='#' rel='modal:close'>Close</a>");
      } else {
        $(STATUS_DIV_ID).html(`You have already registered for a notification for this ${PRODUCT_TYPE}.`);
      }
    });
  } else {
    $(STATUS_DIV_ID).text("The email address you entered is invalid");
  }
  return false;
}

function getFormDataAsJSON(form){
  var $form = $(form)
  // Create JSON Object
  var unindexed_array = $form.serializeArray();
  var indexed_array = {};
  $.map(unindexed_array, function(n, i){
      indexed_array[n['name']] = n['value'];
  });
  return indexed_array;
}

function isValidEmail(address)
{
 if (/^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/.test(address)) return true;
 else return false;
}
