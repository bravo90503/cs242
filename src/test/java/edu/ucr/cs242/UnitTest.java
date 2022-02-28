package edu.ucr.cs242;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.ucr.cs242.repo.model.*;

public class UnitTest {

	private static ObjectMapper mapper = new ObjectMapper();

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

	@Test
	public void KeywordToMongo() {
		URL resource = Thread.currentThread().getContextClassLoader().getResource(".");
		try {
			File file = new File(resource.getPath() + "part-r-00000"); // creates a new file instance
			FileReader fr = new FileReader(file); // reads the file
			BufferedReader br = new BufferedReader(fr); // creates a buffering character input stream
			StringBuffer keysb = new StringBuffer(); // constructs a string buffer with no characters
			StringBuffer documents = new StringBuffer(); // constructs a string buffer with no characters
			String line;
			while ((line = br.readLine()) != null) {

				int beginIndex = line.indexOf("{");
				String key = line.substring(0, beginIndex).trim();
				if (StringUtils.isNotBlank(key) && key.equals("information")) {
					keysb.append(key); // appends line to string buffer
					documents.append(line.substring(beginIndex, line.length()));

					break;
				}
			}
			fr.close(); // closes the stream and release the resources
			System.out.println("Json Contents of File: ");
			System.out.println(keysb.toString()); // returns a string that textually represents the object
			System.out.println(documents.toString()); // returns a string that textually represents the object

			Keyword keyword = mapper.readValue(documents.toString(), Keyword.class);
			keyword.setKey(keysb.toString());
			Double maxScore = null;
			Assert.assertTrue(String.format("expected 1001, got %d", keyword.getDocuments().size()), keyword.getDocuments().size() == 1001);
			for (Document document : keyword.getDocuments()) {
				System.out.println("docId="+document.getDocId() + ", score=" +document.getScore());
				if (maxScore == null) {
					maxScore = new Double(document.getScore());
					//continue;
				}
				
				Assert.assertTrue(maxScore >= document.getScore());
			}

		} catch (

		IOException e) {
			e.printStackTrace();
		}
	}
}
