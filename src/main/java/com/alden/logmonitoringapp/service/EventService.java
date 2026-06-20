package com.alden.logmonitoringapp.service;

// https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html
import com.alden.logmonitoringapp.entity.Event;
import com.alden.logmonitoringapp.entity.User;
import com.alden.logmonitoringapp.dto.event.EventResponse;
import com.alden.logmonitoringapp.dto.event.EventCreateRequest;
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
}
