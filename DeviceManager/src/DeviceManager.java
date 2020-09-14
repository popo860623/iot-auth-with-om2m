import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.Random;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
public class DeviceManager {

	public final static int DEFAULT_PORT = 5030;
	public static String message_to_send;
	public static String Sessionkey1;
	public static String SessionKey2;
	public static String Sessionkey_com;
	public static int counter = 0;
	public static long q[] = new long[7];

	private static long transDec2(String in) {
		try {
			long out = Long.valueOf(in, 16).intValue();
			return out;
		} catch (Exception e) {
			return Long.valueOf(0);
		}
	}

	public static void getkeyDB() {

		String temp[] = new String[7];

		String[] keys = RequestSender.keyDB;
		for (int i = 0; i < keys.length; i++) {
			if (keys[i] != null)
				temp[i] = keys[i].substring(8, 12);
		}

		for (int j = 0; j < keys.length; j++) {
			if (keys[j] != null)
				q[j] = transDec2(temp[j]);
		}
		// 測試getkeyDB回傳(8,12)的substring
//		for(int i = 0;i<temp.length;i++) {
//			if(temp[i]!=null)
//				System.out.println("getkeyDB = " + temp[i]);
//		}
	}

	public static void connecting() {
		int port = DEFAULT_PORT;

		ServerSocket serverSocket = null;

		try {
			serverSocket = new ServerSocket(port);
			// DB的四把key
			int pw1 = (int) q[0];
			int pw2 = (int) q[1];
			int pw3 = (int) q[2];
			int pw4 = (int) q[3];

			int[] pw = { 0, pw1, pw2, pw3, pw4 };
			Socket newSock = null;
			Boolean stop = true;
			Boolean end = true;
			while (stop == true) {
				// System.out.println("Server TCP ready at the port: " + port + "..." );

				// Waiting for the connection with the client
				newSock = serverSocket.accept();
				// System.out.println("accept");
				while (end == true) {

					System.out.println("success connect");
					// 取得Device ID
					BufferedReader is = new BufferedReader(new InputStreamReader(newSock.getInputStream()));
					String deviceID = is.readLine();
					System.out.println("Received DeviceID: " + deviceID);
					// =================================================================================================

					// 讀取Device List
					FileReader fr = new FileReader("C:\\Users\\dcnlab\\git\\DeviceManager\\DeviceList.txt");
					BufferedReader br = new BufferedReader(fr);
					String s11;
					int flag0 = 0;
					System.out.println("User List:");
					while (br.ready()) {

						// System.out.println(br.readLine());
						s11 = br.readLine();
						System.out.print(s11 + ", ");
						flag0 = s11.compareTo(deviceID); // 若相同回傳0
						if (flag0 == 0) {
							System.out.println();
							System.out.println("Compared, DeviceID is in the User List.");
							break;
						}
					}

					fr.close();
					// =================================================================================================

					PrintWriter os = new PrintWriter(newSock.getOutputStream(), true);

					// flag=inputLine.compareTo(legaldeviceID);
					if (flag0 == 0) {
						System.out.println("User List : Legal Device.");
						Scanner in = new Scanner(System.in);
						Random ran = new Random();
						int challenge_int_r1 = ran.nextInt(100);// Server的r1來做challenge

						System.out.println("Random number for challenge: " + challenge_int_r1);
						int a1 = ran.nextInt(4) + 1;
						int a2 = ran.nextInt(4) + 1;

						String s = String.valueOf(a1) + " " + String.valueOf(a2);

						String sendMessage_M2 = s + " " + "(" + challenge_int_r1 + ")";
						System.out
								.println("Send a Challenge to Client,pair of number and challenge: " + sendMessage_M2);
						os.println(sendMessage_M2);
						// os.println(inputLine.toUpperCase());
						os.flush();

						// 接收M3的訊息======================================================================================================
						System.out.println("Receiving M3 Message..............");
						BufferedReader is1 = new BufferedReader(new InputStreamReader(newSock.getInputStream()));
						PrintWriter os1 = new PrintWriter(newSock.getOutputStream(), true); // 寫入M4的訊息
						String receivedFromDevice_M3 = is1.readLine();
						System.out.println("Received encrypted message: " + receivedFromDevice_M3);

						int key2 = pw[a1] | pw[a2]; // 為了解開M3使用k2加密，而k2原本就是游(a1,a2)產生，所以可以用key2來解密
						int key2_temp = key2;

						String output = "";
						// 要解M3 的 key
						// 轉二進位，長度需要16可以改while作法前面補0
						// 計算結果
						for (int n1 = 1; n1 <= 4; n1++)// 計算每四個字元要空白
						{
							for (int i1 = 1; i1 <= 4; i1++) {
								int remainder = key2_temp % 2;// 餘數
								key2_temp /= 2;
								output = String.valueOf(remainder) + output;
							}
						}

						final String secretKey = output;// 就是key2 用來解M3

						String decryptedString = AESM.decrypt(receivedFromDevice_M3, secretKey);
						// System.out.println("decyrpted message: "+decryptedString);
						// check
						String regex = "\\d*";
						Pattern p = Pattern.compile(regex);
						int n = 0;
						Matcher m = p.matcher(decryptedString);
						String[] y = new String[10];
						while (m.find()) {
							if (!"".equals(m.group())) {
								y[n] = m.group();
								n = n + 1;
							}
						}
						Sessionkey1 = y[1];
						System.out.println("Decrypted message from device : r1=" + y[0] + " t2=" + y[1] + " c2={" + y[2]
								+ "," + y[3] + "} r2= " + y[4]);
						int response_from_m3_int = Integer.valueOf(y[0]);
						if (response_from_m3_int == challenge_int_r1) {
							System.out.println("Correct challenge & response Server -> Device");
						} else {
							System.out.println("illegal device");
							break;
						}

						// M4 =============================================== M4

						// c2
						int b1 = Integer.valueOf(y[2]);
						int b2 = Integer.valueOf(y[3]);
						// Generate Server's k1
						int temp = pw[b1] | pw[b2];
						// k1 | k2 加密 M4
						int key_server = temp | key2;
						int quotient1 = 0;// 商數
						String output_server = " ";

						// 計算結果
						for (int n2 = 1; n2 <= 4; n2++)// 計算每四個字元要空白
						{
							for (int i2 = 1; i2 <= 4; i2++) {
								int remainder1 = key_server % 2;// 餘數
								quotient1 = key_server / 2;
								key_server = quotient1;
								output_server = remainder1 + output_server;
							}

							output_server = " " + output_server;// 印出空白
						}
						// secretKey1 = key1 | key2 用來加密M4
						final String secretKey1 = output_server;

						String Sessionkey = Integer.toString(ran.nextInt(1000));
						String originalString = y[4] + " " + Sessionkey;
						SessionKey2 = Sessionkey;
						// 此處只完成 M4的加密，尚未送到Client
						String encryptedString = AESM.encrypt(originalString, secretKey1);
						// String decryptedString1 = AESM.decrypt(encryptedString, secretKey1) ;
						System.out.println("Send to client(M4):r2=" + y[4] + " t1= " + SessionKey2);
//						System.out.println("Encyrpted message: " + encryptedString);
						// 此Global變數只為了讓M4送出訊息到Client，程式寫不好，可以再改得更漂亮
						message_to_send = encryptedString;

					} else {
						System.out.println("Illegal Device");
						break;
					}
					is.close();
					os.close();
					newSock.close();
					stop = false;
					end = false;
					break;
					/*
					 * if(inputLine.equals("END")) { System.out.println("ciclo uscita"); end =
					 * false; is.close(); os.close(); newSock.close(); }
					 * 
					 * if(inputLine.equals("STOP")) { os.println( "Server aborted!"); is.close();
					 * os.close(); newSock.close(); stop = false; end = false; }
					 * 
					 */
				}
			}

			// Thread th = new HandleConnectionThread(newSock);
			// th.start();

		} catch (IOException e) {
			System.err.println("Error2 " + e);
		}
	}

