var originLink = window.location.origin;





$(window).load(function() {

    if ($('body').is('.sidebar-collapse')) {
        $('body').removeClass('sidebar-collapse');
    }
});

$(document).ready(function() {
   var ws;
function connect() {
    ws = new WebSocket('wss://192.169.236.100:8441/user');
    ws.onmessage = function(data) {
      if (!(event.data === undefined)) {
          //pick_pair($("#currencyId").attr("exchange-base"), $("#currencyId").attr("exchange-counter"), $("#currencyId").attr("percentage"), $("#currencyId").attr("last"), $("#currencyId").attr("volume"), $("#currencyId").attr("color"));
          pairsData($("#currencyId").attr("exchange-base"), $("#currencyId").attr("exchange-counter"));
        console.log('messages rec');
      }
    }
} 
connect();
function disconnect() {
    if (ws != null) {
        ws.close();
    }
    console.log("Websocket is in disconnected state");
}

   


    var pp_template = Handlebars.compile($("#pair-picker-template").html());
    var curData_template = Handlebars.compile($("#currency-data-template").html());
    var exchangeModel = {};

    function pairsData(base, counter) {
        API.pairs_data().success(function(pair_data) {
            var la, vo, per, col;
            var currencyDataRecord;
            for (var i = 0; i < pair_data.length; i++) {

                var lastPrice = (pair_data[i].last === undefined ? 0 : pair_data[i].last);
                var price_24 = (pair_data[i].price === undefined ? 0 : pair_data[i].price);
                var volume = (pair_data[i].sum === undefined ? 0 : pair_data[i].sum);
                var percentage = (((parseFloat(lastPrice) - price_24) / price_24) * 100).toFixed(2);
                pair_data[i].last = lastPrice;
                pair_data[i].price = price_24;
                pair_data[i].sum = volume;
                pair_data[i].percentage = isNaN(percentage) ? 0 : percentage;
                if (pair_data[i].percentage < 0) {
                    pair_data[i].color = "red";
                    pair_data[i].fafa = "down";
                } else {
                    pair_data[i].color = "green";
                    pair_data[i].fafa = "up";
                    pair_data[i].percentage = '+' + pair_data[i].percentage;
                }
                exchangeModel.data = pair_data;
                if (pair_data[i].base == base) {
                    la = pair_data[i].last;
                    vo = pair_data[i].sum;
                    per = pair_data[i].percentage;
                    col = (per >= 0) ? "green" : "red";
                }
            }
            //apiPair(la,vo,per,col);
            $('.currencybutton').html(pp_template(exchangeModel.data));
            $('.currencydata').html(curData_template(currencyDataRecord));
            $('#t1').text(base + '/' + counter);
            $("#currencyId").attr("exchange-base", base);
            $("#currencyId").attr("exchange-counter", counter);
            $("#currencyId").attr("percentage", per);
            $("#currencyId").attr("last", la);
            $("#currencyId").attr("volume", vo);
            $("#currencyId").attr("color", col);
            $('#lastTradePrice').val(addThousandsSeparator(la));
            $("#currencyId").attr("percentage", per);
            $("#currencyId").attr("last", la);
            $("#currencyId").attr("volume", vo);
            pick_pair(base, counter, per, la, vo, col);
        });
    }


    API.pairs_data().success(function(pair_data) {
        var la, vo, per, col;
        var currencyDataRecord;
        for (var i = 0; i < pair_data.length; i++) {

            var lastPrice = (pair_data[i].last === undefined ? 0 : pair_data[i].last);
            var price_24 = (pair_data[i].price === undefined ? 0 : pair_data[i].price);
            var volume = (pair_data[i].sum === undefined ? 0 : pair_data[i].sum);
            var percentage = (((parseFloat(lastPrice) - price_24) / price_24) * 100).toFixed(2);
            pair_data[i].last = lastPrice;
            pair_data[i].price = price_24;
            pair_data[i].sum = volume;
            pair_data[i].percentage = isNaN(percentage) ? 0 : percentage;
            if (pair_data[i].percentage < 0) {
                pair_data[i].color = "red";
                pair_data[i].fafa = "down";
            } else {
                pair_data[i].color = "green";
                pair_data[i].fafa = "up";
                pair_data[i].percentage = '+' + pair_data[i].percentage;
            }
            exchangeModel.data = pair_data;
            if (pair_data[i].base == 'BTC') {
                la = pair_data[i].last;
                vo = pair_data[i].sum;
                per = pair_data[i].percentage;
                col = (per >= 0) ? "green" : "red";
            }
        }
        //apiPair(la,vo,per,col);
        $('.currencybutton').html(pp_template(exchangeModel.data));
        $('.currencydata').html(curData_template(currencyDataRecord));
        $('.currencydrop div').click(function(e) {
             $('.currencydrop').removeClass('active');
            var $this = $(this);
            $('#t1').text($this.attr('exchange-base') + '/' + $this.attr('exchange-counter'));
            $("#currencyId").attr("exchange-base", $this.attr('exchange-base'));
            $("#currencyId").attr("exchange-counter", $this.attr('exchange-counter'));
            $("#currencyId").attr("percentage", $this.attr('percentage'));
            $("#currencyId").attr("last", $this.attr('last'));
            $("#currencyId").attr("volume", $this.attr('volume'));
            $("#currencyId").attr("color", $this.attr('color'));
            pick_pair($this.attr('exchange-base'), $this.attr('exchange-counter'), $this.attr('percentage'), $this.attr('last'), $this.attr('volume'), $this.attr('color'));
            $('#lastTradePrice').val(addThousandsSeparator(la));
        });
        $("#currencyId").attr("percentage", per);
        $("#currencyId").attr("last", la);
        $("#currencyId").attr("volume", vo);
        pick_pair('BTC', 'INR', per, la, vo, col);
    });

    /* setInterval(function() {
        API.get_updated_market_data(Date.now()-2000,Date.now()).success(function(data){
           if(data.update==true){
             pairsData($('#currencyId').attr("exchange-base"),$('#currencyId').attr("exchange-counter"));
              // pick_pair($("#currencyId").attr("exchange-base"), $("#currencyId").attr("exchange-counter") , $("#currencyId").attr("percentage"),$("#currencyId").attr("last"),$("#currencyId").attr("volume"),$("#currencyId").attr("color"));
           }
       });
        API.get_updated_orders_data(Date.now()-2000,Date.now()).success(function(data){
         if(data.update==true){
               pick_pair($("#currencyId").attr("exchange-base"), $("#currencyId").attr("exchange-counter") , $("#currencyId").attr("percentage"),$("#currencyId").attr("last"),$("#currencyId").attr("volume"),$("#currencyId").attr("color"));
           }
       });
    //pick_pair($("#currencyId").attr("exchange-base"), $("#currencyId").attr("exchange-counter") , $("#currencyId").attr("percentage"),$("#currencyId").attr("last"),$("#currencyId").attr("volume"),$("#currencyId").attr("color"));
    }, 2000);*/

    $('.currencybutton').mouseover(function() {

        $('.currencydrop').addClass('active');
    });
    $('.currencybutton').mouseout(function() {
        $('.currencydrop').removeClass('active');
    });

    $('#loginId').click(function(e) {
        $("#loading-div-background").show();
        $("#formId").submit();

    });

});




