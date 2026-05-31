package com.example.sca.scan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.sca.config.ScanProperties;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class GitCheckoutServiceTest {
    @Test
    void retriesTransientCloneFailures() {
        ScanProperties properties = new ScanProperties();
        properties.setGitMaxAttempts(3);
        properties.setGitTimeoutSeconds(1);
        AtomicInteger calls = new AtomicInteger();
        GitCheckoutService service = new GitCheckoutService(properties, (command, timeoutSeconds) -> {
            if (calls.incrementAndGet() < 2) {
                return new GitCheckoutService.GitCommandResult(true, 128, "network error");
            }
            return new GitCheckoutService.GitCommandResult(true, 0, "ok");
        });

        service.checkout("https://github.com/WebGoat/WebGoat/", "main", Paths.get("/tmp/example"));

        assertThat(calls.get()).isEqualTo(2);
    }

    @Test
    void reportsAttemptsWhenCloneKeepsFailing() {
        ScanProperties properties = new ScanProperties();
        properties.setGitMaxAttempts(2);
        GitCheckoutService service = new GitCheckoutService(properties, (List<String> command, long timeoutSeconds) ->
                new GitCheckoutService.GitCommandResult(true, 128, "network error"));

        assertThatThrownBy(() -> service.checkout("https://github.com/WebGoat/WebGoat/", "main", Paths.get("/tmp/example")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("after 2 attempt");
    }

    @Test
    void injectsRepositoryTokenIntoHttpsCloneUrl() {
        ScanProperties properties = new ScanProperties();
        properties.setRepositoryToken("secret-token");
        final List<String>[] captured = new List[1];
        GitCheckoutService service = new GitCheckoutService(properties, (List<String> command, long timeoutSeconds) -> {
            captured[0] = command;
            return new GitCheckoutService.GitCommandResult(true, 0, "ok");
        });

        service.checkout("https://github.com/acme/repo.git", "main", Paths.get("/tmp/example"));

        assertThat(captured[0]).contains("https://secret-token@github.com/acme/repo.git");
    }
}
