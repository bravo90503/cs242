package edu.ucr.cs242.web.dto;

import java.text.DecimalFormat;

public class DocumentDto implements Comparable<DocumentDto> {
	private String id;
	private String preview;
	private String url;
	private Keyword[] mins;
	private double min;
	private double max;
	private boolean found;
	private boolean match;

	public boolean isFound() {
		return found;
	}

	public void setFound(boolean found) {
		this.found = found;
	}

	public DocumentDto() {
	}

	public double getMin() {
		return min;
	}

	public void setMin(double min) {
		this.min = min;
	}

	public Keyword[] getMins() {
		return mins;
	}

	public void setMins(Keyword[] mins) {
		this.mins = mins;
	}

	public double getMax() {
		return max;
	}

	public void setMax(double max) {
		this.max = max;
	}

	public DocumentDto(String id, String preview, String url) {
		this.id = id;
		this.preview = preview;
		this.url = url;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPreview() {
		return preview;
	}

	public void setPreview(String preview) {
		this.preview = preview;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getShortId() {
		return this.id.substring(0, this.id.indexOf("_"));
	}

	public boolean isMatch() {
		return match;
	}

	public void setMatch(boolean match) {
		this.match = match;
	}

	@Override
	public int compareTo(DocumentDto d2) {
		if (min < d2.min)
			return 1;
		else if (min > d2.min)
			return -1;
		return 0;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("id=").append(id).append(";score=").append(min);

		return sb.toString();
	}

	public String getScore() {
		String pattern = "######.0000";
		DecimalFormat df = new DecimalFormat(pattern);
		return df.format(this.min);
	}

	public static class Keyword {
		public Keyword() {
		}

		public Keyword(double min, String keyword, long[] positions) {
			this.min = min;
			this.keyword = keyword;
			this.positions = positions;
		}

		private double min;
		private String keyword;
		private long[] positions;

		public double getMin() {
			return min;
		}

		public void setMin(double min) {
			this.min = min;
		}

		public String getKeyword() {
			return keyword;
		}

		public void setKeyword(String keyword) {
			this.keyword = keyword;
		}

		public long[] getPositions() {
			return positions;
		}

		public void setPositions(long[] positions) {
			this.positions = positions;
		}

	}

}
