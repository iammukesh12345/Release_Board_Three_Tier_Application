package com.devops.app;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired MockMvc mvc;
    @MockBean TaskRepository repository;

    @Test
    void listReturnsTasks() throws Exception {
        when(repository.findAll()).thenReturn(List.of(new Task("Run smoke tests")));
        mvc.perform(get("/api/tasks"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[0].title").value("Run smoke tests"));
    }

    @Test
    void createRejectsBlankTitle() throws Exception {
        mvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON).content("{\"title\":\" \"}"))
           .andExpect(status().isBadRequest());
    }

    @Test
    void createSavesTask() throws Exception {
        when(repository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));
        mvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"Tag release\"}"))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.title").value("Tag release"));
    }

    @Test
    void toggleReturns404WhenMissing() throws Exception {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        mvc.perform(put("/api/tasks/99/toggle")).andExpect(status().isNotFound());
    }
}
