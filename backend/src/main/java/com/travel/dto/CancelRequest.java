package com.travel.dto;

import lombok.Data;

@Data
public class CancelRequest {
    private Long applicationId;
    private Long participantId;
    private String cancelType; // FULL_CANCEL / PARTICIPANT_REMOVE
    private String reason;
    private Long newContactParticipantId;
    private Long employeeId;
}
