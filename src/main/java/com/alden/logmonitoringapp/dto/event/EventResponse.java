package com.alden.logmonitoringapp.dto.event;

// response shape for all event endpoints
// no nested User object
import java.time.LocalDateTime;
import com.alden.logmonitoringapp.entity.IncidentStatus;
import com.alden.logmonitoringapp.entity.Severity;

public class EventResponse {

    private Long id;
    private Long userId;
    private String title;
    private Severity severity;
    private IncidentStatus incidentStatus;
    private String sourceName;
    private String description;
    private String originIp;
    private LocalDateTime eventTimestamp;
    private LocalDateTime timeCreated;

    public EventResponse(Long id, Long userId, String title, Severity severity,
                         IncidentStatus incidentStatus, String sourceName, String description,
                         String originIp, LocalDateTime eventTimestamp, LocalDateTime timeCreated) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.severity = severity;
        this.incidentStatus = incidentStatus;
        this.sourceName = sourceName;
        this.description = description;
        this.originIp = originIp;
        this.eventTimestamp = eventTimestamp;
        this.timeCreated = timeCreated;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getTitle() { return title; }
    public Severity getSeverity() { return severity; }
    public IncidentStatus getIncidentStatus() { return incidentStatus; }
    public String getSourceName() { return sourceName; }
    public String getDescription() { return description; }
    public String getOriginIp() { return originIp; }
    public LocalDateTime getEventTimestamp() { return eventTimestamp; }
    public LocalDateTime getTimeCreated() { return timeCreated; }
}
