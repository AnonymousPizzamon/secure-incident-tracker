package com.alden.logmonitoringapp.entity;


import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_logs")
public class Event {

    protected Event(){} // no arg

    public Event(String title, IncidentStatus status, Severity severity, String sourceName,
                 String description, String originIp, LocalDateTime eventTimestamp) {
        this.title = title;
        this.incidentStatus = status;
        this.severity = severity;
        this.sourceName = sourceName;
        this.description = description;
        this.originIp = originIp;
        this.eventTimestamp = eventTimestamp;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Long (wrapper) so it will be null before generation.
                    // Could also use Integer but Long is safer from overflow.

    // many events to one user
    @ManyToOne(fetch = FetchType.LAZY) // User row isn't loaded unless getUser() is called.
    @JoinColumn(name = "user_id", nullable = false) // Creates the user_id column in event_logs.
    private User user;


    // Db can't store type "Severity", need to convert to acceptable datatype.
    // Storing explicitly as string is clearer than EnumType.ORDINAL (0,1,2...).
    @Column (nullable = false)
    @Enumerated(EnumType.STRING)
    private Severity severity;

    @Column (nullable = false)
    @Enumerated(EnumType.STRING)
    private IncidentStatus incidentStatus;

    // Default type String changed to TEXT as stored type in database.
    // String has 255 char limit vs text which is unlimited.
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String title;

    private String sourceName;  // where log originated
    private String originIp;
    private LocalDateTime eventTimestamp; // when event was detected by sources (will be optional)
    @Column(nullable = false)
    private LocalDateTime timeCreated;    // when log was added in app


    @PrePersist // Calls it automatically before insertion.
    protected void onCreate() {
        this.timeCreated = LocalDateTime.now();
    }


    public void setUser(User user) { this.user = user; } // need to set user for each EventLog

    public Long getId() { return id; }
    public User getUser() { return user; }
    public IncidentStatus getIncidentStatus() { return incidentStatus; }
    public Severity getSeverity() { return severity; }
    public String getDescription() { return description; }
    public String getTitle() { return title; }
    public String getSourceName() { return sourceName; }
    public String getOriginIp() { return originIp; }
    public LocalDateTime getEventTimestamp() { return eventTimestamp; }
    public LocalDateTime getTimeCreated() { return timeCreated; }
}
