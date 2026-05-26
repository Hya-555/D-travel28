package com.travel.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class GroupDTO {
    private String groupCode;
    private String routeCode;
    private LocalDate departureDate;
    private LocalDate deadline;
    private Integer maxCapacity;
    private Long employeeId;
}
