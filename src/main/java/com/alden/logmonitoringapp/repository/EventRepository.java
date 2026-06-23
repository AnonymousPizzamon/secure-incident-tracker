package com.alden.logmonitoringapp.repository;

// Spring Data derives the SQL from the method name. no @Query needed
// https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html
import com.alden.logmonitoringapp.entity.User;
import java.util.Optional;
import com.alden.logmonitoringapp.entity.Event;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {

    // JpaRepository<Event, Long> gives save, findById, findAll, delete, count.

    // findByUser becomes WHERE user_id = ?. lists only this owner's events
    List<Event> findByUser(User user);

    // findByIdAndUser becomes WHERE id = ? AND user_id = ?.
    // checks ownership in the query so another user's row is never loaded.
    Optional<Event> findByIdAndUser(Long id, User user);

}
