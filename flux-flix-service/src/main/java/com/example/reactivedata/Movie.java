package com.example.reactivedata;

import org.springframework.data.mongodb.core.mapping.Document;

@Document
public record Movie(String id, String title, String genre){ 
	
	public Movie {
		if ((title == null) || (title.length() <= 2)) {
			throw new IllegalArgumentException("Invalid title. Checkout whether the title is informed and longer than 2 characters.");
		}
	}
}
