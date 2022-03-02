package edu.ucr.cs242;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ucr.cs242.repo.model.Document;
import edu.ucr.cs242.repo.model.Keyword;
import edu.ucr.cs242.web.dto.DocumentDto;

public class UnitTest {

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
		PriorityQueue<edu.ucr.cs242.web.dto.DocumentDto> finalScores = new PriorityQueue<>();

		URL resource = Thread.currentThread().getContextClassLoader().getResource(".");
		try {
			FileInputStream stream = new FileInputStream(resource.getPath() + "part-r-00000.gz"); // creates a new file
			// instance
			GZIPInputStream gzipstream = new GZIPInputStream(stream);
			Reader decoder = new InputStreamReader(gzipstream, "UTF-8");
			BufferedReader buffered = new BufferedReader(decoder);
			StringBuffer information = new StringBuffer(); // constructs a string buffer with no characters
			StringBuffer informations = new StringBuffer(); // constructs a string buffer with no characters
			StringBuffer retrieval = new StringBuffer(); // constructs a string buffer with no characters
			StringBuffer retrievals = new StringBuffer(); // constructs a string buffer with no characters
			String line;
			while ((line = buffered.readLine()) != null) {

				int beginIndex = line.indexOf("{");
				String key = line.substring(0, beginIndex).trim();
				if (StringUtils.isNotBlank(key) && key.equals("information")) {
					information.append(key); // appends line to string buffer
					informations.append(line.substring(beginIndex, line.length()));
				}
				if (StringUtils.isNotBlank(key) && key.equals("retrieval")) {
					retrieval.append(key); // appends line to string buffer
					retrievals.append(line.substring(beginIndex, line.length()));
				}
			}
			buffered.close(); // closes the stream and release the resources

			Keyword informationKeyword = mapper.readValue(informations.toString(), Keyword.class);
			informationKeyword.setKey(information.toString());
			Double maxScore = null;
			Assert.assertTrue(String.format("expected 1001, got %d", informationKeyword.getDocuments().size()),
					informationKeyword.getDocuments().size() == 1001);
			for (Document document : informationKeyword.getDocuments()) {
				// System.out.println("docId=" + document.getDocId() + ", score=" +
				// document.getScore());
				if (maxScore == null) {
					maxScore = new Double(document.getScore());
					// continue;
				}

				Assert.assertTrue(maxScore >= document.getScore());
			}

			Keyword retrievalKeyword = mapper.readValue(retrievals.toString(), Keyword.class);
			retrievalKeyword.setKey(information.toString());
			maxScore = null;
			Assert.assertTrue(String.format("expected 319, got %d", retrievalKeyword.getDocuments().size()),
					retrievalKeyword.getDocuments().size() == 319);
			for (Document document : retrievalKeyword.getDocuments()) {
				// System.out.println("docId=" + document.getDocId() + ", score=" +
				// document.getScore());
				if (maxScore == null) {
					maxScore = new Double(document.getScore());
					// continue;
				}

				Assert.assertTrue(maxScore >= document.getScore());
			}

			int howMany = 320;
			int iteration = 1;

			// keywords
			List<Keyword> keywords = new ArrayList<>();
			keywords.add(informationKeyword);
			keywords.add(retrievalKeyword);

			PriorityQueue<DocumentDto> topDocs = new PriorityQueue<>();
			List<Document> iterationDocs = new ArrayList<Document>();
			Map<String, DocumentDto> Q = new LinkedHashMap<>();
			boolean done = false;
			for (;;) {
				// next iteration documents
				for (Keyword keyword : keywords) {
					Document document = keyword.getDocuments().poll();
					if (document == null) {
						done = true;
						break;
					}
					iterationDocs.add(document);
				}

				if (done) {
					break;
				}

				rankDocuments(iterationDocs, Q, topDocs);

				if (topDocs.size() >= howMany) {
					break;
				}

				iteration++;
				iterationDocs.clear();
			}

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

	private void rankDocuments(List<Document> iDocs, Map<String, DocumentDto> cache,
			PriorityQueue<DocumentDto> topDocs) {

		// cache and set lower bounds for indices
		int k = 0;
		int length = iDocs.size();
		for (Document iDoc : iDocs) {
			DocumentDto cached = cache.get(iDoc.getDocId());
			if (cached == null) {
				cached = new DocumentDto();
				cached.setId(iDoc.getDocId());
				cached.setMin(iDoc.getScore());
				double mins[] = new double[length];
				mins[k] = iDoc.getScore();
				cached.setMins(mins);
				cache.put(iDoc.getDocId(), cached);
			} else {
				cached.getMins()[k] = iDoc.getScore();
			}
			k++;
		}

		// finalize lower and upper bounds for buffered items
		List<String> topDocsIds = new ArrayList<>();
		for (DocumentDto cached : cache.values()) {
			double lowerBound = 0.0;
			double upperBound = 0.0;
			for (int i = 0; i < length; i++) {
				lowerBound += cached.getMins()[i];
				upperBound += iDocs.get(i).getScore();
			}
			cached.setMin(lowerBound);
			cached.setMax(upperBound);

			if (lowerBound >= upperBound) {
				topDocsIds.add(cached.getId());
			}
		}

		for (String id : topDocsIds) {
			topDocs.add(cache.get(id));
			cache.remove(id);
		}

	}

}
