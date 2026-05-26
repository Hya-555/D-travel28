package com.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("participant")
public class Participant {
    @TableId(type = IdType.AUTO)
    private Long participantId;
    private Long applicationId;
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
    private Integer isContactPerson;
    private String status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
