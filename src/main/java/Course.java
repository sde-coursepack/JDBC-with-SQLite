import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Course {
    private final int crn;
    private final String subject;
    private final int courseNumber;
    private final int sectionNumber;
    private String meetingTime;

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

    public void setMeetingTime(String meetingTime) {
        this.meetingTime = meetingTime;
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

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Course course)) return false;

        return crn == course.crn && courseNumber == course.courseNumber && sectionNumber == course.sectionNumber && subject.equals(course.subject) && meetingTime.equals(course.meetingTime);
    }

    @Override
    public int hashCode() {
        int result = crn;
        result = 31 * result + subject.hashCode();
        result = 31 * result + courseNumber;
        result = 31 * result + sectionNumber;
        return result;
    }
}
