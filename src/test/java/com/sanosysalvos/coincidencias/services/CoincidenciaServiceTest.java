package com.sanosysalvos.coincidencias.services;

import com.sanosysalvos.coincidencias.messaging.NotificacionPublisher;
import com.sanosysalvos.coincidencias.model.DTO.NotificacionDTO;
import com.sanosysalvos.coincidencias.model.DTO.ReporteDTO;
import com.sanosysalvos.coincidencias.model.coincidenciaModel;
import com.sanosysalvos.coincidencias.model.reporteModel;
import com.sanosysalvos.coincidencias.repository.reporteRepository;
import com.sanosysalvos.coincidencias.repository.repositoryCoincidencia;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CoincidenciaServiceTest {

    @Mock
    private repositoryCoincidencia repoCoincidencia;

    @Mock
    private reporteRepository repoReportes;

    @Mock
    private NotificacionPublisher notificacionPublisher;

    @InjectMocks
    private coincidenciaService service;

    private ReporteDTO reportePerdido;
    private reporteModel candidatoVisto;

    @BeforeEach
    void setUp() {
        reportePerdido = new ReporteDTO();
        reportePerdido.setId(1L);
        reportePerdido.setTipoReporte("PERDIDO");
        reportePerdido.setTipoMascota("Perro");
        reportePerdido.setRaza("Labrador");
        reportePerdido.setTamano("Grande");
        reportePerdido.setColor("Amarillo");
        reportePerdido.setSexo("Macho");
        reportePerdido.setCoordenadas("-33.45,-70.65");

        candidatoVisto = new reporteModel();
        candidatoVisto.setIdReporte(2L);
        candidatoVisto.setTipoReporte("VISTO");
        candidatoVisto.setTipoMascota("Perro");
        candidatoVisto.setRaza("Labrador");
        candidatoVisto.setTamano("Grande");
        candidatoVisto.setColor("Amarillo");
        candidatoVisto.setSexo("Macho");
        candidatoVisto.setCoordenadas("-33.45,-70.65");
        candidatoVisto.setEstado("ACTIVO");
    }

    @Test
    void procesarNuevoReporte_cuandoReporteEsNull_retornaNull() {
        assertThat(service.procesarNuevoReporte(null)).isNull();
        verifyNoInteractions(repoReportes, repoCoincidencia, notificacionPublisher);
    }

    @Test
    void procesarNuevoReporte_cuandoIdEsNull_retornaNull() {
        ReporteDTO reporte = new ReporteDTO();
        reporte.setId(null);

        assertThat(service.procesarNuevoReporte(reporte)).isNull();
        verifyNoInteractions(repoReportes, repoCoincidencia, notificacionPublisher);
    }

    @Test
    void procesarNuevoReporte_cuandoHayCoincidenciaExacta_guardaYPublicaNotificacion() {
        coincidenciaModel guardada = new coincidenciaModel(10L, 1L, 2L, "2025-01-01T00:00:00");

        when(repoReportes.findByTipoReporteAndEstado("VISTO", "ACTIVO"))
                .thenReturn(List.of(candidatoVisto));
        when(repoCoincidencia.save(any())).thenReturn(guardada);
        when(repoReportes.findById(1L)).thenReturn(Optional.of(candidatoVisto));

        NotificacionDTO resultado = service.procesarNuevoReporte(reportePerdido);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getIdReportePerdida()).isEqualTo(1L);
        assertThat(resultado.getIdReporteEncontrado()).isEqualTo(2L);
        verify(repoCoincidencia).save(any(coincidenciaModel.class));
        verify(notificacionPublisher).publicarNotificacion(any(NotificacionDTO.class));
    }

    @Test
    void procesarNuevoReporte_cuandoScoreEsBajo_noGuardaNiPublica() {
        // Score = 0 (todo distinto), umbral es 0.60
        candidatoVisto.setTipoMascota("Gato");
        candidatoVisto.setRaza("Siames");
        candidatoVisto.setTamano("Pequeno");
        candidatoVisto.setColor("Negro");
        candidatoVisto.setSexo("Hembra");
        candidatoVisto.setCoordenadas("-40.0,-75.0");

        when(repoReportes.findByTipoReporteAndEstado("VISTO", "ACTIVO"))
                .thenReturn(List.of(candidatoVisto));

        assertThat(service.procesarNuevoReporte(reportePerdido)).isNull();
        verify(repoCoincidencia, never()).save(any());
        verify(notificacionPublisher, never()).publicarNotificacion(any());
    }

    @Test
    void procesarNuevoReporte_cuandoScoreExactoSuperaUmbral_guardaCoincidencia() {
        // tipoMascota(0.25) + raza(0.25) + tamano(0.15) = 0.65 >= 0.60 → debe hacer match
        candidatoVisto.setColor(null);
        candidatoVisto.setSexo("Hembra");
        candidatoVisto.setCoordenadas(null);

        coincidenciaModel guardada = new coincidenciaModel(11L, 1L, 2L, "2025-01-01T00:00:00");

        when(repoReportes.findByTipoReporteAndEstado("VISTO", "ACTIVO"))
                .thenReturn(List.of(candidatoVisto));
        when(repoCoincidencia.save(any())).thenReturn(guardada);
        when(repoReportes.findById(1L)).thenReturn(Optional.empty());

        assertThat(service.procesarNuevoReporte(reportePerdido)).isNotNull();
        verify(repoCoincidencia).save(any());
    }

    @Test
    void procesarNuevoReporte_cuandoReporteEsVisto_asignaIdsCorrectamente() {
        // Un reporte tipo VISTO debe guardar: candidato PERDIDO = idReportePerdida, reporte VISTO = idReporteEncontrado
        ReporteDTO reporteVisto = new ReporteDTO();
        reporteVisto.setId(5L);
        reporteVisto.setTipoReporte("VISTO");
        reporteVisto.setTipoMascota("Perro");
        reporteVisto.setRaza("Labrador");
        reporteVisto.setTamano("Grande");
        reporteVisto.setCoordenadas("-33.45,-70.65");

        reporteModel candidatoPerdido = new reporteModel();
        candidatoPerdido.setIdReporte(3L);
        candidatoPerdido.setTipoMascota("Perro");
        candidatoPerdido.setRaza("Labrador");
        candidatoPerdido.setTamano("Grande");
        candidatoPerdido.setCoordenadas("-33.45,-70.65");

        coincidenciaModel guardada = new coincidenciaModel(20L, 3L, 5L, "2025-01-01T00:00:00");

        when(repoReportes.findByTipoReporteAndEstado("PERDIDO", "ACTIVO"))
                .thenReturn(List.of(candidatoPerdido));
        when(repoCoincidencia.save(any())).thenReturn(guardada);
        when(repoReportes.findById(3L)).thenReturn(Optional.empty());

        service.procesarNuevoReporte(reporteVisto);

        verify(repoCoincidencia).save(argThat(c ->
                c.getIdReportePerdida().equals(3L) && c.getIdReporteEncontrado().equals(5L)
        ));
    }

    @Test
    void procesarNuevoReporte_sinCandidatos_retornaNull() {
        when(repoReportes.findByTipoReporteAndEstado("VISTO", "ACTIVO"))
                .thenReturn(Collections.emptyList());

        assertThat(service.procesarNuevoReporte(reportePerdido)).isNull();
        verify(repoCoincidencia, never()).save(any());
    }

    @Test
    void procesarNuevoReporte_colorParcialmenteCoincide_sumaPuntajeParcial() {
        // "Amarillo dorado" contiene "Amarillo" → score parcial 0.05 en vez de 0.10
        // tipoMascota(0.25) + raza(0.25) + tamano(0.15) + colorParcial(0.05) = 0.70 → match
        candidatoVisto.setColor("Amarillo dorado");
        candidatoVisto.setSexo("Hembra");
        candidatoVisto.setCoordenadas(null);

        coincidenciaModel guardada = new coincidenciaModel(12L, 1L, 2L, "2025-01-01T00:00:00");

        when(repoReportes.findByTipoReporteAndEstado("VISTO", "ACTIVO"))
                .thenReturn(List.of(candidatoVisto));
        when(repoCoincidencia.save(any())).thenReturn(guardada);
        when(repoReportes.findById(1L)).thenReturn(Optional.empty());

        assertThat(service.procesarNuevoReporte(reportePerdido)).isNotNull();
    }
}
