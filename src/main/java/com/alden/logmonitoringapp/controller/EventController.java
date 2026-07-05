package com.alden.logmonitoringapp.controller;

// https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller.html
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alden.logmonitoringapp.entity.IncidentStatus;
import com.alden.logmonitoringapp.entity.Severity;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import com.alden.logmonitoringapp.dto.event.EventUpdateRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import com.alden.logmonitoringapp.entity.User;
import com.alden.logmonitoringapp.dto.event.EventCreateRequest;
import jakarta.validation.Valid;
import com.alden.logmonitoringapp.dto.event.EventStatsResponse;
import com.alden.logmonitoringapp.dto.event.EventResponse;
import org.springframework.http.ResponseEntity;
import com.alden.logmonitoringapp.security.CurrentUserProvider;
import com.alden.logmonitoringapp.service.EventService;

@RestController
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;
    private final CurrentUserProvider currentUserProvider;

    public EventController(EventService eventService, CurrentUserProvider currentUserProvider) {
        this.eventService = eventService;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping
    public ResponseEntity<EventResponse> create(@Valid @RequestBody EventCreateRequest request) {
        // owner comes from the JWT via CurrentUserProvider, never from the request body
        User owner = currentUserProvider.getCurrentUser();
        //Use eventService to create an EventResponse
        EventResponse response = eventService.create(request, owner);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /events. all filters optional, owner scoped, paged
    // @DateTimeFormat parses ISO datetimes like 2026-06-01T00:00:00
    // @PageableDefault sets newest first, 20 per page when no paging params are sent
    @GetMapping
    public Page<EventResponse> list(
            @RequestParam(required = false) Severity severity,
            @RequestParam(required = false) IncidentStatus incidentStatus,
            @RequestParam(required = false) String sourceName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 20, sort = "timeCreated", direction = Sort.Direction.DESC) Pageable pageable) {
        User owner = currentUserProvider.getCurrentUser();
        return eventService.search(owner, severity, incidentStatus, sourceName, startDate, endDate, pageable);
    }

    // GET /events/stats. summary of the caller's own events
    // literal /stats outranks /{id} so "stats" is never parsed as an id
    @GetMapping("/stats")
    public EventStatsResponse stats() {
        User owner = currentUserProvider.getCurrentUser();
        return eventService.stats(owner);
    }

    // GET /events/{id}. @PathVariable means it gets id from url path
    // service returns 404 if the event is missing or not owned
    @GetMapping("/{id}")
    public EventResponse getOne(@PathVariable Long id) {
        User owner = currentUserProvider.getCurrentUser();
        return eventService.getForUser(id, owner);
    }

    @PutMapping("/{id}") // PUT because we are replacing info not creating new event (idempotent: same result even if done multiple times unlike POST which would create duplicates)
    public EventResponse updateEvent(@PathVariable Long id, @Valid @RequestBody EventUpdateRequest request) {
        User owner = currentUserProvider.getCurrentUser();
        // again, identity is from current JWT not http req's
        EventResponse response = eventService.update(request, id, owner);
        return response;
    }

    // DELETE /events/{id}. no body to return so 204 No Content
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        User owner = currentUserProvider.getCurrentUser();
        eventService.delete(id, owner);
    }

}
