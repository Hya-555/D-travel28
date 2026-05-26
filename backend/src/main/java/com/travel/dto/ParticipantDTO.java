package com.travel.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ParticipantDTO {
    private String name;
    private String gender;
    private LocalDate birthDate;
    private String phone;
    private String address;
    private String zipCode;
    private String email;
    private String emergencyContact;
    private String emergencyAddress;
    private String emergencyPhone;
    private String relationship;
    private Boolean isContactPerson;
}
