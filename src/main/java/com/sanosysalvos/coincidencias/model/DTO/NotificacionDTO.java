package com.sanosysalvos.coincidencias.model.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificacionDTO {
    @JsonProperty("id_coincidencia")
    private Long idCoincidencia;

    @JsonProperty("id_reporte_perdida")
    private Long idReportePerdida;

    @JsonProperty("id_reporte_encontrado")
    private Long idReporteEncontrado;

    @JsonProperty("fecha_coincidencia")
    private String fechaCoincidencia;

    @JsonProperty("nombre_mascota")
    private String nombreMascota;

    @JsonProperty("tipo_mascota")
    private String tipoMascota;

    @JsonProperty("direccion")
    private String direccion;

    @JsonProperty("coordenadas")
    private String coordenadas;

    @JsonProperty("id_usuario_reporte_perdida")
    private Long idUsuarioReportePerdida;

    @JsonProperty("email_usuario")
    private String emailUsuario;
}