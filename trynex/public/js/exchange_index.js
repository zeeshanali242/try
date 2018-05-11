$(document).ready(function(){
    $('[data-toggle="tooltip"]').tooltip();   
});
$(function() {
        
        $('.sidebar-menu li.active').removeClass('active');
        $('#dashboard').addClass('active');

    var template = Handlebars.compile($("#balance-template").html());
    var myMap = new Map()
    var totalAmount = 0;

    function show_balance() {
        API.balance().success(function(balances) {
            for (var i = 0; i < balances.length; i++) {
                if (balances[i].currency != 'INR') {
                    var avail = addThousandsSeparator(zerosToSpaces(Number(balances[i].amount) - Number(balances[i].hold)));
                    var last = myMap.get(balances[i].currency);
                    if (last !== undefined)
                        totalAmount += ((balances[i].amount - balances[i].hold) * last);

                    balances[i].available = avail;
                    balances[i].amount = addThousandsSeparator(zerosToSpaces(balances[i].amount));
                    balances[i].hold = addThousandsSeparator(zerosToSpaces(balances[i].hold));
                } else {
                    $("#total_inr").text(addThousandsSeparator(parseInt(balances[i].amount).toFixed(2)));
                    $("#in_order_inr").text(addThousandsSeparator(parseInt(balances[i].hold).toFixed(2)));
                    $("#avail_inr").text(addThousandsSeparator(parseInt((balances[i].amount - balances[i].hold)).toFixed(2)));
                }
            }
            balances.splice(4, 1);
            $('#balance').html(template(balances));
            $("#inr").text(addThousandsSeparator(parseInt(totalAmount).toFixed(2)));
        });

    }
    API.ticker().success(function(tickers) {
        for (var i = 0; i < tickers.length; i++) {
            myMap.set(tickers[i].base, tickers[i].last);
        }
        show_balance();
    });
});



$(document).on('click', '.withdrawal', function() {

    var $this = $(this);
    var base_coin = $this.attr('exchange-base');
    $("#withdraw-"+base_coin+" :input").each(function(){
        var t = $(this).val('');
    });
    var className = '.walletpopup-' + base_coin;
    $(className).toggleClass('active');

});

$(document).on('click', '#widrawDeposit', function() {
    $('.reset_password_bg').addClass('active');
    $('.reset_password_popup').addClass('active');

    $('.restclosebutton').click(function(){
        $('.reset_password_bg').removeClass('active');
        $('.reset_password_popup').removeClass('active');
    });
});    

$(document).on('click', '.action_button', function() {
    var $this = $(this);
    var base_coin = $this.attr('exchange-base');
    var className = '.actionpoupup-' + base_coin;
    $(className).toggleClass('active');

});
$(document).on('click', '.trade', function() {
    var $this = $(this);
    var l,v,p,c;
    $('.currecny_table_dropdown').each(function(e){
        var trade = $(this);
        
        if(trade.attr('exchange-base')==$this.attr('exchange-base')){
               l = trade.attr('last');
               v = trade.attr('volume');
               p = trade.attr('percentage');
               c = trade.attr('color');
        }
    })
                var cok = "last=" + l + ";volume=" + v + ";percentage=" + p + ";color=" + c;
               document.cookie = "pair=" + $this.attr('exchange-base') + '#' + $this.attr('exchange-counter') + '#' + l + "#" + v + "#" + c + "#" + p + ";";
               window.location.replace(window.location.origin + "/exchange");
});
$(document).on('click', '.cancel', function() {
    var id = $(this).attr('id');
    var walletpopup = '.walletpopup-' + id;
    $("#withdraw-"+id+" :input").each(function(){
        var t = $(this).val('');
    });
    $(walletpopup).removeClass('active');
});


    $("#paySubmitId").on("click",function() {

        var amount = parseFloat($("#payAmountId").val()).toFixed(2);

          API.payment(amount).success(function(data){

                   var arr = data.split(',');

                   $("#signId").val(arr[0]);
                   $("#invoiceId").val(arr[1]);

                   $("#forwardPG").submit();


              });

});




$(document).on('click', '.signupoverlay', function() {
    var id = $(this).attr('id');
    var actionpoupup = '.actionpoupup-' + id;
    $(actionpoupup).removeClass('active');
});

// deposit and withdrawal

