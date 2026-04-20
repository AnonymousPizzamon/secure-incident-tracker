package com.alden.logmonitoringapp;

import com.alden.logmonitoringapp.entity.EventLog;
import com.alden.logmonitoringapp.entity.Severity;
import com.alden.logmonitoringapp.entity.User;
import com.alden.logmonitoringapp.repository.EventLogRepository;
import com.alden.logmonitoringapp.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest //more efficient than springboottest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) //H2 db is default, so this makes it so it uses our postgres db
class PersistenceTest {

    @Autowired //needed or else these would be null, can't create object from interface.
    private UserAccountRepository userRepository;

    @Autowired
    private EventLogRepository eventLogRepository;


    @Test //for test methods
    void saveAndRetrieveEventLog() {

        // create and save a user
        User user = new User("testuser", "hashedpassword");
        userRepository.save(user);

        // create an event, assign the owner, save it
        EventLog event = new EventLog(
                "Test Event",
                Severity.HIGH,
                "server-01",
                "Something happened on the server",
                "192.168.1.1",
                LocalDateTime.now()
        );
        event.setUser(user);
        eventLogRepository.save(event);

        // retrieve it by id and assert the values are correct
        EventLog found = eventLogRepository.findById(event.getId()).orElseThrow();
        assertThat(found.getTitle()).isEqualTo("Test Event");
        assertThat(found.getSeverity()).isEqualTo(Severity.HIGH);
        assertThat(found.getUser().getId()).isEqualTo(user.getId());
    }
}
