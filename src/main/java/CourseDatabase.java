import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CourseDatabase {
    private static final String DEFAULT_SQLITE_FILE = "courses_inclass.db";

    private final String databaseFilename;
    private Connection connection;

    public CourseDatabase() {
        this(DEFAULT_SQLITE_FILE);
    }

    public CourseDatabase(String databaseFilename) {
        this.databaseFilename = databaseFilename;
    }

    public void connect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            throw new IllegalStateException("The database connection is already active");
        }
        connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFilename);
        PreparedStatement preparedStatement = connection.prepareStatement("PRAGMA foreign_keys=ON");
        preparedStatement.execute();

        connection.setAutoCommit(false);
    }

    public void disconnect() throws SQLException {
        if (connection.isClosed()) {
            throw new IllegalStateException("Connection is already closed");
        }
        connection.close();
    }

    public void commit() throws SQLException {
        if (connection.isClosed()) {
            throw new IllegalStateException("Connection is already closed");
        }
        connection.commit();
    }

    public void rollback() throws SQLException {
        if (connection.isClosed()) {
            throw new IllegalStateException("Connection is already closed");
        }
        connection.rollback();
    }

    public void createTablesIfNeeded() throws SQLException {
        if (connection.isClosed()) {
            throw new IllegalStateException("Connection is already closed");
        }
        createStudentsTable();
        createCoursesTable();
        createEnrollmentsTable();
    }

    private void createStudentsTable() throws SQLException {
        try(PreparedStatement preparedStatement = connection.prepareStatement("""
                CREATE TABLE IF NOT EXISTS Students (
                    StudentId INTEGER PRIMARY KEY,
                    FirstName TEXT,
                    LastName TEXT NOT NULL,
                    ComputingID TEXT UNIQUE NOT NULL
                ) STRICT;
                """)) {
            preparedStatement.executeUpdate();
        }

    }

    private void createCoursesTable() throws SQLException {
        try(PreparedStatement preparedStatement = connection.prepareStatement("""
                CREATE TABLE IF NOT EXISTS Courses (\s
                  Crn INTEGER PRIMARY KEY,\s
                  Subject TEXT,
                  CourseNumber INTEGER,
                  Section INTEGER,
                  MeetingTime TEXT,
                  UNIQUE (Subject, Section, CourseNumber)
                ) STRICT;
                """)) {
            preparedStatement.executeUpdate();
        }
    }

    private void createEnrollmentsTable() throws SQLException {
        try(PreparedStatement preparedStatement = connection.prepareStatement("""
                CREATE TABLE IF NOT EXISTS Enrollments(
                    EnrollmentID INTEGER PRIMARY KEY,
                    StudentID    INTEGER NOT NULL,
                    CRN          INTEGER NOT NULL,
                    UNIQUE (StudentID, CRN),
                    FOREIGN KEY (StudentID) REFERENCES Students (StudentID) ON DELETE CASCADE,
                    FOREIGN KEY (CRN) REFERENCES Courses (CRN) ON DELETE CASCADE
                ) STRICT;
                """)) {
            preparedStatement.executeUpdate();
        }
    }

    public void clearTables() throws SQLException {
        try (PreparedStatement deleteEnrollments = connection.prepareStatement("""
            DELETE FROM Enrollments;
            """)) {
            deleteEnrollments.executeUpdate();
        }
        try (PreparedStatement deleteStudents = connection.prepareStatement("""
            DELETE FROM Students;
            """)) {
            deleteStudents.executeUpdate();
        }
        try (PreparedStatement deleteCourses = connection.prepareStatement("""
        DELETE FROM Courses;
        """)) {
            deleteCourses.executeUpdate();
        }
    }

    public void dropTables() throws SQLException {
        try (PreparedStatement deleteEnrollments = connection.prepareStatement("""
            DROP TABLE IF EXISTS Enrollments;
            """)) {
            deleteEnrollments.executeUpdate();
        }
        try (PreparedStatement deleteStudents = connection.prepareStatement("""
            DROP TABLE IF EXISTS Students;
            """)) {
            deleteStudents.executeUpdate();
        }
        try (PreparedStatement deleteCourses = connection.prepareStatement("""
            DROP TABLE IF EXISTS Courses;
            """)) {
            deleteCourses.executeUpdate();
        }
    }

    public int getNextStudentID() throws SQLException {
        try(PreparedStatement selectNextId = connection.prepareStatement("""
                SELECT Max(StudentId) + 1 AS NextID FROM Students;"""
        )) {
            ResultSet resultSet = selectNextId.executeQuery();
            resultSet.next();
            int nextID = resultSet.getInt("NextID");
            return nextID > 0 ? nextID : 1;
        }
    }

    /**
     * Shallow insert
     * @param student
     * @throws SQLException
     */
    public void addNewStudent(Student student) throws SQLException{
        try(PreparedStatement studentInsert = connection.prepareStatement("""
                INSERT INTO Students(StudentID, FirstName, LastName, ComputingID)
                    VALUES(?, ?, ?, ?);"""
        )) {
            studentInsert.setInt(1, student.getId());
            studentInsert.setString(2, student.getFirstName());
            studentInsert.setString(3, student.getLastName());
            studentInsert.setString(4, student.getComputingID());

            studentInsert.executeUpdate();
        }
    }

    /**
     * Shallow upsert - allows adding a student or changing and student's firstName/lastName
     * @param student
     * @throws SQLException
     */
    public void upsertStudent(Student student) throws SQLException{
        try(PreparedStatement studentUpsert = connection.prepareStatement("""
                INSERT INTO Students(StudentID, FirstName, LastName, ComputingID)
                    VALUES(?, ?, ?, ?) ON CONFLICT(StudentID) DO UPDATE
                        SET FirstName = excluded.FirstName,
                            LastName = excluded.LastName;"""
        )) {
            //Values
            studentUpsert.setInt(1, student.getId());
            studentUpsert.setString(2, student.getFirstName());
            studentUpsert.setString(3, student.getLastName());
            studentUpsert.setString(4, student.getComputingID());

            studentUpsert.executeUpdate();
        }
    }

    public void addNewCourse(Course course) throws SQLException{
        try(PreparedStatement courseInsert = connection.prepareStatement("""
                INSERT INTO Courses(Crn, Subject, CourseNumber, Section, MeetingTime)
                    VALUES(?, ?, ?, ?, ?);"""
        )) {
            courseInsert.setInt(1, course.getCourseNumber());
            courseInsert.setString(2, course.getSubject());
            courseInsert.setInt(3, course.getCourseNumber());
            courseInsert.setInt(4, course.getSectionNumber());
            courseInsert.setString(5, course.getMeetingTime());

            courseInsert.executeUpdate();
        }
    }

    /**
     * Shallow upsert of course - does not affect enrollments, only MeetingTime can be updated
     * @param course
     * @throws SQLException
     */
    public void upsertCourse(Course course) throws SQLException {
        try(PreparedStatement courseUpsert = connection.prepareStatement("""
                INSERT INTO Courses(Crn, Subject, CourseNumber, Section, MeetingTime)
                    VALUES(?, ?, ?, ?, ?) ON CONFLICT(Crn) DO UPDATE
                        SET MeetingTime = excluded.MeetingTime;"""
        )) {
            courseUpsert.setInt(1, course.getCrn());
            courseUpsert.setString(2, course.getSubject());
            courseUpsert.setInt(3, course.getCourseNumber());
            courseUpsert.setInt(4, course.getSectionNumber());
            courseUpsert.setString(5, course.getMeetingTime());
            courseUpsert.executeUpdate();
        }
    }

    public void addEnrollment(Student student, Course course) throws SQLException {
        try(PreparedStatement enrollmentInsert = connection.prepareStatement("""
                INSERT INTO Enrollments(StudentID, CRN)
                    VALUES(?, ?);"""
        )) {
            enrollmentInsert.setInt(1, student.getId());
            enrollmentInsert.setInt(2, course.getCrn());
            enrollmentInsert.executeUpdate();
        } catch (SQLException e) {
            rollback();
            throw e;
        }
    }

    /**
     * Gets a **shallow copy** of students - does not include course lists sorted by student ID
     * @return a shallow copy of students without their course lists
     */
    public List<Student> getStudents() throws SQLException {
        try(PreparedStatement selectStudent = connection.prepareStatement("""
                SELECT StudentId, FirstName, LastName, ComputingID
                    FROM Students
                    ORDER BY StudentID;
                """)) {
            ResultSet resultSet = selectStudent.executeQuery();
            List<Student> students = new ArrayList<>();
            while(resultSet.next()) {
                int studentId = resultSet.getInt("StudentID");
                String firstName = resultSet.getString("FirstName");
                String lastName = resultSet.getString("LastName");
                String computingID = resultSet.getString("ComputingID");
                students.add(new Student(studentId, firstName, lastName, computingID));
            }
            return students;
        }
    }

    public Optional<Student> getStudent(int studentID) throws SQLException {
        try(PreparedStatement selectStudent = connection.prepareStatement("""
                SELECT StudentId, FirstName, LastName, ComputingID
                    FROM Students
                    WHERE StudentID = ?
                """)) {
            selectStudent.setInt(1, studentID);

            ResultSet resultSet = selectStudent.executeQuery();

            // check if result set is empty - if it is, .next() returns false
            if (!resultSet.next()) {
                return Optional.empty();
            }
            // otherwise .next() is pointing at the student
            int studentId = resultSet.getInt("StudentID");
            String firstName = resultSet.getString("FirstName");
            String lastName = resultSet.getString("LastName");
            String computingID = resultSet.getString("ComputingID");
            return Optional.of(new Student(studentId, firstName, lastName, computingID));
        }
    }

    public List<Student> getStudentsByCourse(Course course) throws SQLException {
        try(PreparedStatement selectEnrollment = connection.prepareStatement("""
            SELECT StudentID FROM Enrollments
                WHERE CourseID = ?
            """)) {
            selectEnrollment.setInt(1, course.getCrn());
            ResultSet resultSet = selectEnrollment.executeQuery();

            List<Student> students = new ArrayList<>();
            while(resultSet.next()) {
                int studentID = resultSet.getInt("StudentID");
                Optional<Student> optionalStudent = getStudent(studentID);
                optionalStudent.ifPresent( student -> students.add(student) );
            }
            return students;
        }
    }
}
