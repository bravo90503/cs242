package edu.ucr.cs242.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.zip.GZIPInputStream;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ucr.cs242.repo.KeywordRepository;
import edu.ucr.cs242.repo.model.Document;
import edu.ucr.cs242.repo.model.Keyword;
import edu.ucr.cs242.web.dto.DocumentDto;

@Service
public class HadoopSearch {
	@Autowired
	KeywordRepository keywordRepo;
	@Value("${enable.keyword.repo:true}")
	boolean flag;
	
	Logger logger = LoggerFactory.getLogger(HadoopSearch.class);
	private static ObjectMapper mapper = new ObjectMapper();
	public static Map<String, String> URLS_MAP = new HashMap<>();
	private Map<String, Keyword> keywordsMap = new HashMap<>();

	@PostConstruct
	private void loadUrls() throws IOException, NoSuchAlgorithmException {
		BufferedReader br = null;
		try {
			MessageDigest DIGEST = MessageDigest.getInstance("SHA-256");
			File file = ResourceUtils.getFile("classpath:" + "hadoop/urls.txt");
			br = new BufferedReader(new FileReader(file)); // creates a buffering character input stream
			String line;
			while ((line = br.readLine()) != null) {
				byte[] decodedBytes = Base64.getDecoder().decode(line);
				String url = new String(decodedBytes);
				String decodedUrl = URLDecoder.decode(url, StandardCharsets.UTF_8.toString());
				byte[] encodedhash = DIGEST.digest(decodedUrl.getBytes(StandardCharsets.UTF_8));
				String key = bytesToHex(encodedhash);
				URLS_MAP.put(key, decodedUrl);
			}

		} finally {
			if (br != null) {
				br.close();
			}
		}
	}

	public PriorityQueue<DocumentDto> searchByContent(String content, int howMany) {
		PriorityQueue<DocumentDto> topDocs = new PriorityQueue<>();
		try {
			List<Keyword> keywords = getKeywords(content.toLowerCase().split(" "));
			if (keywords.size() > 0) {
				findTopDocuments(keywords, howMany, topDocs);
			}
		} catch (IOException e) {
			logger.error("searchByContent", e);
		}

		logger.info("hadoop search completed");
		return topDocs;
	}

	public List<Keyword> getKeywords(String[] tokens) throws IOException {
		if (flag) {
			return keywordRepo.findByIds(Arrays.asList(tokens));
		}
			
		List<Keyword> keywords = new ArrayList<>();
		Map<String, String> tokenMap = new HashMap<>();
		for (String token : tokens) {
			Keyword keyword;
			if ((keyword = keywordsMap.get(token)) == null) {
				tokenMap.put(token, token);
			} else {
				if (StringUtils.isNotBlank(keyword.getKey())) {
					keywords.add(keyword);
				} // else keyword not found in file
			}
		}
		if (!tokenMap.isEmpty()) {
			File file = ResourceUtils.getFile("classpath:" + "part-r-00000.gz");
			FileInputStream stream = new FileInputStream(file); // creates a new file
			// instance
			GZIPInputStream gzipstream = new GZIPInputStream(stream);
			Reader decoder = new InputStreamReader(gzipstream, "UTF-8");
			BufferedReader buffered = new BufferedReader(decoder);
			StringBuilder[] keys = new StringBuilder[tokenMap.size()];
			StringBuilder[] docs = new StringBuilder[tokenMap.size()];
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

			for (int i = 0; i < keys.length; i++) {
				if (docs[i] != null) {
					Keyword keyword = mapper.readValue(docs[i].toString(), Keyword.class);
					keyword.setKey(keys[i].toString());
					keywords.add(keyword);
					keywordsMap.put(keys[i].toString(), keyword);
				} else {
					keywordsMap.put(tokens[i].toString(), new Keyword());
				}
			}
		}

		return keywords;
	}

	public int findTopDocuments(List<Keyword> keywords, int howMany, PriorityQueue<DocumentDto> topDocs) {
		Document[] iterationDocs = new Document[keywords.size()];
		Map<String, DocumentDto> CACHE = new LinkedHashMap<>();

		int iteration = 0;
		for (;;) {
			// next iteration documents
			int i = 0;
			for (Keyword keyword : keywords) {
				Document document = null;
				if (iteration < keyword.getDocuments().size()) {
					document = keyword.getDocuments().get(iteration);
				}
				if (document == null) {
					return iteration;
				}

				document.setKeyword(keyword.getKey());
				iterationDocs[i] = document;
				i++;
			}

			rankDocuments(iterationDocs, CACHE, topDocs, howMany);

			if (topDocs.size() >= howMany) {
				return iteration;
			}

			iteration++;
		}

	}

