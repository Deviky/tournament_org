package com.deviky.Participant_Service.dto;

import lombok.Data;

@Data
public class TransferCaptainRequest {
    private Long teamId;
    private Long newCaptainId;
}
