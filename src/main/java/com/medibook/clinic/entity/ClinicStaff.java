package com.medibook.clinic.entity;

import com.medibook.common.entity.BaseEntity;
import com.medibook.common.util.Role;
import com.medibook.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
    name = "clinic_staff",
    uniqueConstraints = @UniqueConstraint(columnNames = {"clinic_id", "user_id"})
)
@Getter
@Setter
public class ClinicStaff extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinic_id", nullable = false)
    private Clinic clinic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role roleInClinic;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StaffStatus status = StaffStatus.ACTIVE;
}