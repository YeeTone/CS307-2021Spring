package reference.service;

import cn.edu.sustech.cs307.database.SQLDataSource;
import cn.edu.sustech.cs307.dto.Course;
import cn.edu.sustech.cs307.dto.CourseSection;
import cn.edu.sustech.cs307.dto.CourseSectionClass;
import cn.edu.sustech.cs307.dto.Student;
import cn.edu.sustech.cs307.dto.prerequisite.Prerequisite;
import cn.edu.sustech.cs307.exception.IntegrityViolationException;
import cn.edu.sustech.cs307.service.CourseService;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.util.List;

@ParametersAreNonnullByDefault
public class ReferenceCourseService implements CourseService {

    @Override
    public void addCourse(String courseId, String courseName,
                          int credit, int classHour, Course.CourseGrading grading,
                          @Nullable Prerequisite prerequisite) {

    }

    @Override
    public int addCourseSection(String courseId, int semesterId, String sectionName, int totalCapacity) {

        try(Connection con= SQLDataSource.getInstance().getSQLConnection()){
            String sql
                    ="insert into coursesection" +
                    "(courseId, semesterId, sectionName, totalCapacity)" +
                    " values(?,?,?,?);";

            PreparedStatement preparedStatement=con.prepareStatement(sql);

            preparedStatement.setString(1,courseId);
            preparedStatement.setInt(2,semesterId);
            preparedStatement.setString(3,sectionName);
            preparedStatement.setInt(4,totalCapacity);

            preparedStatement.executeUpdate(sql,PreparedStatement.RETURN_GENERATED_KEYS);

            ResultSet rs= preparedStatement.getGeneratedKeys();

            if(rs.next()){
                return rs.getInt(3);
            }else {
                throw new IntegrityViolationException();
            }
        }catch (SQLException e){
            throw new IntegrityViolationException();
        }
    }

    @Override
    public int addCourseSectionClass(int sectionId, int instructorId, DayOfWeek dayOfWeek, List<Short> weekList, short classStart, short classEnd, String location) {
        return 0;
    }

    @Override
    public void removeCourse(String courseId) {

    }

    @Override
    public void removeCourseSection(int sectionId) {

    }

    @Override
    public void removeCourseSectionClass(int classId) {

    }

    @Override
    public List<Course> getAllCourses() {
        return null;
    }

    @Override
    public List<CourseSection> getCourseSectionsInSemester(String courseId, int semesterId) {
        return null;
    }

    @Override
    public Course getCourseBySection(int sectionId) {
        return null;
    }

    @Override
    public List<CourseSectionClass> getCourseSectionClasses(int sectionId) {
        return null;
    }

    @Override
    public CourseSection getCourseSectionByClass(int classId) {
        return null;
    }

    @Override
    public List<Student> getEnrolledStudentsInSemester(String courseId, int semesterId) {
        return null;
    }
}
