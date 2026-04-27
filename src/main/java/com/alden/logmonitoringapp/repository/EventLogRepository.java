package com.alden.logmonitoringapp.repository;

import com.alden.logmonitoringapp.entity.EventLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventLogRepository extends JpaRepository<EventLog, Long> {
/**
 *
 * JpaRepository<EventLog, Long> gives: save, findById, findAll, delete, and count.
 * Custom queries for filtering come in a later step.
 */




}
