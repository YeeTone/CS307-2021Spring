
package reference.service;
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
        List<CourseSection> result = new ArrayList<>();
        try (Connection con = SQLDataSource.getInstance().getSQLConnection()) {
            String sql1 = "select * from courseSectionClass where instructorId = ?";
            PreparedStatement preparedStatement = con.prepareStatement(sql1);
            preparedStatement.setInt(1, instructorId);
            preparedStatement.executeUpdate();
            ResultSet resultSet = preparedStatement.executeQuery();
            String sql2 = "select * from courseSection where semesterId = ? and sectionId = ?";
            PreparedStatement preparedStatement1 = con.prepareStatement(sql2);
            preparedStatement1.setInt(1, semesterId);
            preparedStatement1.setInt(2, resultSet.getInt(1));
            preparedStatement1.executeUpdate();
            ResultSet resultSet1 = preparedStatement1.executeQuery();
            CourseSection courseSection = new CourseSection();
            String sql3 = "select cs.sectionId, cs.sectionname, cs.totalcapacity, count(s.studentId) " +
                    "from coursesection as cs " +
                    "inner join studentcourseselection as s " +
                    "on cs.sectionId = s.sectionId " +
                    "where courseId = ? and semesterId = ? group by s.studentId, cs.sectionId";
            PreparedStatement preparedStatement2 = con.prepareStatement(sql3);
            preparedStatement2.setString(1, resultSet1.getString(1));
            preparedStatement2.setInt(2, semesterId);
            ResultSet resultSet2 = preparedStatement2.executeQuery();
            while (resultSet1.next()) {
                courseSection.id = resultSet1.getInt(3);
                courseSection.name = resultSet1.getString(4);
                courseSection.leftCapacity = resultSet1.getInt(5)-resultSet2.getInt(4);
                courseSection.totalCapacity = resultSet1.getInt(5);

                result.add(courseSection);

            }
            return result;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new IntegrityViolationException();
        }
    }
}
