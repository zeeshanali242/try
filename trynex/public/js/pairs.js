var pp_template = Handlebars.compile($("#pair-picker-template").html());

API.pairs().success(function(data){
    exchangeModel.pairs = data;

    $('#pair-picker').html(pp_template(data));
   /* $('#pair-picker a').click(function(e){
        var $this = $(this);
        //TODO: mark as active only after the page is done loading; maybe show some kind of spinner
        pick_pair($this.attr('exchange-base'), $this.attr('exchange-counter'));
        $this.parent().siblings().removeClass('active');
        $this.parent().addClass('active');
        e.preventDefault();
    });*/

    // pick_pair(data[0].base, data[0].counter);
});
$('document').ready(function(){
		$('.currencybutton a').click(function(){
			$('.currencydrop').toggleClass('active');
		});
	});
