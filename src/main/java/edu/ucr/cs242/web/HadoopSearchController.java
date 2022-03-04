package edu.ucr.cs242.web;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.apache.commons.io.IOUtils;
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
		query.setContent("information retrieval");
		query.setHowMany(100);
		model.addAttribute("search", query);

		return "hadoop-query-gz";
	}

	@PostMapping("/search")
	public String search(Model model, HadoopQuery query) {
		Map<String, String> urlsMap = HadoopSearch.URLS_MAP;
		try {
			int howMany = query.getHowMany();
			String content = query.getContent();
            
			PriorityQueue<DocumentDto> docs = hadoopSearchService.searchByContent(content, howMany);
			List<DocumentDto> documents = new ArrayList<>();
			DocumentDto sortedDoc;
			while ((sortedDoc = docs.poll()) != null)  {
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

	@GetMapping(value = "/document/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public @ResponseBody byte[] getFile(@PathVariable String id) throws IOException {
		InputStream in = getClass().getResourceAsStream("/documents/" + id);
		return IOUtils.toByteArray(in);
	}
}
