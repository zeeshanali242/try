$('document').ready(function() {
	kycData();

});

function kycData() {

 API.kycSubmissionData().success(function(data){
	 window.location.href = 'profile1';
	});
}