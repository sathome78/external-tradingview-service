package me.exrates.externalservice.services;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;

@RunWith(SpringRunner.class)
@ActiveProfiles("local")
@SpringBootTest
public abstract class AbstractTest {

    static final String TEST_PAIR = "TEST1TEST2";
    static final String CONVERTED_TEST_PAIR = "TEST1/TEST2";
    static final String GOOGLE_2FA_SECRET = "secret";

    static final LocalDateTime NOW = LocalDateTime.now();
}