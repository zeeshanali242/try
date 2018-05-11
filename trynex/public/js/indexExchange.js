
$(document).ready(function(){
var ob_template = Handlebars.compile($("#open-bids-template").html());
var oa_template = Handlebars.compile($("#open-asks-template").html());
var pp_template = Handlebars.compile($("#pair-picker-template").html());


API.pairs().success(function(data){

        $('#pair-picker').html(pp_template(data));

        $('#pair-picker a').click(function(e){
            var $this = $(this);
            //TODO: mark as active only after the page is done loading; maybe show some kind of spinner
            pick_pair($this.attr('exchange-base'), $this.attr('exchange-counter'));
            $this.parent().siblings().removeClass('active');
            $this.parent().addClass('active');
            e.preventDefault();
        });
            pick_pair('BTC', 'INR');
    });
});

function pick_pair(base, counter) {

var ob_template = Handlebars.compile($("#open-bids-template").html());
var oa_template = Handlebars.compile($("#open-asks-template").html());
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
               bids.orders[i].price = addThousandsSeparator(zerosToSpaces(bids.orders[i].price));;
           }
           bids.total_counter = zerosTrim(data.total_counter);
           var asks = {};
           asks.orders = data.asks;
           asks.base = base;
           asks.counter = counter;
           for (i = 0; i < asks.orders.length; i++) {
               asks.orders[i].value = zerosToSpaces((Number(asks.orders[i].amount) * Number(asks.orders[i].price)));
               asks.orders[i].amount = zerosToSpaces(asks.orders[i].amount);
               asks.orders[i].price = addThousandsSeparator(zerosToSpaces(bids.orders[i].price));
           }
           asks.total_base = zerosTrim(data.total_base);

           $('#open-bids').html(ob_template(bids));
           $('#open-asks').html(oa_template(asks));

       });

        var rt_template = Handlebars.compile($("#recent-trades-template").html());
        API.recent_trades(base, counter).success(function(data){
            var i;
            var trades = {};
            trades.orders = data;
            trades.base = base;
            trades.counter = counter;
            for (i = 0; i < trades.orders.length; i++){
                trades.orders[i].value = zerosToSpaces((Number(trades.orders[i].amount) * Number(trades.orders[i].price)));
                trades.orders[i].amount = zerosToSpaces(trades.orders[i].amount);
                trades.orders[i].price = addThousandsSeparator(zerosToSpaces(bids.orders[i].price));
                trades.orders[i].created = moment(Number(trades.orders[i].created)).format("YYYY-MM-DD HH:mm:ss");
                if (trades.orders[i].typ == "ask") {
                    trades.orders[i].order_type = Messages("java.api.messages.trade.sell");
                } else {
                    trades.orders[i].order_type = Messages("java.api.messages.trade.buy");
                }
            }
            $('#recent-trades').html(rt_template(trades));

        });
   });
}
$('#pair-picker a').click(function(e) {
   var $this = $(this);
   //TODO: mark as active only after the page is done loading; maybe show some kind of spinner
   pick_pair($this.attr('exchange-base'), $this.attr('exchange-counter'));
   $this.parent().siblings().removeClass('active');
   $this.parent().addClass('active');
   e.preventDefault();
});
