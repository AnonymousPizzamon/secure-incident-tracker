package com.alden.logmonitoringapp.repository;

// Spring Data derives the SQL from the method name. no @Query needed
// https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import com.alden.logmonitoringapp.entity.IncidentStatus;
import com.alden.logmonitoringapp.entity.Severity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import com.alden.logmonitoringapp.entity.User;
import java.util.List;
import java.util.Optional;
import com.alden.logmonitoringapp.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {

    // JpaRepository<Event, Long> gives save, findById, findAll, delete, count.

    // findByIdAndUser becomes WHERE id = ? AND user_id = ?.
    // checks ownership in the query so another user's row is never loaded.
    Optional<Event> findByIdAndUser(Long id, User user);

    // filtering-
    // one query, every filter optional so that multiple filters can be applied together.
    // owner is always applied so results can never cross users
    // @Param names the jpql bind variables so they match by name not position
    // cast on the dates: postgres cannot infer a param type from ":x IS NULL" alone, so we tell it timestamp
    @Query("SELECT e FROM Event e WHERE e.user = :owner "
            + "AND (:severity IS NULL OR e.severity = :severity) "
            + "AND (:status IS NULL OR e.incidentStatus = :status) "
            + "AND (:sourceName IS NULL OR e.sourceName = :sourceName) "
            + "AND (cast(:startDate as timestamp) IS NULL OR e.eventTimestamp >= :startDate) "
            + "AND (cast(:endDate as timestamp) IS NULL OR e.eventTimestamp <= :endDate)")
    Page<Event> search(@Param("owner") User owner,
                       @Param("severity") Severity severity,
                       @Param("status") IncidentStatus status,
                       @Param("sourceName") String sourceName,
                       @Param("startDate") LocalDateTime startDate,
                       @Param("endDate") LocalDateTime endDate,
                       Pageable pageable);

    // stats-
    // total number of the caller's events
    long countByUser(User owner);

    // GROUP BY gives one row per severity with its count. the database does the counting
    // each row is [Severity, Long]. severities with zero events are simply absent
    @Query("SELECT e.severity, COUNT(e) FROM Event e WHERE e.user = :owner GROUP BY e.severity")
    List<Object[]> countBySeverity(@Param("owner") User owner);

    @Query("SELECT e.incidentStatus, COUNT(e) FROM Event e WHERE e.user = :owner GROUP BY e.incidentStatus")
    List<Object[]> countByStatus(@Param("owner") User owner);

    // MAX ignores null timestamps. returns null when the user has no dated events
    @Query("SELECT MAX(e.eventTimestamp) FROM Event e WHERE e.user = :owner")
    LocalDateTime findMostRecentEventTime(@Param("owner") User owner);

}
