package com.fitnessapp.fitapp_api.route.model;

import jakarta.persistence.Embeddable;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class Checkpoint {
    private String name;   // Nombre del checkpoint
    private String point;  // Coordenada en formato "lat,lon"
}
