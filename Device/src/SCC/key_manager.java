package SCC;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.*;
import java.util.Scanner;
import java.lang.String;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.io.FileWriter;
import java.math.*;
import java.net.URL;

public class key_manager {
	public static void change() throws IOException, NoSuchAlgorithmException {
		System.out.println("Changing Key....");
		URL url = new URL("http://127.0.0.1:8282/~/mn-cse/mn-name/KEY_MANAGER");
		RequestSender.send_request("Create", url, "POST");
	}
}