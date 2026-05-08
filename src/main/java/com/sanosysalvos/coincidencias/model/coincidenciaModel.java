package com.sanosysalvos.coincidencias.model;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.GenerationType;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "coincidencias")
public class coincidenciaModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCoincidencia;

    private Long idReportePerdida; /* Foreign Key de reportePerdida */

    private Long idReporteEncontrado; /* Foreign Key de reporteEncontrado */

    private String fechaCoincidencia;
    
}
