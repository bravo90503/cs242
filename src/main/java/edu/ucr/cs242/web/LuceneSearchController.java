package edu.ucr.cs242.web;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import edu.ucr.cs242.service.LuceneSearch;
import edu.ucr.cs242.web.dto.LuceneQuery;

@Controller
@RequestMapping("/lucene")
public class LuceneSearchController {

	@Autowired
	private LuceneSearch luceneSearchService;

	@GetMapping("/query")
	public String query(Model model) {
		LuceneQuery query = new LuceneQuery();
		query.setContent("retrieval");
		query.setHowMany(10);
		model.addAttribute("search", query);

		return "lucene-query";
	}

	@PostMapping("/search")
	public String search(Model model, LuceneQuery query) {
		try {
			int counter = 0;
			int howMany = query.getHowMany();
			String content = query.getContent();

			TopDocs foundDocs3 = luceneSearchService.searchByContent(content, howMany);
			List<edu.ucr.cs242.web.dto.Document> documents = new ArrayList<>();
			for (ScoreDoc sd : foundDocs3.scoreDocs) {

				Document d = luceneSearchService.getSearcher().doc(sd.doc);

				String url = d.get("url");
				String id = d.get("id");
				String preview = d.get("preview");

				System.out.println(++counter + ". Url=" + url + ", DocID=" + id);
				documents.add(new edu.ucr.cs242.web.dto.Document(id, preview, url));
				System.out.println(d);
				System.out.println(preview + "...");
			}

			model.addAttribute("documents", documents);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "lucene-results";
	}

	@GetMapping(value = "/documents", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public @ResponseBody byte[] getFile(@RequestParam String id) throws IOException {
		InputStream in = getClass().getResourceAsStream("/lucene/documents/" + id);
		return IOUtils.toByteArray(in);
	}
}
