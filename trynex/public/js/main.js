var pp_template = Handlebars.compile($("#pair-picker-template").html());
var exchangeModel = {};
var myMap = new Map();
    API.ticker().success(function(tickers){
        for (var i = 0; i < tickers.length; i++) {
             myMap.set(tickers[i].base ,tickers[i].last);
        }
        apiPair();
    });


    function apiPair(){
      API.pairs().success(function(data) {
          exchangeModel.pairs = data;
          for (var i = 0; i < data.length; i++) {
             var last =  myMap.get(data[i].base);
             if(last=='' || last === undefined){
                  data[i]['last'] = 0;
             }
             else{
                  data[i]['last'] = parseInt(last).toFixed(2);
             }
          }
   $('.currencybutton').html(pp_template(data));

     $('.currencydrop a').click(function(e){

            var $this = $(this);
            document.cookie = "pair="+$this.attr('exchange-base')+'-'+$this.attr('exchange-counter');
            window.location.replace(window.location.origin+"/exchange");

        });

     $('.currencybutton a').click(function(){

            $('.currencydrop').toggleClass('active');
        });
    

});

  }




        

