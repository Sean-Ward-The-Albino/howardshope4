package org.howards4hope.repository;

import org.howards4hope.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByUserEmailIgnoreCase(String userEmail);
    List<Ticket> findByEventId(Long eventId);
}
