package org.howards4hope.repository;

import org.howards4hope.model.NewsletterSubscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface NewsletterRepository extends JpaRepository<NewsletterSubscriber, Long> {
    Optional<NewsletterSubscriber> findByEmail(String email);
}
