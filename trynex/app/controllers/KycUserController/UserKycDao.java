package controllers.KycUserController;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import controllers.KycUserController.UserKycAuthDetailsBean;

public class UserKycDao {

	public void insertKycUserCredential(UserKycAuthDetailsBean usrKycAuthBean,String exchange_id) throws SQLException {
		// TODO Auto-generated method stub

		Connection conn = null;
		Statement stmt = null;

		try {

			/*Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection("jdbc:postgresql://18.222.97.182:5432/exchange", "postgres", "root");
			*/
			conn=DBConnection.getConnectionString();
			stmt = conn.createStatement();

			String sql = "INSERT INTO kyc_user_credential(user_id,access_token,customer_id,user_role,user_creation_date,username,user_email,user_mobile_no,auto_login_url,application_url,mobile_login_url,mobile_auto_login_url,merchant_id) "
					+ "VALUES ("+exchange_id+",'" + usrKycAuthBean.getAccess_token() + "', '" + usrKycAuthBean.getCustomer_id() + "','"
					+ usrKycAuthBean.getRole() + "','" + usrKycAuthBean.getUser_created_date() + "','"
					+ usrKycAuthBean.getUser_name() + "','" + usrKycAuthBean.getUser_email() + "','"
					+ usrKycAuthBean.getMobile_number() + "','" + usrKycAuthBean.getAuto_login_url() + "','"
					+ usrKycAuthBean.getApplication_url() + "','" + usrKycAuthBean.getMobile_login_url() + "','"
					+ usrKycAuthBean.getMobile_auto_login_url() + "','" + usrKycAuthBean.getMerchant_id() + "')";
			stmt.executeUpdate(sql);

		} catch (Exception e) {
			e.printStackTrace();

		} finally {

			stmt.close();
			DBConnection.closeConnection(conn);
		}

	}