	// 此FUNCTION的功能只負責送M4的加密訊息到M3，有點多餘
	public static void connecting1() throws IOException {
		int port = DEFAULT_PORT + 1; // 5031

		ServerSocket serverSocket = null;

		try {
			serverSocket = new ServerSocket(port);
			Socket newSock = null;
			Boolean stop = true;
			Boolean end = true;
			while (stop == true) {
				// Waiting for the connection with the client
				newSock = serverSocket.accept();
				while (end == true) {
					BufferedReader is = new BufferedReader(new InputStreamReader(newSock.getInputStream()));
					PrintWriter os = new PrintWriter(newSock.getOutputStream(), true);
					System.out.println("Send Encrypted M4 to Client : " + message_to_send);
					os.println(message_to_send);
					os.flush();
					stop = false;
					end = false;
				}
			}
		}

		catch (IOException e) {
			System.err.println("Error2 " + e);
		}

	}

	public static void connect2() {
		int port = DEFAULT_PORT + 2;

		ServerSocket serverSocket = null;

		try {
			serverSocket = new ServerSocket(port);
			Socket newSock = null;
			Boolean stop = true;
			Boolean end = true;
			int temp_k = 0;
			while (stop == true) {
				// System.out.println("Server TCP ready at the port: " + port + "..." );

				// Waiting for the connection with the client
				newSock = serverSocket.accept();
				while (end == true) {
					// System.out.println("success connect");
					BufferedReader is = new BufferedReader(new InputStreamReader(newSock.getInputStream()));
					PrintWriter os = new PrintWriter(newSock.getOutputStream(), true);
					String inputLine = is.readLine();
					// System.out.println("Received message: " + inputLine);
					// String decrypted_message=AESM.decrypt(inputLine,Sessionkey_com);
					System.out.println("Receive encrypted message:" + inputLine);
					String decrypted_message = AESM.decrypt(inputLine, Sessionkey_com);
					System.out.println("Decrypted message:\n" + decrypted_message);
					String regex = "\\d+";
					Pattern p = Pattern.compile(regex);
					int n = 0;
					Matcher m = p.matcher(decrypted_message);
					String[] y;
					y = new String[100];
					while (m.find()) {
						if (!"".equals(m.group()))
//						 System.out.println("come here:" + m.group());
						{
							y[n] = m.group();
							System.out.println(y[n]);
							n = n + 1;
						}
					}
//					System.out.println("y[0]= "+y[0]);
					int flag;
					flag = decrypted_message.compareTo("Communication Over!");
					if (flag == 0) {
						stop = false;
						end = false;
						newSock.close();
						break;
					}
					int temp = Integer.valueOf(y[0]);
					if (temp_k == temp) {
						counter--;
					}
					temp_k = temp;
					temp = temp - 1;
					String flag_temp = Integer.toString(temp);
					String message = "ok" + " " + flag_temp;
					message = AESM.encrypt(message, Sessionkey_com);
					System.out.printf("Communication times left: %d", temp);
					System.out.printf("\n");
					os.println(message);
					os.flush();

				}
			}
		}

		catch (IOException e) {
			System.err.println("Error2 " + e);
		}

	}
	public static void notifyReceiver() {
		int port = 9988;
		try {
			ServerSocket serverSocket = new ServerSocket(port);
			Socket socket = null;
			socket = serverSocket.accept();
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String inputLine = reader.readLine();
			System.out.println("Receive notification message : " + inputLine);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public static void Web_Server() {
		System.out.println("*** Notification Listeing on Port 9988 ***");
		int server_port = 9988;
		Server server = new Server(server_port); 
		ServletContextHandler handler = new ServletContextHandler(server, "/");  //根目錄為/
		handler.addServlet(Notification.class, "/"); 
		try {
			server.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	// end main
	public static void main(String[] args) throws IOException {
		System.out.println("*** Creating AE ***");
		ResourceCreater.CreateAE();
		System.out.println("*** Creating Container ***");
		ResourceCreater.createContainer();
		System.out.println("*** Creating ContentInstances ***");
		URL url = null;
		try {
			url = new URL("http://127.0.0.1:8282/~/mn-cse/mn-name/KEY_MANAGER/DATA?fu=1");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		RequestSender.send_request("", url, "GET");
		// 接收Notify的Server		
		getkeyDB();
		connecting();
		Web_Server();
		connecting1();
		// System.out.println("sessionkey1: "+Sessionkey1+" Sessionkey2: "+SessionKey2);
		System.out.println("*** Authentication done ***");
		System.out.println("===============================");
		
		System.out.println("*** Creating Subscribption ***");
		ResourceCreater.createSubscribption("Device");
		
		int ses1 = Integer.valueOf(Sessionkey1);
		int ses2 = Integer.valueOf(SessionKey2);
		// System.out.println("ses1: "+Sessionkey1+" ses2: "+SessionKey2);
		int com_sessionkey = ses1 | ses2;
		int quotient1 = 0;// 商數
		String output_server = " ";

		// 計算結果
		for (int n2 = 1; n2 <= 4; n2++)// 計算每四個字元要空白
		{
			for (int i2 = 1; i2 <= 4; i2++) {
				int remainder1 = com_sessionkey % 2;// 餘數
				quotient1 = com_sessionkey / 2;
				com_sessionkey = quotient1;
				output_server = remainder1 + output_server;
			}

			output_server = " " + output_server;// 印出空白
		}
		Sessionkey_com = output_server;
//		System.out.println("sessionkey_com = " + Sessionkey_com);
		int n;
		System.out.println("*** Starting Communication Phase ***");
		connect2();
		System.out.println("Session end");
		System.out.println("===============================");
		
		if (counter == 0) {
			int port = DEFAULT_PORT + 5;

			ServerSocket serverSocket = null;

			try {
				serverSocket = new ServerSocket(port);
				Socket newSock = null;
				Boolean stop = true;
				Boolean end = true;
				while (stop == true) {
					// System.out.println("Server TCP ready at the port: " + port + "..." );
					System.out.println("Waiting for test message...");
					// Waiting for the connection with the client
					newSock = serverSocket.accept();
					while (end == true) {
						// System.out.println("success connect");
						BufferedReader is = new BufferedReader(new InputStreamReader(newSock.getInputStream()));
						PrintWriter os = new PrintWriter(newSock.getOutputStream(), true);
						// String inputLine = is.readLine();
						System.out.println("Received message: Environment test");

						os.println("Environment is ok");
						os.flush();
						stop = false;
						end = false;
						newSock.close();

					}
				}
			}

			catch (IOException e) {
				System.err.println("Error2 " + e);
			}

		} else {
			System.out.println("ready for next authentication");
		}
	}

}