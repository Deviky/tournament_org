package com.deviky.Participant_Service.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(schema = "participant", name = "organization_profile")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Organization {
    @Id
    Long id;
    @Column(name = "organizer_name")
    String organizerName;
    String description;
}
