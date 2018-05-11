package service;
import org.json.JSONObject;
import org.json.JSONArray;      
import org.apache.http.util.EntityUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;


public class CryptoPanicService{
	public String getNewsOfCurrency(){
		try{
	    HttpClient client = HttpClientBuilder.create().build();
		String url = "https://cryptopanic.com/api/posts/?auth_token=cbe7ba9804960e59c097287ab8be56b183450a55";
		HttpGet request = new HttpGet(url);
		request.addHeader("content-type", "application/json");
		HttpResponse response = client.execute(request);
		HttpEntity entity = response.getEntity();
		String responseString = EntityUtils.toString(entity, "UTF-8");
		JSONObject jsonObj = new JSONObject(responseString);
		JSONArray jsonArray = jsonObj.getJSONArray("results");
		JSONArray  returnJsonArray = new JSONArray();
		if(jsonArray!=null){
			for(int i = 0;i<jsonArray.length();i++){
				returnJsonArray.put(jsonArray.getJSONObject(i));
				if(i==4) break;
			}
		}
		return returnJsonArray.toString();
	}
	catch(Exception e){
		e.printStackTrace();
		return null;
	}
	}
}