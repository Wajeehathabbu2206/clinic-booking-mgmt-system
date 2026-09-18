package com.medibook.slot.service;

import com.medibook.clinic.entity.Clinic;
import com.medibook.common.exception.ResourceNotFoundException;
import com.medibook.common.exception.SlotNotAvailableException;
import com.medibook.doctor.entity.Doctor;
import com.medibook.doctor.repository.DoctorRepository;
import com.medibook.slot.dto.GenerateSlotsRequest;
import com.medibook.slot.dto.SlotResponse;
import com.medibook.slot.entity.Slot;
import com.medibook.slot.enums.SlotStatus;
import com.medibook.slot.repository.SlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SlotService {

    private final SlotRepository slotRepository;
    private final DoctorRepository doctorRepository;

    @Transactional
    public List<SlotResponse> generateSlots(GenerateSlotsRequest request) {
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor not found with id: " + request.getDoctorId()));

        if (!request.getToDate().isAfter(request.getFromDate().minusDays(1))) {
            throw new IllegalArgumentException("To date must be on or after from date");
        }
        if (!request.getDailyEndTime().isAfter(request.getDailyStartTime())) {
            throw new IllegalArgumentException("Daily end time must be after daily start time");
        }

        // Build a set of existing (date, startTime) keys to avoid duplicate-generation
        // failures against the unique constraint.
        Set<String> existingKeys = slotRepository
                .findExistingDateTimeKeys(request.getDoctorId(), request.getFromDate(), request.getToDate())
                .stream()
                .map(row -> row[0] + "_" + row[1])
                .collect(Collectors.toSet());

        List<Slot> newSlots = new ArrayList<>();

        for (LocalDate date = request.getFromDate();
             !date.isAfter(request.getToDate());
             date = date.plusDays(1)) {

            if (request.getWorkingDays() != null
                    && !request.getWorkingDays().isEmpty()
                    && !request.getWorkingDays().contains(date.getDayOfWeek())) {
                continue;
            }

            LocalTime cursor = request.getDailyStartTime();
            while (!cursor.plusMinutes(request.getSlotDurationMinutes()).isAfter(request.getDailyEndTime())) {
                LocalTime slotEnd = cursor.plusMinutes(request.getSlotDurationMinutes());
                String key = date + "_" + cursor;

                if (!existingKeys.contains(key)) {
                    newSlots.add(Slot.builder()
                            .doctor(doctor)
                            .clinic(doctor.getClinic())
                            .slotDate(date)
                            .startTime(cursor)
                            .endTime(slotEnd)
                            .status(SlotStatus.AVAILABLE)
                            .build());
                    existingKeys.add(key); // guard against dupes within this same generation run
                }
                cursor = slotEnd;
            }
        }

        List<Slot> saved = slotRepository.saveAll(newSlots);
        return saved.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SlotResponse> getAvailableSlots(Long doctorId, LocalDate fromDate, LocalDate toDate) {
        return slotRepository
                .findByDoctor_IdAndSlotDateBetweenAndStatusOrderBySlotDateAscStartTimeAsc(
                        doctorId, fromDate, toDate, SlotStatus.AVAILABLE)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SlotResponse> getAllSlots(Long doctorId, LocalDate fromDate, LocalDate toDate) {
        return slotRepository
                .findByDoctor_IdAndSlotDateBetweenOrderBySlotDateAscStartTimeAsc(doctorId, fromDate, toDate)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Core locking method. Called from within an outer @Transactional method
     * (this one, or — in Phase 5 — AppointmentService.createAppointment()).
     * Takes a PESSIMISTIC_WRITE lock on the row, so a second concurrent caller
     * blocks here until the first transaction commits, then sees the updated
     * status and correctly gets SlotNotAvailableException.
     */
    @Transactional
    public Slot lockAndBook(Long slotId) {
        Slot slot = slotRepository.findByIdForUpdate(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found with id: " + slotId));

        if (slot.getStatus() != SlotStatus.AVAILABLE) {
            throw new SlotNotAvailableException("This slot is no longer available");
        }

        slot.setStatus(SlotStatus.BOOKED);
        return slotRepository.save(slot);
    }

    /** Called from AppointmentService when an appointment is cancelled. */
    @Transactional
    public void release(Long slotId) {
        Slot slot = slotRepository.findByIdForUpdate(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found with id: " + slotId));
        slot.setStatus(SlotStatus.AVAILABLE);
        slotRepository.save(slot);
    }

    @Transactional
    public SlotResponse blockSlot(Long slotId) {
        Slot slot = slotRepository.findByIdForUpdate(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found with id: " + slotId));
        if (slot.getStatus() == SlotStatus.BOOKED) {
            throw new SlotNotAvailableException("Cannot block a slot that is already booked");
        }
        slot.setStatus(SlotStatus.BLOCKED);
        return mapToResponse(slotRepository.save(slot));
    }

    @Transactional
    public SlotResponse unblockSlot(Long slotId) {
        Slot slot = slotRepository.findByIdForUpdate(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found with id: " + slotId));
        if (slot.getStatus() != SlotStatus.BLOCKED) {
            throw new IllegalStateException("Slot is not currently blocked");
        }
        slot.setStatus(SlotStatus.AVAILABLE);
        return mapToResponse(slotRepository.save(slot));
    }

    @Transactional(readOnly = true)
    public SlotResponse getById(Long slotId) {
        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found with id: " + slotId));
        return mapToResponse(slot);
    }

    private SlotResponse mapToResponse(Slot slot) {
        Clinic clinic = slot.getClinic();
        return SlotResponse.builder()
                .id(slot.getId())
                .doctorId(slot.getDoctor().getId())
                .doctorName(slot.getDoctor().getUser().getFullName())
                .clinicId(clinic.getId())
                .clinicName(clinic.getName())
                .slotDate(slot.getSlotDate())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .status(slot.getStatus())
                .build();
    }
}