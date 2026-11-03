package com.netszky.grooks.account;


import com.netszky.grooks.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc()
public class RegisterTests {
    @Autowired
    MockMvc mockMvc;

    private static String createJsonBody(String email){
        return """
        {
          "email": "%s",
          "password": "passwordTest",
          "firstName": "John",
          "lastName": "Doe"
        }
        """.formatted(email);
    }

    @Test
    void registerShouldReturnCreated() throws Exception {
        String body = createJsonBody("created@test.com");
        mockMvc.perform(post("/auth/register")
                .contentType("application/json")
                .content(body))
                .andDo(MockMvcResultHandlers.print())
                .andExpectAll(
                        status().isCreated()
                );
    }

    @Test
    void registerWithMalformedJsonShouldReturnMalformedRequest() throws Exception {
        String body = createJsonBody("malformedjson@test.com");
        mockMvc.perform(post("/auth/register")
                        .contentType("application/json")
                        .content(body))
                .andDo(MockMvcResultHandlers.print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json(
                                """
                                        {
                                        "code":"MALFORMED_REQUEST",
                                        "message":"La requête est incorrecte."
                                        }
                                        """
                        )

                );
    }
    @Test
    void registerWithMissingLastNameShouldReturnValidationError() throws Exception {
        String body = createJsonBody("missingname@test.com");
        mockMvc.perform(post("/auth/register")
                        .contentType("application/json")
                        .content(body))
                .andDo(MockMvcResultHandlers.print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json(
                                """
                                        {
                                        "code":"VALIDATION_ERROR",
                                        "message":"Le nom est obligatoire"
                                        }
                                        """
                        )

                );
    }

    @Test
    void registerWithExistingEmailShouldReturnConflict() throws Exception {
        String body = createJsonBody("existing@test.com");
        mockMvc.perform(post("/auth/register")
                        .contentType("application/json")
                        .content(body))
                .andDo(MockMvcResultHandlers.print())
                .andExpectAll(
                        status().isCreated()
                );

        mockMvc.perform(post("/auth/register")
                        .contentType("application/json")
                        .content(body))
                .andDo(MockMvcResultHandlers.print())
                .andExpectAll(
                        status().isConflict(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json(
                                """
                                        {
                                        "code": "EMAIL_ALREADY_USED",
                                        "message": "Un compte avec cet email existe déjà."
                                        }
                                        """
                        )

                );
    }

    @Test
    void registerWithExistingEmailCaseInsensitiveShouldReturnConflict() throws Exception {

        String body = createJsonBody("conflictcase@test.com");
        String body2 = createJsonBody("ConflictCas@test.com");
        mockMvc.perform(post("/auth/register")
                        .contentType("application/json")
                        .content(body))
                .andDo(MockMvcResultHandlers.print())
                .andExpectAll(
                        status().isCreated()
                );

        mockMvc.perform(post("/auth/register")
                        .contentType("application/json")
                        .content(body2))
                .andDo(MockMvcResultHandlers.print())
                .andExpectAll(
                        status().isConflict(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json(
                                """
                                        {
                                        "code": "EMAIL_ALREADY_USED",
                                        "message": "Un compte avec cet email existe déjà."
                                        }
                                        """
                        )

                );
    }
}
