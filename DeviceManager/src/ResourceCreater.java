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

public class ResourceCreater {
	static String containerName = "Key";
	static String resourceName = "Device-Manager";

	public static void CreateAE() throws MalformedURLException {

		String type = "application/xml;ty=2";
		URL url = new URL("http://127.0.0.1:8080/~/in-cse");
		Namespace m2m = Namespace.getNamespace("m2m", "http://www.onem2m.org/xml/protocols");
		Document document = new Document();
		Element ae = new Element("ae", m2m);
		document.setRootElement(ae);
		Element api = new Element("api");
		Element lbl = new Element("lbl");
		Element rr = new Element("rr");
		Element mni = new Element("mni");
//		Element poa = new Element("poa");
		ae.addNamespaceDeclaration(m2m);
		ae.setAttribute("rn", resourceName);
		mni.addContent("4");
		api.addContent("app-sensor");
		lbl.addContent("Type/sensor Category/temperature Location/home");
		rr.addContent("false");
//		poa.addContent("127.0.0.1:8181");
		ae.addContent(api);
		ae.addContent(lbl);
		ae.addContent(rr);
		ae.addContent(mni);
//		ae.addContent(poa);
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		send_request(outputter.outputString(ae), url, type);
	}

	public static void createContainer() throws MalformedURLException {

		String type = "application/vnd.onem2m-res+xml; ty=3";
		URL url = new URL("http://127.0.0.1:8080/~/in-cse/in-name/" + resourceName);
		Namespace m2m = Namespace.getNamespace("m2m", "http://www.onem2m.org/xml/protocols");
		Document document = new Document();
		Element container = new Element("cnt", m2m);
		Element mni = new Element("mni").addContent("4");
		document.setRootElement(container);
		container.addNamespaceDeclaration(m2m);
		container.setAttribute("rn", containerName);
		container.addContent(mni);
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		System.out.println(outputter.outputString(container));
		send_request(outputter.outputString(container), url, type);
	}

	public static void createInstances(String key) throws MalformedURLException {
		String type = "application/xml;ty=4";
		URL url = new URL("http://127.0.0.1:8080/~/in-cse/in-name/" + resourceName + "/" + containerName);
		Namespace m2m = Namespace.getNamespace("m2m", "http://www.onem2m.org/xml/protocols");
		Document document = new Document();
		Element cin = new Element("cin", m2m);
		document.setRootElement(cin);
		cin.addNamespaceDeclaration(m2m);

		Element cnf = new Element("cnf");
		Element con = new Element("con");

		cnf.addContent("text/plain:0");
		con.addContent("OFF");
		cin.addContent(cnf);
		cin.addContent(con);
		cin.setAttribute("rn", key);
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());

		send_request(outputter.outputString(cin), url, type);
	}

	public static void createSubscribption(String deviceName) throws MalformedURLException {
		String type = "application/xml;ty=23";
		URL url = new URL("http://127.0.0.1:8282/~/mn-cse/mn-name/Device/Key4");
		Namespace m2m = Namespace.getNamespace("m2m", "http://www.onem2m.org/xml/protocols");
		Document document = new Document();
		Element sub = new Element("sub", m2m);
		Element nu = new Element("nu");
		Element nct = new Element("nct");
		nu.addContent("http://localhost:9988/notify");
		nct.addContent("2");
		sub.addContent(nu);
		sub.addContent(nct);
		document.setRootElement(sub);
		sub.addNamespaceDeclaration(m2m);
	
		sub.setAttribute("rn", "DEVICE_MANAGER");
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		
		send_request(outputter.outputString(sub), url, type);
	}

	public static void send_request(String data, URL url, String type) {
		try {

			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestProperty("Content-Type", type);
			connection.setRequestProperty("Authorization",
					"Basic " + Base64.getEncoder().encodeToString("admin:admin".getBytes()));
			connection.setUseCaches(false);
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
			System.out.println(data);
			outputStreamWriter.write(data);
			outputStreamWriter.flush();
			outputStreamWriter.close();
			connection.connect();

//			System.out.println(connection.getContent());

			System.out.println(connection.getResponseCode());

			if (connection.getResponseCode() == 201) { // 201 ��귽���\�Q�Ы�
				System.out.println("Connect Success.");
				InputStream inputStream = connection.getInputStream();
				String reString = null;
				try {
					byte[] data1 = new byte[inputStream.available()];
					inputStream.read(data1);
					reString = new String(data1);
//					System.out.println(reString);
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			} else {
				System.out.println("Connect Failed.");
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