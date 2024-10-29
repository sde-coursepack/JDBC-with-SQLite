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

        // Add the student John Doe - note that if re-running this, Johnathan Doe will be present
        Student johnDoe = new Student(1, "John", "Doe", "abc2def");
        try {
            courseDatabase.addNewStudent(johnDoe);
        } catch (SQLiteException e) {
            // Ignore PrimaryKey Constraint Violation - this way we can run our demo file multiple times
            if (SQLiteErrorCode.SQLITE_CONSTRAINT_PRIMARYKEY != e.getResultCode()) {
                throw e;
            }
        }

        // Add Jane Smith via upsert
        Student janeSmith = new Student(2, "Jane", "Smith", "ghi3jkl");
        courseDatabase.upsertStudent(janeSmith);

        // change John Doe's name to Jonathan and upsert
        johnDoe.setFirstName("Jonathan");
        courseDatabase.upsertStudent(johnDoe);

        // save student adds/changes
        courseDatabase.commit();

        // Create and save a course
        Course sde = new Course(12345, "CS", 3140, 1, "TR 14:00-15:15");
        courseDatabase.upsertCourse(sde);
        courseDatabase.commit();


        // Enroll both students in our course
        johnDoe.addCourse(sde);
        sde.addStudent(johnDoe);
        janeSmith.addCourse(sde);
        sde.addStudent(janeSmith);

        // Save update to database
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
        System.out.println("Printing all students:");
        student.forEach(System.out::println);
        System.out.println(); //adding a blank line before the next print block

        // Demo of getting students by ID
        Student student1 = courseDatabase.getStudent(1).orElseThrow();
        System.out.println("Student ID - 1 -> " + student1);
        Optional<Student> student3 = courseDatabase.getStudent(3);
        System.out.println("Student ID - 3 -> " + student3); // is empty

        int nextAvailableID = courseDatabase.getNextStudentID();
        System.out.println("Next available ID -> " + nextAvailableID);

        courseDatabase.disconnect();
    }
}
