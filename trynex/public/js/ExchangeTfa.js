   $(function(){

    var totp_otps_template = Handlebars.compile($("#totp-otps-template").html());
    var info_template = Handlebars.compile($("#acct-info-template").html());
    var totp_secret_template = Handlebars.compile($("#totp-secret-template").html());
    var turnofftfa_template = Handlebars.compile($("#turn-off-tfa-template").html());
    function reload(){    
        API.user().success(function(user){
                        var verificationText = "";
                        $('#acct-info').html(info_template(user));     
            $('#turnon-tfa').click(function(e){
                            API.gen_totp_secret().success(function(data){
                                function show_otps() {
                                    $("#tfa-enable-modal .modal-content").html(totp_otps_template(data));
                                    $('#tfa-printing-complete').click(show_totp);
                                    $("#tfa-enable-modal").modal().find('.btn-primary');
                                }

                                function show_totp() {
                                    function enable(e) {
                                        var code = $("#tfa-enable-modal").find('.code').val();
                                        var password = $("#tfa-enable-modal").find('.password').val();
                                        API.turnon_tfa(code, password).success(function(){
                                            $.pnotify({
                                                title: Messages("java.api.messages.account.twofactorauthentication"),
                                                text: Messages("java.api.messages.account.twofactorauthenticationturnedon"),
                                                styling: 'bootstrap',
                                                type: 'success',
                                                text_escape: true
                                            });
                                            $("#tfa-enable-modal").modal('hide');
                                            location.reload();
                                        });
                                        e.preventDefault();
                                    }
                                    $("#tfa-enable-modal .modal-content").html(totp_secret_template(data)).find('form').submit(enable);
                                    $("#tfa-enable-qr").qrcode({render: "div", size: 200, text: data.otpauth});
                                    $('#tfa-printing-incomplete').click(show_otps);
                                    $("#tfa-enable-modal").modal().find('.btn-primary').off("click").click(enable);

                                }
                                show_otps();
                            });
                            e.preventDefault();

            });
            $('#turnoff-tfa').click(function(e) {
                            function disable(e) {
                                var code = $("#tfa-disable-modal").find('.code').val();
                                var password = $("#tfa-disable-modal").find('.password').val();
                                API.turnoff_tfa(code, password).success(function(){
                                    $.pnotify({
                                        title: Messages("java.api.messages.account.twofactorauthentication"),
                                        text: Messages("java.api.messages.account.twofactorauthenticationturnedoff"),
                                        styling: 'bootstrap',
                                        type: 'success',
                                        text_escape: true
                                    });
                                    reload();
                                    $("#tfa-disable-modal").modal('hide');
                                });
                                e.preventDefault();
                            }
                            $("#tfa-disable-modal .modal-body").html(turnofftfa_template()).find('form').submit(disable);
                            $("#tfa-disable-modal").modal().find('.btn-primary').off("click").click(disable);
                            e.preventDefault();
            });
        });
    }
    reload();
 });