package com.medibook.calendar.service;

import com.medibook.appointment.entity.Appointment;
import com.medibook.appointment.entity.AppointmentStatus;
import com.medibook.appointment.repository.AppointmentRepository;
import com.medibook.calendar.dto.*;
import com.medibook.calendar.entity.DoctorHoliday;
import com.medibook.common.exception.HolidayConflictException;
import com.medibook.calendar.repository.DoctorHolidayRepository;
import com.medibook.common.exception.ResourceNotFoundException;
import com.medibook.doctor.entity.Doctor;
import com.medibook.doctor.repository.DoctorRepository;
import com.medibook.slot.entity.Slot;
import com.medibook.slot.repository.SlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CalendarService {

    private static final Map<String, String> STATUS_COLORS = Map.of(
            "AVAILABLE", "#4CAF50",
            "BOOKED", "#2196F3",
            "COMPLETED", "#9C27B0",
            "BLOCKED", "#FF9800"
    );

    private final DoctorHolidayRepository holidayRepository;
    private final DoctorRepository doctorRepository;
    private final SlotRepository slotRepository;
    private final AppointmentRepository appointmentRepository;

    public CalendarService(DoctorHolidayRepository holidayRepository,
                            DoctorRepository doctorRepository,
                            SlotRepository slotRepository,
                            AppointmentRepository appointmentRepository) {
        this.holidayRepository = holidayRepository;
        this.doctorRepository = doctorRepository;
        this.slotRepository = slotRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional(readOnly = true)
    public CalendarResponse getDoctorCalendar(Long doctorId, LocalDate fromDate, LocalDate toDate,
                                               boolean includePatientDetails) {
        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("fromDate must not be after toDate");
        }

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + doctorId));

        List<Slot> slots = slotRepository.findByDoctor_IdAndSlotDateBetweenOrderBySlotDateAscStartTimeAsc(doctorId, fromDate, toDate);
        List<DoctorHoliday> holidays = holidayRepository.findByDoctorIdAndHolidayDateBetween(doctorId, fromDate, toDate);

        Set<LocalDate> holidayDates = holidays.stream()
                .map(DoctorHoliday::getHolidayDate)
                .collect(Collectors.toSet());
        Map<LocalDate, String> holidayReasons = holidays.stream()
                .collect(Collectors.toMap(DoctorHoliday::getHolidayDate, h -> h.getReason() == null ? "" : h.getReason()));

        Map<LocalDate, List<Slot>> slotsByDate = slots.stream()
                .collect(Collectors.groupingBy(Slot::getSlotDate));

        List<Long> slotIds = slots.stream().map(Slot::getId).collect(Collectors.toList());
        Map<Long, Appointment> appointmentBySlotId = slotIds.isEmpty()
                ? Collections.emptyMap()
                : appointmentRepository
                    .findBySlotIdInAndStatusIn(slotIds, List.of(AppointmentStatus.BOOKED, AppointmentStatus.COMPLETED))
                    .stream()
                    .collect(Collectors.toMap(a -> a.getSlot().getId(), a -> a));

        List<CalendarDayResponse> days = new ArrayList<>();
        for (LocalDate date = fromDate; !date.isAfter(toDate); date = date.plusDays(1)) {
            CalendarDayResponse dayResponse = new CalendarDayResponse();
            dayResponse.setDate(date);
            boolean isHoliday = holidayDates.contains(date);
            dayResponse.setHoliday(isHoliday);
            dayResponse.setHolidayReason(isHoliday ? holidayReasons.get(date) : null);

            List<Slot> daySlots = slotsByDate.getOrDefault(date, Collections.emptyList());
            List<CalendarSlotResponse> slotResponses = daySlots.stream()
                    .sorted(Comparator.comparing(Slot::getStartTime))
                    .map(slot -> toSlotResponse(slot, appointmentBySlotId.get(slot.getId()), includePatientDetails))
                    .collect(Collectors.toList());

            dayResponse.setSlots(slotResponses);
            days.add(dayResponse);
        }

        CalendarResponse response = new CalendarResponse();
        response.setDoctorId(doctorId);
        response.setDoctorName(doctor.getUser().getFullName());
        response.setFromDate(fromDate);
        response.setToDate(toDate);
        response.setDays(days);
        return response;
    }

    private CalendarSlotResponse toSlotResponse(Slot slot, Appointment appointment, boolean includePatientDetails) {
        CalendarSlotResponse response = new CalendarSlotResponse();
        response.setSlotId(slot.getId());
        response.setStartTime(slot.getStartTime());
        response.setEndTime(slot.getEndTime());

        String effectiveStatus = (appointment != null && appointment.getStatus() == AppointmentStatus.COMPLETED)
                ? "COMPLETED"
                : slot.getStatus().name();

        response.setStatus(effectiveStatus);
        response.setDisplayColor(STATUS_COLORS.getOrDefault(effectiveStatus, "#9E9E9E"));

        if (appointment != null) {
            response.setAppointmentId(appointment.getId());
            if (includePatientDetails) {
                response.setPatientName(appointment.getPatient().getFullName());
            }
        }
        return response;
    }

    @Transactional
    public HolidayResponse addHoliday(Long doctorId, HolidayRequest request) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + doctorId));

        if (holidayRepository.existsByDoctorIdAndHolidayDate(doctorId, request.getHolidayDate())) {
            throw new HolidayConflictException("Holiday already marked for this doctor on " + request.getHolidayDate());
        }

        DoctorHoliday holiday = new DoctorHoliday();
        holiday.setDoctor(doctor);
        holiday.setHolidayDate(request.getHolidayDate());
        holiday.setReason(request.getReason());
        holiday = holidayRepository.save(holiday);

        return toHolidayResponse(holiday);
    }

    @Transactional(readOnly = true)
    public List<HolidayResponse> getHolidays(Long doctorId, LocalDate fromDate, LocalDate toDate) {
        return holidayRepository.findByDoctorIdAndHolidayDateBetween(doctorId, fromDate, toDate)
                .stream()
                .map(this::toHolidayResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeHoliday(Long holidayId) {
        DoctorHoliday holiday = holidayRepository.findById(holidayId)
                .orElseThrow(() -> new ResourceNotFoundException("Holiday not found with id: " + holidayId));
        holidayRepository.delete(holiday);
    }

    private HolidayResponse toHolidayResponse(DoctorHoliday holiday) {
        HolidayResponse response = new HolidayResponse();
        response.setId(holiday.getId());
        response.setDoctorId(holiday.getDoctor().getId());
        response.setHolidayDate(holiday.getHolidayDate());
        response.setReason(holiday.getReason());
        return response;
    }
}