function pick_pair(base, counter, percentage, last, volume, col) {
    if (!(base == '' || base === undefined || counter == '' || counter === undefined)) {
        var ob_template = Handlebars.compile($("#open-bids-template").html());
        var oa_template = Handlebars.compile($("#open-asks-template").html());
        API.price_24_before(base, counter).success(function(price_24_hours) {
            API.trade_fees().success().success(function(data) {
                var one_way = data.one_way;
                var fee = Number(data.linear);

                API.open_trades(base, counter).success(function(data) {
                    var i;
                    var bids = {};
                    bids.orders = data.bids;
                    bids.base = base;
                    bids.counter = counter;
                    for (i = 0; i < bids.orders.length; i++) {
                        bids.orders[i].value = zerosToSpaces((Number(bids.orders[i].amount) * Number(bids.orders[i].price)));
                        bids.orders[i].amount = zerosToSpaces(bids.orders[i].amount);
                        bids.orders[i].price = addThousandsSeparator(zerosToSpaces(bids.orders[i].price));
                    }
                    bids.total_counter = zerosTrim(data.total_counter);
                    var asks = {};
                    asks.orders = data.asks;
                    asks.base = base;
                    asks.counter = counter;
                    for (i = 0; i < asks.orders.length; i++) {
                        asks.orders[i].value = zerosToSpaces((Number(asks.orders[i].amount) * Number(asks.orders[i].price)));
                        asks.orders[i].amount = zerosToSpaces(asks.orders[i].amount);
                        asks.orders[i].price = addThousandsSeparator(zerosToSpaces(asks.orders[i].price));
                    }
                    asks.total_base = zerosTrim(data.total_base);

                    $('#open-bids').html(ob_template(bids));
                    $('#open-asks').html(oa_template(asks));

                });

                var rt_template = Handlebars.compile($("#recent-trades-template").html());
                API.recent_trades(base, counter).success(function(data) {
                    var i;
                    var trades = {};
                    trades.orders = data;
                    trades.base = base;
                    trades.counter = counter;
                    for (i = 0; i < trades.orders.length; i++) {
                        trades.orders[i].value = zerosToSpaces((Number(trades.orders[i].amount) * Number(trades.orders[i].price)));
                        trades.orders[i].amount = zerosToSpaces(trades.orders[i].amount);
                        trades.orders[i].price = addThousandsSeparator(zerosToSpaces(trades.orders[i].price));
                        trades.orders[i].created = moment(Number(trades.orders[i].created)).format("YYYY-MM-DD HH:mm:ss");
                        if (trades.orders[i].typ == "ask") {
                            trades.orders[i].order_type = Messages("java.api.messages.trade.sell");
                            trades.orders[i].klass = "danger";
                        } else {
                            trades.orders[i].order_type = Messages("java.api.messages.trade.buy");
                            trades.orders[i].klass = "success";
                        }
                    }
                    $('#recent-trades').html(rt_template(trades));
                    $('.percentage_price').css("color", col);
                    $('.percentage_price').html(percentage + '%');
                    $('#volume').html(addThousandsSeparator(zerosTrim(volume)));
                    $('#last').html(addThousandsSeparator(zerosTrim(last)));
                });
            });
        });
    }
}