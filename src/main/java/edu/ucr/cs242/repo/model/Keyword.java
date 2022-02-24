package edu.ucr.cs242.repo.model;

import java.util.PriorityQueue;

import org.springframework.data.annotation.Id;

public class Keyword {
	@Id
	private String key;
    private long frequency;
	private PriorityQueue<Document> documents;

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

	public PriorityQueue<Document> getDocuments() {
		return documents;
	}

	public void setDocuments(PriorityQueue<Document> documents) {
		this.documents = documents;
	}

}
