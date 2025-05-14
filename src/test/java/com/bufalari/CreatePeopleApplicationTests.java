package com.bufalari;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles; // <<< IMPORTAR

@SpringBootTest
@ActiveProfiles("test") // <<< ADICIONAR/CONFIRMAR ESTA LINHA
class CreatePeopleApplicationTests {

    @Test
    void contextLoads() {
    }
}