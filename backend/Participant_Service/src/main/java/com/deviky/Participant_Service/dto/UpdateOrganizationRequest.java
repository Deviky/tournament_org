package com.deviky.Participant_Service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateOrganizationRequest {
    private String organizerName;
    private String description;
}
