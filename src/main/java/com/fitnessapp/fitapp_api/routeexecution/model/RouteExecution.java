package com.fitnessapp.fitapp_api.routeexecution.model;

import com.fitnessapp.fitapp_api.auth.model.UserAuth;
import com.fitnessapp.fitapp_api.route.model.Route;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "route_executions"
)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RouteExecution {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "route_id", nullable = false, foreignKey = @ForeignKey(name = "fk_route_execution_route"))
    private Route route;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_route_execution_user"))
    private UserAuth user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RouteExecutionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private ActivityType activityType;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "pause_time", nullable = true)
    private LocalDateTime pauseTime;

    @Column(name = "end_time", nullable = true)
    private LocalDateTime endTime;

    @Column(name = "total_paused_time_sec", nullable = false)
    private Long totalPausedTimeSec = 0L;

    @Column(name = "duration_sec", nullable = true)
    private Long durationSec;

    @Column(name = "calories", precision = 10, scale = 2, nullable = true)
    private BigDecimal calories;

    @Column(name = "notes", length = 500)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // -------------------
    // Enums internos
    // -------------------
    public enum RouteExecutionStatus {
        IN_PROGRESS,
        PAUSED,
        FINISHED
    }

    public enum ActivityType {
        WALKING_SLOW,
        WALKING_MODERATE,
        WALKING_INTENSE,
        RUNNING_SLOW,
        RUNNING_MODERATE,
        RUNNING_INTENSE,
        CYCLING_SLOW,
        CYCLING_MODERATE,
        CYCLING_INTENSE;
    }
}
