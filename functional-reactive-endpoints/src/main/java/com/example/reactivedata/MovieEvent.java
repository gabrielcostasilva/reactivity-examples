package com.example.reactivedata;

import java.util.Date;

public record MovieEvent(Movie movie, Date when, String user) {}
