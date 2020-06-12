package timer.timer;
import org.apache.commons.io.FileUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

public class HttpClient {
	private final CloseableHttpClient httpClient = HttpClients.createDefault();
	private void close() throws IOException {
		httpClient.close();
	}

	public void sendGet() throws Exception {
		String endPointB2B = System.getenv("endPointB2B");
		String cpf = System.getenv("cpf");
		String processNumber = System.getenv("processNumber");
		JSONObject jsonObj = null; 
		HttpGet request = new HttpGet(endPointB2B+cpf+processNumber);

		// add request headers
		request.addHeader("Authorization", "Bearer "+ getToken());
		request.addHeader(HttpHeaders.USER_AGENT, "Douglasbot");

		try (CloseableHttpResponse response = httpClient.execute(request)) {

			// Get HttpResponse Status
			System.out.println(response.getStatusLine().toString());

			HttpEntity entity = response.getEntity();
			Header headers = entity.getContentType();
			System.out.println(headers);

			if (entity != null) {
				// return it as a String
				String result = EntityUtils.toString(entity);
				System.out.println(result);
				//jsonObj = new JSONObject(result);
				//System.out.println(jsonObj.getJSONObject("retorno").getJSONObject("arquivos").getString("link"));
				//String link = jsonObj.getJSONObject("retorno").getJSONObject("arquivos").getString("link");
				//doDownload(link);
				//System.out.println(jsonObj.getString("link"));      
				readJson(result);
			}


		}

	}

	private static void doDownload(String link,String fileName) throws IOException {
		URL url = new URL(link);
		File file = new File("C:\\RETORNO\\"+fileName);
		FileUtils.copyURLToFile(url, file);
	}

	private static String getToken() throws Exception {
		JSONObject jsonObj = null;   
		DataOutputStream wr = null;  
		String endPointToken = System.getenv("endPointToken");
		String parameters = System.getenv("urlTokenParameters");
		String authServerUri = endPointToken;   
		String urlParameters = parameters;   
		byte[] postData       = urlParameters.getBytes( StandardCharsets.UTF_8 );   
		int postDataLength = postData.length;   
		URL    url            = new URL( authServerUri);   
		HttpURLConnection conn= (HttpURLConnection) url.openConnection();           
		conn.setDoOutput( true );   
		conn.setInstanceFollowRedirects( false );   
		conn.setRequestMethod( "POST" );   
		conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded");   
		conn.setRequestProperty("charset", "utf-8");   
		conn.setRequestProperty("Content-Length", Integer.toString(postDataLength ));   
		conn.setUseCaches( false );   
		int responseCode = 0;   
		try{    
			wr = new DataOutputStream( conn.getOutputStream());    
			wr.write( postData );    
			responseCode = conn.getResponseCode();    
			BufferedReader in = new BufferedReader(      
					new InputStreamReader(conn.getInputStream()));    
			String inputLine;    
			StringBuffer response = new StringBuffer();     
			while ((inputLine = in.readLine()) != null) {     
				response.append(inputLine);   
			}    
			in.close();    
			jsonObj = new JSONObject(response.toString());    
			//log.log("<token>"+ jsonObj.getString("access_token")+"</token>");       

			//wfc.addWFContent("sso_token",res.toString());    
		}catch(Exception ex) {    
			System.out.println("Erro getAccessToken " + ex.getMessage());   
			throw new Exception("Erro getAccessToken " + ex.getMessage());
		}finally{    
			conn.disconnect();   
		}   
		return  jsonObj.getString("access_token");  
	}

	public static void readJson(String resultJson) throws IOException {
		/*
		 * StringBuilder json=new StringBuilder("{\r\n" + "        \r\n" +
		 * "        \"retorno\" : { \"arquivos\" :[{\r\n" + "                \r\n" +
		 * "        \"nome-arquivo\" : \"DOUGLAS_REZENDE.pdf\",\r\n" +
		 * "        \"id-arquivo\" : \"721861\",\r\n" +
		 * "        \"link\" : \"https://b2bapl.des.corerj.caixa/DOWNLOAD_FILE_MOBILEAPP?id-arquivo=721861&cpf=88825014478&tipo-arquivo=&numero-processo=016\"\r\n"
		 * + "    },{\r\n" + "                \r\n" +
		 * "        \"nome-arquivo\" : \"DOUGLAS_REZENDE.pdf\",\r\n" +
		 * "        \"id-arquivo\" : \"722265\",\r\n" +
		 * "        \"link\" : \"https://b2bapl.des.corerj.caixa/DOWNLOAD_FILE_MOBILEAPP?id-arquivo=722265&cpf=88825014478&tipo-arquivo=&numero-processo=016\"\r\n"
		 * + "    }] }}");
		 */
		//StringBuilder jsonStringBuilder = new StringBuilder(resultJson);
		JSONObject jsonObj = null;
		StringBuilder jsonStringBuilder = new StringBuilder(resultJson);
		jsonObj = new JSONObject(jsonStringBuilder.toString());
		try{
			if(resultJson.contains("[")) {
				System.out.println("YES");
				JSONArray jsonArray = jsonObj.getJSONObject("retorno").getJSONArray("arquivos");
				int i = 0; 
				while (i < jsonArray.length()) {
					jsonObj  = jsonArray.getJSONObject(i);
					if(createOrUpdateControlDowloadFile(jsonObj.getString("id-arquivo"))) 
						doDownload(jsonObj.getString("link"),jsonObj.getString("nome-arquivo")); 

					i+=1; 
				}
			}else{
				System.out.println("NO");
				jsonObj = new JSONObject(resultJson);
				doDownload(jsonObj.getJSONObject("retorno").getJSONObject("arquivos").getString("link"),
						jsonObj.getJSONObject("retorno").getJSONObject("arquivos").getString("nome-arquivo"));
				createOrUpdateControlDowloadFile(jsonObj.getJSONObject("retorno").getJSONObject("arquivos").getString("id-arquivo"));			
			}
		}catch(JSONException e){
			e.printStackTrace();
		}
	}

	public static boolean createOrUpdateControlDowloadFile(String idFile) throws IOException {
		FileWriter writer = null;
		boolean alreadyDownloaded = false;
		try {
			File file = new File("C:\\RETORNO\\control-download-file.properties");
			if(!file.exists()){
				new File("C:\\RETORNO\\control-download-file.properties").createNewFile();
				writer = new FileWriter("C:\\RETORNO\\control-download-file.properties");
				writer.write("\r\n"+idFile +"="+ idFile+"\r\n");
				alreadyDownloaded = true;
			}else {
				if(getParameter(idFile) == null) {
					writer = new FileWriter("C:\\RETORNO\\control-download-file.properties",true);
					writer.write("\r\n"+idFile +"="+ idFile+"\r\n");  
					alreadyDownloaded = true;
				}
			}
		}catch(Exception ex){
			ex.getMessage();
		}finally {
			if(writer!=null){
				writer.close();	
			}
		}

		return alreadyDownloaded;
		//return (getParameter(idFile) == null) ? true :false;
	}


	public static String getParameter(String key) throws FileNotFoundException, IOException {
		Properties properties = new Properties();
		properties.load(new FileInputStream("C:\\\\RETORNO\\\\control-download-file.properties"));
		return properties.getProperty(key);
	}
}
