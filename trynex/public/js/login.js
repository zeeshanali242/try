$(window).load(function(){
function mobileView(){  
    var is_mobile = false;

    if( $('.mobileButton').css('display')=='block') {
        is_mobile = true;       
    }

    // now i can use is_mobile to run javascript conditionally

    if (is_mobile == true) {
        $('.mobileButton').css('display','none');
    }
    else{
      
    }
    }
    mobileView();
  if ($('body').is('.sidebar-collapse'))
    {
      $('body').removeClass('sidebar-collapse');
    }

 });

$(function(){
    $('form input[name=username]' ).focus()
    $('.alert-danger').fadeOut(5000);
});


$('#reset').click(function(){
            $('.forget_password_bg').addClass('active');
            $('.forget_password_popup').addClass('active');
        });
        $('.forget_password_bg').click(function(){
            $('.forget_password_bg').removeClass('active');
            $('.forget_password_popup').removeClass('active');
        });
        $('.closepopupbutton').click(function(){
            $('.forget_password_bg').removeClass('active');
            $('.forget_password_popup').removeClass('active');
        });




/*$('#reset_email').click(function(){
    var email = $("input[name=email]").val();     
    var pattern = /^\b[A-Z0-9._%-]+@[A-Z0-9.-]+\.[A-Z]{2,4}\b$/i

var emailReg = /^([\w-\.]+@([\w-]+\.)+[\w-]{2,4})?$/;
            if( !emailReg.test( email ) || email=='') {
                alert('Please enter valid email');
                } else {
                alert(email);


                }
       

});*/


$("#forgot_submit").click(function(){
var email = $('#forgot_email').val();

if(email==''){
    alert('Please enter email')
}
else{
   
    API.reset_email(email).success(function(data){

          $('.forget_password_bg').removeClass('active');
            $('.forget_password_popup').removeClass('active');
        if(data.success){
            $('#check_email').html(data.success);
        }
        if(data.error){
            $('#check_email').html(data.error);   
        }
            $('#check_email').fadeOut(7000);
});

}
});
