import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;

public class RequestSender {
	static String[] keyDB = new String[10];

	public static void send_request(String data, URL url, String type) {

		try {
			if (type == "GET") {
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod(type);
				connection.setRequestProperty("Authorization",
						"Basic " + Base64.getEncoder().encodeToString("admin:admin".getBytes()));
				connection.setUseCaches(false);
//				System.out.println("sfadfs");
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
				} else {
					System.out.println("Connect Failed.");
				}
				for (int i = 0; i < keyDB.length; i++) {
					if (keyDB[i] != null)
						ResourceCreater.createInstances(keyDB[i]);
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
