package edu.ucr.cs242.web;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.apache.commons.io.IOUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import edu.ucr.cs242.service.HadoopSearch;
import edu.ucr.cs242.service.LuceneSearch;
import edu.ucr.cs242.web.dto.DocumentDto;
import edu.ucr.cs242.web.dto.Query;
import edu.ucr.cs242.web.dto.Search;
import edu.ucr.cs242.web.dto.SearchModel;

@Controller
public class SearchController {

	@Autowired
	private HadoopSearch hadoopSearchService;
	@Autowired
	private LuceneSearch luceneSearchService;
	
	
	
	@GetMapping("/")
	public String query(Model model, Search search) {
		Query query = new Query();
		query.setSearchModel(SearchModel.LUCENE);
		query.setHowMany(100);
		model.addAttribute("query", query);
		return "search-query";
	}

	@PostMapping("/search")
	public String search(Model model, Query query) {
		switch (query.getSearchModel()) {
		case LUCENE:
			return searchLucene(model, query);
		default:
			return searchHadoop(model, query);
		}
	}

	@GetMapping(value = "/document/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public @ResponseBody byte[] getFile(@PathVariable String id) throws IOException {
		InputStream in = getClass().getResourceAsStream("/documents/" + id);
		return IOUtils.toByteArray(in);
	}

	public String searchHadoop(Model model, Query query) {
		Map<String, String> urlsMap = HadoopSearch.URLS_MAP;
		try {
			int howMany = query.getHowMany();
			String content = query.getContent();

			PriorityQueue<DocumentDto> docs = hadoopSearchService.searchByContent(content, howMany);
			List<DocumentDto> documents = new ArrayList<>();
			DocumentDto sortedDoc;
			while ((sortedDoc = docs.poll()) != null) {
				documents.add(sortedDoc);
				String[] split = sortedDoc.getId().split("_");
				String[] keys = split[1].split("\\.");
				String url = urlsMap.get(keys[0]);
				sortedDoc.setUrl(url);
			}

			model.addAttribute("query", query);
			model.addAttribute("documents", documents);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "hadoop-results-gz";
	}

	public String searchLucene(Model model, Query query) {
		try {
			int counter = 0;
			int howMany = query.getHowMany();
			String content = query.getContent();

			TopDocs foundDocs3 = luceneSearchService.searchByContent(content, howMany);
			List<edu.ucr.cs242.web.dto.DocumentDto> documents = new ArrayList<>();
			for (ScoreDoc sd : foundDocs3.scoreDocs) {

				Document d = luceneSearchService.getSearcher().doc(sd.doc);

				String url = d.get("url");
				String id = d.get("id");
				String preview = d.get("preview");

				System.out.println(++counter + ". Url=" + url + ", DocID=" + id);
				documents.add(new edu.ucr.cs242.web.dto.DocumentDto(id, preview, url));
				System.out.println(d);
				System.out.println(preview + "...");
			}

			model.addAttribute("query", query);
			model.addAttribute("documents", documents);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "lucene-results";
	}
}
