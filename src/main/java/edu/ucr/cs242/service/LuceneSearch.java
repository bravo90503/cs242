package edu.ucr.cs242.service;

import java.io.File;
import java.io.IOException;

import javax.annotation.PostConstruct;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

@Service
public class LuceneSearch {
	@Value("${index.dir:lucene}")
	private String indexDir;

	private IndexSearcher searcher;

	@PostConstruct
	private void createSearcher() throws IOException {
		File file = ResourceUtils.getFile("classpath:" + indexDir);
		Directory dir = FSDirectory.open(file.toPath());
		IndexReader reader = DirectoryReader.open(dir);
		searcher = new IndexSearcher(reader);
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
