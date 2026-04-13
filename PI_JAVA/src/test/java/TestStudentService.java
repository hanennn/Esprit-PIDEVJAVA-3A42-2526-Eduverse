import Services.StudentService;
import entities.Student;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import utils.DataBase;
import java.util.List;
import java.sql.SQLException;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestStudentService {

    static StudentService studentService;

    @BeforeAll
    static void setup() {
        studentService = new StudentService();
    }

    @Test
    @Order(1)
    void TestAjoutStudent() throws SQLException {
        Student student = new Student(
                "StudentTest",
                "TestStudent",
                "Student1",
                "Student@gmail.com",
                "0000",
                true,
                null,
                null
        );
        studentService.AjouterStudent(student);
        List<Student> ListStudents = studentService.AfficherStudents();
        assertFalse(ListStudents.isEmpty());
        assertTrue(
                ListStudents.stream().anyMatch(s -> s.getUserName().equals("Student1"))
        );
    }

    @Test
    @Order(2)
    void TestModifierStudent() throws SQLException {
        Student student = studentService.FindStudentByUsername("Student1");
        assertNotNull(student);

        student.setFirstName("UpdatedTestStudent");
        student.setEmail("UpdatedStudentEmail@gmail.com");
        studentService.ModifierStudent(student);

        Student updatedStudent = studentService.FindStudentByUsername("Student1");
        assertNotNull(updatedStudent);
        assertEquals("UpdatedStudentEmail@gmail.com", updatedStudent.getEmail());
        assertEquals("UpdatedTestStudent", updatedStudent.getFirstName());
    }

    @Test
    @Order(3)
    void TestSupprimerStudent() throws SQLException {
        Student student = studentService.FindStudentByUsername("Student1");
        assertNotNull(student);

        studentService.SupprimerStudent(student.getId());
        Student deletedStudent = studentService.FindStudentByUsername("Student1");
        assertNull(deletedStudent);
    }
}