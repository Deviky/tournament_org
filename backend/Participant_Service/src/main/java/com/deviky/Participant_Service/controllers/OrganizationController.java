package com.deviky.Participant_Service.controllers;

import com.deviky.Participant_Service.dto.ApiResponse;
import com.deviky.Participant_Service.dto.CreateOrganizationRequest;
import com.deviky.Participant_Service.dto.UpdateOrganizationRequest;
import com.deviky.Participant_Service.models.Organization;
import com.deviky.Participant_Service.services.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/participant/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @PostMapping("/private/create")
    public ResponseEntity<ApiResponse<Organization>> createOrganization(
            @RequestBody CreateOrganizationRequest dto
    ) {
        ApiResponse<Organization> response = organizationService.createOrganization(dto);
        HttpStatus status = response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }

    @PutMapping("/private/update")
    public ResponseEntity<ApiResponse<Organization>> updateOrganization(
            @RequestHeader("X-User-Id") Long selfId,
            @RequestBody UpdateOrganizationRequest dto
    ) {
        ApiResponse<Organization> response = organizationService.updateOrganization(selfId, dto);
        HttpStatus status = response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/public/get/{organizationId}")
    public ResponseEntity<ApiResponse<Organization>> getOrganization(
            @PathVariable Long organizationId
    ) {
        ApiResponse<Organization> response = organizationService.getOrganization(organizationId);
        HttpStatus status = response.isError() ? HttpStatus.NOT_FOUND : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/public/get_all")
    public ResponseEntity<ApiResponse<List<Organization>>> getOrganizations() {
        ApiResponse<List<Organization>> response = organizationService.getOrganizations();
        HttpStatus status = response.isError() ? HttpStatus.NOT_FOUND : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }
}
