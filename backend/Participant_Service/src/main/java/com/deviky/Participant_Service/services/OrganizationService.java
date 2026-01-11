package com.deviky.Participant_Service.services;


import com.deviky.Participant_Service.dto.ApiResponse;
import com.deviky.Participant_Service.dto.CreateOrganizationRequest;
import com.deviky.Participant_Service.dto.CreatePlayerRequest;
import com.deviky.Participant_Service.dto.PlayerDto;
import com.deviky.Participant_Service.models.Organization;
import com.deviky.Participant_Service.models.Player;
import com.deviky.Participant_Service.models.TeamPlayer;
import com.deviky.Participant_Service.repositories.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrganizationService {
    private final OrganizationRepository organizationRepository;

    public ApiResponse<Organization> createOrganization(CreateOrganizationRequest dto) {
        try {
            Organization organization = new Organization();
            organization.setId(dto.getId());
            organization.setOrganizerName(dto.getOrganizerName());
            organization.setDescription(dto.getDescription());
            Organization saved = organizationRepository.save(organization);
            return new ApiResponse<>("Организация создана", saved, false);
        } catch (Exception ex) {
            return new ApiResponse<>("Ошибка при создании организации: " + ex.getMessage(), null, true);
        }
    }

    public ApiResponse<Organization> getOrganization(Long organizationId) {
        try {
            Organization organization = organizationRepository.findById(organizationId)
                    .orElseThrow(() -> new Exception("Организация не найдена"));
            return new ApiResponse<>("Организация найдена", organization, false);
        } catch (Exception ex) {
            return new ApiResponse<>(ex.getMessage(), null, true);
        }
    }
}
