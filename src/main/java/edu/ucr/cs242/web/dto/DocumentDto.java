package edu.ucr.cs242.web.dto;

import java.text.DecimalFormat;

public class DocumentDto implements Comparable<DocumentDto> {
	private String id;
	private String preview;
	private String url;
	private double[] mins;
	private double min;
	private double max;
	private boolean found;

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

	public double[] getMins() {
		return mins;
	}

	public void setMins(double[] mins) {
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

}
