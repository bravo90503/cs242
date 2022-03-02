package edu.ucr.cs242.web;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import org.apache.commons.io.IOUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import edu.ucr.cs242.service.HadoopSearch;
import edu.ucr.cs242.web.dto.DocumentDto;
import edu.ucr.cs242.web.dto.HadoopQuery;
import edu.ucr.cs242.web.dto.LuceneQuery;

@Controller
@RequestMapping("/hadoop")
public class HadoopSearchController {

	@Autowired
	private HadoopSearch hadoopSearchService;

	@GetMapping("/query")
	public String query(Model model) {
		LuceneQuery query = new LuceneQuery();
		query.setContent("retrieval");
		query.setHowMany(10);
		model.addAttribute("search", query);

		return "hadoop-query";
	}

	@PostMapping("/search")
	public String search(Model model, HadoopQuery query) {
		try {
			int howMany = query.getHowMany();
			String content = query.getContent();

			PriorityQueue<DocumentDto> documents = hadoopSearchService.searchByContent(content, howMany);

			model.addAttribute("documents", documents);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "hadoop-results";
	}

	@GetMapping(value = "/document/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public @ResponseBody byte[] getFile(@PathVariable String id) throws IOException {
		InputStream in = getClass().getResourceAsStream("/lucene/documents/" + id);
		return IOUtils.toByteArray(in);
	}
}