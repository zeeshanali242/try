 var exchangeModel = {};
 $(document).ready(function() {
var ws;
function connect() {
    ws = new WebSocket('wss://192.169.236.100:8441/user');
    ws.onmessage = function(data) {
      if (!(event.data === undefined)) {
          //pick_pair($("#currencyId").attr("exchange-base"), $("#currencyId").attr("exchange-counter"), $("#currencyId").attr("percentage"), $("#currencyId").attr("last"), $("#currencyId").attr("volume"), $("#currencyId").attr("color"));
           pairData();  
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

     var loc = '#' + (window.location.pathname.slice(1));
     $(loc).addClass('active');

     $('.currencybutton a').click(function() {
         $('.currencydrop').toggleClass('active');
     });
     $('.mobileButton').click(function() {
         $('.mobile_menu').addClass('active');
     });
     $('.close_menu a').click(function() {
         $('.mobile_menu').removeClass('active');
     });

     $('.readmoreb').click(function() {
         $('.readmore a.readmoreb').hide();
         $('.readmore a.lessmore').show();
         $('.page_content').addClass('showmore');
     });
     $('.lessmore').click(function() {
         $('.readmore a.readmoreb').show();
         $('.readmore a.lessmore').hide();
         $('.page_content').removeClass('showmore');
     });



     var pp_template = Handlebars.compile($("#pair-picker-template").html());
     var curData_template = Handlebars.compile($("#currency-data-template").html());
     pairData();
     function pairData(){
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
               pair_data[i].percentage = isNaN(percentage) ? 0.00 : percentage;
               if (pair_data[i].percentage < 0) {
                   pair_data[i].color = "red";
                   pair_data[i].fafa = "down";
               } else {
                   pair_data[i].color = " green";
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
           $('.currencybutton').html(pp_template(exchangeModel.data));
           $('.currencydata').html(curData_template(currencyDataRecord));
           $("#currencyId").attr("percentage", per);
           $("#currencyId").attr("last", la);
           $("#currencyId").attr("volume", vo);
           $('.percentage_price').css('color', col);
           var pWithSym = per + '%';
           $('.percentage_price').html(pWithSym);
           $('#last').html(la)
           $('#volume').html(vo);
           $('.currencydrop div').click(function(e) {
               var $this = $(this);
               var l = $this.attr('last');
               var v = $this.attr('volume');
               var p = $this.attr('percentage');
               var c = $this.attr('color');
               var cok = "last=" + l + ";volume=" + v + ";percentage=" + p + ";color=" + c;
               document.cookie = "pair=" + $this.attr('exchange-base') + '#' + $this.attr('exchange-counter') + '#' + l + "#" + v + "#" + c + "#" + p + ";";
               window.location.replace(window.location.origin + "/exchange");
           });
       });
      }
     $('.currencybutton').mouseover(function() {

         $('.currencydrop').addClass('active')
     });
     $('.currencybutton').mouseout(function() {
         $('.currencydrop').removeClass('active')
     });
 });