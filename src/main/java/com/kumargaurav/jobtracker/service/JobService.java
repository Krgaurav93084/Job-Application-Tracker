package com.kumargaurav.jobtracker.service;

import com.kumargaurav.jobtracker.dto.Dtos.JobRequest;
import com.kumargaurav.jobtracker.dto.Dtos.JobResponse;
import com.kumargaurav.jobtracker.dto.Dtos.PageResponse;
import com.kumargaurav.jobtracker.exception.NotFoundException;
import com.kumargaurav.jobtracker.model.ApplicationStatus;
import com.kumargaurav.jobtracker.model.JobApplication;
import com.kumargaurav.jobtracker.model.User;
import com.kumargaurav.jobtracker.repository.JobApplicationRepository;
import com.kumargaurav.jobtracker.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for job applications. Every method is scoped to the logged-in
 * user's email, so one user can never read or change another user's data.
 */
@Service
public class JobService {

    private final JobApplicationRepository jobs;
    private final UserRepository users;

    public JobService(JobApplicationRepository jobs, UserRepository users) {
        this.jobs = jobs;
        this.users = users;
    }

    @Transactional
    public JobResponse create(String email, JobRequest request) {
        User owner = users.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
        JobApplication job = new JobApplication();
        apply(job, request);
        job.setOwner(owner);
        return JobResponse.from(jobs.save(job));
    }

    @Transactional(readOnly = true)
    public PageResponse<JobResponse> list(String email, ApplicationStatus status, Pageable pageable) {
        Page<JobApplication> page = (status == null)
                ? jobs.findByOwnerEmail(email, pageable)
                : jobs.findByOwnerEmailAndStatus(email, status, pageable);
        return new PageResponse<>(
                page.getContent().stream().map(JobResponse::from).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public JobResponse get(String email, Long id) {
        return JobResponse.from(find(email, id));
    }

    @Transactional
    public JobResponse update(String email, Long id, JobRequest request) {
        JobApplication job = find(email, id);
        apply(job, request);
        return JobResponse.from(job);
    }

    @Transactional
    public void delete(String email, Long id) {
        jobs.delete(find(email, id));
    }

    private JobApplication find(String email, Long id) {
        return jobs.findByIdAndOwnerEmail(id, email)
                .orElseThrow(() -> new NotFoundException("Application " + id + " not found"));
    }

    private void apply(JobApplication job, JobRequest request) {
        job.setCompany(request.company().trim());
        job.setRole(request.role().trim());
        job.setStatus(request.status());
        job.setAppliedOn(request.appliedOn());
        job.setNotes(request.notes());
    }
}
