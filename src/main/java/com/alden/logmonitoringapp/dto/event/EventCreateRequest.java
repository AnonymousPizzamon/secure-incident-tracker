package com.alden.logmonitoringapp.dto.event;

// request body for POST /events. no user id — owner comes from the JWT

import java.time.LocalDateTime;
import com.alden.logmonitoringapp.entity.IncidentStatus;
import jakarta.validation.constraints.NotNull;
import com.alden.logmonitoringapp.entity.Severity;
import jakarta.validation.constraints.NotBlank;

public class EventCreateRequest {

    @NotBlank
    private String title;

    @NotNull
    private Severity severity;

    // defaults to UNRESOLVED if the client omits it
    private IncidentStatus incidentStatus = IncidentStatus.UNRESOLVED;

    private String sourceName;
    private String description;
    private String originIp;
    private LocalDateTime eventTimestamp;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    public IncidentStatus getIncidentStatus() { return incidentStatus; }
    public void setIncidentStatus(IncidentStatus incidentStatus) { this.incidentStatus = incidentStatus; }
    public String getSourceName() { return sourceName; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getOriginIp() { return originIp; }
    public void setOriginIp(String originIp) { this.originIp = originIp; }
    public LocalDateTime getEventTimestamp() { return eventTimestamp; }
    public void setEventTimestamp(LocalDateTime eventTimestamp) { this.eventTimestamp = eventTimestamp; }
}
