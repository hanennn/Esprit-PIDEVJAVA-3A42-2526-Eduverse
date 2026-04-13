package services;

import models.bourses;
import org.junit.jupiter.api.*;
import java.sql.Timestamp;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BoursesServiceTest {
    static boursesService service = new boursesService();
    static int testId;

    @Test @Order(1)
    void testAdd() {
        bourses b = new bourses();
        b.setTitre("Bourse Test JUnit");
        b.setDescription("Description test");
        b.setImage("test.jpg");
        b.setMontant(999.0);
        b.setDate_attribution(new Timestamp(System.currentTimeMillis()));
        b.setDate_fin(new Timestamp(System.currentTimeMillis() + 86400000L));
        int before = service.getAll().size();
        service.add(b);
        assertEquals(before + 1, service.getAll().size());
    }

    @Test @Order(2)
    void testGetAll() {
        assertNotNull(service.getAll());
        assertFalse(service.getAll().isEmpty());
    }

    @Test @Order(3)
    void testUpdate() {
        bourses last = service.getAll().get(service.getAll().size() - 1);
        testId = last.getId();
        last.setMontant(1234.56);
        service.update(last);
        assertEquals(1234.56, service.getById(testId).getMontant());
    }

    @Test @Order(4)
    void testDelete() {
        service.delete(testId);
        assertNull(service.getById(testId));
    }
}
