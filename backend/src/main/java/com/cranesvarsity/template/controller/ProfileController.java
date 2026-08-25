package com.cranesvarsity.template.controller;

import com.cranesvarsity.template.dto.ChangePasswordRequest;
import com.cranesvarsity.template.dto.ProfilePageResponse;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import com.cranesvarsity.template.service.ChangePasswordService;
import com.cranesvarsity.template.service.ProfileService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/student/profile")
public class ProfileController {

    private final ProfileService profileService;
    private final ChangePasswordService changePasswordService;

    public ProfileController(ProfileService profileService, ChangePasswordService changePasswordService) {
        this.profileService = profileService;
        this.changePasswordService = changePasswordService;
    }

    @GetMapping("/details")
    public ProfilePageResponse getDetails(@AuthenticationPrincipal AuthenticatedStudent student) {
        return profileService.getProfile(student);
    }

    @PostMapping("/change-password")
    public Map<String, String> changePassword(@AuthenticationPrincipal AuthenticatedStudent student,
                                                @RequestBody ChangePasswordRequest request) {
        String status = changePasswordService.changePassword(student, request);
        return Map.of("status", status);
    }
}
