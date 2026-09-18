package com.medibook.slot.repository;

import com.medibook.slot.entity.Slot;
import com.medibook.slot.enums.SlotStatus;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SlotRepository extends JpaRepository<Slot, Long> {

    // Pessimistic write lock — holds the row lock until the enclosing
    // transaction commits/rolls back. 3s lock-wait timeout so a losing
    // request fails fast instead of hanging.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    @Query("SELECT s FROM Slot s WHERE s.id = :id")
    Optional<Slot> findByIdForUpdate(@Param("id") Long id);

    List<Slot> findByDoctor_IdAndSlotDateBetweenOrderBySlotDateAscStartTimeAsc(
            Long doctorId, LocalDate fromDate, LocalDate toDate);

    List<Slot> findByDoctor_IdAndSlotDateBetweenAndStatusOrderBySlotDateAscStartTimeAsc(
            Long doctorId, LocalDate fromDate, LocalDate toDate, SlotStatus status);

    // Used during bulk generation to skip dates/times that already have a slot
    @Query("SELECT s.slotDate, s.startTime FROM Slot s " +
           "WHERE s.doctor.id = :doctorId AND s.slotDate BETWEEN :fromDate AND :toDate")
    List<Object[]> findExistingDateTimeKeys(@Param("doctorId") Long doctorId,
                                             @Param("fromDate") LocalDate fromDate,
                                             @Param("toDate") LocalDate toDate);
}