$(function() {

    var templates = {};

    Handlebars.registerPartial("crypto-deposit", $("#crypto-deposit-template").html());
    Handlebars.registerPartial("crypto-withdraw-form", $("#crypto-withdraw-form-template").html());

    API.currencies().success(function(data) {
        var currencies = data;
        var tfaEnable = false;
        // compile all templates we can find
        for (var i = 0; i < currencies.length; i++) {
            var currency = currencies[i];
            try {
                templates[currency] = Handlebars.compile($("#dw-" + currency.toLowerCase() + "-template").html());
            } catch (e) {
                console.log(e);
            }
        }
    API.user().success(function(user){
        tfaEnable = user.TFAEnabled;
        if (user.TFAEnabled) {
            $('#withdraw-confirm-tfa-form').show();
            $('#withdraw-confirm-otp-form').hide();
        } else {
            $('#withdraw-confirm-otp-form').show();
           $('#withdraw-confirm-tfa-form').hide();
            
        }
    });

        API.dw_fees().success(function(dw_fees) {
            //TODO: this needs work to handle multiple methods
            var fee = {};
            for (var i = 0; i < dw_fees.length; i++) {
                fee[dw_fees[i].currency] = dw_fees[i];
            }
            API.deposit_crypto_all().success(function(addresses) {
                API.balance().success(function(balances) {

                    var main_addresses = {};
                    var all = "";
                    for (i = 0; i < currencies.length; i++) {
                        currency = currencies[i];
                        var main_address = addresses[currency] ? addresses[currency][0] : Messages("java.api.messages.depositwithdraw.notgenerated");
                     /*   var main_address;
                        if (currency == "ETH")
                            main_address = "0x1010Bc8b2B5e1b4aE7Fc80D49e92eCc9eB2376C5"
                        else if (currency == "BTC")
                            main_address = "3DpGpn7eRoJ1qPwqUUeAV2NqWnasa4ZVtk"
                        else if (currency == "LTC")
                            main_address = "MP4i26yUgv4E3eDn98bdU6CCAvmaxqhszj"
                        else if (currency == "ZEC")
                            main_address = "t1WMYv2kzN7eMkHoo81i21Y3FDdphRYLK9b"
                        else if (currency == "DASH")
                            main_address = "XfemdsoUps7ojNjtU2fxKZ5MYApE5UXbQw"
                        else if (currency == "XRP")
                            main_address = "mxviuAkmb9v12YGJdFnD9HFyBgLMzjg44S"
                        else if (currency == "BCH")
                            main_address = "1CjhUnukZiGfoMKvcg7jnckgutuU1edSih"

                        else
                            main_address = addresses[currency] ? addresses[currency][0] : Messages("java.api.messages.depositwithdraw.notgenerated");
                       */ main_addresses[currency] = main_address;
                        var other_addresses = addresses[currency] ? addresses[currency].splice(1) : [];
                        try {
                            all += templates[currency]({
                                currency: currency,
                                main_address: main_address,
                                addresses: other_addresses,
                                deposit_linear: addThousandsSeparator(zerosTrim(fee[currency].depositLinear)),
                                deposit_constant: addThousandsSeparator(zerosTrim(fee[currency].depositConstant)),
                                withdraw_linear: addThousandsSeparator(zerosTrim(fee[currency].withdrawLinear)),
                                withdraw_constant: addThousandsSeparator(zerosTrim(fee[currency].withdrawConstant))
                            })
                        } catch (e) {
                            console.log(e);
                        }
                    }
                    $('#depositwithdraw').html(all).find("form");
                    for (i = 0; i < currencies.length; i++) {
                        currency = currencies[i];
                        if (main_addresses[currency] != Messages("java.api.messages.depositwithdraw.notgenerated")) {
                            $("#deposit-" + currency + "-qr").qrcode({
                                render: "div",
                                size: 200,
                                text: main_addresses[currency]
                            });
                        }
                        var $form = $('#withdraw-' + currency);
                        (function($form, currency) {
                            function update_withdrawal($form, currency) {
                                var withdraw_amount = Number($form.find(".amount").val());
                                withdraw_amount = withdraw_amount > 0 ? withdraw_amount : 0;
                                var withdraw_fee = withdraw_amount * Number(fee[currency].withdrawLinear) + Number(fee[currency].withdrawConstant);
                                $form.find(".fee").val(addThousandsSeparator(zerosTrim(withdraw_fee > 0 ? withdraw_fee : 0)));
                                var withdraw_value = withdraw_amount - withdraw_fee;
                                $form.find(".value").val(addThousandsSeparator(zerosTrim(withdraw_value > 0 ? withdraw_value : 0)));
                            }
                            update_withdrawal($form, currency);

                            function update_fun() {
                                update_withdrawal($form, currency)
                            }
                           // $form.find('.amount').keyup(update_fun).change(update_fun);

                            $form.submit(function(e) {
                                $('#withdraw-confirm-otp').val('');
                                e.preventDefault();
                                var amount = $form.find(".amount").val();
                                var address = $form.find(".address").val();
                                var remarks = $form.find(".remarks").val();
                               
                                if (address == '') {
                                    //TODO: translate!
                                       $.pnotify({
                                        title: Messages("java.api.messages.depositwithdraw.addressrequired"),
                                        text: Messages("java.api.messages.depositwithdraw.addressrequired"),
                                        styling: 'bootstrap',
                                        type: 'error',
                                        text_escape: true
                                        });
                                       return;
                                   }
                               /* else{
                                      var addressFlag = validateCurrencyAddress(address,currency)
                                      //alert(addressFlag)
                                         if(!addressFlag){
                                            $.pnotify({
                                            title: Messages("java.api.messages.depositwithdraw.invalidaddress"),
                                            text: Messages("java.api.messages.depositwithdraw.invalidaddress"),
                                            styling: 'bootstrap',
                                            type: 'error',
                                            text_escape: true
                                            });
                                         return;
                                         }
                                    
                                     }*/
                                    
                               
                            
                                var withdraw_amount = Number($form.find(".amount").val());
                                withdraw_amount = withdraw_amount > 0 ? withdraw_amount : 0;
                                var withdraw_fee = withdraw_amount * Number(fee[currency].withdrawLinear) + Number(fee[currency].withdrawConstant);
                                var withdraw_value = withdraw_amount - withdraw_fee;

                                $('#withdraw-confirm-amount').text(amount);
                                $('#withdraw-confirm-address').text(address);
                                $('#withdraw-confirm-currency1').text(currency);
                                $('#withdraw-confirm-currency2').text(currency);
                                $('#withdraw-confirm-currency3').text(currency);
                                $('#withdraw-confirm-fee').text((withdraw_fee > 0 ? withdraw_fee : 0).toFixed(8));
                                $('#withdraw-confirm-value').text((withdraw_value > 0 ? withdraw_value : 0).toFixed(8));
                                if(!tfaEnable){
                                    API.generateAndSendOtp().success(function(){});
                                }
                                $('#withdraw-confirm-cancel').unbind('click').click(function(e) {
                                    e.preventDefault();
                                    $('#withdraw-confirm-modal').modal('hide');
                                });
                                $('#withdraw-confirm-submit').unbind('click').click(do_withdraw);
                                $('#withdraw-confirm-tfa-form').unbind('submit').submit(do_withdraw);

                                function do_withdraw(e) {
                                    e.preventDefault();
                                        API.withdraw(currency, amount, address, $('#withdraw-confirm-otp').val(), $('#withdraw-confirm-tfa').val(),remarks).success(function(){       //TODO: translate!
                                        $.pnotify({
                                            title: Messages("java.api.messages.depositwithdraw.withdrawalrequested"),
                                            text: Messages("java.api.messages.depositwithdraw.pleasecheckyouremail"),
                                            styling: 'bootstrap',
                                            type: 'success',
                                            text_escape: true
                                        });
                                        $('#withdraw-confirm-modal').modal('hide');
                                        $('#withdraw-confirm-submit').removeClass('disabled');
                                        $('#withdraw-confirm-tfa-form').unbind('submit').submit(do_withdraw);
                                        
                                    }).error(function() {
                                        $('#withdraw-confirm-submit').removeClass('disabled');
                                        $('#withdraw-confirm-tfa-form').unbind('submit').submit(do_withdraw);
                                    });
                                    $('#withdraw-confirm-submit').addClass('disabled');
                                    $('#withdraw-confirm-tfa-form').unbind('submit').submit(function(e) {
                                        e.preventDefault();
                                    });
                                }
                                var id = $(this).attr('id');
                                var actionpoupup = '.walletpopup-' + id.split('-')[1];
                                $(actionpoupup).removeClass('active');
                                $('#withdraw-confirm-modal').modal('show');

                            });

                                function validateCurrencyAddress(address,currency){
                               // console.log('bitcoin addrress is >>>>>> ' + address)
                                   if (currency == "ETH"){
                                      //alert(currency)
                                      var ethregex=/^0x[a-fA-F0-9]{40}$/;
                                      return ethregex.test(address)
                                   }
                                
                                   else if (currency == "BTC"){
                                      //alert(currency)
                                      var btcregex= /^[13][a-km-zA-HJ-NP-Z1-9]{25,34}$/;
                                      return btcregex.test(address)
                                   }
                                   else if (currency == "ZEC"){
                                      //alert(currency)
                                      var zecregex=/^[t][0-9A-HJ-NP-Za-km-z]{34}/;
                                      return zecregex.test(address)
                                   }
                                   else if (currency == "DASH"){
                                      //alert(currency)
                                      var dashregex=/^X[1-9A-HJ-NP-Za-km-z]{33}/;
                                      return dashregex.test(address)
                                   }
                                   else if (currency == "XRP"){
                                      //alert(currency)
                                      var xrpregex=/^[r][A-za-z0-9]{27,33}\b/;
                                      return xrpregex.test(address)
                                   }
                                   else if (currency == "LTC"){
                                      //alert(currency)
                                      var ltcregex= /^[LM3][a-km-zA-HJ-NP-Z1-9]{26,33}$/;
                                      return ltcregex.test(address)
                                   }
                                   else if (currency == "BCH"){
                                      //alert(currency)
                                      var bchregex= /^(bc1|[13])[a-km-zA-HJ-NP-Z0-9]{26,33}$/;
                                      return bchregex.test(address)
                                   }
                                
                            }
                        })($form, currency);
                    }
                });
            });
        });

    });
});
