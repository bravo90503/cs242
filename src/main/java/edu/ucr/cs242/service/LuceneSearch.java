package edu.ucr.cs242.service;

import java.io.Console;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

@Service
public class LuceneSearch {
	private static Map<String, String> urlsMap = new HashMap<>();
	@Autowired
	private ApplicationContext applicationContext;

	private IndexSearcher searcher;

	@PostConstruct
	public IndexSearcher createSearcher() throws IOException, NoSuchAlgorithmException {
		String[] spinner = new String[] {"\u0008/", "\u0008-", "\u0008\\", "\u0008|" };
		Resource[] meta = applicationContext.getResources("classpath*:documents/urls.txt");
		readMetadataFile(meta[0]);
		Resource[] resources = applicationContext.getResources("classpath*:documents/*.txt");
		ByteBuffersDirectory directory = new ByteBuffersDirectory();
		try (IndexWriter directoryWriter = new IndexWriter(directory, new IndexWriterConfig(new StandardAnalyzer()))) {
            int i=0;
            System.out.println("Please wait while lucene in-memory index is being built.");
			for (Resource r : resources) {
				byte[] bdata = FileCopyUtils.copyToByteArray(r.getInputStream());
				String filename = r.getFilename();
				String data = new String(bdata, StandardCharsets.UTF_8);
				String lines[] = data.split("\n");

				Document doc = createDocument(lines, filename);
				directoryWriter.addDocument(doc);
				System.out.printf("%s", spinner[i % spinner.length]);
				i++;
			}
		}

		DirectoryReader indexReader = DirectoryReader.open(directory);
		searcher = new IndexSearcher(indexReader);

		return searcher;
	}
	
	
	private static void readMetadataFile(Resource r) throws IOException, NoSuchAlgorithmException {
		byte[] bdata = FileCopyUtils.copyToByteArray(r.getInputStream());
		String data = new String(bdata, StandardCharsets.UTF_8);
		String lines[] = data.split("\n");

		try {
			for(String line : lines) {
				byte[] decodedBytes = Base64.getDecoder().decode(line);
				String url = new String(decodedBytes);
				String decodedUrl = URLDecoder.decode(url, StandardCharsets.UTF_8.toString());
				MessageDigest DIGEST = MessageDigest.getInstance("SHA-256");
				byte[] encodedhash = DIGEST.digest(decodedUrl.getBytes(StandardCharsets.UTF_8));
				String key = bytesToHex(encodedhash);
				urlsMap.put(key, decodedUrl);
			}
		} finally {
			
		}
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

	private static Document createDocument(String[] lines, String fileName) throws IOException {
		try {

			Document document = new Document();
			document.add(new StringField("id", fileName, Field.Store.YES));
			String url = "";
			String[] splits = fileName.split("_");
			if (splits.length > 1) {
				splits = splits[1].split("\\.");
				if (splits.length > 1) {
					url = urlsMap.get(splits[0]);
					if (url != null) {
						document.add(new StringField("url", url, Field.Store.YES));
					}
				}
			}

			int counter = 0;
			StringBuilder sb = new StringBuilder();
			for (String line : lines) {
				if (StringUtils.isNotBlank(line)) {
					if (++counter <= 3) {
						sb.append(line);
					}
					document.add(new TextField("content", line, Field.Store.NO));
					if (counter > 2 && counter < 4) {
						document.add(new TextField("preview", sb.toString(), Field.Store.YES));
					}

				}
			}
			if (counter < 3) {
				document.add(new TextField("preview", sb.toString(), Field.Store.YES));
			}

			return document;
		} finally {

		}
	}

	public TopDocs searchByContent(String content, int howMany) throws Exception {
		QueryParser qp = new QueryParser("content", new StandardAnalyzer());
		Query contentQuery = qp.parse(content);
		TopDocs hits = searcher.search(contentQuery, howMany);
		return hits;
	}

	public IndexSearcher getSearcher() {
		return this.searcher;
	}
}
