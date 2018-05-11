package controllers.KycUserController;

import java.sql.SQLException;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import controllers.KycUserController.UserKycBean;
import controllers.KycUserController.UserKycDao;

import controllers.KycUserController.UserKycBean;

public class GetUserKycController {
	

	public void getKycUserResponse(Long user_id) throws JSONException, SQLException {
		UserKycBean usrkycbean = null;
		SendKycSubmissionMail kycSUbmissionMail = null;
		String customer_id=null;
		String merchant_id=null;
		String access_token=null;
		String user_email=null;
		Map<String, String> kycCredential = null;
		HttpClient httpClient = HttpClientBuilder.create().build();
		UserKycDao userKycDao=new UserKycDao();
		System.out.println("user id from trynex is >>>>>>>>>>>" +user_id);
		kycCredential=userKycDao.getCustomerCredential(Long.toString(user_id));
		System.out.println("result from db is >>>>>>>>>>>" +kycCredential);

		if(kycCredential==null){
			System.out.println("======= User kyc not done =========== ");
		}
		else{


		customer_id=kycCredential.get("customerId");
	    merchant_id=kycCredential.get("merchantId");
	    access_token=kycCredential.get("accessToken");
	    user_email=kycCredential.get("userEmail");
	    System.out.println("customer_id>>>>>>>>>>>" + customer_id);
           
	    System.out.println("merchant_id>>>>>>>>>>>" +merchant_id);
	       
	    System.out.println("access_token>>>>>>>>>" + access_token);

	    System.out.println("access_token>>>>>>>>>" + user_email);
	       
		
		JSONObject pullKycData = new JSONObject(
				"{\"customerId\" : \""+customer_id+"\",\"merchantId\" :\""+merchant_id+"\"}");
		
		try {
			HttpPost request = new HttpPost("https://simple-individual-onboarding.signzy.tech/api/pullmerchants");
			request.addHeader("authorization", access_token);
			StringEntity params = new StringEntity(pullKycData.toString());
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse response = httpClient.execute(request);
			
			HttpEntity entity = response.getEntity();
			String responseString = EntityUtils.toString(entity, "UTF-8");
			//System.out.println(responseString);

			// Parse response

			JSONObject jsonObject = new JSONObject(responseString);
			usrkycbean = new UserKycBean();
			//System.out.println("KYC RESULT IS   :::::::::" + jsonObject.get("result"));
			JSONArray kyc_result = (JSONArray) jsonObject.get("result");
			JSONObject kyc_result_data = (JSONObject) kyc_result.get(0);
			usrkycbean.setMerchant_id(kyc_result_data.get("merchantId").toString());
			usrkycbean.setCustomer_id(kyc_result_data.get("customerId").toString());
			usrkycbean.setUser_status_type(kyc_result_data.get("status").toString());
			usrkycbean.setUser_id(Long.toString(user_id));;
			JSONObject user_verification_result = (JSONObject) kyc_result_data.get("verificationResult");
			usrkycbean.setBank_account_status(user_verification_result.get("bankAccount").toString());
			usrkycbean.setUser_amlStatus(user_verification_result.get("amlStatus").toString());
			usrkycbean.setUser_overalStatus(user_verification_result.get("overalStatus").toString());
			
			JSONArray user_id_cardsResult = (JSONArray) user_verification_result.get("idCards");

			for (int i = 0; i < user_id_cardsResult.length(); i++) {
				JSONObject kyc_idcard_data = (JSONObject) user_id_cardsResult.get(i);
				// KYC USER RESPONSE
				if (kyc_idcard_data.get("purpose").equals("POA") || kyc_idcard_data.get("type").equals("aadhaar")) {
					usrkycbean.setAadhar_status(kyc_idcard_data.get("status").toString());
					usrkycbean.setAadhaar_doc_type(kyc_idcard_data.get("purpose").toString());
				}
				else
				{
					usrkycbean.setPan_status(kyc_idcard_data.get("status").toString());
					usrkycbean.setPan_doc_type(kyc_idcard_data.get("purpose").toString());
				}
				
			}
			JSONObject user_verification_data = (JSONObject) kyc_result_data.get("verificationData");
			JSONArray bank_document = (JSONArray) user_verification_data.get("documents");
			for (int i = 0; i < bank_document.length(); i++) {
				JSONObject bank_document_data = (JSONObject) bank_document.get(i);
				usrkycbean.setBank_account_holder_name(bank_document_data.getString("beneficiaryName"));
				usrkycbean.setBank_ifsc_code(bank_document_data.getString("beneficiaryIFSC"));
				usrkycbean.setBank_img_path(bank_document_data.getString("images"));
				usrkycbean.setBank_mobile_no(bank_document_data.getString("beneficiaryMobile"));
				usrkycbean.setBank_account_number(bank_document_data.getString("beneficiaryAccount"));
				usrkycbean.setBank_branch_name(bank_document_data.getString("branchName"));
				usrkycbean.setBank_name(bank_document_data.getString("bankName"));
				usrkycbean.setBank_doc_type(bank_document_data.getString("type"));
			}
			JSONArray kyc_id_cards_detail = (JSONArray) user_verification_data.get("idCards");
			//System.out.println(kyc_id_cards_detail);
			for (int i = 0; i < kyc_id_cards_detail.length(); i++) {
				JSONObject kyc_id_cards_data = (JSONObject) kyc_id_cards_detail.get(i);
				
				
				if (kyc_id_cards_data.get("purpose").equals("POA") || kyc_id_cards_data.get("type").equals("aadhaar")) {
					
					
					usrkycbean.setAadhaar_name(kyc_id_cards_data.get("name").toString());
					usrkycbean.setAadhaar_number(kyc_id_cards_data.get("idNo").toString());
					usrkycbean.setAadhar_dob(kyc_id_cards_data.get("dob").toString());
					usrkycbean.setAadhar_address(kyc_id_cards_data.get("address").toString());
					usrkycbean.setAadhar_pincode(kyc_id_cards_data.get("pincode").toString());
					usrkycbean.setAadhar_state(kyc_id_cards_data.get("state").toString());
					JSONArray aadhar_images = (JSONArray) kyc_id_cards_data.get("images");
					usrkycbean.setAadhar_front_img_path(aadhar_images.get(0).toString());
					usrkycbean.setAadhar_back_img_path(aadhar_images.get(1).toString());
					JSONObject aadhar_verification_status = (JSONObject) kyc_id_cards_data.get("verificationStatus");
					usrkycbean.setAadhar_verification_status_flag(aadhar_verification_status.get("verified").toString());
					usrkycbean.setAadhar_verification_message(aadhar_verification_status.get("message").toString());
			}
				else
				{
					usrkycbean.setPan_name(kyc_id_cards_data.get("name").toString());
					usrkycbean.setPan_dob(kyc_id_cards_data.get("dob").toString());
					usrkycbean.setPan_number(kyc_id_cards_data.get("idNo").toString());
					JSONArray pan_images = (JSONArray) kyc_id_cards_data.get("images");
					usrkycbean.setPan_img_path(pan_images.get(0).toString());
					
					JSONObject pan_verification_status = (JSONObject) kyc_id_cards_data.get("verificationStatus");
					usrkycbean.setPan_verification_status_flag(pan_verification_status.get("verified").toString());
					usrkycbean.setPan_verification_message(pan_verification_status.get("message").toString());
				}
				
			}
			
			if (usrkycbean!=null) {
				userKycDao=new UserKycDao();
				userKycDao.insertKycData(usrkycbean,access_token);
				kycSUbmissionMail=new SendKycSubmissionMail();
				kycSUbmissionMail.sendMail(user_email,usrkycbean.getAadhaar_name());
			}
			
		} catch (Exception ex) {
			
			System.out.println(ex);
			// handle exception here
		} finally {
			// shutdown resource
		}


		}
		
	   
		
	
	}
}