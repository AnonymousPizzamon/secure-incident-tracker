package com.alden.logmonitoringapp;

// full-stack tests for event crud, ownership isolation, filtering, and stats.
// same setup as AuthApiTest. the non-owner tests matter most: another user's event
// must return 404 (not 403) so api never reveals it exists.
// https://spring.io/guides/gs/testing-web/
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.MediaType;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.Map;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class EventApiTest {

    @Autowired
    private WebApplicationContext context;

    private RestTestClient restTestClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // bind to the app context and apply springSecurity() so the real jwt filter chain runs
        restTestClient = RestTestClient.bindToApplicationContext(context)
                .configureServer(builder -> builder.apply(springSecurity()))
                .build();
    }

    @Test
    void createEvent_returnsCreatedWithFieldsAndOwner() throws Exception {
        String token = registerAndLogin();
        restTestClient.post().uri("/events")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(eventFields("Failed login", "HIGH"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isNumber()
                .jsonPath("$.title").isEqualTo("Failed login")
                .jsonPath("$.severity").isEqualTo("HIGH")
                .jsonPath("$.incidentStatus").isEqualTo("UNRESOLVED")  // defaults when omitted
                .jsonPath("$.userId").isNumber();  // owner stamped from jwt
    }

    @Test
    void createEvent_withInvalidSeverityValue_returnsBadRequest() throws Exception {
        String token = registerAndLogin();
        // jackson can't map BANANAS to the Severity enum, fails before validation.
        // must come back 400 from the unreadable-body handler, not the 500 catch-all
        restTestClient.post().uri("/events")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(eventFields("Bad severity", "BANANAS"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.error").isEqualTo("Bad Request");
    }

    @Test
    void createEvent_withMalformedJson_returnsBadRequest() throws Exception {
        String token = registerAndLogin();
        // raw string body so jackson sees broken json, not a serialized map
        restTestClient.post().uri("/events")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"title\": \"broken\"")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);
    }

    @Test
    void listEvents_returnsOnlyOwnEvents() throws Exception {
        String ownerToken = registerAndLogin();
        createEvent(ownerToken, eventFields("event one", "LOW"));
        createEvent(ownerToken, eventFields("event two", "HIGH"));

        String otherToken = registerAndLogin();
        createEvent(otherToken, eventFields("other one", "MEDIUM"));

        // GET /events renders as PagedModel: rows under $.content, paging metadata under $.page
        // fresh user so this list only has its own two events, the other user's event never shows up
        restTestClient.get().uri("/events").header("Authorization", "Bearer " + ownerToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(2)
                .jsonPath("$.page.totalElements").isEqualTo(2);

        restTestClient.get().uri("/events").header("Authorization", "Bearer " + otherToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.content.length()").isEqualTo(1);
    }

    @Test
    void getEvent_byOwner_returnsIt() throws Exception {
        String token = registerAndLogin();
        long id = createEvent(token, eventFields("Owned event", "MEDIUM"));
        restTestClient.get().uri("/events/" + id).header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.title").isEqualTo("Owned event");
    }

    @Test
    void getEvent_byNonOwner_returnsNotFound() throws Exception {
        String ownerToken = registerAndLogin();
        long id = createEvent(ownerToken, eventFields("owner secret", "HIGH"));

        String otherToken = registerAndLogin();
        // 404 not 403 so other user can't tell event exists
        restTestClient.get().uri("/events/" + id).header("Authorization", "Bearer " + otherToken)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void updateEvent_byOwner_succeeds() throws Exception {
        String token = registerAndLogin();
        long id = createEvent(token, eventFields("Before", "LOW"));  // starts UNRESOLVED by default

        restTestClient.put().uri("/events/" + id)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(eventFields("After", "CRITICAL", "RESOLVED"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo("After")
                .jsonPath("$.severity").isEqualTo("CRITICAL")
                .jsonPath("$.incidentStatus").isEqualTo("RESOLVED");  // changed from the default
    }

    @Test
    void updateEvent_byNonOwner_returnsNotFoundAndLeavesItUnchanged() throws Exception {
        String ownerToken = registerAndLogin();
        long id = createEvent(ownerToken, eventFields("original title", "LOW"));

        String otherToken = registerAndLogin();
        restTestClient.put().uri("/events/" + id)
                .header("Authorization", "Bearer " + otherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(eventFields("attempted overwrite", "CRITICAL"))
                .exchange()
                .expectStatus().isNotFound();

        // owner's event is untouched
        restTestClient.get().uri("/events/" + id).header("Authorization", "Bearer " + ownerToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.title").isEqualTo("original title");
    }

    @Test
    void deleteEvent_byOwner_removesItFromGetAndList() throws Exception {
        String token = registerAndLogin();
        long id = createEvent(token, eventFields("To delete", "LOW"));

        restTestClient.delete().uri("/events/" + id).header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNoContent();

        restTestClient.get().uri("/events/" + id).header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNotFound();

        restTestClient.get().uri("/events").header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.content.length()").isEqualTo(0);
    }

    @Test
    void deleteEvent_byNonOwner_returnsNotFoundAndLeavesItAlive() throws Exception {
        String ownerToken = registerAndLogin();
        long id = createEvent(ownerToken, eventFields("Keep me", "LOW"));

        String otherToken = registerAndLogin();
        restTestClient.delete().uri("/events/" + id).header("Authorization", "Bearer " + otherToken)
                .exchange()
                .expectStatus().isNotFound();

        restTestClient.get().uri("/events/" + id).header("Authorization", "Bearer " + ownerToken)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void filter_bySeverity_returnsOnlyMatching() throws Exception {
        String token = registerAndLogin();
        createEvent(token, eventFields("high one", "HIGH"));
        createEvent(token, eventFields("low one", "LOW"));

        restTestClient.get().uri("/events?severity=HIGH").header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(1)
                .jsonPath("$.content[0].severity").isEqualTo("HIGH");
    }

    @Test
    void filter_byDateRange_returnsOnlyEventsInsideRange() throws Exception {
        String token = registerAndLogin();
        // the rejected events miss by one second, so a comparison that is off at all fails here.
        // the kept one sits exactly on startDate because the bounds are inclusive
        Map<String, Object> inside = eventFields("inside range", "HIGH");
        inside.put("eventTimestamp", "2026-01-01T00:00:00");
        createEvent(token, inside);
        Map<String, Object> tooEarly = eventFields("one second early", "LOW");
        tooEarly.put("eventTimestamp", "2025-12-31T23:59:59");
        createEvent(token, tooEarly);
        Map<String, Object> tooLate = eventFields("one second late", "LOW");
        tooLate.put("eventTimestamp", "2026-08-01T00:00:01");
        createEvent(token, tooLate);

        restTestClient.get().uri("/events?startDate=2026-01-01T00:00:00&endDate=2026-08-01T00:00:00")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(1)
                .jsonPath("$.content[0].title").isEqualTo("inside range");
    }

    // eventTimestamp is optional and a null one is left out of date filters on purpose
    // sql compares null to anything as null, never true, so the row falls outside every range
    // it is still reachable in the unfiltered list, where the ui will label the missing date
    @Test
    void filter_byDateRange_excludesEventWithNoTimestamp() throws Exception {
        String token = registerAndLogin();
        Map<String, Object> dated = eventFields("has timestamp", "HIGH");
        dated.put("eventTimestamp", "2026-06-01T10:00:00");
        createEvent(token, dated);
        Map<String, Object> undated = eventFields("no timestamp", "LOW");
        undated.remove("eventTimestamp");
        createEvent(token, undated);

        restTestClient.get().uri("/events?startDate=2000-01-01T00:00:00&endDate=2100-01-01T00:00:00")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(1)
                .jsonPath("$.content[0].title").isEqualTo("has timestamp");

        // unfiltered the event is still there, so it is hidden from date ranges, not lost
        restTestClient.get().uri("/events").header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.page.totalElements").isEqualTo(2);
    }

    @Test
    void stats_countOnlyOwnEvents() throws Exception {
        String token = registerAndLogin();
        createEvent(token, eventFields("one", "HIGH", "UNRESOLVED"));
        createEvent(token, eventFields("two", "HIGH", "RESOLVED"));
        createEvent(token, eventFields("three", "LOW", "IN_PROGRESS"));

        restTestClient.get().uri("/events/stats").header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.totalEvents").isEqualTo(3)
                .jsonPath("$.countBySeverity.HIGH").isEqualTo(2)
                .jsonPath("$.countBySeverity.LOW").isEqualTo(1)
                .jsonPath("$.countByStatus.UNRESOLVED").isEqualTo(1)
                .jsonPath("$.countByStatus.RESOLVED").isEqualTo(1)
                .jsonPath("$.countByStatus.IN_PROGRESS").isEqualTo(1);
    }

    @Test
    void deletedEvent_isNotCountedInStats() throws Exception {
        String token = registerAndLogin();
        long id = createEvent(token, eventFields("stays", "HIGH"));
        createEvent(token, eventFields("goes", "LOW"));

        restTestClient.delete().uri("/events/" + id).header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNoContent();

        restTestClient.get().uri("/events/stats").header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.totalEvents").isEqualTo(1);
    }

    // unique per call so tests don't collide with each other or rows already in db
    private String uniqueName() {
        return "user_" + UUID.randomUUID().toString().substring(0, 8);
    }

    // event body without a status, so the server defaults it to UNRESOLVED
    private Map<String, Object> eventFields(String title, String severity) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("title", title);
        fields.put("severity", severity);
        fields.put("sourceName", "server-01");
        fields.put("description", "test event");
        fields.put("originIp", "10.0.0.1");
        fields.put("eventTimestamp", "2026-06-01T10:00:00");
        return fields;
    }

    // same body but with an explicit status, for the tests that assert on it
    private Map<String, Object> eventFields(String title, String severity, String incidentStatus) {
        Map<String, Object> fields = eventFields(title, severity);
        fields.put("incidentStatus", incidentStatus);
        return fields;
    }

    // make a fresh unique user and return their bearer token
    private String registerAndLogin() throws Exception {
        String username = uniqueName();
        restTestClient.post().uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("username", username, "email", username + "@example.com", "password", "password123"))
                .exchange().expectStatus().isCreated();
        String loginBody = restTestClient.post().uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("username", username, "password", "password123"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).returnResult().getResponseBody();
        return objectMapper.readTree(loginBody).get("token").asText();
    }

    // create an event as the given user and return its generated id
    private long createEvent(String token, Map<String, ?> fields) throws Exception {
        String body = restTestClient.post().uri("/events")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(fields)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(String.class).returnResult().getResponseBody();
        return objectMapper.readTree(body).get("id").asLong();
    }
}
