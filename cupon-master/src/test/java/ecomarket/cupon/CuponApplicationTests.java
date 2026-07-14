package ecomarket.cupon;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class CuponApplicationTests {

    @Test
    void mainEjecutaAplicacion() {
        CuponApplication.main(new String[] {});
    }
}