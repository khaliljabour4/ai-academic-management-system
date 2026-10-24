package com.codewithkahlil.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Verifies that each role can only reach its own endpoints. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RoleAccessTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void anonymousCannotReachProtectedEndpoints() throws Exception {
        mvc.perform(get("/api/v1/admin/students")).andExpect(status().is4xxClientError());
        mvc.perform(get("/api/v1/student/modules")).andExpect(status().is4xxClientError());
    }

    @Test
    void anonymousCannotRegisterAccounts() throws Exception {
        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"x@x.com\",\"password\":\"pw\",\"role\":\"Administrator\"}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(authorities = "Student")
    void studentCannotUseAdminEndpoints() throws Exception {
        mvc.perform(get("/api/v1/admin/students")).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/director/stats")).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/professor/modules")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "Professor")
    void professorCannotUseStudentOrAdminEndpoints() throws Exception {
        mvc.perform(get("/api/v1/student/modules")).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/admin/profs")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "Director")
    void directorCannotModifyAdminResources() throws Exception {
        mvc.perform(get("/api/v1/admin/students")).andExpect(status().isForbidden());
    }

    @Test
    void invalidTokenIsRejectedWithoutServerError() throws Exception {
        mvc.perform(get("/api/v1/student/modules").header("Authorization", "Bearer not.a.jwt"))
                .andExpect(status().is4xxClientError());
    }
}
