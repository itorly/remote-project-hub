package com.itorly.rph.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void illegalStateDefaultsToBadRequest() throws Exception {
        mockMvc.perform(get("/test-exceptions/illegal/bad"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Operation failed"))
                .andExpect(jsonPath("$.path").value("/test-exceptions/illegal/bad"))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    void illegalStateWithForbiddenKeywordsReturnsForbidden() throws Exception {
        mockMvc.perform(get("/test-exceptions/illegal/forbidden"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Action not allowed for this user"))
                .andExpect(jsonPath("$.path").value("/test-exceptions/illegal/forbidden"));
    }

    @Test
    void entityNotFoundWithMessageReturns404() throws Exception {
        mockMvc.perform(get("/test-exceptions/not-found/message"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Project not found"))
                .andExpect(jsonPath("$.path").value("/test-exceptions/not-found/message"));
    }

    @Test
    void entityNotFoundWithoutMessageUsesDefault() throws Exception {
        mockMvc.perform(get("/test-exceptions/not-found/default"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Resource not found"))
                .andExpect(jsonPath("$.path").value("/test-exceptions/not-found/default"));
    }

    @Test
    void validationErrorsAreFormatted() throws Exception {
        ValidationRequest request = new ValidationRequest("");

        mockMvc.perform(post("/test-exceptions/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors", contains("name must not be blank")));
    }

    @RestController
    @RequestMapping("/test-exceptions")
    static class TestController {

        @GetMapping("/illegal/bad")
        void illegalBad() {
            throw new IllegalStateException("Operation failed");
        }

        @GetMapping("/illegal/forbidden")
        void illegalForbidden() {
            throw new IllegalStateException("Action not allowed for this user");
        }

        @GetMapping("/not-found/message")
        void notFoundWithMessage() {
            throw new EntityNotFoundException("Project not found");
        }

        @GetMapping("/not-found/default")
        void notFoundDefault() {
            throw new EntityNotFoundException();
        }

        @PostMapping("/validate")
        void validatePayload(@Valid @RequestBody ValidationRequest request) {
            // no-op, validation happens before method body
        }
    }

    record ValidationRequest(@NotBlank String name) {
    }
}

