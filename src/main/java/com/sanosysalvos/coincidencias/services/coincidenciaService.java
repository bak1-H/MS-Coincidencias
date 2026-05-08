package com.sanosysalvos.coincidencias.services;

import com.sanosysalvos.coincidencias.model.reporteModel;
import com.sanosysalvos.coincidencias.messaging.NotificacionPublisher;
import com.sanosysalvos.coincidencias.model.coincidenciaModel;
import com.sanosysalvos.coincidencias.model.DTO.NotificacionDTO;
import com.sanosysalvos.coincidencias.model.DTO.ReporteDTO;
import com.sanosysalvos.coincidencias.repository.reporteRepository;
import com.sanosysalvos.coincidencias.repository.repositoryCoincidencia;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class coincidenciaService {

    private final repositoryCoincidencia repoCoincidencia;
    private final reporteRepository repoReportes;
    private final NotificacionPublisher notificacionPublisher;

    private static final double MATCH_THRESHOLD = 0.60;

    public coincidenciaService(
            repositoryCoincidencia repoCoincidencia,
            reporteRepository repoReportes,
            NotificacionPublisher notificacionPublisher) {
        this.repoCoincidencia = repoCoincidencia;
        this.repoReportes = repoReportes;
        this.notificacionPublisher = notificacionPublisher;
    }

    /**
     * Procesa un nuevo reporte recibido desde RabbitMQ (ya guardado en BD por Reportes)
     * Busca matches contra reportes del tipo contrario
     * Si encuentra coincidencia, la guarda y publica notificación
     */
    public NotificacionDTO procesarNuevoReporte(ReporteDTO reporte) {
        if (reporte == null || reporte.getId() == null) {
            return null;
        }

        // Obtener candidatos del tipo contrario y ACTIVOS desde la BD compartida
        String tipoContrario = "PERDIDO".equalsIgnoreCase(reporte.getTipoReporte()) ? "VISTO" : "PERDIDO";
        List<reporteModel> candidatos = repoReportes.findByTipoReporteAndEstado(tipoContrario, "ACTIVO");

        // Buscar coincidencias
        for (reporteModel candidato : candidatos) {
            double score = calcularScore(reporte, candidato);

            if (score >= MATCH_THRESHOLD) {
                // Crear y guardar coincidencia
                coincidenciaModel coincidencia = new coincidenciaModel();
                
                if ("PERDIDO".equalsIgnoreCase(reporte.getTipoReporte())) {
                    coincidencia.setIdReportePerdida(reporte.getId());
                    coincidencia.setIdReporteEncontrado(candidato.getIdReporte());
                } else {
                    coincidencia.setIdReportePerdida(candidato.getIdReporte());
                    coincidencia.setIdReporteEncontrado(reporte.getId());
                }

                coincidencia.setFechaCoincidencia(LocalDateTime.now().toString());
                coincidenciaModel guardada = repoCoincidencia.save(coincidencia);

                // Determinar idUsuario del reporte perdido (evitar lambda que modifica variable externa)
                Long idUsuarioReportePerdida = null;
                Long idReportePerdida = guardada.getIdReportePerdida();
                if (idReportePerdida != null) {
                    idUsuarioReportePerdida = repoReportes.findById(idReportePerdida)
                            .map(reporteModel::getIdUsuario)
                            .orElse(null);
                }

                // Construir y publicar notificación incluyendo idUsuarioReportePerdida
                NotificacionDTO notificacion = NotificacionDTO.builder()
                        .idCoincidencia(guardada.getIdCoincidencia())
                        .idReportePerdida(guardada.getIdReportePerdida())
                        .idReporteEncontrado(guardada.getIdReporteEncontrado())
                        .fechaCoincidencia(guardada.getFechaCoincidencia())
                        .nombreMascota(reporte.getNombreMascota())
                        .tipoMascota(reporte.getTipoMascota())
                        .direccion(reporte.getDireccion())
                        .coordenadas(reporte.getCoordenadas())
                        .idUsuarioReportePerdida(idUsuarioReportePerdida)
                        .build();

                notificacionPublisher.publicarNotificacion(notificacion);
                return notificacion;
            }
        }

        return null;
    }

    private double calcularScore(ReporteDTO a, reporteModel b) {
        double score = 0.0;

        if (a.getTipoMascota() != null && a.getTipoMascota().equalsIgnoreCase(b.getTipoMascota())) {
            score += 0.25;
        }
        if (a.getRaza() != null && b.getRaza() != null && a.getRaza().equalsIgnoreCase(b.getRaza())) {
            score += 0.25;
        }
        if (a.getTamano() != null && a.getTamano().equalsIgnoreCase(b.getTamano())) {
            score += 0.15;
        }
        if (a.getColor() != null && b.getColor() != null) {
            String ca = a.getColor().toLowerCase();
            String cb = b.getColor().toLowerCase();
            if (ca.equals(cb)) score += 0.10;
            else if (ca.contains(cb) || cb.contains(ca)) score += 0.05;
        }
        if (a.getSexo() != null && a.getSexo().equalsIgnoreCase(b.getSexo())) {
            score += 0.05;
        }
        if (a.getCoordenadas() != null && b.getCoordenadas() != null) {
            Double distanciaKm = calcularDistanciaKm(a.getCoordenadas(), b.getCoordenadas());
            if (distanciaKm != null) {
                if (distanciaKm <= 1.0) score += 0.20;
                else if (distanciaKm <= 3.0) score += 0.10;
            }
        }

        return score;
    }

    private Double calcularDistanciaKm(String coordA, String coordB) {
        try {
            String[] a = coordA.split(",");
            String[] b = coordB.split(",");
            double lat1 = Double.parseDouble(a[0].trim());
            double lon1 = Double.parseDouble(a[1].trim());
            double lat2 = Double.parseDouble(b[0].trim());
            double lon2 = Double.parseDouble(b[1].trim());

            final int R = 6371;
            double dLat = Math.toRadians(lat2 - lat1);
            double dLon = Math.toRadians(lon2 - lon1);
            double sinDLat = Math.sin(dLat / 2);
            double sinDLon = Math.sin(dLon / 2);
            double aa = sinDLat * sinDLat +
                    Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                            sinDLon * sinDLon;
            double c = 2 * Math.atan2(Math.sqrt(aa), Math.sqrt(1 - aa));
            return R * c;
        } catch (Exception e) {
            return null;
        }
    }
}