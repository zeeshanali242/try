

$(function(){
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
    $('#signup-form').submit(function(e) {
        if ($('#password').val() != $('#confirm_password').val()) {
            e.preventDefault();
            $('#confirm_password').addClass("has-error");
            $('#confpassword_error').show();
        }
    });


    $('.c_block a').click(function(e){
        // alert("sdadas");
        

         $('#defualtactive').css('display','none');
         $('.signup_blocks').find('.active').removeClass('active');
         $('.check').css('display','none');
         $(this).parent().find('.check').css('display', 'block');
         $(this).closest('div').addClass('active');
    });


    function formValidation(){
        var mobile_number = $('#mobile_number').val();
        var password = $('#password').val();
        var confirm_password = $('#confirm_password').val();
        var email = $('#email').val();
        var first_name = $('#first_name').val();
        var last_name = $('#last_name').val();
        var flag = true;
        if(first_name==''){
          $('#first_error').css('display','block');
         flag = false;
        }
        else{
            $('#first_error').css('display','none');
        }
        if(last_name==''){
          $('#last_error').css('display','block');
          flag = false;
        }
        else{
            $('#last_error').css('display','none');
        }
        if(mobile_number.length!=10){
          $('#mobile_error').css('display','block');
          flag = false;
          }
        else{
            $('#mobile_error').css('display','none');
        }
        

        if(password==''){
          $('#password_error').css('display','block');
           $('#password_error').html("Please enter the password");
            flag = false;
        }
        else{
          if(confirm_password !=password){
              $('#password_error').css('display','block');
              $('#password_error').html("Password doesn't match"); 
              flag = false;
            }
            else{
            $('#password_error').css('display','none'); 
          } 
        }

        if(email==''){
            $('#email_error').css('display','block');
          flag = false;
        }
        else{
          $('#email_error').css('display','none');
        }
        return flag;
    }

    $("#send_otp").click(function(e){
        e.preventDefault();
         var mobile_number = $('#mobile_number').val();
         var email = $('#email').val();
        var flag = formValidation();
        if(flag){
              API.signup_otp(mobile_number,email).success(function(data){
                    if(data=='false'){
                      $('.error_log').css('display',"block");
                    }
                    else{
                      $('.resend_otp').css('display','block');
                      $('.send_otp').css('display','none');
                      $('.error_log').css('display',"none");
                      $('.userexist').html("");

                    }
              });
        }
        $(this).unbind("#send_otp");
    });    

    $("#resend_otp").click(function(e){
         e.preventDefault();
         var mobile_number = $('#mobile_number').val();
         var email = $('#email').val();
        var flag = formValidation();
        if(flag){
          API.resend_otp(mobile_number,email).success(function(data){
          });

        }
    });    


});


