package com.medibook.user.service;

import com.medibook.common.util.Role;
import com.medibook.user.dto.UserProfileUpdateRequest;
import com.medibook.user.entity.Gender;
import com.medibook.user.entity.User;
import com.medibook.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserServiceImplTest {

    @Test
    void updatesAndReturnsPatientProfileFields() {
        UserRepository repository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        UserServiceImpl service = new UserServiceImpl(repository, passwordEncoder);
        User user = User.builder()
                .fullName("A Patient")
                .email("patient@example.com")
                .password("encoded")
                .role(Role.PATIENT)
                .build();
        user.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.save(user)).thenReturn(user);

        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setFullName("Alex Patient");
        request.setPhoneNumber("9876543210");
        request.setGender(Gender.OTHER);
        request.setDateOfBirth(LocalDate.of(1990, 5, 10));
        request.setAddressLine1("1 Main Street");
        request.setCity("Pune");
        request.setState("Maharashtra");
        request.setPincode("411001");
        request.setProfilePictureUrl("https://example.com/profile.png");

        var response = service.updateProfile(1L, request);

        assertEquals("Alex Patient", response.getFullName());
        assertEquals("9876543210", response.getPhoneNumber());
        assertEquals(Gender.OTHER, response.getGender());
        assertEquals(LocalDate.of(1990, 5, 10), response.getDateOfBirth());
        assertEquals("1 Main Street", response.getAddressLine1());
        assertEquals("Pune", response.getCity());
        assertEquals("Maharashtra", response.getState());
        assertEquals("411001", response.getPincode());
        assertEquals("https://example.com/profile.png", response.getProfilePictureUrl());
    }
}
