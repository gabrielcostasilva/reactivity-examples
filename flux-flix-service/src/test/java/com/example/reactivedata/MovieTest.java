package com.example.reactivedata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class MovieTest {

	@Test
	public void successfullyCreateNewInstance() {
		var instance = new Movie(null, "The return of the dead", "Horror");
		assertNull(instance.id());

		assertThat(instance.genre()).contains("Horror");
	}

	@Test
	public void invalidTitleCreateNewInstance() {
		assertThatExceptionOfType(IllegalArgumentException.class)
			.isThrownBy(() -> new Movie(null, null, "horror"))
			.withMessage("Invalid title. Checkout whether the title is informed and longer than 2 characters.");
	}
}
