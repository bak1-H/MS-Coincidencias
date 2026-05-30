package com.sanosysalvos.coincidencias;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CoincidenciasApplicationTests {

	@Test
	void aplicacionExiste() {
		assertThat(CoincidenciasApplication.class).isNotNull();
	}

}
