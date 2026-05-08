package com.sanosysalvos.coincidencias.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanosysalvos.coincidencias.model.DTO.NotificacionDTO;
import com.sanosysalvos.coincidencias.model.DTO.ReporteDTO;
import com.sanosysalvos.coincidencias.services.coincidenciaService;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class RabbitConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public Queue ReporteCreadoQueue() {
        return new Queue("reporte.creado.queue", true);
    }

    @Bean
    public Queue NotificacionQueue() {
        return new Queue("notificaciones.queue", true);
    }

    @Component
    public class ReporteListener {

        private final coincidenciaService coincidenciaService;

        public ReporteListener(coincidenciaService coincidenciaService) {
            this.coincidenciaService = coincidenciaService;
        }

        @RabbitListener(queues = "reporte.creado.queue")
        public void procesarNuevoReporte(ReporteDTO reporte) {
            // Ya no guardamos una réplica local en Coincidencias cuando usamos DB compartida.
            NotificacionDTO noti = coincidenciaService.procesarNuevoReporte(reporte);
            if (noti != null) {
                System.out.println(">>> Coincidencia encontrada y publicada: " + noti);
            } else {
                System.out.println(">>> No se encontró coincidencia para el reporte: " + reporte.getId());
            }
        }

    }
}