//TODO: use a real framework
var exchangeModel = {};
var originLink = window.location.origin;
var ws;
function connect() {
    ws = new WebSocket('wss://192.169.236.100:8441/user');
    ws.onmessage = function(data) {
      if (!(event.data === undefined)) {
       // pick_pair($("#currencyId").attr("exchange-base"), $("#currencyId").attr("exchange-counter"), $("#currencyId").attr("percentage"), $("#currencyId").attr("last"), $("#currencyId").attr("volume"), $("#currencyId").attr("color"));
        pairsData($("#currencyId").attr("exchange-base"),$("#currencyId").attr("exchange-counter"));
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


$('document').ready(function() {
    

    $('.sidebar-menu li.active').removeClass('active');
    $('#exchange').addClass('active');

    $('.currencybutton a').click(function() {
        $('.currencydrop').toggleClass('active');
    });
    $('.buy').click(function() {
        $('.buyform').addClass('active');
        //$('.open_order').toggleClass('small');
        $('.buy').addClass('active');
        $('.sellform').removeClass('active');
        $('.sell').removeClass('active');

    });
    $('.sell').click(function() {
        $('.sellform').addClass('active');
        // $('.open_order').toggleClass('small');
        $('.sell').addClass('active');
        $('.buyform').removeClass('active');
        $('.buy').removeClass('active');
    });

    charAtLoad('BTC', 'INR');
    $('.currencybutton a').click(function() {
        $('.currencydrop').toggleClass('active');
    });
    $('.mobileButton').click(function() {
        $('.mobile_menu').addClass('active');
    });
    $('.close_menu a').click(function() {
        $('.mobile_menu').removeClass('active');
    });
});

function getCookieData(name) {
    var pairs = document.cookie.split("; "),
        count = pairs.length,
        parts;
    while (count--) {
        parts = pairs[count].split("=");
        if (parts[0] === name)
            return parts[1];
    }
    return false;
}


function charAtLoad(base, counter) {

    API.chart(base, counter).success(function(data) {
        $(document.getElementById("graph")).height("375px");

        // create the chart
        Highcharts.stockChart('graph', {


            rangeSelector: {
                buttons: [{
                    type: 'day',
                    count: 1,
                    text: '1d'
                }, {
                    type: 'week',
                    count: 1,
                    text: '1w'
                }, {
                    type: 'month',
                    count: 1,
                    text: '1m'
                }, {
                    type: 'month',
                    count: 3,
                    text: '3m'
                }, {
                    type: 'month',
                    count: 6,
                    text: '6m'
                }]
            },


            title: {
                text: 'Trynex Price Chart'
            },

            series: [{
                type: 'candlestick',
                name: 'Trynex Price Chart',
                data: data,
                dataGrouping: {
                    units: [
                        [
                            'week', // unit name
                            [1] // allowed multiples
                        ],
                        [
                            'month', [1, 3, 6]
                        ]
                    ]
                }
            }]
        });



    });
}

function delete_cookie() {
    document.cookie = 'pair=; expires=' + new Date();
}

$('#test a').click(function(e) {
    var base = $('#currencyId').attr('exchange-base');
    var counter = $('#currencyId').attr('exchange-counter');
    var $this = $(this);
    API.chart(base, counter).success(function(data) {
        $(document.getElementById("graph")).height("375px");

        if ($this.attr('id') == 'liveChart') {
            var liveData = [];
            if (data != null && data.length > 0) {
                for (var i = 0; i < data.length; i++) {
                    var oldData = data[i];
                    var newData = [];
                    newData.push(oldData[0]);
                    newData.push(oldData[3]);
                    liveData.push(newData);

                }
            }
            Highcharts.setOptions({
                global: {
                    timezoneOffset: -300
                }
            });
            Highcharts.chart('graph', {


                rangeSelector: {
                    enabled: true,
                    buttons: [{
                        type: 'day',
                        count: 1,
                        text: '1d',

                    }, {
                        type: 'month',
                        count: 1,
                        text: '1m',

                    }, {
                        type: 'month',
                        count: 3,
                        text: '3m'
                    }, {
                        type: 'month',
                        count: 6,
                        text: '6m'
                    }, {
                        type: 'ytd',
                        text: 'YTD'
                    }, {
                        type: 'year',
                        count: 1,
                        text: '1y'
                    }, {
                        type: 'all',
                        text: 'All'
                    }]
                },

                chart: {
                    zoomType: 'x'
                },
                title: {
                    text: 'Price Live Chart'
                },
                subtitle: {
                    text: document.ontouchstart === undefined ?
                        'Click and drag in the plot area to zoom in' : 'Pinch the chart to zoom in'
                },
                xAxis: {
                    type: 'datetime'
                },
                yAxis: {
                    title: {
                        text: 'Exchange rate'

                    }
                },
                legend: {
                    enabled: false
                },
                plotOptions: {
                    area: {
                        fillColor: {
                            linearGradient: {
                                x1: 0,
                                y1: 0,
                                x2: 0,
                                y2: 1
                            },
                            stops: [
                                [0, Highcharts.getOptions().colors[0]],
                                [1, Highcharts.Color(Highcharts.getOptions().colors[0]).setOpacity(0).get('rgba')]
                            ]
                        },
                        marker: {
                            radius: 2
                        },
                        lineWidth: 1,
                        states: {
                            hover: {
                                lineWidth: 1
                            }
                        },
                        threshold: null
                    },
                    series: {

                    }
                },

                series: [{
                    type: 'area',
                    name: 'BTC PRICE',
                    data: liveData
                }]
            });
        } else {

            Highcharts.stockChart('graph', {


                rangeSelector: {
                    buttons: [{
                        type: 'day',
                        count: 1,
                        text: '1d'
                    }, {
                        type: 'week',
                        count: 1,
                        text: '1w'
                    }, {
                        type: 'month',
                        count: 1,
                        text: '1m'
                    }, {
                        type: 'month',
                        count: 3,
                        text: '3m'
                    }, {
                        type: 'month',
                        count: 6,
                        text: '6m'
                    }]
                },


                title: {
                    text: 'Trynex Price Chart'
                },

                series: [{
                    type: 'candlestick',
                    name: 'Trynex Price Chart',
                    data: data,
                    dataGrouping: {
                        units: [
                            [
                                'week', // unit name
                                [1] // allowed multiples
                            ],
                            [
                                'month', [1, 3, 6]
                            ]
                        ]
                    }
                }]
            });
        }
    });



});
var myMap = new Map();
var volumeMap = new Map();


    var pp_template = Handlebars.compile($("#pair-picker-template").html());
    var bid_template = Handlebars.compile($("#bid-template").html());
    var ask_template = Handlebars.compile($("#ask-template").html());
    var curData_template = Handlebars.compile($("#currency-data-template").html());
    function pairsData(base, counter) {
        API.pairs_data().success(function(pair_data) {
            var la, vo, per, col,percen;
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
                var percen = pair_data[i].percentage;
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
                    col = (percen >= 0) ? "green" : "red";
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
        var la, vo, per, col,percen;
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
            percen = pair_data[i].percentage;
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
                col = (percen >= 0) ? "green" : "red";
            }
        }
        //apiPair(la,vo,per,col);
        $('.currencybutton').html(pp_template(exchangeModel.data));
        $('.currencydata').html(curData_template(currencyDataRecord));
        $('#currency_list div').click(function(e) {
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

        var cookie = getCookieData("pair");
        if (cookie != false) {
            var fields = cookie.split('#');
            $("#currencyId").attr("exchange-base", fields[0]);
            $("#currencyId").attr("exchange-counter", fields[1]);
            $("#currencyId").attr("percentage", fields[5]);
            $("#currencyId").attr("last", fields[2]);
            $("#currencyId").attr("volume", fields[3]);
            $("#currencyId").attr("color", fields[4]);
            pick_pair(fields[0], fields[1], fields[5], fields[2], fields[3], fields[4]);
            $('#t1').text(fields[0] + "/" + fields[1]);
            $('.percentage_price').css("color", );
            delete_cookie();
        } else {
            pick_pair('BTC', 'INR', per, la, vo, col);
        }

    });
    /*
       setInterval(function() {
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
     }, 2000);
    */

    $('.currencybutton').mouseover(function() {

        $('.currencydrop').addClass('active');
    });
    $('.currencybutton').mouseout(function() {
        $('.currencydrop').removeClass('active')
    });


    function pick_pair(base, counter, percentage, last, volume, col) {
        $('.percentage_price').css("color", col);
        $('.percentage_price').html(percentage + '%');
        $('#volume').html(addThousandsSeparator(zerosTrim(volume)));
        $('#last').html(addThousandsSeparator(zerosTrim(last)));
        if (!(base == '' || base === undefined || counter == '' || counter === undefined)) {
            if (base && counter) {
                exchangeModel.base = base;
                exchangeModel.counter = counter;
            } else {
                base = exchangeModel.base;
                counter = exchangeModel.counter;
            }
            charAtLoad(base, counter);
            var pt_template = Handlebars.compile($("#pending-trades-template").html());
            API.pending_trades(base, counter).success(function(trades) {
                var i;
                for (i = 0; i < trades.length; i++) {
                    trades[i].value = zerosToSpaces((Number(trades[i].amount) * Number(trades[i].price)));
                    trades[i].amount = zerosToSpaces(trades[i].amount);
                    trades[i].price = zerosToSpaces(trades[i].price);
                    trades[i].created = moment(Number(trades[i].created)).format("YYYY-MM-DD HH:mm:ss");
                    if (trades[i].typ == "ask") {
                    trades[i].order_type = Messages("java.api.messages.trade.sell");
                        trades[i].klass = "danger";
                    } else {
                        trades[i].order_type = Messages("java.api.messages.trade.buy");
                        trades[i].klass = "success";
                    }
                }
                $('#pending-trades').html(pt_template(trades)).find('a').click(function() {
                    var $this = $(this);
                    var id = $this.attr('exchange-trade-id');
                    API.cancel(id).success(function() {
                        $.pnotify({
                            title: Messages("java.api.messages.trade.tradecancelled"),
                            text: Messages("java.api.messages.trade.tradecancelledsuccessfully"),
                            styling: 'bootstrap',
                            type: 'success',
                            text_escape: true
                        });
                        //pick_pair($('#currencyId').attr('exchange-base'), $('#currencyId').attr('exchange-counter'), $('#currencyId').attr('percentage'), $('#currencyId').attr('last'), $('#currencyId').attr('volume'), $('#currencyId').attr('color'));
                        var data = JSON.stringify({
                            'user' : 'test'
                        });
                        ws.send(data);
                         pairsData($("#currencyId").attr("exchange-base"),$("#currencyId").attr("exchange-counter"));
                        refresh_ticker();
                    });
                });
                $('.fa-caret-down').click(function() {
                    var $this = $(this);
                    var id = $this.parent().parent().next();
                    $(id).toggle();
                });

            });

            //XXX: Not the best way to do things... these calls should either be done in parallel or as one call

            var ob_template = Handlebars.compile($("#open-bids-template").html());
            var oa_template = Handlebars.compile($("#open-asks-template").html());
            API.balance().success(function(balances) {
                var balance_base = "0.00000000";
                var balance_counter = "0.00000000";

                for (var i = 0; i < balances.length; i++) {
                    var bal = balances[i];
                    if (bal.currency == base) {
                        balance_base = zerosTrim((bal.amount - bal.hold));
                    }
                    if (bal.currency == counter) {
                        balance_counter = zerosTrim((bal.amount - bal.hold));
                    }
                }

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
                            bids.orders[i].price = zerosToSpaces(bids.orders[i].price);
                        }
                        bids.total_counter = zerosTrim(data.total_counter);
                        var asks = {};
                        asks.orders = data.asks;
                        asks.base = base;
                        asks.counter = counter;
                        for (i = 0; i < asks.orders.length; i++) {
                            asks.orders[i].value = zerosToSpaces((Number(asks.orders[i].amount) * Number(asks.orders[i].price)));
                            asks.orders[i].amount = zerosToSpaces(asks.orders[i].amount);
                            asks.orders[i].price = zerosToSpaces(asks.orders[i].price);
                        }
                        asks.total_base = zerosTrim(data.total_base);

                        $('#open-bids').html(ob_template(bids));
                        $('#open-asks').html(oa_template(asks));


                        var bid_price = asks.orders.length > 0 ? asks.orders[0].price.trim() : "";
                        var ask_price = bids.orders.length > 0 ? bids.orders[0].price.trim() : "";

                        $('#bid').html(bid_template({
                            balance: balance_counter,
                            price: bid_price,
                            base: base,
                            counter: counter,
                            one_way: one_way
                        })).off("submit").submit(function(e) {
                            $('#bid-submit').addClass('disabled');
                            var $form = $('#bid');
                            var amount = Number($form.find(".amount").val());
                            var price = Number($form.find(".price").val());
                            API.bid(base, counter, amount, price).success(function(data) {
                                var remains = Number(data.remains);
                                $.pnotify({
                                    title: Messages("java.api.messages.trade.buyorder") + " " + (remains > 0 ? (remains == amount ? Messages("java.api.messages.trade.placed") : Messages("java.api.messages.trade.partiallyfilled")) : Messages("java.api.messages.trade.filled")),
                                    text: Messages("java.api.messages.trade.market") + " " + base + "/" + counter + ', ' + Messages("java.api.messages.trade.amount") + amount + ', ' + Messages("java.api.messages.trade.remains") + remains,
                                    styling: 'bootstrap',
                                    type: 'success',
                                    text_escape: true
                                });
                                //pick_pair($('#currencyId').attr('exchange-base'), $('#currencyId').attr('exchange-counter') , $('#currencyId').attr('percentage'),$('#currencyId').attr('last'),$('#currencyId').attr('volume'),$('#currencyId').attr('color'));
                                var data = JSON.stringify({
                                    'user' : 'test'
                                });
                                ws.send(data);
                                 pairsData($("#currencyId").attr("exchange-base"),$("#currencyId").attr("exchange-counter"));
                                refresh_ticker();
                            }).error(function() {
                                $('#bid-submit').removeClass('disabled');
                            });
                            e.preventDefault();
                        });

                        /*  $('#send_otp').click(function(){
                             alert('called');

                        });*/

                        $('#ask').html(ask_template({
                            balance: balance_base,
                            price: ask_price,
                            base: base,
                            counter: counter,
                            one_way: one_way
                        })).off("submit").submit(function(e) {
                            $('#ask-submit').addClass('disabled');
                            var $form = $('#ask');
                            var amount = Number($form.find(".amount").val());
                            var price = Number($form.find(".price").val());
                            API.ask(base, counter, amount, price).success(function(data) {
                                var remains = Number(data.remains);
                                $.pnotify({
                                    title: Messages("java.api.messages.trade.sellorder") + " " + (remains > 0 ? (remains == amount ? Messages("java.api.messages.trade.placed") : Messages("java.api.messages.trade.partiallyfilled")) : Messages("java.api.messages.trade.filled")),
                                    text: Messages("java.api.messages.trade.market") + " " + base + "/" + counter + ', ' + Messages("java.api.messages.trade.amount") + amount + ', ' + Messages("java.api.messages.trade.remains") + remains,
                                    styling: 'bootstrap',
                                    type: 'success',
                                    text_escape: true
                                });
                                // pick_pair($('#currencyId').attr('exchange-base'), $('#currencyId').attr('exchange-counter') , $('#currencyId').attr('percentage'),$('#currencyId').attr('last'),$('#currencyId').attr('volume'),$('#currencyId').attr('color'));
                                var data = JSON.stringify({
                                    'user' : 'test'
                                });
                                ws.send(data);
                                 pairsData($("#currencyId").attr("exchange-base"),$("#currencyId").attr("exchange-counter"));
                                refresh_ticker();
                                $('#bid-submit').removeClass('disabled');
                            }).error(function() {
                                $('#ask-submit').removeClass('disabled');
                            });
                            e.preventDefault();
                        });


                        function update_bid() {
                            $form = $('#bid');
                            var order_amount = $form.find(".amount").val();
                            var order_price = $form.find(".price").val();
                            var order_value = order_amount * order_price;
                            var fee_in_per = (order_value * fee) / 100;
                            order_value += fee_in_per;
                            $form.find(".counter").val(zerosTrim((order_value > 0 ? order_value : 0)));
                            $form.find(".fee").val(zerosTrim((fee_in_per > 0 ? fee_in_per : 0)));
                        }
                        update_bid();
                        $('#bid').find('.amount,.price').keyup(update_bid).change(update_bid);

                        function update_ask() {
                            $form = $('#ask');
                            var order_amount = $form.find(".amount").val();
                            var order_price = $form.find(".price").val();
                            var order_value = order_amount * order_price;
                            var fee_in_per = (order_value * fee) / 100;
                            var ttt = $form.find(".counter");
                            $form.find(".counter").val(zerosTrim((order_value > 0 ? order_value : 0)));
                            $form.find(".fee").val(zerosTrim((fee_in_per > 0 ? fee_in_per : 0)));
                        }
                        update_ask();
                        $('#ask').find('.amount,.price').keyup(update_ask).change(update_ask);
                    });
                });
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

            });

        }
    }

    /*function refreshOrdersData(base, counter, percentage, last, volume, col) {
        if (!(base == '' || base === undefined || counter == '' || counter === undefined)) {
            if (base && counter) {
                exchangeModel.base = base;
                exchangeModel.counter = counter;
            } else {
                base = exchangeModel.base;
                counter = exchangeModel.counter;
            }

            var pt_template = Handlebars.compile($("#pending-trades-template").html());
            API.pending_trades(base, counter).success(function(trades) {
                var i;
                for (i = 0; i < trades.length; i++) {
                    trades[i].value = zerosToSpaces((Number(trades[i].amount) * Number(trades[i].price)));
                    trades[i].amount = zerosToSpaces(trades[i].amount);
                    trades[i].price = addThousandsSeparator(zerosToSpaces(trades[i].price));
                    trades[i].created = moment(Number(trades[i].created)).format("YYYY-MM-DD HH:mm:ss");
                    if (trades[i].typ == "ask") {
                        trades[i].order_type = Messages("java.api.messages.trade.sell");
                        trades[i].klass = "danger";
                    } else {
                        trades[i].order_type = Messages("java.api.messages.trade.buy");
                        trades[i].klass = "success";
                    }
                }
                $('#pending-trades').html(pt_template(trades)).find('a').click(function() {
                    var $this = $(this);
                    var id = $this.attr('exchange-trade-id');
                    API.cancel(id).success(function() {
                        console.log("coolledd");
                        $.pnotify({
                            title: Messages("java.api.messages.trade.tradecancelled"),
                            text: Messages("java.api.messages.trade.tradecancelledsuccessfully"),
                            styling: 'bootstrap',
                            type: 'success',
                            text_escape: true
                        });
                        
                        //pick_pair($('#currencyId').attr('exchange-base'), $('#currencyId').attr('exchange-counter'), $('#currencyId').attr('percentage'), $('#currencyId').attr('last'), $('#currencyId').attr('volume'), $('#currencyId').attr('color'));
                        // refresh_ticker($('#currencyId').attr('exchange-base'), $('#currencyId').attr('exchange-counter') , $('#currencyId').attr('percentage'),$('#currencyId').attr('last'),$('#currencyId').attr('volume'),$('#currencyId').attr('color'));
                    });
                    ws.send([$('#currencyId').attr("exchange-base"), $('#currencyId').attr("exchange-counter")]);
                });
                $('.fa-caret-down').click(function() {
                    var $this = $(this);
                    var id = $this.parent().parent().next();
                    $(id).toggle();
                });

            });
            var ob_template = Handlebars.compile($("#open-bids-template").html());
            var oa_template = Handlebars.compile($("#open-asks-template").html());

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

            });
            $('.percentage_price').css("color", col);
            $('.percentage_price').html(percentage + '%');
            $('#volume').html(addThousandsSeparator(zerosTrim(volume)));
            $('#last').html(addThousandsSeparator(zerosTrim(last)));

        }
    }*/
