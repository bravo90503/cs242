package edu.ucr.cs242.lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

import com.google.common.base.Stopwatch;

public class LuceneWriteIndex {
	private static Map<String, String> urlsMap = new HashMap<>();
	private static String USAGE = "./indexbuilder.sh <input_dir> <analyzer:standard>";
	private static String inputDir;
	private static boolean isStandard;
	private static boolean isSimple;

	public static void main(String[] args) throws Exception {
		int errCode = validateArgs(args);
		if (errCode < 0) {
			return;
		}
		System.out.println("Creating IndexWriter...");
		IndexWriter writer = null;
		if (isStandard) {
			writer = createStandardAnalyzerWriter();
		}
		if (isSimple) {
			writer = createSimpleAnalyzerWriter();
		}

		if (writer == null) {
			System.out.println("Error creating analyzer");
			return;
		}

		List<Document> documents = new ArrayList<>();
		final File folder = new File(inputDir);
		readMetadataFile(folder);

		// start document creation
		Stopwatch stopwatch = Stopwatch.createStarted();
		createFilesForFolder(folder, documents, stopwatch);

		// Let's clean everything first
		writer.deleteAll();

		System.out.println("Adding documents to IndexWriter...");
		writer.addDocuments(documents);
		writer.commit();
		System.out.println("Closing writer...");
		writer.close();
		System.out.println("Program completed in " + stopwatch.elapsed(TimeUnit.SECONDS) + "s.");
	}

	private static int validateArgs(String[] args) {
		if (args.length < 2) {
			System.err.println("program arguments missing");
			System.out.println(USAGE);
			return -1;
		}

		inputDir = args[0];
		if (StringUtils.isBlank(inputDir)) {
			System.err.println(String.join(" ", args));
			System.out.println(USAGE);
			return -1;
		}

		if (inputDir.endsWith("/")) {
			inputDir = inputDir.substring(0, inputDir.length() - 1);
		}

		File directory = new File(inputDir);
		if (!directory.exists()) {
			System.err.println(inputDir + " not found");
			System.err.println(String.join(" ", args));
			System.out.println(USAGE);
			return -1;
		}

//		outputDir = args[1];
//		if (StringUtils.isBlank(outputDir)) {
//			System.err.println(String.join(" ", args));
//			System.out.println(USAGE);
//			return -1;
//		}
//
//		if (outputDir.endsWith("/")) {
//			outputDir = outputDir.substring(0, outputDir.length() - 1);
//		}
//
//		File outDirectory = new File(outputDir);
//		if (outDirectory.exists()) {
//			deleteDirectory(outDirectory);
//		}
//		outDirectory.mkdirs();

		String analyzer = args[1];
		if (StringUtils.isBlank(analyzer)) {
			System.err.println(String.join(" ", args));
			System.out.println(USAGE);
			return -1;
		} else {
			String[] splits = analyzer.split(":");
			if (splits.length < 2 || !StringUtils.equalsIgnoreCase(splits[0], "analyzer")) {
				System.err.println(String.join(" ", args));
				System.out.println(USAGE);
				return -1;
			}
			if (StringUtils.equalsIgnoreCase(splits[1], "standard")) {
				isStandard = true;
			} else if (StringUtils.equalsIgnoreCase(splits[1], "simple")) {
				isSimple = true;
			} else {
				System.err.println("Sorry, only standard or simple analyzers are supported right now.");
				System.err.println(String.join(" ", args));
				System.out.println(USAGE);
				return -1;
			}

		}

		return 0;
	}

//	private static boolean deleteDirectory(File directoryToBeDeleted) {
//		File[] allContents = directoryToBeDeleted.listFiles();
//		if (allContents != null) {
//			for (File file : allContents) {
//				deleteDirectory(file);
//			}
//		}
//		return directoryToBeDeleted.delete();
//	}

	private static Document createDocument(File file, Stopwatch stopwatch, int count) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file)); // creates a buffering character input stream
		try {
			System.out.println("--------creating document:" + file.getName());
			Document document = new Document();
			document.add(new StringField("id", file.getName(), Field.Store.YES));
			String url = "";
			String fileName = file.getName().toString();
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
			String line = reader.readLine();
			while (line != null) {
				if (StringUtils.isNotBlank(line)) {
					if (++counter <= 3) {
						sb.append(line);
					}
					document.add(new TextField("content", line, Field.Store.NO));
					if (counter > 2 && counter < 4) {
						document.add(new TextField("preview", sb.toString(), Field.Store.YES));
					}

				}
				line = reader.readLine();
			}
			if (counter < 3) {
				document.add(new TextField("preview", sb.toString(), Field.Store.YES));
			}
			System.out.println("--------document #:" + count + ", elapsed time:" + stopwatch.elapsed(TimeUnit.SECONDS));
			return document;
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	private static IndexWriter createStandardAnalyzerWriter() throws IOException {
		FSDirectory dir = FSDirectory.open(Paths.get("./lucene"));
		IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
		IndexWriter writer = new IndexWriter(dir, config);
		return writer;
	}

	private static IndexWriter createSimpleAnalyzerWriter() throws IOException {
		FSDirectory dir = FSDirectory.open(Paths.get("."));
		IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
		IndexWriter writer = new IndexWriter(dir, config);
		return writer;
	}

	private static void readMetadataFile(final File folder) throws IOException, NoSuchAlgorithmException {
		BufferedReader reader = new BufferedReader(new FileReader(folder.getAbsolutePath() + "/urls.txt"));
		try {
			String line = reader.readLine();
			while (line != null) {
				byte[] decodedBytes = Base64.getDecoder().decode(line);
				String url = new String(decodedBytes);
				String decodedUrl = URLDecoder.decode(url, StandardCharsets.UTF_8.toString());
				MessageDigest DIGEST = MessageDigest.getInstance("SHA-256");
				byte[] encodedhash = DIGEST.digest(decodedUrl.getBytes(StandardCharsets.UTF_8));
				String key = bytesToHex(encodedhash);
				urlsMap.put(key, decodedUrl);
				line = reader.readLine();
			}
		} finally {
			if (reader != null) {
				reader.close();
			}
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

	private static void createFilesForFolder(final File folder, List<Document> documents, Stopwatch stopwatch)
			throws IOException {
		int count = 0;
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				createFilesForFolder(fileEntry, documents, stopwatch);
			} else {
				if (!fileEntry.getName().equals("urls.txt")) {
					Document document3 = createDocument(fileEntry, stopwatch, ++count);
					documents.add(document3);
				}
			}
		}
	}

}
