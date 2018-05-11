var refresh_ticker = function(){};

$(function(){
    var $ttemplate = $("#tickers-template");
    if ($ttemplate.length) {
        var template = Handlebars.compile($ttemplate.html());

        refresh_ticker = function(){
            API.ticker().success(function(tickers){
                for (var i = 0; i < tickers.length; i++) {
                    if (Number(tickers[i].last) > Number(tickers[i].first)) {
                        tickers[i].color = "green";
                        tickers[i].icon = "chevron-up";
                    } else if (Number(tickers[i].last) < Number(tickers[i].first)) {
                        tickers[i].color = "red";
                        tickers[i].icon = "chevron-down";
                    } else {
                        tickers[i].color = "gray";
                        tickers[i].icon = "minus";
                    }
                    tickers[i].last = zerosTrim(tickers[i].last);
                }
                $('#ticker').html(template(tickers));
            });
        };
        refresh_ticker();
    }
    showEmails();
});

function showEmails() {
    $('.hidden-email').each(function(idx, el) {
        var $el = $(el);
        $el.text($el.attr('data-name') + '@' + $el.attr('data-domain'));
        $el.attr('href', 'mailto:' + $el.text());
    });
}

function keepNumLength(m){
    var i = m.length - 1;
    while (m[i] == '0') {
        i--;
    }
    if (m[i] == '.') {
        i++;
    }
    return i;
}

function zerosToSpaces(m){
    m = Number(m).toFixed(8);
    var i = keepNumLength(m);
    return m.substring(0, i + 1) + Array(m.length - i).join('\xA0');
}

function zerosTrim(m){
    m = Number(m).toFixed(8);
    var i = keepNumLength(m);
    return m.substring(0, i + 1);
}

function addThousandsSeparator(price) {
    var output_price = price
    if (parseFloat(price)) {
        input = new String(price); // so you can perform string operations
        var parts = input.split("."); // remove the decimal part
        parts[0] = parts[0].split("").reverse().join("").replace(/(\d{3})(?!$)/g, "$1,").split("").reverse().join("");
        output_price = parts.join(".");
    }
    return output_price;
}

$('#hideShowId a').click(function(e) {

            if ($('body').is('.sidebar-collapse'))
              {
                $('body').removeClass('sidebar-collapse');
              }

              else
              {
                $('body').addClass('sidebar-collapse');
              }
           });
