package com.devops.app;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class TaskApiIT {

    @Autowired
    TestRestTemplate rest;

    @Test
    void createToggleAndDeleteTask() {

        ResponseEntity<Task> created =
                rest.postForEntity(
                        "/api/tasks",
                        new Task("Deploy to staging"),
                        Task.class
                );

        assertThat(created.getStatusCode())
                .isEqualTo(HttpStatus.CREATED);

        Long id = created.getBody().getId();

        ResponseEntity<Task> toggled =
                rest.exchange(
                        "/api/tasks/" + id + "/toggle",
                        org.springframework.http.HttpMethod.PUT,
                        null,
                        Task.class
                );

        assertThat(toggled.getBody().isDone())
                .isTrue();

        rest.delete("/api/tasks/" + id);

        ResponseEntity<Task[]> all =
                rest.getForEntity(
                        "/api/tasks",
                        Task[].class
                );

        assertThat(all.getBody())
                .noneMatch(t -> t.getId().equals(id));
    }

    @Test
    void healthEndpointIsUp() {

        ResponseEntity<String> res =
                rest.getForEntity(
                        "/actuator/health",
                        String.class
                );

        assertThat(res.getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }
}
