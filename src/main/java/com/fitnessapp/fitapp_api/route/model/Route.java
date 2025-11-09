package com.fitnessapp.fitapp_api.route.model;

import com.fitnessapp.fitapp_api.auth.model.UserAuth;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Entity
@Table(
        name = "routes"
)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Route {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_route_user"))
    private UserAuth user;

    @NotBlank
    @Size(min = 1, max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank
    @Column(name = "start_point", nullable = false)
    private String startPoint; // ejemplo: "lat,lng"

    @NotBlank
    @Column(name = "end_point", nullable = false)
    private String endPoint;

    @Column(name = "distance_km", nullable = false)
    private Double distanceKm;

    @Column(nullable = false)
    private Boolean deleted = false;

}
