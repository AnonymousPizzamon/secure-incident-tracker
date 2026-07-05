package com.alden.logmonitoringapp.dto.event;

// response for GET /events/stats. summary numbers for the caller's own events
import java.util.Map;
import java.time.LocalDateTime;
import com.alden.logmonitoringapp.entity.IncidentStatus;
import com.alden.logmonitoringapp.entity.Severity;

public class EventStatsResponse {

    private long totalEvents;
    private Map<Severity, Long> countBySeverity;
    private Map<IncidentStatus, Long> countByStatus;
    private LocalDateTime mostRecentEventTime;

    public EventStatsResponse(long totalEvents, Map<Severity, Long> countBySeverity,
                              Map<IncidentStatus, Long> countByStatus, LocalDateTime mostRecentEventTime) {
        this.totalEvents = totalEvents;
        this.countBySeverity = countBySeverity;
        this.countByStatus = countByStatus;
        this.mostRecentEventTime = mostRecentEventTime;
    }

    public long getTotalEvents() { return totalEvents; }
    public Map<Severity, Long> getCountBySeverity() { return countBySeverity; }
    public Map<IncidentStatus, Long> getCountByStatus() { return countByStatus; }
    public LocalDateTime getMostRecentEventTime() { return mostRecentEventTime; }
}
