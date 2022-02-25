package edu.ucr.cs242.lucene;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class LuceneReadSearchIndex {
	private static final String INDEX_DIR = "/Users/marlonbravo/workspace/webserver";

	public static void main(String[] args) throws Exception {
		int howMany = 10;
		IndexSearcher searcher = createSearcher();

		// Search by content
		TopDocs foundDocs3 = searchByContent("information", searcher, howMany);

		System.out.println("Total Results :: " + foundDocs3.totalHits);
		System.out.println("Showing Top :: " + howMany);
		int counter = 0;
		for (ScoreDoc sd : foundDocs3.scoreDocs) {

			Document d = searcher.doc(sd.doc);
			
			String url = d.get("url");
			String id = d.get("id");
			String preview = d.get("preview");
            
			System.out.println(
					++counter + ". Url=" + url + ", DocID=" + id);
			System.out.println(d);
			System.out.println(preview + "...");
		}
	}

	private static TopDocs searchByContent(String content, IndexSearcher searcher, int howMany) throws Exception {
		QueryParser qp = new QueryParser("content", new StandardAnalyzer());
		Query contentQuery = qp.parse(content);
		TopDocs hits = searcher.search(contentQuery, howMany);
		return hits;
	}

	private static IndexSearcher createSearcher() throws IOException {
		Directory dir = FSDirectory.open(Paths.get(INDEX_DIR));
		IndexReader reader = DirectoryReader.open(dir);
		IndexSearcher searcher = new IndexSearcher(reader);
		return searcher;
	}
}