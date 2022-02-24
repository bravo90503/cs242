package edu.ucr.cs242;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

import org.junit.jupiter.api.Test;

public class UnitTest {

	@Test
	public void readFileLineByLine() {
		URL resource = Thread.currentThread().getContextClassLoader().getResource(".");
		try {
			File file = new File(resource.getPath() + "part-r-00000"); // creates a new file instance
			FileReader fr = new FileReader(file); // reads the file
			BufferedReader br = new BufferedReader(fr); // creates a buffering character input stream
			StringBuffer sb = new StringBuffer(); // constructs a string buffer with no characters
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line); // appends line to string buffer
				sb.append("\n"); // line feed
			}
			fr.close(); // closes the stream and release the resources
			System.out.println("Contents of File: ");
			System.out.println(sb.toString()); // returns a string that textually represents the object
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void parseJsonPortion() {
		URL resource = Thread.currentThread().getContextClassLoader().getResource(".");
		try {
			File file = new File(resource.getPath() + "part-r-00000"); // creates a new file instance
			FileReader fr = new FileReader(file); // reads the file
			BufferedReader br = new BufferedReader(fr); // creates a buffering character input stream
			StringBuffer sb = new StringBuffer(); // constructs a string buffer with no characters
			String line;
			while ((line = br.readLine()) != null) {
				int beginIndex = line.indexOf("{");
				sb.append(line.substring(beginIndex, line.length())); // appends line to string buffer
				sb.append("\n"); // line feed
			}
			fr.close(); // closes the stream and release the resources
			System.out.println("Json Contents of File: ");
			System.out.println(sb.toString()); // returns a string that textually represents the object
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void parseKeyPortion() {
		URL resource = Thread.currentThread().getContextClassLoader().getResource(".");
		try {
			File file = new File(resource.getPath() + "part-r-00000"); // creates a new file instance
			FileReader fr = new FileReader(file); // reads the file
			BufferedReader br = new BufferedReader(fr); // creates a buffering character input stream
			StringBuffer sb = new StringBuffer(); // constructs a string buffer with no characters
			String line;
			while ((line = br.readLine()) != null) {
				int beginIndex = line.indexOf("{");
				sb.append(line.substring(0, beginIndex).trim()); // appends line to string buffer
				sb.append("\n"); // line feed
			}
			fr.close(); // closes the stream and release the resources
			System.out.println("Json Contents of File: ");
			System.out.println(sb.toString()); // returns a string that textually represents the object
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
