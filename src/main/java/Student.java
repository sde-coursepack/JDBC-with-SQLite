import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Student {
    private final int id;
    private String firstName;
    private String lastName;
    private final String computingID;
    private final List<Course> courses;

    public Student(int id, String firstName, String lastName, String computingID) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.computingID = computingID;
        courses = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getComputingID() {
        return computingID;
    }

    public List<Course> getCourses() {
        return Collections.unmodifiableList(courses);
    }

    public void addCourse(Course course) {
        courses.add(course);
    }

    public void removeCourse(Course course) {
        courses.remove(course);
    }

    @Override
    public String toString() {
        return "Student{" +
               "id=" + id +
               ", firstName='" + firstName + '\'' +
               ", lastName='" + lastName + '\'' +
               ", computingID='" + computingID + '\'' +
               ", courses=" + courses +
               '}';
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Student student)) return false;

        return id == student.id &&
               Objects.equals(firstName, student.firstName) &&
               Objects.equals(lastName, student.lastName) &&
               computingID.equals(student.computingID);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + computingID.hashCode();
        return result;
    }
}
