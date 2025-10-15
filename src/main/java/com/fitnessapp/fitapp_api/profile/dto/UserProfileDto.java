// java
package com.fitnessapp.fitapp_api.profile.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDto {
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private BigDecimal heightCm;
    private BigDecimal weightKg;
}
