package controllers.KycUserController;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import controllers.KycUserController.UserKycAuthDetailsBean;
import controllers.KycUserController.UserKycDao;

public class CreateKycUserCredentialController {

	public String getUrlForRedirect(String exchange_id,UserKycAuthDetailsBean usrKycAuth, String username, String mobile_number,
			String user_email) throws JSONException {
		
		String responseString=null;
		UserKycDao userKycDao = null;
		/*JSONObject userData = new JSONObject("{\"email\" : \" " + user_email + "\",\"username\" :\" " + username
				+ "\",\"phone\" :\"" + mobile_number + "\"}");*/
		//JSONObject userData = new JSONObject("{\"email\" : \"\",\"username\" :\"aman\",\"phone\" :\"9874561230\"}");
		JSONObject userData = new JSONObject();

		userData.put("email",user_email);
		userData.put("username",exchange_id);
		userData.put("phone",mobile_number);

		//System.out.println("=============JSON DATA IS ==============" + userData);
		HttpClient httpClient = HttpClientBuilder.create().build();

		try {
			HttpPost request = new HttpPost("https://simple-individual-onboarding.signzy.tech/api/customers/"
					+ usrKycAuth.getCustomer_id() + "/merchants");
			request.addHeader("authorization", usrKycAuth.getAccess_token());
			StringEntity params = new StringEntity(userData.toString());
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			HttpResponse response = httpClient.execute(request);
			//System.out.println(response.getStatusLine().getStatusCode());

			HttpEntity entity = response.getEntity();
			responseString = EntityUtils.toString(entity, "UTF-8");
			//System.out.println(responseString);

			// Parse response

			JSONObject jsonObject = new JSONObject(responseString);
			usrKycAuth.setAuto_login_url(jsonObject.get("autoLoginUrL").toString());
			usrKycAuth.setApplication_url(jsonObject.get("applicationUrl").toString());
			usrKycAuth.setMobile_login_url(jsonObject.get("mobileLoginUrl").toString());
			usrKycAuth.setMobile_auto_login_url(jsonObject.get("mobileAutoLoginUrl").toString());
			usrKycAuth.setUser_name(jsonObject.get("username").toString());
			usrKycAuth.setUser_email(jsonObject.get("email").toString());
			usrKycAuth.setMobile_number(jsonObject.get("phone").toString());
			usrKycAuth.setMerchant_id(jsonObject.get("id").toString());
			userKycDao = new UserKycDao();
			userKycDao.insertKycUserCredential(usrKycAuth,exchange_id);
			//System.out.println("auto login url is :: " + usrKycAuth.getAuto_login_url());

		} catch (Exception ex) {
			// handle exception here
			JSONObject jsonObject = new JSONObject(responseString);
			JSONObject errorMessage = (JSONObject) jsonObject.get("error");
			
			
			//System.out.println("==================="+ errorMessage.get("message"));
			//return errorMessage.get("message").toString();
		return "User already exists " + user_email;
		} finally {
			// shutdown resource
		}
			return usrKycAuth.getAuto_login_url();
	}

}