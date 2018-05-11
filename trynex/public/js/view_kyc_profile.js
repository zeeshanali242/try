$('document').ready(function(){
	//alert('Before to get kyc data')


});

$(function(){
  //alert('Going to get kyc data')
  var view_kyc_details = Handlebars.compile($("#view_kyc_details").html());
  // var view_bank_details = Handlebars.compile($("#view_bank_details").html());

API.get_kyc_data().success(function(data){
//console.log(data)
$('#kycdetails').html(view_kyc_details(data));
});
});



$(function(){
  //alert('Going to get kyc data')
  
  var view_bank_details = Handlebars.compile($("#view_bank_details").html());

API.get_kyc_data().success(function(bank){
$('#bankdetails').html(view_bank_details(bank));
});
});



$(function(){
  //alert('Going to get kyc data')
  
  var view_kyc_documents = Handlebars.compile($("#view_kyc_documents").html());

API.get_kyc_data().success(function(bank){
$('#kycdocuments').html(view_kyc_documents(bank));
});
});