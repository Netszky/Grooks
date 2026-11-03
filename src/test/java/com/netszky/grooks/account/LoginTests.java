package com.netszky.grooks.account;


import com.netszky.grooks.TestcontainersConfiguration;
import com.netszky.grooks.common.security.AccessToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;


import java.time.Duration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc()
public class LoginTests {
    @Autowired
    MockMvc mockMvc;

    @Value("${grooks.jwt.duration}")
    private Duration jwtDuration;

    private static String createJsonBody(String email) {
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
    void loginShouldReturnNoContentAndCookie() throws Exception {

        String body = createJsonBody("created@test.com");

        mockMvc.perform(post("/auth/register")
                        .contentType("application/json")
                        .content(body))
                .andDo(MockMvcResultHandlers.print())
                .andExpectAll(
                        status().isCreated()
                );

        mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content("""
                                {
                                "email": "created@test.com",
                                "password": "passwordTest"
                                }
                                """))
                .andDo(MockMvcResultHandlers.print())
                .andExpectAll(
                        status().isNoContent(),
                        cookie()
                                .sameSite(AccessToken.ACCESS_TOKEN_COOKIE, "Strict"),
                        cookie().httpOnly(AccessToken.ACCESS_TOKEN_COOKIE, true),
                        cookie().maxAge(AccessToken.ACCESS_TOKEN_COOKIE, Math.toIntExact(jwtDuration.toSeconds()))


                ).andReturn();
    }

    @Test
    void badLoginShouldReturnUnauthorized() throws Exception {

        String body = createJsonBody("badlogin@test.com");

        mockMvc.perform(post("/auth/register")
                        .contentType("application/json")
                        .content(body))
                .andExpectAll(
                        status().isCreated()
                );

        mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content("""
                                {
                                "email": "badlogin@test.com",
                                "password": "badpassword"
                                }
                                """))
                .andExpectAll(
                        status().isUnauthorized(),
                        content().contentType(MediaType.APPLICATION_JSON_VALUE),
                        content().json(
                                """
                                        {
                                        "code": "INVALID_CREDENTIALS",
                                        "message": "Email ou mot de passe incorrect"
                                        }
                                        """
                        ),
                        cookie().doesNotExist(AccessToken.ACCESS_TOKEN_COOKIE)

                ).andReturn();
    }
}
