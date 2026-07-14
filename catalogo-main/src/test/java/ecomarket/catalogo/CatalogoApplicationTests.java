package ecomarket.catalogo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class CatalogoApplicationTests {

@Test
void mainEjecutaAplicacion() {
    CatalogoApplication.main(new String[]{
            "--spring.profiles.active=test",
            "--server.port=0"
    });
}
}