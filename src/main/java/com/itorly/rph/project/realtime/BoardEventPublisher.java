package com.itorly.rph.project.realtime;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;

@Component
public class BoardEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public BoardEventPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publish(Long projectId, BoardEventType type, Object payload) {
        BoardEvent event = new BoardEvent(type, projectId, payload, Instant.now());
        publishAfterCommit(projectId, event);
    }

    private void publishAfterCommit(Long projectId, BoardEvent event) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    send(projectId, event);
                }
            });
            return;
        }
        send(projectId, event);
    }

    private void send(Long projectId, BoardEvent event) {
        messagingTemplate.convertAndSend("/topic/projects/" + projectId, event);
    }
}
