package com.medibook.calendar.repository;

import com.medibook.calendar.entity.DoctorHoliday;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DoctorHolidayRepository extends JpaRepository<DoctorHoliday, Long> {

    List<DoctorHoliday> findByDoctorIdAndHolidayDateBetween(Long doctorId, LocalDate from, LocalDate to);

    boolean existsByDoctorIdAndHolidayDate(Long doctorId, LocalDate date);
}