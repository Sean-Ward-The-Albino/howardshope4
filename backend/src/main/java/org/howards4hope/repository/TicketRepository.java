package org.howards4hope.repository;

import org.howards4hope.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByUserEmailIgnoreCase(String userEmail);
    List<Ticket> findByEventId(Long eventId);
    java.util.Optional<Ticket> findByTicketId(String ticketId);
    java.util.Optional<Ticket> findByConfirmationToken(String confirmationToken);
    List<Ticket> findByUserEmailIgnoreCaseAndConfirmationToken(String userEmail, String confirmationToken);
}
