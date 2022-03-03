package edu.ucr.cs242.repo.model;

import java.util.List;

import org.springframework.data.annotation.Id;

public class Keyword {
	@Id
	private String key;
    private long frequency;
	private List<Document> documents;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public long getFrequency() {
		return frequency;
	}

	public void setFrequency(long frequency) {
		this.frequency = frequency;
	}

	public List<Document> getDocuments() {
		return documents;
	}

	public void setDocuments(List<Document> documents) {
		this.documents = documents;
	}

}
