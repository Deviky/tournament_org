package com.deviky.Participant_Service.controllers;

import com.deviky.Participant_Service.dto.ApiResponse;
import com.deviky.Participant_Service.dto.CreateOrganizationRequest;
import com.deviky.Participant_Service.models.Organization;
import com.deviky.Participant_Service.services.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/participant/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    // ------------------ Создание организации ------------------
    @PostMapping("/private/create")
    public ResponseEntity<ApiResponse<Organization>> createOrganization(
            @RequestBody CreateOrganizationRequest dto
    ) {
        ApiResponse<Organization> response = organizationService.createOrganization(dto);
        HttpStatus status = response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }

    // ------------------ Получение организации по ID ------------------
    @GetMapping("/public/{organizationId}")
    public ResponseEntity<ApiResponse<Organization>> getOrganization(
            @PathVariable Long organizationId
    ) {
        ApiResponse<Organization> response = organizationService.getOrganization(organizationId);
        HttpStatus status = response.isError() ? HttpStatus.NOT_FOUND : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }
}
