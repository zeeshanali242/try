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
import java.sql.SQLException;
import controllers.KycUserController.UserKycAuthDetailsBean;

public class UserKycAuthController {
	static String signzyRedirectUrl=null;

	public String getAccessToken(Long exchange_id,String username, String mobile_number,
			String user_email) throws JSONException,SQLException{
		//String signzyRedirectUrl=null;

		System.out.println("============ Exchange user id is ============ "+ exchange_id);
		JSONObject loginCredential = new JSONObject(
				"{\"username\" : \"syncrasytech\",\"password\" :\"VAYl0mgNECK4nqzvr8dso3x8uhnbPKufab2yvF0o\"}");
		
		//System.out.println("=========== data check=== "+checkSignzyLoginUrl(Long.toString(exchange_id)));
		signzyRedirectUrl=checkSignzyLoginUrl(Long.toString(exchange_id));
		//System.out.println("========= signzyRedirectUrl ===== " + signzyRedirectUrl);
		//signzyRedirectUrl ="https://simple-individual-onboarding.signzy.tech/syncrasytech/5a8d0c6c011b7303959ef00d/5aa28ba05ae2ae045e6fbd17/2314340514996141600/main";

		if (signzyRedirectUrl == null) {
			UserKycAuthDetailsBean usrKycAuth = null;
			CreateKycUserCredentialController usrCredentialController = null;
			HttpClient httpClient = HttpClientBuilder.create().build();
			//System.out.println("========= If it is null ==== " + signzyRedirectUrl);

			try {
				HttpPost request = new HttpPost("https://simple-individual-onboarding.signzy.tech/api/customers/login");
				StringEntity params = new StringEntity(loginCredential.toString());
				request.addHeader("content-type", "application/json");
				request.setEntity(params);
				HttpResponse response = httpClient.execute(request);
				//System.out.println(response.getStatusLine().getStatusCode());
				/*
				 * String responseString = new
				 * BasicResponseHandler().handleResponse(response);
				 * System.out.println(responseString);
				 */
				HttpEntity entity = response.getEntity();
				String responseString = EntityUtils.toString(entity, "UTF-8");
				//System.out.println("====== res =============="+responseString);

				// Parse response

				JSONObject jsonObject = new JSONObject(responseString);
				usrKycAuth = new UserKycAuthDetailsBean();

				usrKycAuth.setAccess_token(jsonObject.get("id").toString());
				usrKycAuth.setCustomer_id(jsonObject.get("userId").toString());
				usrKycAuth.setUser_created_date(jsonObject.get("created").toString());
				usrKycAuth.setRole(jsonObject.get("role").toString());

				//System.out.println("access token is :: " + usrKycAuth.getAccess_token());
				//System.out.println("customer id is :: " + usrKycAuth.getCustomer_id());
				//System.out.println("user role is :: " + usrKycAuth.getRole());
				//System.out.println("user creation  date is :: " + usrKycAuth.getUser_created_date());
				usrCredentialController = new CreateKycUserCredentialController();
				signzyRedirectUrl=usrCredentialController.getUrlForRedirect(Long.toString(exchange_id),usrKycAuth, username, mobile_number, user_email);
				//signzyRedirectUrl="https://simple-individual-onboarding.signzy.tech/syncrasytech/5a8d0c6c011b7303959ef00d/5aa28ba05ae2ae045e6fbd17/2314340514996141600/main";
				
			} catch (Exception ex) {
				System.out.println(ex);
				// handle exception here
			} finally {
				// shutdown resource
			}
			return signzyRedirectUrl;
		}
		else {

			return signzyRedirectUrl;
		}
    }
	private static String checkSignzyLoginUrl(String exchange_id) throws SQLException
	 {
		UserKycDao userKycDao = new UserKycDao();
		signzyRedirectUrl=userKycDao.checkAutoLoginUrl(exchange_id);
		//System.out.println("===data from db==="+signzyRedirectUrl);
		return signzyRedirectUrl;
		
	}

}