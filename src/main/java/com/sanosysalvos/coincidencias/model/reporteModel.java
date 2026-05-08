package com.sanosysalvos.coincidencias.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Immutable  // Indica que esta entidad es de solo lectura
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reportes")
public class reporteModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reporte")
    private Long idReporte; 

    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(name = "tipo_reporte")
    private String tipoReporte;

    @Column(name = "tipo_mascota")
    private String tipoMascota;

    @Column(name = "nombre_mascota")
    private String nombreMascota;

    @Column(name = "color")
    private String color;

    @Column(name = "tamano")
    private String tamano;

    @Column(name = "raza")
    private String raza;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "direccion", columnDefinition = "TEXT")
    private String direccion;

    @Column(name = "coordenadas")
    private String coordenadas;

    @Column(name = "estado")
    private String estado;

    @Column(name = "sexo")
    private String sexo;

    @Column(name = "foto_mascota")
    private String fotoMascota;
}