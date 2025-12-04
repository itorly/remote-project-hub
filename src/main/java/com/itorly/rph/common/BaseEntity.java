package com.itorly.rph.common;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.*;

@MappedSuperclass
@Data
@EqualsAndHashCode
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime localDateTime = getLocalDateTime();

        createdAt = localDateTime;
        updatedAt = localDateTime;
    }

    @PreUpdate
    protected void onUpdate() {

        updatedAt = getLocalDateTime();
    }

    private static LocalDateTime getLocalDateTime() {
        ZoneId zoneId = ZoneId.systemDefault();
        Clock clock = Clock.system(zoneId);
        Instant now = Instant.now(clock);
        ZonedDateTime localTime = now.atZone(zoneId);
        return localTime.toLocalDateTime();
    }

    // getters/setters
}
