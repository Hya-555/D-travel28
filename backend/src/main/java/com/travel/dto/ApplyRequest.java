package com.travel.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ApplyRequest {
    @NotBlank(message = "旅游团代码不能为空")
    private String groupCode;
    @NotBlank(message = "责任人姓名不能为空")
    private String contactName;
    @NotBlank(message = "责任人电话不能为空")
    private String contactPhone;
    @NotNull(message = "大人人数不能为空")
    private Integer adultCount;
    @NotNull(message = "小孩人数不能为空")
    private Integer childCount;
    private Long employeeId;
}
