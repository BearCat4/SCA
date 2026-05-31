package com.example.sca.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class TeamControllerManagementTest {
    private final InMemoryUserRepository userRepository = new InMemoryUserRepository();
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final TeamController controller = new TeamController(userRepository, passwordEncoder);

    @Test
    void updatesMemberRoleAndPassword() {
        AppUser admin = save("admin", UserRole.ADMIN);
        AppUser member = save("member", UserRole.USER);
        TeamController.UpdateMemberRequest request = new TeamController.UpdateMemberRequest();
        request.role = UserRole.ADMIN;
        request.password = "newpass123";

        TeamController.MemberDto result = controller.update(member.getId(), request, principal(admin));

        AppUser saved = userRepository.findById(member.getId()).get();
        assertThat(result.role).isEqualTo(UserRole.ADMIN);
        assertThat(saved.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(passwordEncoder.matches("newpass123", saved.getPasswordHash())).isTrue();
    }

    @Test
    void deletesMember() {
        AppUser admin = save("admin", UserRole.ADMIN);
        AppUser member = save("member", UserRole.USER);

        controller.delete(member.getId(), principal(admin));

        assertThat(userRepository.findById(member.getId())).isEmpty();
    }

    private AppUser save(String username, UserRole role) {
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode("password"));
        user.setRole(role);
        return userRepository.save(user);
    }

    private UserPrincipal principal(AppUser user) {
        return new UserPrincipal(user.getId(), user.getUsername(), user.getRole());
    }
}
