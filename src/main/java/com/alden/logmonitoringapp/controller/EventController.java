package com.alden.logmonitoringapp.controller;

// https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller.html
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.HttpStatus;
import com.alden.logmonitoringapp.entity.User;
import com.alden.logmonitoringapp.dto.event.EventCreateRequest;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import com.alden.logmonitoringapp.dto.event.EventResponse;
import org.springframework.http.ResponseEntity;
import com.alden.logmonitoringapp.security.CurrentUserProvider;
import com.alden.logmonitoringapp.service.EventService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
