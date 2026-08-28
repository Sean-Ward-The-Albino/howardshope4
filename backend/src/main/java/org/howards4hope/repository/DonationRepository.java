package org.howards4hope.repository;

import org.howards4hope.model.Donation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DonationRepository extends JpaRepository<Donation, Long> {
    Optional<Donation> findByTaxReceiptNumber(String taxReceiptNumber);
    List<Donation> findByDonorEmailIgnoreCase(String donorEmail);
    List<Donation> findAllByOrderByDonationDateDesc();
}
