package controllers.KycUserController;

public class UserKycAuthDetailsBean {
	private String access_token;
	private String customer_id;
	private String application_url;
	private String auto_login_url;
	private String mobile_login_url;
	private String mobile_auto_login_url;
	private String role;
	private String user_created_date;
	private String user_email;
	private String user_name;
	private String mobile_number;
	private String merchant_id;

	public String getMerchant_id() {
		return merchant_id;
	}

	public void setMerchant_id(String merchant_id) {
		this.merchant_id = merchant_id;
	}

	public String getUser_email() {
		return user_email;
	}

	public void setUser_email(String user_email) {
		this.user_email = user_email;
	}

	public String getUser_name() {
		return user_name;
	}

	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}

	public String getMobile_number() {
		return mobile_number;
	}

	public void setMobile_number(String mobile_number) {
		this.mobile_number = mobile_number;
	}

	public String getUser_created_date() {
		return user_created_date;
	}

	public void setUser_created_date(String user_created_date) {
		this.user_created_date = user_created_date;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getAccess_token() {
		return access_token;
	}

	public void setAccess_token(String access_token) {
		this.access_token = access_token;
	}

	public String getCustomer_id() {
		return customer_id;
	}

	public void setCustomer_id(String customer_id) {
		this.customer_id = customer_id;
	}

	public String getApplication_url() {
		return application_url;
	}

	public void setApplication_url(String application_url) {
		this.application_url = application_url;
	}

	public String getAuto_login_url() {
		return auto_login_url;
	}

	public void setAuto_login_url(String auto_login_url) {
		this.auto_login_url = auto_login_url;
	}

	public String getMobile_login_url() {
		return mobile_login_url;
	}

	public void setMobile_login_url(String mobile_login_url) {
		this.mobile_login_url = mobile_login_url;
	}

	public String getMobile_auto_login_url() {
		return mobile_auto_login_url;
	}

	public void setMobile_auto_login_url(String mobile_auto_login_url) {
		this.mobile_auto_login_url = mobile_auto_login_url;
	}

}
