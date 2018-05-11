/*window.onbeforeunload = function() { 
    window.setTimeout(function () { 
        window.location = 'login';
    }, 0); 
    window.onbeforeunload = null; // necessary to prevent infinite loop, that kills your browser 
}*/


 $("#resend_otp").click(function(e){
	 API.login_resend_otp().success(function(data) {
	 
	 });
});

  $("#otp").on("keypress keyup blur", function(event) {
     $(this).val($(this).val().replace(/[^\d].+/, ""));
     if ((event.which < 48 || event.which > 57) || $(this).val().length>5) {
         event.preventDefault();
     }
     if ( $(this).val().length==6) {
     	$('#otp_error').html('');
        $("input[type=submit]").prop("disabled", false);
    }
    else {
    	$('.alert-danger').html('');
    	$('#otp_error').html('please enter a valid otp');
        $("input[type=submit]").prop("disabled", true);
    }
 });