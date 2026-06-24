package com.alden.logmonitoringapp.dto.event;

import com.alden.logmonitoringapp.entity.IncidentStatus;
import com.alden.logmonitoringapp.entity.Severity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

// doesn't contain id, user, or timestamp, these should be unchanged.

public class EventUpdateRequest {
    @NotBlank
    private String title;

    @NotNull
    private Severity severity;

    private IncidentStatus incidentStatus;
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
    public void setOriginIp(String originIp) { this.originIp = originIp;}
}
