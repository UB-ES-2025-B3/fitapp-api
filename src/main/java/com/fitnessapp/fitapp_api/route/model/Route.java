package com.fitnessapp.fitapp_api.route.model;

import com.fitnessapp.fitapp_api.auth.model.UserAuth;
import jakarta.persistence.*;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;
import lombok.*;
import java.math.BigDecimal;

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

}
