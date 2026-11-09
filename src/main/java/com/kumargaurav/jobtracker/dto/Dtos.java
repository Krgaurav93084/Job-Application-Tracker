package com.kumargaurav.jobtracker.dto;

import com.kumargaurav.jobtracker.model.ApplicationStatus;
import com.kumargaurav.jobtracker.model.JobApplication;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public final class Dtos {

    private Dtos() {
    }

    public record AuthRequest(
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, max = 72) String password) {
    }

    public record AuthResponse(String token) {
    }

    public record JobRequest(
            @NotBlank @Size(max = 200) String company,
            @NotBlank @Size(max = 200) String role,
            @NotNull ApplicationStatus status,
            LocalDate appliedOn,
            @Size(max = 2000) String notes) {
    }

    public record JobResponse(
            Long id,
            String company,
            String role,
            ApplicationStatus status,
            LocalDate appliedOn,
            String notes) {

        public static JobResponse from(JobApplication j) {
            return new JobResponse(j.getId(), j.getCompany(), j.getRole(), j.getStatus(),
                    j.getAppliedOn(), j.getNotes());
        }
    }

    public record PageResponse<T>(List<T> items, int page, int size, long total) {
    }

    public record ApiError(int status, String message, Map<String, String> errors) {
    }
}
