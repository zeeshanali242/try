 $('document').ready(function(){
 var news_template = Handlebars.compile($("#news-template").html());
 
 API.news().success(function(data){
        var news = data.news;
        var arrayNews = JSON.parse(news);
        if(!(news===undefined)){
        		$('.announcments').html(news_template(arrayNews));
            
        }
    });
setInterval(function() {
  API.news().success(function(data){
        var news = data.news;
        var arrayNews = JSON.parse(news);
        if(!(news===undefined)){
        		$('.announcments').html(news_template(arrayNews));
            
        }
    });
},30000);
});

