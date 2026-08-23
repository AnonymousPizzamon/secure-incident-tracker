package com.alden.logmonitoringapp;

// full-stack tests for register, login, and protected route access.
// @SpringBootTest boots the whole app, so the real security chain and jwt logic run.
// RestTestClient calls the api like a client would. real postgres (replace = NONE) like PersistenceTest.
// each test uses a unique username so it won't clash with rows already in db.
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
import java.util.UUID;
import java.util.Map;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional // rolls back tests / leaves no trace in DB
class AuthApiTest {

    @Autowired
    private WebApplicationContext context;

    private RestTestClient restTestClient;

    // local mapper to read fields back out of responses
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // guide injects RestTestClient directly, but its sample app has no security.
        // ours binds to the app context and applies springSecurity() so the real jwt filter chain runs.
        restTestClient = RestTestClient.bindToApplicationContext(context)
                .configureServer(builder -> builder.apply(springSecurity()))
                .build();
    }

    @Test
    void register_returnsCreatedAndUserBody() throws Exception {
        String username = uniqueName();
        String email = username + "@example.com";
        restTestClient.post().uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("username", username, "email", email, "password", "password123"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.username").isEqualTo(username)
                .jsonPath("$.email").isEqualTo(email)
                .jsonPath("$.id").isNumber();
    }

    @Test
    void register_duplicateUsername_returnsConflict() throws Exception {
        String username = uniqueName();
        Map<String, String> body = Map.of("username", username, "email", username + "@example.com", "password", "password123");
        restTestClient.post().uri("/auth/register").contentType(MediaType.APPLICATION_JSON).body(body)
                .exchange().expectStatus().isCreated();
        // second register clashes with the row we just made, handler returns 409
        restTestClient.post().uri("/auth/register").contentType(MediaType.APPLICATION_JSON).body(body)
                .exchange().expectStatus().isEqualTo(409);
    }

    @Test
    void register_shortPassword_returnsBadRequest() throws Exception {
        String username = uniqueName();
        // password under @Size(min = 8), validation fails with 400
        restTestClient.post().uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("username", username, "email", username + "@example.com", "password", "short"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.fieldErrors.password").exists();
    }

    @Test
    void login_withValidCredentials_returnsToken() throws Exception {
        String username = uniqueName();
        register(username, username + "@example.com", "password123");
        restTestClient.post().uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("username", username, "password", "password123"))
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.token").isNotEmpty();
    }

    @Test
    void login_withWrongPassword_returnsUnauthorized() throws Exception {
        String username = uniqueName();
        register(username, username + "@example.com", "password123");
        // wrong password returns 401 with one generic message
        restTestClient.post().uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("username", username, "password", "wrongpassword"))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void protectedRoute_withoutToken_isRejected() throws Exception {
        // SecurityConfig's entry point returns 401 for an unauthenticated request
        restTestClient.get().uri("/users/me")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void protectedRoute_withValidToken_returnsCurrentUser() throws Exception {
        String username = uniqueName();
        register(username, username + "@example.com", "password123");
        String token = login(username, "password123");
        // identity comes from the token, not the client
        restTestClient.get().uri("/users/me")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.username").isEqualTo(username)
                .jsonPath("$.email").isEqualTo(username + "@example.com");
    }

    // unique per call so tests don't collide with each other or rows already in db
    private String uniqueName() {
        return "user_" + UUID.randomUUID().toString().substring(0, 8);
    }

    private void register(String username, String email, String password) {
        restTestClient.post().uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("username", username, "email", email, "password", password))
                .exchange().expectStatus().isCreated();
    }

    // log in and pull raw token string out of the {"token":"..."} response
    private String login(String username, String password) throws Exception {
        String responseBody = restTestClient.post().uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("username", username, "password", password))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).returnResult().getResponseBody();
        return objectMapper.readTree(responseBody).get("token").asText();
    }
}
