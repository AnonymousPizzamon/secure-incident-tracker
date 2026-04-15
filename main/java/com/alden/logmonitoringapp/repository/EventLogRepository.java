package com.alden.logmonitoringapp.repository;

import com.alden.logmonitoringapp.entity.EventLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventLogRepository extends JpaRepository<EventLog, Long> {




}
