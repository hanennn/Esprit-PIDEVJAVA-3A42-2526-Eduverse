import Services.ProfessorService;
import entities.Professor;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import utils.DataBase;
import java.util.List;
import java.sql.SQLException;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestProfessorService {

    static ProfessorService professorService;

    @BeforeAll
    static void setup() {
        professorService = new ProfessorService();
    }

    @Test
    @Order(1)
    void TestAjoutProfessor() throws SQLException {
        Professor professor = new Professor(
                "ProfessorTest",
                "TestProfessor",
                "Professor1",
                "Professor@gmail.com",
                "0000",
                true,
                null,
                "5 years",
                "Math",
                null

        );
        professorService.AjouterProfessor(professor);
        List<Professor> ListProfessors = professorService.AfficherProfessors();
        assertFalse(ListProfessors.isEmpty());
        assertTrue(
                ListProfessors.stream().anyMatch(p -> p.getUserName().equals("Professor1"))
        );
    }

    @Test
    @Order(2)
    void TestModifierProfessor() throws SQLException {
        Professor professor = professorService.FindProfessorByUsername("Professor1");
        assertNotNull(professor);

        professor.setFirstName("UpdatedTestProfessor");
        professor.setEmail("UpdatedProfessorEmail@gmail.com");
        professorService.ModifierProfessor(professor);

        Professor updatedProfessor = professorService.FindProfessorByUsername("Professor1");
        assertNotNull(updatedProfessor);
        assertEquals("UpdatedProfessorEmail@gmail.com", updatedProfessor.getEmail());
        assertEquals("UpdatedTestProfessor", updatedProfessor.getFirstName());
    }

    @Test
    @Order(3)
    void TestSupprimerProfessor() throws SQLException {
        Professor professor = professorService.FindProfessorByUsername("Professor1");
        assertNotNull(professor);

        professorService.SupprimerProfessor(professor.getId());
        Professor deletedProfessor = professorService.FindProfessorByUsername("Professor1");
        assertNull(deletedProfessor);
    }
}