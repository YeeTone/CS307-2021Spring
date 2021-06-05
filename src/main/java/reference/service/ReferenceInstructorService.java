package Reference.ServiceImplementation;

import cn.edu.sustech.cs307.database.SQLDataSource;
import cn.edu.sustech.cs307.dto.CourseSection;
import cn.edu.sustech.cs307.exception.IntegrityViolationException;
import cn.edu.sustech.cs307.service.InstructorService;

import javax.annotation.ParametersAreNonnullByDefault;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
public class ReferenceInstructorService implements InstructorService {
    @Override
    public void addInstructor(int userId, String firstName, String lastName) {
        try (Connection con = SQLDataSource.getInstance().getSQLConnection()) {
            String sql1 = "insert into instructor" +
                    "(userId, firstName, lastName)" +
                    "values (?, ?, ?)";
            PreparedStatement preparedStatement = con.prepareStatement(sql1);
            preparedStatement.setInt(1, userId);
            preparedStatement.setString(2, firstName);
            preparedStatement.setString(3, lastName);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IntegrityViolationException();
        }
    }

    @Override
    public List<CourseSection> getInstructedCourseSections(int instructorId, int semesterId) {
        try (Connection con = SQLDataSource.getInstance().getSQLConnection()) {
            List<CourseSection> result = new ArrayList<>();
            String sql="select s.sectionid, m.sectionname, m.totalcapacity, count(s.studentid)" +
                    "from (" +
                    "    (select c2.*" +
                    "      from instructor" +
                    "               left outer join coursesectionclass c on instructor.userid = c.instructorid" +
                    "               inner join coursesection c2 on c2.sectionid = c.sectionid" +
                    "      where instructor.userid = ?" +
                    "        and c2.semesterid = ?) m" +
                    "         inner join studentcourseselection s on s.sectionid = m.sectionid)" +
                    "group by s.sectionid, m.sectionname, m.totalcapacity;";
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setInt(1, instructorId);
            preparedStatement.setInt(2, semesterId);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                CourseSection courseSection = new CourseSection();
                courseSection.id = resultSet.getInt(1);
                courseSection.name = resultSet.getString(2);
                courseSection.totalCapacity = resultSet.getInt(3);
                courseSection.leftCapacity = courseSection.totalCapacity - resultSet.getInt(4);
                result.add(courseSection);
            }
            return result;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new IntegrityViolationException();
        }

    }
}
