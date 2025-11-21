package com.fitnessapp.fitapp_api.route.model;

import com.fitnessapp.fitapp_api.auth.model.UserAuth;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(
        name = "routes"
)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@SoftDelete(strategy = SoftDeleteType.DELETED, columnName = "deleted")
public class Route {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_route_user"))
    private UserAuth user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "start_point", nullable = false)
    private String startPoint;

    @Column(name = "end_point", nullable = false)
    private String endPoint;

    @Column(name = "distance_km", precision = 10, scale = 2)
    private BigDecimal distanceKm;

    @ElementCollection
    @CollectionTable(
            name = "checkpoints",
            joinColumns = @JoinColumn(name = "route_id", foreignKey = @ForeignKey(name = "fk_checkpoint_route"))
    )
    private List<Checkpoint> checkpoints = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}