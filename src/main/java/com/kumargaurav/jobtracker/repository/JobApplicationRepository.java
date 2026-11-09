package com.kumargaurav.jobtracker.repository;

import com.kumargaurav.jobtracker.model.ApplicationStatus;
import com.kumargaurav.jobtracker.model.JobApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    Page<JobApplication> findByOwnerEmail(String email, Pageable pageable);

    Page<JobApplication> findByOwnerEmailAndStatus(String email, ApplicationStatus status, Pageable pageable);

    Optional<JobApplication> findByIdAndOwnerEmail(Long id, String email);
}
