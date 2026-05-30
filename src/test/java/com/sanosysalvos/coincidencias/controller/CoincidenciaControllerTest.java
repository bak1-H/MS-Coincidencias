package com.sanosysalvos.coincidencias.controller;

import com.controller.CoincidenciaController;
import com.sanosysalvos.coincidencias.model.coincidenciaModel;
import com.sanosysalvos.coincidencias.repository.repositoryCoincidencia;
import com.sanosysalvos.coincidencias.services.coincidenciaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CoincidenciaControllerTest {

    @Mock
    private coincidenciaService coincidenciaService;

    @Mock
    private repositoryCoincidencia repositoryCoincidencia;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        CoincidenciaController controller = new CoincidenciaController(coincidenciaService, repositoryCoincidencia);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void obtenerTodas_cuandoNoHayDatos_retornaListaVacia() throws Exception {
        when(repositoryCoincidencia.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/coincidencias"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void obtenerTodas_cuandoHayDatos_retornaLista() throws Exception {
        coincidenciaModel modelo = new coincidenciaModel(1L, 10L, 20L, "2025-01-01");
        when(repositoryCoincidencia.findAll()).thenReturn(List.of(modelo));

        mockMvc.perform(get("/coincidencias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id_coincidencia").value(1))
                .andExpect(jsonPath("$[0].id_reporte_perdida").value(10))
                .andExpect(jsonPath("$[0].id_reporte_encontrado").value(20));
    }

    @Test
    void obtenerPorId_cuandoExiste_retorna200ConDatos() throws Exception {
        coincidenciaModel modelo = new coincidenciaModel(1L, 10L, 20L, "2025-01-01");
        when(repositoryCoincidencia.findById(1L)).thenReturn(Optional.of(modelo));

        mockMvc.perform(get("/coincidencias/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_coincidencia").value(1))
                .andExpect(jsonPath("$.id_reporte_perdida").value(10));
    }

    @Test
    void obtenerPorId_cuandoNoExiste_retorna404() throws Exception {
        when(repositoryCoincidencia.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/coincidencias/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerPorReporte_cuandoHayCoincidencias_retorna200() throws Exception {
        coincidenciaModel modelo = new coincidenciaModel(1L, 5L, 10L, "2025-01-01");
        when(repositoryCoincidencia.findAll()).thenReturn(List.of(modelo));

        mockMvc.perform(get("/coincidencias/reporte/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id_reporte_perdida").value(5));
    }

    @Test
    void obtenerPorReporte_cuandoNoHayCoincidencias_retorna204() throws Exception {
        when(repositoryCoincidencia.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/coincidencias/reporte/99"))
                .andExpect(status().isNoContent());
    }

    @Test
    void obtenerPorReportePerdida_cuandoExiste_retorna200() throws Exception {
        coincidenciaModel modelo = new coincidenciaModel(1L, 7L, 15L, "2025-01-01");
        when(repositoryCoincidencia.findAll()).thenReturn(List.of(modelo));

        mockMvc.perform(get("/coincidencias/perdido/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_reporte_perdida").value(7));
    }

    @Test
    void obtenerPorReporteEncontrado_cuandoExiste_retorna200() throws Exception {
        coincidenciaModel modelo = new coincidenciaModel(1L, 7L, 15L, "2025-01-01");
        when(repositoryCoincidencia.findAll()).thenReturn(List.of(modelo));

        mockMvc.perform(get("/coincidencias/visto/15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_reporte_encontrado").value(15));
    }

    @Test
    void eliminar_cuandoExiste_retorna204() throws Exception {
        when(repositoryCoincidencia.existsById(1L)).thenReturn(true);

        mockMvc.perform(delete("/coincidencias/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void eliminar_cuandoNoExiste_retorna404() throws Exception {
        when(repositoryCoincidencia.existsById(99L)).thenReturn(false);

        mockMvc.perform(delete("/coincidencias/99"))
                .andExpect(status().isNotFound());
    }
}
