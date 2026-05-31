package com.example.sca.auth;

import com.example.sca.common.ApiException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/team")
public class TeamController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public TeamController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/members")
    public List<MemberDto> members(@CurrentUser UserPrincipal user) {
        requireAdmin(user);
        return userRepository.findAll().stream().map(MemberDto::from).collect(Collectors.toList());
    }

    @PostMapping("/members")
    public MemberDto create(@RequestBody CreateMemberRequest request, @CurrentUser UserPrincipal user) {
        requireAdmin(user);
        if (request.username == null || request.username.trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Username is required");
        }
        if (request.password == null || request.password.trim().length() < 6) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Password must be at least 6 characters");
        }
        if (userRepository.findByUsername(request.username.trim()).isPresent()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Username already exists");
        }
        AppUser member = new AppUser();
        member.setUsername(request.username.trim());
        member.setPasswordHash(passwordEncoder.encode(request.password));
        member.setRole(request.role == null ? UserRole.USER : request.role);
        return MemberDto.from(userRepository.save(member));
    }

    @PutMapping("/members/{id}")
    public MemberDto update(@PathVariable Long id, @RequestBody UpdateMemberRequest request, @CurrentUser UserPrincipal user) {
        requireAdmin(user);
        AppUser member = userRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        if (request.role != null) {
            member.setRole(request.role);
        }
        if (request.password != null && !request.password.trim().isEmpty()) {
            if (request.password.trim().length() < 6) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Password must be at least 6 characters");
            }
            member.setPasswordHash(passwordEncoder.encode(request.password));
        }
        return MemberDto.from(userRepository.save(member));
    }

    @DeleteMapping("/members/{id}")
    public void delete(@PathVariable Long id, @CurrentUser UserPrincipal user) {
        requireAdmin(user);
        if (user.getId().equals(id)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot delete current user");
        }
        if (!userRepository.existsById(id)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "User not found");
        }
        userRepository.deleteById(id);
    }

    private void requireAdmin(UserPrincipal user) {
        if (user.getRole() != UserRole.ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Admin role is required");
        }
    }

    public static class CreateMemberRequest {
        public String username;
        public String password;
        public UserRole role;
    }

    public static class UpdateMemberRequest {
        public String password;
        public UserRole role;
    }

    public static class MemberDto {
        public Long id;
        public String username;
        public UserRole role;

        static MemberDto from(AppUser user) {
            MemberDto dto = new MemberDto();
            dto.id = user.getId();
            dto.username = user.getUsername();
            dto.role = user.getRole();
            return dto;
        }
    }
}
