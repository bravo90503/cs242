package edu.ucr.cs242;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.List;
import java.util.PriorityQueue;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ucr.cs242.repo.model.Document;
import edu.ucr.cs242.repo.model.Keyword;
import edu.ucr.cs242.service.HadoopSearch;
import edu.ucr.cs242.web.dto.DocumentDto;

public class UnitTest {
	HadoopSearch hadoopSearchService = new HadoopSearch();

	private static ObjectMapper mapper = new ObjectMapper();

	@Test
	public void KeywordToMongo() {
		URL resource = Thread.currentThread().getContextClassLoader().getResource(".");
		try {
			FileInputStream stream = new FileInputStream(resource.getPath() + "part-r-00000.gz"); // creates a new file
			// instance
			GZIPInputStream gzipstream = new GZIPInputStream(stream);
			Reader decoder = new InputStreamReader(gzipstream, "UTF-8");
			BufferedReader buffered = new BufferedReader(decoder);
			StringBuffer keysb = new StringBuffer(); // constructs a string buffer with no characters
			StringBuffer documents = new StringBuffer(); // constructs a string buffer with no characters
			String line;
			while ((line = buffered.readLine()) != null) {

				int beginIndex = line.indexOf("{");
				String key = line.substring(0, beginIndex).trim();
				if (StringUtils.isNotBlank(key) && key.equals("information")) {
					keysb.append(key); // appends line to string buffer
					documents.append(line.substring(beginIndex, line.length()));

					break;
				}
			}
			buffered.close(); // closes the stream and release the resources
			System.out.println("Json Contents of File: ");
			System.out.println(keysb.toString()); // returns a string that textually represents the object
			System.out.println(documents.toString()); // returns a string that textually represents the object

			Keyword keyword = mapper.readValue(documents.toString(), Keyword.class);
			keyword.setKey(keysb.toString());
			Double maxScore = null;
			Assert.assertTrue(String.format("expected 1001, got %d", keyword.getDocuments().size()),
					keyword.getDocuments().size() == 1001);
			for (Document document : keyword.getDocuments()) {
				System.out.println("docId=" + document.getDocId() + ", score=" + document.getScore());
				if (maxScore == null) {
					maxScore = new Double(document.getScore());
					// continue;
				}

				Assert.assertTrue(maxScore >= document.getScore());
			}

		} catch (

		IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void KeywordScoring() {
		try {
			String[] tokens = new String[] { "information", "retrieval" };
			// keywords
			List<Keyword> keywords = hadoopSearchService.getKeywords(tokens);
			// documents to return
			int howMany = 320;
			// sorted by highest score
			PriorityQueue<DocumentDto> topDocs = new PriorityQueue<>();

			int iteration = hadoopSearchService.findTopDocuments(keywords, howMany, topDocs);
			int index = 0;
			DocumentDto poll;
			while ((poll = topDocs.poll()) != null) {
				System.out.println("#" + ++index + " : score= " + poll.getMin() + " : id= " + poll.getId());
			}

			System.out.println("iterations=" + iteration);

		} catch (

		IOException e) {
			e.printStackTrace();
		}
	}

}
