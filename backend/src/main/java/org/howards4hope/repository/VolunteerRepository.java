package org.howards4hope.repository;

import org.howards4hope.model.VolunteerApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VolunteerRepository extends JpaRepository<VolunteerApplication, Long> {
    List<VolunteerApplication> findAllByOrderByAppliedAtDesc();
    List<VolunteerApplication> findByStatusOrderByAppliedAtDesc(String status);
}