	public void insertKycData(UserKycBean usrkycbean, String access_token) throws SQLException {
		// TODO Auto-generated method stub
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		boolean kycflag = false;

		try {
				/*Class.forName("org.postgresql.Driver");
				
				conn = DriverManager.getConnection("jdbc:postgresql://18.222.97.182:5432/exchange", "postgres", "root");
				*/	
				conn=DBConnection.getConnectionString();
				stmt = conn.createStatement();

				rs = stmt.executeQuery("SELECT * FROM kyc_aadhar_card_detail where user_id=" + usrkycbean.getUser_id());
				while (rs.next()) {
					kycflag = true;
				}

						if (kycflag) {
						// update existing record
						System.out.println("===== Record already exist ===== ");

						String aadhar_data = "UPDATE kyc_aadhar_card_detail SET customer_id='" + usrkycbean.getCustomer_id()
								+ "', merchant_id='" + usrkycbean.getMerchant_id() + "', user_status_type='"
								+ usrkycbean.getUser_status_type() + "', aadhar_name='" + usrkycbean.getAadhaar_name()
								+ "', aadhar_number='" + usrkycbean.getAadhaar_number() + "', aadhar_dob='"
								+ usrkycbean.getAadhar_dob() + "', aadhar_address='" + usrkycbean.getAadhar_address()
								+ "', aadhar_pincode='" + usrkycbean.getAadhar_pincode() + "', aadhar_front_image_path='"
								+ usrkycbean.getAadhar_front_img_path() + "', aadhar_back_image_path='"
								+ usrkycbean.getAadhar_back_img_path() + "', aadhar_verification_status_flag='"
								+ usrkycbean.getAadhar_verification_status_flag() + "', aadhar_verification_message='"
								+ usrkycbean.getPan_verification_message() + "', aadhar_document_type='"
								+ usrkycbean.getAadhaar_doc_type() + "', aadhar_verification_status='"
								+ usrkycbean.getAadhar_status() + "', overall_status='" + usrkycbean.getUser_overalStatus()
								+ "', insert_time=now(), aadhar_state='" + usrkycbean.getAadhar_state() + "', access_token='"
								+ access_token + "' WHERE user_id=" + usrkycbean.getUser_id();
						
						stmt.executeUpdate(aadhar_data);

						String pan_data = "UPDATE kyc_pan_card_detail SET customer_id='" + usrkycbean.getCustomer_id()
								+ "', merchant_id='" + usrkycbean.getMerchant_id() + "', user_status_type='"
								+ usrkycbean.getUser_status_type() + "',pancard_name='" + usrkycbean.getPan_name()
								+ "', pancard_number='" + usrkycbean.getPan_number() + "', pancard_dob='" + usrkycbean.getPan_dob()
								+ "', pancard_image_path='" + usrkycbean.getPan_img_path() + "', pan_verification_status_flag='"
								+ usrkycbean.getPan_verification_status_flag() + "', pan_verification_message='"
								+ usrkycbean.getPan_verification_message() + "', pan_document_type='"
								+ usrkycbean.getPan_doc_type() + "', pan_verification_status='" + usrkycbean.getPan_status()
								+ "', overall_status='" + usrkycbean.getUser_overalStatus() + "', insert_time=now(), access_token='"
								+ access_token + "' WHERE user_id=" + usrkycbean.getUser_id();

						
						stmt.executeUpdate(pan_data);

						String bank_data = "UPDATE kyc_bank_detail SET user_account_no='" + usrkycbean.getBank_account_number()
								+ "', user_ifsc_code='" + usrkycbean.getBank_ifsc_code() + "', user_account_holder_name='"
								+ usrkycbean.getBank_account_holder_name() + "', user_mobile_no='"
								+ usrkycbean.getBank_mobile_no() + "', merchant_id='" + usrkycbean.getMerchant_id()
								+ "', customer_id='" + usrkycbean.getCustomer_id() + "', user_status_type='"
								+ usrkycbean.getUser_status_type() + "', bank_account_status='"
								+ usrkycbean.getBank_account_status() + "', aml_status='" + usrkycbean.getUser_amlStatus()
								+ "', overall_status='" + usrkycbean.getUser_overalStatus() + "', bank_image_path='"
								+ usrkycbean.getBank_img_path() + "', bank_name='" + usrkycbean.getBank_name()
								+ "', bank_branch_name='" + usrkycbean.getBank_branch_name() + "', bank_document_type='"
								+ usrkycbean.getBank_doc_type() + "', insert_time=now(), access_token='" + access_token
								+ "' WHERE user_id=" + usrkycbean.getUser_id();
						
						stmt.executeUpdate(bank_data);

						
					} 
						else {
							System.out.println("===== No Record found, going to insert ===== ");

							String aadhar_data = "INSERT INTO kyc_aadhar_card_detail(user_id,customer_id,merchant_id,user_status_type,aadhar_name,aadhar_number,aadhar_dob,aadhar_address,aadhar_front_image_path,aadhar_back_image_path,aadhar_document_type,aadhar_verification_status,aadhar_pincode,aadhar_state,aadhar_verification_status_flag,aadhar_verification_message,overall_status,access_token,insert_time) "
									+ "VALUES (" + usrkycbean.getUser_id() + ",'" + usrkycbean.getCustomer_id() + "', '"
									+ usrkycbean.getMerchant_id() + "','" + usrkycbean.getUser_status_type() + "','"
									+ usrkycbean.getAadhaar_name() + "','" + usrkycbean.getAadhaar_number() + "','"
									+ usrkycbean.getAadhar_dob() + "','" + usrkycbean.getAadhar_address() + "','"
									+ usrkycbean.getAadhar_front_img_path() + "','" + usrkycbean.getAadhar_back_img_path() + "','"
									+ usrkycbean.getAadhaar_doc_type() + "','" + usrkycbean.getAadhar_status() + "','"
									+ usrkycbean.getAadhar_pincode() + "','" + usrkycbean.getAadhar_state() + "','"
									+ usrkycbean.getAadhar_verification_status_flag() + "','"
									+ usrkycbean.getAadhar_verification_message() + "','" + usrkycbean.getUser_overalStatus() + "','"
									+ access_token + "',now())";
							stmt.executeUpdate(aadhar_data);

							String pan_data = "INSERT INTO kyc_pan_card_detail(user_id,customer_id,merchant_id,user_status_type,pancard_name,pancard_number,pancard_dob,pancard_image_path,pan_document_type,pan_verification_status,pan_verification_status_flag,pan_verification_message,overall_status,access_token,insert_time) "
									+ "VALUES (" + usrkycbean.getUser_id() + ",'" + usrkycbean.getCustomer_id() + "', '"
									+ usrkycbean.getMerchant_id() + "','" + usrkycbean.getUser_status_type() + "','"
									+ usrkycbean.getPan_name() + "','" + usrkycbean.getPan_number() + "','" + usrkycbean.getPan_dob()
									+ "','" + usrkycbean.getPan_img_path() + "','" + usrkycbean.getPan_doc_type() + "','"
									+ usrkycbean.getPan_status() + "','" + usrkycbean.getPan_verification_status_flag() + "','"
									+ usrkycbean.getPan_verification_message() + "','" + usrkycbean.getUser_overalStatus() + "','"
									+ access_token + "',now())";
							stmt.executeUpdate(pan_data);

							String bank_data = "INSERT INTO kyc_bank_detail(user_id,customer_id,merchant_id,user_status_type,user_account_no,user_ifsc_code,user_account_holder_name,user_mobile_no,bank_account_status,bank_image_path,bank_name,bank_branch_name,bank_document_type,aml_status,access_token,insert_time) "
									+ "VALUES (" + usrkycbean.getUser_id() + ",'" + usrkycbean.getCustomer_id() + "', '"
									+ usrkycbean.getMerchant_id() + "','" + usrkycbean.getUser_status_type() + "','"
									+ usrkycbean.getBank_account_number() + "','" + usrkycbean.getBank_ifsc_code() + "','"
									+ usrkycbean.getBank_account_holder_name() + "','" + usrkycbean.getBank_mobile_no() + "','"
									+ usrkycbean.getBank_account_status() + "','" + usrkycbean.getBank_img_path() + "','"
									+ usrkycbean.getBank_name() + "','" + usrkycbean.getBank_branch_name() + "','"
									+ usrkycbean.getBank_doc_type() + "','" + usrkycbean.getUser_amlStatus() + "','" + access_token
									+ "',now())";
							stmt.executeUpdate(bank_data);
						}


					if (usrkycbean.getUser_status_type().equals("pending")) 
					{
						String update_user_kyc_flag = "update users set kyc='" + "pending" + "' where id=" + usrkycbean.getUser_id();
						stmt.executeUpdate(update_user_kyc_flag);

				 	} 

			}

			catch (Exception e) {
				e.printStackTrace();
					
				} 
			finally {
				rs.close();
				stmt.close();
				DBConnection.closeConnection(conn);
			}
	}

public String checkAutoLoginUrl(String exchange_id) throws SQLException {
		// TODO Auto-generated method stub
		String signzyLoginUrl=null;
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;


		try {

			/*Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection("jdbc:postgresql://18.222.97.182:5432/exchange", "postgres", "root");
			*/
			conn=DBConnection.getConnectionString();
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT * FROM kyc_user_credential where user_id="+exchange_id);

			while (rs.next()) {
				signzyLoginUrl=rs.getString("auto_login_url");
			}


		} catch (Exception e) {
			e.printStackTrace();
			
		} finally {
			rs.close();
			stmt.close();
			DBConnection.closeConnection(conn);
		}

		return signzyLoginUrl;
	}


public Map<String, String> getCustomerCredential(String exchange_id) throws SQLException {
		// TODO Auto-generated method stub
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		Map<String, String> kycCredential = null;
		try {

			/*Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection("jdbc:postgresql://18.222.97.182:5432/exchange", "postgres", "root");
			*/
			conn=DBConnection.getConnectionString();
			stmt = conn.createStatement();
			rs = stmt.executeQuery("select access_token,customer_id,merchant_id,user_email from kyc_user_credential where user_id="
					+ exchange_id);

			while (rs.next()) {
				kycCredential=new HashMap<String,String>();
				kycCredential.put("customerId", rs.getString("customer_id"));
				kycCredential.put("merchantId", rs.getString("merchant_id"));
				kycCredential.put("accessToken", rs.getString("access_token"));
				kycCredential.put("userEmail", rs.getString("user_email"));
				
			}

		} catch (Exception e) {
			e.printStackTrace();
			
		} finally {
			rs.close();
			stmt.close();
			DBConnection.closeConnection(conn);
		}
		return kycCredential;

	}




}
