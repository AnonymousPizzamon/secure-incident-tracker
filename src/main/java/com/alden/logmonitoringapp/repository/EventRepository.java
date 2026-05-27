package com.alden.logmonitoringapp.repository;

import com.alden.logmonitoringapp.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
/**
 *
 * JpaRepository<Event, Long> gives: save, findById, findAll, delete, and count.
 * Custom queries for filtering come in a later step.
 */




}
