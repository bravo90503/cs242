package edu.ucr.cs242.repo.model;

import org.springframework.data.annotation.Id;

public class User {
	@Id
	private String id;
	
	private String password;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}



}
