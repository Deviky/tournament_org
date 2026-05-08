package com.deviky.Participant_Service.services;


import com.deviky.Participant_Service.dto.ApiResponse;
import com.deviky.Participant_Service.dto.CreateOrganizationRequest;
import com.deviky.Participant_Service.dto.UpdateOrganizationRequest;
import com.deviky.Participant_Service.models.Organization;
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

    public ApiResponse<List<Organization>> getOrganizations() {
        try {
            List<Organization> organizations = organizationRepository.findAll();
            return new ApiResponse<>("", organizations, false);
        } catch (Exception ex) {
            return new ApiResponse<>(ex.getMessage(), null, true);
        }
    }

    public ApiResponse<Organization> updateOrganization(Long selfId, UpdateOrganizationRequest dto) {
        try {
            Organization organization = organizationRepository.findById(selfId)
                    .orElseThrow(() -> new Exception("Организация не найдена"));

            if (dto.getOrganizerName() == null || dto.getOrganizerName().isBlank()) {
                return new ApiResponse<>("Название организатора обязательно", null, true);
            }

            organization.setOrganizerName(dto.getOrganizerName().trim());
            organization.setDescription(dto.getDescription());

            Organization saved = organizationRepository.save(organization);
            return new ApiResponse<>("Профиль организации обновлен", saved, false);
        } catch (Exception ex) {
            return new ApiResponse<>("Ошибка при обновлении организации: " + ex.getMessage(), null, true);
        }
    }
}
