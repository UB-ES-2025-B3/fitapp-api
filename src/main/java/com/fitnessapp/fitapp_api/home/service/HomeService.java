package com.fitnessapp.fitapp_api.home.service;

import com.fitnessapp.fitapp_api.home.dto.HomeKpisTodayResponseDTO;

public interface HomeService {
    HomeKpisTodayResponseDTO  getHomeKpisToday(String email);
}
