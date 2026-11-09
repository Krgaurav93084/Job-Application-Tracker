package com.kumargaurav.jobtracker.controller;

import com.kumargaurav.jobtracker.dto.Dtos.JobRequest;
import com.kumargaurav.jobtracker.dto.Dtos.JobResponse;
import com.kumargaurav.jobtracker.dto.Dtos.PageResponse;
import com.kumargaurav.jobtracker.model.ApplicationStatus;
import com.kumargaurav.jobtracker.service.JobService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/applications")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JobResponse create(@Valid @RequestBody JobRequest request, Principal principal) {
        return jobService.create(principal.getName(), request);
    }

    @GetMapping
    public PageResponse<JobResponse> list(
            @RequestParam(required = false) ApplicationStatus status,
            @PageableDefault(size = 10, sort = "appliedOn", direction = Sort.Direction.DESC) Pageable pageable,
            Principal principal) {
        return jobService.list(principal.getName(), status, pageable);
    }

    @GetMapping("/{id}")
    public JobResponse get(@PathVariable Long id, Principal principal) {
        return jobService.get(principal.getName(), id);
    }

    @PutMapping("/{id}")
    public JobResponse update(@PathVariable Long id,
                              @Valid @RequestBody JobRequest request,
                              Principal principal) {
        return jobService.update(principal.getName(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Principal principal) {
        jobService.delete(principal.getName(), id);
    }
}
