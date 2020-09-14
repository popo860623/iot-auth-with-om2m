package SCC;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import SCC.ResourceCreater;

public class RequestSender {
	static String[] keyDB = new String[10];

//	public static void main(String[] args) {
//		URL url = null;
//		try {
//			url = new URL("http://127.0.0.1:8282/~/mn-cse/mn-name/KEY_MANAGER/DATA?fu=1");
//		} catch (MalformedURLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		send_request("", url, "GET");
//	}

	public static void send_request(String data, URL url, String type) {

		try {
			// Get IPE KEY_MANAGER's KEY
			if (type == "GET") {
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod(type);
				connection.setRequestProperty("Authorization",
						"Basic " + Base64.getEncoder().encodeToString("admin:admin".getBytes()));
				connection.setUseCaches(false);
				System.out.println("Respnose Code = " + connection.getResponseCode());
				if (connection.getResponseCode() == 200) {
					System.out.println("Connect Success.");
					InputStream inputStream = connection.getInputStream();
					String reString = null;
					try {
						byte[] data1 = new byte[inputStream.available()];
						inputStream.read(data1);
						reString = new String(data1);
//						System.out.println(reString);
						int index = 0;
						int in = 0;
						while (reString.indexOf("DATA/", index) >= 0) {
							index = reString.indexOf("DATA/", index);
							keyDB[in] = reString.substring(index + 5, index + 21);
//							System.out.println(tempStrings[in]);
							in++;
							index += 21;
						}
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}
					for (int i = 0; i < keyDB.length; i++) {
						if (keyDB[i] != null)
							ResourceCreater.createInstances(keyDB[i]);
					}
				} else {
					System.out.println("Connect Failed.");
				}
			}
			// POST Request to KEY_MANAGER to change key
			if (type == "POST") {
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				
				connection.setRequestMethod("POST");
				connection.setDoOutput(true);
				connection.setDoInput(true);
				connection.setRequestProperty("Content-Type", type);
				connection.setRequestProperty("Authorization",
						"Basic " + Base64.getEncoder().encodeToString("admin:admin".getBytes()));
				connection.setUseCaches(false);
				OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
				
				outputStreamWriter.write(data);
				outputStreamWriter.flush();
				outputStreamWriter.close();
				connection.connect();
				System.out.println("Response Code = " + connection.getResponseCode());
				if (connection.getResponseCode() < 201) {
					System.out.println("Connect Success.");
				} else {
					System.out.println("Connect Failed.");
				}
			}

		} catch (MalformedURLException e) {
			// TODO: handle exception
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO: handle exception
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			// TODO: handle exception
		}
	}
}
