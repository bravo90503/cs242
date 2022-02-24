package edu.ucr.cs242.repo.model;

import org.springframework.data.annotation.Id;

public class Document {
	@Id
	private String docID;
	private double score;
	private long position;
	private String preview;

	public String getDocID() {
		return docID;
	}

	public void setDocID(String docID) {
		this.docID = docID;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public long getPosition() {
		return position;
	}

	public void setPosition(long position) {
		this.position = position;
	}

	public String getPreview() {
		return preview;
	}

	public void setPreview(String preview) {
		this.preview = preview;
	}
	
}
