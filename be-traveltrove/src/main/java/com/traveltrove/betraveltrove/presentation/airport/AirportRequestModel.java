package com.traveltrove.betraveltrove.presentation.airport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AirportRequestModel {
    private String name;
    private String cityId;
}
