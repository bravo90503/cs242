package edu.ucr.cs242.repo.model;

import org.springframework.data.annotation.Id;

public class Document implements Comparable<Document> {
	@Id
	private String docId;
	private double score;
	private long positions[];
	private String snippet;
	private String keyword;

	public String getDocId() {
		return docId;
	}

	public void setDocId(String docId) {
		this.docId = docId;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public long[] getPositions() {
		return positions;
	}

	public void setPositions(long[] positions) {
		this.positions = positions;
	}

	public String getSnippet() {
		return snippet;
	}

	public void setSnippet(String snippet) {
		this.snippet = snippet;
	}

	@Override
	public int compareTo(Document d2) {
		if (score < d2.score)
			return 1;
		else if (score > d2.score)
			return -1;
		return 0;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("score=").append(score);
		sb.append("\nid=").append(docId);
		return sb.toString();
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

}
