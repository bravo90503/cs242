package edu.ucr.cs242.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ucr.cs242.repo.model.Document;
import edu.ucr.cs242.repo.model.Keyword;
import edu.ucr.cs242.web.dto.DocumentDto;

@Service
public class HadoopSearch {

	private static ObjectMapper mapper = new ObjectMapper();

	public PriorityQueue<DocumentDto> searchByContent(String content, int howMany) {
		PriorityQueue<DocumentDto> topDocs = new PriorityQueue<>();
		try {
			List<Keyword> keywords = getKeywords(content.split(" "));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return topDocs;
	}

	public List<Keyword> getKeywords(String[] tokens) throws IOException {
		Map<String, String> tokenMap = new HashMap<>();
		for (String token : tokens) {
			tokenMap.put(token, token);
		}
		File file = ResourceUtils.getFile("classpath:" + "part-r-00000.gz");
		FileInputStream stream = new FileInputStream(file); // creates a new file
		// instance
		GZIPInputStream gzipstream = new GZIPInputStream(stream);
		Reader decoder = new InputStreamReader(gzipstream, "UTF-8");
		BufferedReader buffered = new BufferedReader(decoder);
		StringBuilder[] keys = new StringBuilder[tokens.length];
		StringBuilder[] docs = new StringBuilder[tokens.length];
		String line;
		int index = 0;
		while ((line = buffered.readLine()) != null) {
			int beginIndex = line.indexOf("{");
			String key = line.substring(0, beginIndex).trim();
			if (StringUtils.isNotBlank(key) && tokenMap.get(key) != null) {
				StringBuilder keysb = new StringBuilder();
				StringBuilder doc = new StringBuilder();
				keysb.append(key); // appends line to string buffer
				doc.append(line.substring(beginIndex, line.length()));
				keys[index] = keysb;
				docs[index] = doc;
				index++;
			}
		}
		buffered.close(); // closes the stream and release the resources

		List<Keyword> keywords = new ArrayList<>(keys.length);
		for (int i = 0; i < keys.length; i++) {
			Keyword keyword = mapper.readValue(docs[i].toString(), Keyword.class);
			keyword.setKey(keys[i].toString());
			keywords.add(keyword);
		}

		return keywords;
	}

	public int findTopDocuments(List<Keyword> keywords, int howMany, PriorityQueue<DocumentDto> topDocs) {
		Document[] iterationDocs = new Document[keywords.size()];
		Map<String, DocumentDto> CACHE = new LinkedHashMap<>();
		boolean done = false;
		int iteration = 0;
		for (;;) {
			// next iteration documents
			int i = 0;
			for (Keyword keyword : keywords) {
				Document document = keyword.getDocuments().poll();
				if (document == null) {
					done = true;
					break;
				}
				iterationDocs[i] = document;
				i++;
			}

			if (done) {
				break;
			}

			rankDocuments(iterationDocs, CACHE, topDocs);

			if (topDocs.size() >= howMany) {
				break;
			}

			iteration++;
		}

		return iteration;
	}

	public void rankDocuments(Document[] iDocs, Map<String, DocumentDto> CACHE, PriorityQueue<DocumentDto> topDocs) {
		// cache incoming docs, set lowerbounds on their respective indices
		int k = 0;
		int length = iDocs.length;
		for (Document iDoc : iDocs) {
			DocumentDto cached = CACHE.get(iDoc.getDocId());
			if (cached == null) {
				cached = new DocumentDto();
				cached.setId(iDoc.getDocId());
				cached.setMin(iDoc.getScore());
				double mins[] = new double[length];
				mins[k] = iDoc.getScore();
				cached.setMins(mins);
				CACHE.put(iDoc.getDocId(), cached);
			} else {
				// previously cached, just update new index with incoming lowerbound
				cached.getMins()[k] = iDoc.getScore();
			}
			k++;
		}

		// finalize lower and upper bounds for buffered (cached) items
		List<String> topDocsIds = new ArrayList<>();
		for (DocumentDto cached : CACHE.values()) {
			double lowerBound = 0.0;
			double upperBound = 0.0;
			for (int i = 0; i < length; i++) {
				lowerBound += cached.getMins()[i];
				upperBound += iDocs[i].getScore();
			}
			cached.setMin(lowerBound);
			cached.setMax(upperBound);

			if (lowerBound >= upperBound) {
				topDocsIds.add(cached.getId());
			}
		}

		for (String id : topDocsIds) {
			topDocs.add(CACHE.get(id));
			CACHE.remove(id);
		}

	}

}
