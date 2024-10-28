import org.sqlite.SQLiteErrorCode;
import org.sqlite.SQLiteException;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class Demo {
    public static void main(String[] args) throws SQLException {
        CourseDatabase courseDatabase = new CourseDatabase();
        courseDatabase.connect();
        courseDatabase.createTablesIfNeeded();

        Student johnDoe = new Student(1, "John", "Doe", "abc2def");
        try {
            courseDatabase.addNewStudent(johnDoe);
        } catch (SQLiteException e) {
            // Ignore PrimaryKey Constraint Violation - this way we can run our demo file multiple times
            if (SQLiteErrorCode.SQLITE_CONSTRAINT_PRIMARYKEY != e.getResultCode()) {
                throw e;
            }
        }

        Student janeSmith = new Student(2, "Jane", "Smith", "ghi3jkl");
        courseDatabase.upsertStudent(janeSmith);

        johnDoe.setFirstName("Jonathan");
        courseDatabase.upsertStudent(johnDoe);

        courseDatabase.commit();

        Course sde = new Course(12345, "CS", 3140, 1, "TR 14:00-15:15");
        courseDatabase.upsertCourse(sde);

        courseDatabase.commit();

        johnDoe.addCourse(sde);
        sde.addStudent(johnDoe);

        janeSmith.addCourse(sde);
        sde.addStudent(janeSmith);

        try {
            courseDatabase.addEnrollment(johnDoe, sde);
        } catch (SQLiteException e) {
            // Ignore UniqueConstraintErrors - this way we can run our demo file multiple times
            if (SQLiteErrorCode.SQLITE_CONSTRAINT_UNIQUE != e.getResultCode()) {
                throw e;
            }
        }

        try {
            courseDatabase.addEnrollment(janeSmith, sde);
        } catch (SQLiteException e) {
            // Ignore UniqueConstraintErrors - this way we can run our demo file multiple times
            if (SQLiteErrorCode.SQLITE_CONSTRAINT_UNIQUE != e.getResultCode()) {
                throw e;
            }
        }

        courseDatabase.commit();

        List<Student> student = courseDatabase.getStudents();
        student.forEach(System.out::println);

        Student student1 = courseDatabase.getStudent(1).orElseThrow();
        Optional<Student> student3 = courseDatabase.getStudent(3);
        System.out.println(student3);

        courseDatabase.disconnect();
    }
}
