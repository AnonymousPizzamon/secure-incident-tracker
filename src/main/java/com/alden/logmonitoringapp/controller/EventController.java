package com.alden.logmonitoringapp.controller;

// https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller.html
import java.util.List;

import com.alden.logmonitoringapp.dto.event.EventUpdateRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import com.alden.logmonitoringapp.entity.User;
import com.alden.logmonitoringapp.dto.event.EventCreateRequest;
import jakarta.validation.Valid;
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

    // GET /events returns only this owner's events. 200 with a json array
    @GetMapping
    public List<EventResponse> list() {
        User owner = currentUserProvider.getCurrentUser();
        return eventService.listForUser(owner);
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