	/**
	 * <p>
	 * Uses NRA to rank documents.
	 * </p>
	 * 
	 * @param iDocs   per-round documents, one document for every keyword
	 * @param CACHE   buffer to keep track of visited documents
	 * @param topDocs list to put top documents in
	 * @return nothing, uses topDocs in param to put results in
	 * @see <a href=
	 *      "http://www-db.deis.unibo.it/courses/SI-M/slides/01.2.TopK.advanced1.pdf">Top-k
	 *      queries</a>
	 * @since 1.0
	 */
	public void rankDocuments(Document[] iDocs, Map<String, DocumentDto> CACHE, PriorityQueue<DocumentDto> topDocs,
			int howMany) {
		int remaining = howMany - topDocs.size();
		// cache incoming docs, set lowerbounds on their respective indices
		int k = 0;
		int length = iDocs.length;

		for (Document iDoc : iDocs) {
			DocumentDto cached = CACHE.get(iDoc.getDocId());
			if (cached == null) {
				cached = new DocumentDto();
				cached.setId(iDoc.getDocId());
				cached.setMin(iDoc.getScore());

				DocumentDto.Keyword mins[] = new DocumentDto.Keyword[length];
				mins[k] = new DocumentDto.Keyword(iDoc.getScore(), iDoc.getKeyword(), iDoc.getPositions());
				cached.setMins(mins);
				CACHE.put(iDoc.getDocId(), cached);
			} else {
				cached.getMins()[k] = new DocumentDto.Keyword(iDoc.getScore(), iDoc.getKeyword(), iDoc.getPositions());
			}
			k++;
		}

		// finalize lower and upper bounds for buffered (cached) items
		List<String> topDocsIds = new ArrayList<>();
		int iteration = 0;
		for (DocumentDto cached : CACHE.values()) {
			double lowerBound = 0.0;
			double upperBound = 0.0;

			for (int i = 0; i < length; i++) {
				lowerBound += cached.getMins()[i] == null ? 0 : cached.getMins()[i].getMin();
				upperBound += iDocs[i].getScore();
			}

			lowerBound += lowerBound * rankByDistance(cached);

			cached.setMin(lowerBound);
			cached.setMax(upperBound);

			if (lowerBound >= upperBound) {
				topDocsIds.add(cached.getId());
			}

			if (topDocsIds.size() >= remaining) {
				// early termination from having to look at entire cache
				break;
			}
			iteration++;
		}

		if (logger.isDebugEnabled()) {
			int size = CACHE.size();
			logger.debug(size + " vs " + iteration);
		}

		for (String id : topDocsIds) {
			topDocs.add(CACHE.get(id));
			CACHE.remove(id);
		}

	}

	private double rankByDistance(DocumentDto cached) {
		double additionalRank = 0.0;
		for (int i = 0; i < cached.getMins().length; i++) {
			if (cached.getMins()[i] == null) {
				return 0.00;
			}
		}

		DocumentDto.Keyword[] keywords = cached.getMins();
		int[] ptrs = new int[cached.getMins().length];

		boolean match = false;
		boolean done = false;
        double found = 0;
		for (;;) {
			for (int i = 0; i < ptrs.length; i++) {
				if (ptrs[i] > keywords[i].getPositions().length) {
					done = true;
					break;
				}
			}

			if (done) {
				break;
			}

			// advance 2nd, 3rd offsets?
			long[] offsets = new long[ptrs.length];
			boolean continueout = false;
			for (int j = 0; j < ptrs.length; j++) {
				if (ptrs[j] < keywords[j].getPositions().length) {
					offsets[j] = keywords[j].getPositions()[ptrs[j]];
					if (j == 0) {
						continue;
					}
					long prevOffset = offsets[j - 1];
					long curOffset = offsets[j];
					int curOffsetLength = keywords[j].getKeyword().length() + 1;
					if (prevOffset > curOffset + curOffsetLength) {
						ptrs[j]++;
						continueout = true;
					}
				}
			}

			if (continueout) {
				continue;
			}

			// compare
			long a = 0;
			match = true;
			for (int k = 0; k < ptrs.length - 1; k++) {
				a = offsets[k] + keywords[k].getKeyword().length() + 1;
				if (a != offsets[k + 1]) {
					match = false;
				}
			}

			if (match) {
				found++;
				//break;
			}

			for (int l = 0; l < ptrs.length - 1; l++) {
				ptrs[l]++;
			}

		}

		if (found > 0) {
			cached.setMatch(true);
			additionalRank = .50 * found;
		}

		return additionalRank;
	}

	private static String bytesToHex(byte[] hash) {
		StringBuilder hexString = new StringBuilder(2 * hash.length);
		for (int i = 0; i < hash.length; i++) {
			String hex = Integer.toHexString(0xff & hash[i]);
			if (hex.length() == 1) {
				hexString.append('0');
			}
			hexString.append(hex);
		}
		return hexString.toString();
	}

}
