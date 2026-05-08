package com.controller;
import com.sanosysalvos.coincidencias.model.coincidenciaModel;
import com.sanosysalvos.coincidencias.model.DTO.NotificacionDTO;
import com.sanosysalvos.coincidencias.repository.repositoryCoincidencia;
import com.sanosysalvos.coincidencias.services.coincidenciaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/coincidencias")
public class CoincidenciaController {

    private final coincidenciaService coincidenciaService;
    private final repositoryCoincidencia repositoryCoincidencia;

    public CoincidenciaController(
            coincidenciaService coincidenciaService,
            repositoryCoincidencia repositoryCoincidencia) {
        this.coincidenciaService = coincidenciaService;
        this.repositoryCoincidencia = repositoryCoincidencia;
    }

    /**
     * GET /coincidencias
     * Obtiene todas las coincidencias registradas
     */
    @GetMapping
    public ResponseEntity<List<NotificacionDTO>> obtenerTodas() {
        List<NotificacionDTO> coincidencias = repositoryCoincidencia.findAll()
                .stream()
                .map(this::mapToNotificacionDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(coincidencias);
    }

    /**
     * GET /coincidencias/{id}
     * Obtiene una coincidencia específica por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<NotificacionDTO> obtenerPorId(@PathVariable Long id) {
        return repositoryCoincidencia.findById(id)
                .map(coincidencia -> ResponseEntity.ok(mapToNotificacionDTO(coincidencia)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * GET /coincidencias/reporte/{idReporte}
     * Obtiene todas las coincidencias donde el reporte es PERDIDO o ENCONTRADO
     */
    @GetMapping("/reporte/{idReporte}")
    public ResponseEntity<List<NotificacionDTO>> obtenerPorReporte(@PathVariable Long idReporte) {
        List<NotificacionDTO> coincidencias = repositoryCoincidencia.findAll()
                .stream()
                .filter(c -> c.getIdReportePerdida().equals(idReporte) || 
                             c.getIdReporteEncontrado().equals(idReporte))
                .map(this::mapToNotificacionDTO)
                .collect(Collectors.toList());
        
        if (coincidencias.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(coincidencias);
    }

    /**
     * GET /coincidencias/perdido/{idReportePerdida}
     * Obtiene la coincidencia específica para un reporte perdido
     */
    @GetMapping("/perdido/{idReportePerdida}")
    public ResponseEntity<NotificacionDTO> obtenerPorReportePerdida(@PathVariable Long idReportePerdida) {
        return repositoryCoincidencia.findAll()
                .stream()
                .filter(c -> c.getIdReportePerdida().equals(idReportePerdida))
                .findFirst()
                .map(coincidencia -> ResponseEntity.ok(mapToNotificacionDTO(coincidencia)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * GET /coincidencias/encontrado/{idReporteEncontrado}
     * Obtiene la coincidencia específica para un reporte encontrado
     */
    @GetMapping("/visto/{idReporteEncontrado}")
    public ResponseEntity<NotificacionDTO> obtenerPorReporteEncontrado(@PathVariable Long idReporteEncontrado) {
        return repositoryCoincidencia.findAll()
                .stream()
                .filter(c -> c.getIdReporteEncontrado().equals(idReporteEncontrado))
                .findFirst()
                .map(coincidencia -> ResponseEntity.ok(mapToNotificacionDTO(coincidencia)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * DELETE /coincidencias/{id}
     * Elimina una coincidencia por ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!repositoryCoincidencia.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repositoryCoincidencia.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Mapea entidad coincidenciaModel a NotificacionDTO
     */
    private NotificacionDTO mapToNotificacionDTO(coincidenciaModel modelo) {
        return NotificacionDTO.builder()
                .idCoincidencia(modelo.getIdCoincidencia())
                .idReportePerdida(modelo.getIdReportePerdida())
                .idReporteEncontrado(modelo.getIdReporteEncontrado())
                .fechaCoincidencia(modelo.getFechaCoincidencia())
                .nombreMascota("")  // estos campos podrías llenarlos si guardas los reportes en BD
                .direccion("")
                .coordenadas("")
                .build();
    }
}