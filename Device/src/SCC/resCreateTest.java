package SCC;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class resCreateTest {

	public static void main(String[] args) throws MalformedURLException {
		// TODO Auto-generated method stub
//		String containerName = "Key2";
//		String type = "application/vnd.onem2m-res+xml; ty=3";
//		Namespace m2m = Namespace.getNamespace("m2m", "http://www.onem2m.org/xml/protocols");
//		Document document = new Document();
//		
//		Element container = new Element("cnt", m2m);
//		document.setRootElement(container);
//		
//		container.addNamespaceDeclaration(m2m);
//		container.setAttribute("rn", containerName);
//		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
//		send_request(outputter.outputString(container),type);
		createInstances();
	}
	public static void createInstances() throws MalformedURLException {
		String type = "application/vnd.onem2m-res+json; ty=4";
		URL url = new URL("http://127.0.0.1:8282/~/mn-cse/mn-name/Device/Key4");
		Namespace m2m = Namespace.getNamespace("m2m", "http://www.onem2m.org/xml/protocols");
		Document document = new Document();
		Element instancElement = new Element("cin", m2m);
		document.setRootElement(instancElement);
		instancElement.addNamespaceDeclaration(m2m);
		Element cnf = new Element("cnf");
		Element con = new Element("con");
		cnf.addContent("text/plain:0");
		con.addContent("OFF");
//		instancElement.setAttribute("rn", "key_test");
		
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		System.out.println(outputter.outputString(instancElement));
		send_request(outputter.outputString(instancElement), url, type);
	}
	public static void send_request(String data,URL url, String type) {
		try {
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		
		connection.setRequestMethod("POST");
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setRequestProperty("Content-Type", type);
		connection.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString("admin:admin".getBytes()));
		connection.setUseCaches(false);		
		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());		
//		System.out.println(data);
		outputStreamWriter.write(data);
		outputStreamWriter.flush();
		outputStreamWriter.close();
		connection.connect();		
		
		System.out.println(connection.getResponseCode());
		
		if(connection.getResponseCode() == 201) { //201 ��귽���\�Q�Ы�
			System.out.println("Connect Success.");
			InputStream inputStream = connection.getInputStream();
			String reString = null;
			try {
				byte[] data1 = new byte[inputStream.available()];
				inputStream.read(data1);
				reString=new String(data1);
				System.out.println(reString);
			}catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		else {
			System.out.println("Connect Failed.");
		}
		
		} catch (MalformedURLException e) {
			// TODO: handle exception
			e.printStackTrace();
		}catch (UnsupportedEncodingException e) {
			// TODO: handle exception
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
			// TODO: handle exception
		}
	}
}
