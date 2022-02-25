package edu.ucr.cs242.web.dto;

public class Document {
	private String id;
	private String preview;
	private String url;
	
	public Document(String id, String preview, String url) {
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

}
