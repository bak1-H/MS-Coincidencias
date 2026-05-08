package com.sanosysalvos.coincidencias.repository;

import org.springframework.stereotype.Repository;
import com.sanosysalvos.coincidencias.model.coincidenciaModel;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface repositoryCoincidencia extends JpaRepository<coincidenciaModel, Long> {

    
}
