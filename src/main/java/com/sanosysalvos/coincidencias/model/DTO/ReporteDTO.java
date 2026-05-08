package com.sanosysalvos.coincidencias.model.DTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteDTO {
    private Long id; 
    private Integer idUsuario;
    
    // Datos principales del reporte
    private String tipoReporte; 
    
    private String direccion;
    private String coordenadas;
    
    // Características de la mascota
    private String nombreMascota;
    private String tipoMascota;
    private String raza;
    private String tamano;
    private String color;
    private String sexo;
    
    // Detalles adicionales
    private String descripcion;
    private String fotoMascota;
    
    // Estado interno del reporte
    private String estado; // Ej: ACTIVO, RESUELTO
}