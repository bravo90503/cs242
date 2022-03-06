package edu.ucr.cs242.web.dto;

public class Query {
	private String content;
	private int howMany;
	private SearchModel searchModel;

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getHowMany() {
		return howMany;
	}

	public void setHowMany(int howMany) {
		this.howMany = howMany;
	}

	public SearchModel getSearchModel() {
		return searchModel;
	}

	public void setSearchModel(SearchModel searchModel) {
		this.searchModel = searchModel;
	}
}
