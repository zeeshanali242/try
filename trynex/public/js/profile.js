$(function() {
         
           $('#login-form-link').click(function(e) {
           $("#login-form").delay(100).fadeIn(100);
           $("#register-form").fadeOut(100);
           $('#register-form-link').removeClass('active');
           $(this).addClass('active');
           e.preventDefault();
         });
         $('#register-form-link').click(function(e) {
           $("#register-form").delay(100).fadeIn(100);
           $("#login-form").fadeOut(100);
           $('#login-form-link').removeClass('active');
           $(this).addClass('active');
           e.preventDefault();
         });
         
         });
         
         $(document).ready(function() {
           $('#login-form').formValidation({
               framework: 'bootstrap',
               icon: {
                   valid: 'glyphicon glyphicon-ok',
                   invalid: 'glyphicon glyphicon-remove',
                   validating: 'glyphicon glyphicon-refresh'
               },
               fields: {
                 agree: {
                       validators: {
                           notEmpty: {
                               message: 'You must agree with the terms and conditions'
                           }
                       }
                   }
               }
           });
         });
         $(document).ready(function () {
         $('#btnUpload').click(onOpenChange);
         });
         
         function onOpenChange() {
           var filePath = document.getElementById("fileUpload").value;
           alert(filePath);
           var startIndex = filePath.indexOf('\\') >= 0 ? filePath.lastIndexOf('\\') : filePath.lastIndexOf('/');
           var filename = filePath.substring(startIndex);
           if(filename.indexOf('\\') === 0 || filename.indexOf('/') === 0) {
               filename = filename.substring(1);
           }
           alert('path is '+filename);
           $.ajax({
               url: filename,
               success: onOpenLoad
           });
           }
           function onOpenLoad(fileContent) {
           var data = JSON.parse(fileContent);
           alert('data is '+data);
           // do something with the data
         }
