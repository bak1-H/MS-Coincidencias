package com.sanosysalvos.coincidencias.messaging;

import com.sanosysalvos.coincidencias.model.DTO.NotificacionDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificacionPublisher {

    private final RabbitTemplate rabbitTemplate;
    public static final String NOTIFICACION_QUEUE = "notificaciones.queue";

    public NotificacionPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicarNotificacion(NotificacionDTO notificacion) {
        rabbitTemplate.convertAndSend(NOTIFICACION_QUEUE, notificacion);
        System.out.println(">>> [RabbitMQ] Notificación enviada: " + notificacion.getIdCoincidencia());
    }
}