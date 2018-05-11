$(function(){

    $('.sidebar-menu li.active').removeClass('active');

    
    $('#history').addClass('active');
    var login_history_row_template = Handlebars.compile($("#login-history-row-template").html());
    var trade_history_row_template = Handlebars.compile($("#trade-history-row-template").html());
    var dw_history_row_template = Handlebars.compile($("#deposit-withdraw-history-row-template").html());
    API.login_history().success(function(data){
            var i;
            for (i = 0; i < data.length; i++){
                data[i].created = moment(Number(data[i].created)).format("YYYY-MM-DD HH:mm:ss");
            }
             $('#login-history').html(login_history_row_template(data));    
        $(document).ready(function() {
                $('#login_history_table').dataTable( {
                    searching: false,
                    bInfo:false,
                    bLengthChange: false,
                });
            });
     });
API.trade_history().success(function(data){
            var i;
            for (i = 0; i < data.length; i++){
                data[i].value = addThousandsSeparator(zerosTrim(Number(data[i].amount) * Number(data[i].price)));
                data[i].amount = addThousandsSeparator(zerosTrim(data[i].amount));
                data[i].price = addThousandsSeparator(zerosTrim(data[i].price));
                data[i].created = moment(Number(data[i].created)).format("YYYY-MM-DD HH:mm:ss");
                data[i].fee = zerosTrim(data[i].fee);
                if (data[i].typ == "ask") {
                    data[i].fee_currency = data[i].counter;
                    data[i].order_type = Messages("java.api.messages.trade.sell");
                    data[i].klass = "account_histor_table_td";
                } else {
                    data[i].fee_currency = data[i].base;
                    data[i].order_type = Messages("java.api.messages.trade.buy");
                    data[i].klass = "success";
                }
            }
             $('#trade-history').html(trade_history_row_template(data));

      
       $(document).ready(function() {
                $('#trade_history_table').dataTable( {
                    searching: false,
                    bInfo:false,
                    bLengthChange: false,
                });
            });
     });




API.deposit_withdraw_history().success(function(data){
           var i;
           for (i = 0; i < data.length; i++){
               data[i].created = moment(Number(data[i].created)).format("YYYY-MM-DD HH:mm:ss");
               data[i].amount = addThousandsSeparator(zerosToSpaces(data[i].amount));
               data[i].fee = addThousandsSeparator(zerosToSpaces(data[i].fee));
               if (data[i].typ == 'd') {
                   data[i].typ = Messages("java.api.messages.depositwithdraw.deposit");
                   data[i].klass = "success";
               } else {
                   data[i].typ = Messages("java.api.messages.depositwithdraw.withdrawal");
                   data[i].klass = "account_histor_table_td";
               }
           }
            $('#deposit-withdraw-history').html(dw_history_row_template(data));
 
       $(document).ready(function() {
                $('#dw_history_table').dataTable( {
                    searching: false,
                    bInfo:false,
                    bLengthChange: false,
                });
            });
       $('#dw_history_table').removeClass('dataTable');

     });





    $("#tradeReport").click(function(e){
        var startDate=$("#startDate").val();
        var endDate=$("#endDate").val();
        var selectedCoin = $(".select_coin option:selected").val();
        var selectedTransaction = $(".select_transaction_trade option:selected").val();
        if(selectedTransaction=='Trade Type')
        selectedTransaction="Both"
          API.tradeReport(startDate,endDate,selectedCoin,selectedTransaction).success(function(data){
                alert('Trade report send to your email'); 
           });

        });

        $("#walletReport").click(function(e){
        var startDate=$("#walletStartDate").val();
        var endDate=$("#walletEndDate").val();
        var selectedTransaction = $(".select_transaction option:selected").val();
        var selectedCoinWallet = $(".select_coin_wallet option:selected").val();
        if(selectedTransaction=='Transaction Type')
        selectedTransaction="Both"

        ///alert("start date-"+startDate+"--end date-"+endDate+"---selectedCoin--"+selectedCoin)
          API.walletReport(startDate,endDate,selectedTransaction,selectedCoinWallet).success(function(data){
            alert('Wallet transaction report send to your email');
           });

        });
});
