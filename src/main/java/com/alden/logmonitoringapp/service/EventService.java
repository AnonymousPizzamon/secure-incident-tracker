package com.alden.logmonitoringapp.service;

// https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html
import java.util.EnumMap;
import java.util.Map;
import java.time.LocalDateTime;
import com.alden.logmonitoringapp.dto.event.EventStatsResponse;
import com.alden.logmonitoringapp.entity.IncidentStatus;
import com.alden.logmonitoringapp.entity.Severity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import com.alden.logmonitoringapp.dto.event.EventCreateRequest;
import com.alden.logmonitoringapp.dto.event.EventUpdateRequest;
import com.alden.logmonitoringapp.exception.NotFoundException;
import com.alden.logmonitoringapp.entity.Event;
import com.alden.logmonitoringapp.entity.User;
import com.alden.logmonitoringapp.dto.event.EventResponse;
import com.alden.logmonitoringapp.repository.EventRepository;
import org.springframework.stereotype.Service;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public EventResponse create(EventCreateRequest request, User owner) {
        Event event = new Event(
                request.getTitle(),
                request.getIncidentStatus(),
                request.getSeverity(),
                request.getSourceName(),
                request.getDescription(),
                request.getOriginIp(),
                request.getEventTimestamp()
        );
        event.setUser(owner);
        Event saved = eventRepository.save(event);
        return toResponse(saved);
    }

    // formats to fit EventResponse constructor
    EventResponse toResponse(Event event) {
        return new EventResponse(
                event.getId(),
                event.getUser().getId(),
                event.getTitle(),
                event.getSeverity(),
                event.getIncidentStatus(),
                event.getSourceName(),
                event.getDescription(),
                event.getOriginIp(),
                event.getEventTimestamp(),
                event.getTimeCreated()
        );
    }

    // owner-scoped filtered page. .map converts each Event to a dto and keeps the paging metadata
    public Page<EventResponse> search(User owner, Severity severity, IncidentStatus status,
            String sourceName, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return eventRepository.search(owner, severity, status, sourceName, startDate, endDate, pageable)
                .map(this::toResponse);
    }

    // empty Optional means missing or not owned. both become 404 via NotFoundException
    public EventResponse getForUser(Long id, User owner) {
        Event event = eventRepository.findByIdAndUser(id, owner)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        return toResponse(event);
    }

    public EventResponse update(EventUpdateRequest request, Long id, User owner) {
        Event event = eventRepository.findByIdAndUser(id, owner)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        event.updateFields(
                request.getTitle(), request.getSeverity(), request.getIncidentStatus(), request.getSourceName(), request.getDescription(), request.getOriginIp()
        );

        eventRepository.save(event); // updates original event in repo
        return toResponse(event); // return dto for json response


    }

    // load first so we only delete an event the caller owns
    // deleting by id alone would skip the ownership check
    public void delete(Long id, User owner) {
        Event event = eventRepository.findByIdAndUser(id, owner)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        eventRepository.delete(event);
    }

    // owner-scoped summary. counts come from aggregate queries, not by loading every row
    public EventStatsResponse stats(User owner) {
        long total = eventRepository.countByUser(owner);

        // start every bucket at zero so a severity with no events still shows up as 0
        Map<Severity, Long> bySeverity = new EnumMap<>(Severity.class);
        for (Severity s : Severity.values()) {
            bySeverity.put(s, 0L);
        }
        // each row from the GROUP BY is [Severity, Long]. overwrite the zero with the real count
        for (Object[] row : eventRepository.countBySeverity(owner)) {
            bySeverity.put((Severity) row[0], (Long) row[1]);
        }

        Map<IncidentStatus, Long> byStatus = new EnumMap<>(IncidentStatus.class);
        for (IncidentStatus st : IncidentStatus.values()) {
            byStatus.put(st, 0L);
        }
        for (Object[] row : eventRepository.countByStatus(owner)) {
            byStatus.put((IncidentStatus) row[0], (Long) row[1]);
        }

        LocalDateTime mostRecent = eventRepository.findMostRecentEventTime(owner);

        return new EventStatsResponse(total, bySeverity, byStatus, mostRecent);
    }
}
