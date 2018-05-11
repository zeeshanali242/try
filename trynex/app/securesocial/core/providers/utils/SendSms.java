package securesocial.core.providers.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;
	public class SendSms { 
		public static void sendSms(String number , String message) {
			try {

				//String message = "You need an OTP to login. The OTP is "+otp+". This OTP will be valid for 5 minutes.NEVER SHARE IT WITH ANYONE.";
				// Construct data
				//final String USER_AGENT = "Mozilla/5.0";
				//https://www.smsgatewayhub.com/api/mt/SendSMS?APIKey=CqR5K6XrM0qW6ZOTZg0khg&senderid=TESTIN&channel=2&DCS=0&flashsms=1&number=91989xxxxxxx&text=test message&route=13
				String messages = URLEncoder.encode(message, "UTF-8");
				StringBuffer numbers = new StringBuffer("91").append(number);
				StringBuffer url = new StringBuffer("https://www.smsgatewayhub.com/api/mt/SendSMS?APIKey=CqR5K6XrM0qW6ZOTZg0khg&senderid=TRYNEX&channel=2&DCS=0&flashsms=0&route=13");
				
				//https://www.smsgatewayhub.com/api/mt/SendSMS?APIKey=bW4Z5f93g0awQngX6o62UQ&senderid=TESTIN&channel=2&DCS=0&flashsms=0&number=917503288127&text=testmessage&route=13
				url.append("&number=").append(numbers);
				url.append("&text=").append(messages);
				System.out.println("Main URL is ::" + url);
				String smsurl = new String(url);
				URL obj = new URL(smsurl);
				HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

				//add request header
				con.setRequestMethod("POST");
				//con.setRequestProperty("User-Agent", USER_AGENT);
				con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

				//String urlParameters = "sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";

				// Send post request
				con.setDoOutput(true);
				DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			//	wr.writeBytes(urlParameters);
				wr.flush();
				wr.close();

				int responseCode = con.getResponseCode();
				System.out.println("\nSending 'POST' request to URL : " + url);
			//	System.out.println("Post parameters : " + urlParameters);
				System.out.println("Response Code : " + responseCode);

				BufferedReader in = new BufferedReader(
				        new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();

				//print result
				System.out.println(response.toString());

			} catch (Exception e) {
				System.out.println("Error SMS "+e);
			}
		}
		
	}