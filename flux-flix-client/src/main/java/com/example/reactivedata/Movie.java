package com.example.reactivedata;

import org.springframework.data.mongodb.core.mapping.Document;

@Document
public record Movie(String id, String title, String genre){ }
