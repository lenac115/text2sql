package com.ai.main.repository;

import com.ai.main.domain.TimeDeal;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimeDealRepository extends JpaRepository<TimeDeal, Long> {

    @Query("SELECT td FROM TimeDeal td JOIN FETCH td.product " +
           "WHERE td.startAt <= :now AND td.endAt > :now AND td.remainingStock > 0 " +
           "ORDER BY td.endAt ASC")
    List<TimeDeal> findActiveDeals(@Param("now") LocalDateTime now);

    @Query("SELECT td FROM TimeDeal td JOIN FETCH td.product " +
           "WHERE td.startAt > :now ORDER BY td.startAt ASC")
    List<TimeDeal> findUpcomingDeals(@Param("now") LocalDateTime now);

    @Query("SELECT td FROM TimeDeal td JOIN FETCH td.product WHERE td.id = :id")
    Optional<TimeDeal> findByIdWithProduct(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT td FROM TimeDeal td WHERE td.id = :id")
    Optional<TimeDeal> findByIdWithLock(@Param("id") Long id);
}