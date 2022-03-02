package edu.ucr.cs242.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.springframework.stereotype.Service;

import edu.ucr.cs242.repo.model.Document;
import edu.ucr.cs242.web.dto.DocumentDto;

@Service
public class HadoopSearch {

	public void rankDocuments(List<Document> iDocs, Map<String, DocumentDto> cache,
			PriorityQueue<DocumentDto> topDocs) {
		// cache incoming docs, set lowerbounds on their respective indices
		int k = 0;
		int length = iDocs.size();
		for (Document iDoc : iDocs) {
			DocumentDto cached = cache.get(iDoc.getDocId());
			if (cached == null) {
				cached = new DocumentDto();
				cached.setId(iDoc.getDocId());
				cached.setMin(iDoc.getScore());
				double mins[] = new double[length];
				mins[k] = iDoc.getScore();
				cached.setMins(mins);
				cache.put(iDoc.getDocId(), cached);
			} else {
				// previously cached, just update new index with incoming lowerbound
				cached.getMins()[k] = iDoc.getScore();
			}
			k++;
		}

		// finalize lower and upper bounds for buffered (cached) items
		List<String> topDocsIds = new ArrayList<>();
		for (DocumentDto cached : cache.values()) {
			double lowerBound = 0.0;
			double upperBound = 0.0;
			for (int i = 0; i < length; i++) {
				lowerBound += cached.getMins()[i];
				upperBound += iDocs.get(i).getScore();
			}
			cached.setMin(lowerBound);
			cached.setMax(upperBound);

			if (lowerBound >= upperBound) {
				topDocsIds.add(cached.getId());
			}
		}

		for (String id : topDocsIds) {
			topDocs.add(cache.get(id));
			cache.remove(id);
		}

	}

}
