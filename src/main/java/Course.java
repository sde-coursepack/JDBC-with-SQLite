import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Course {
    private final int crn;
    private final String subject;
    private final int courseNumber;
    private final int sectionNumber;
    private final String meetingTime;

    private final List<Student> students;

    public Course(int crn, String subject, int courseNumber, int sectionNumber, String meetingTime) {
        this.crn = crn;
        this.subject = subject;
        this.courseNumber = courseNumber;
        this.sectionNumber = sectionNumber;
        this.meetingTime = meetingTime;
        students = new ArrayList<Student>();
    }

    public int getCrn() {
        return crn;
    }

    public String getSubject() {
        return subject;
    }

    public int getCourseNumber() {
        return courseNumber;
    }

    public int getSectionNumber() {
        return sectionNumber;
    }

    public String getMeetingTime() {
        return meetingTime;
    }

    public List<Student> getStudents() {
        return Collections.unmodifiableList(students);
    }

    public void addStudent(Student student) {
        students.add(student);
    }

    public void removeStudent(Student student) {
        students.remove(student);
    }

    @Override
    public String toString() {
        return "Course{" +
               "crn=" + crn +
               ", subject='" + subject + '\'' +
               ", courseNumber=" + courseNumber +
               ", sectionNumber=" + sectionNumber +
               ", meetingTime='" + meetingTime + '\'' +
               ", students=" + students +
               '}';
    }
}
