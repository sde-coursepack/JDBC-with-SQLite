import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CourseDatabaseTest {
    static CourseDatabase courseDatabase;

    static Student johnDoe = new Student(1, "John", "Doe", "abc2def");
    static Student janeSmith = new Student(2, "Jane", "Smith", "ghi3jkl");

    static Course sde = new Course(12345, "CS", 3140, 1, "TR 14:00 - 15:15");

    @BeforeAll
    static void initialize() throws SQLException {
        courseDatabase = new CourseDatabase(":memory:");
    }

    @BeforeEach
    void setUp() throws SQLException {
        courseDatabase.connect();
        courseDatabase.createTablesIfNeeded();
    }

    @Test
    void getStudents() throws SQLException {
        courseDatabase.addNewStudent(johnDoe);
        courseDatabase.addNewStudent(janeSmith);

        List<Student> students = courseDatabase.getStudents();

        assertEquals(2, students.size());
        assertTrue(students.contains(johnDoe));
        assertTrue(students.contains(janeSmith));
    }

    @Test
    void getNextStudentId_empty() throws SQLException {
        int nextId = courseDatabase.getNextStudentID();
        assertEquals(1, nextId);
    }

    @Test
    void getNextStudentId_notEmpty() throws SQLException {
        courseDatabase.addNewStudent(johnDoe);
        courseDatabase.addNewStudent(janeSmith);

        int nextId = courseDatabase.getNextStudentID();

        assertEquals(3, nextId);
    }

    @Test
    void addNewStudent() throws SQLException {
        courseDatabase.addNewStudent(johnDoe);

        Optional<Student> optionalStudent = courseDatabase.getStudent(johnDoe.getId());

        assertTrue(optionalStudent.isPresent());
        Student student = optionalStudent.get();
        assertEquals(johnDoe, student);
    }

    @Test
    void upsertStudent_existing() throws SQLException {
        courseDatabase.addNewStudent(johnDoe);
        johnDoe.setFirstName("Jonathan");
        courseDatabase.upsertStudent(johnDoe);

        Optional<Student> optionalStudent = courseDatabase.getStudent(johnDoe.getId());

        assertTrue(optionalStudent.isPresent());
        Student student = optionalStudent.get();
        assertEquals(johnDoe, student);
    }

    void getStudentsByCourse() throws SQLException {
        courseDatabase.addNewStudent(johnDoe);
        courseDatabase.addNewStudent(janeSmith);
        courseDatabase.addNewCourse(sde);
        courseDatabase.addEnrollment(johnDoe, sde);
        courseDatabase.addEnrollment(janeSmith, sde);

        List<Student> students = courseDatabase.getStudentsByCourse(sde);

        assertEquals(2, students.size());
        assertTrue(students.contains(johnDoe));
        assertTrue(students.contains(janeSmith));
    }

    @AfterEach
    void tearDown() throws SQLException {
        courseDatabase.dropTables();
        courseDatabase.commit();
        courseDatabase.disconnect();
    }
}