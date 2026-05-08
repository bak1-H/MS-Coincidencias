package com.sanosysalvos.coincidencias.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.sanosysalvos.coincidencias.model.reporteModel;

@Repository
public interface reporteRepository extends JpaRepository<reporteModel, Long> {
    List<reporteModel> findByTipoReporte(String tipoReporte);
    List<reporteModel> findByEstado(String estado);
    List<reporteModel> findByTipoReporteAndEstado(String tipoReporte, String estado);
}
