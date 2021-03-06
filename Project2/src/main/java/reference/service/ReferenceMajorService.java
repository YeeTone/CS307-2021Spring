
package reference.service;

import cn.edu.sustech.cs307.database.SQLDataSource;
import cn.edu.sustech.cs307.dto.Major;
import cn.edu.sustech.cs307.exception.EntityNotFoundException;
import cn.edu.sustech.cs307.exception.IntegrityViolationException;
import cn.edu.sustech.cs307.service.MajorService;

import javax.annotation.ParametersAreNonnullByDefault;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
public class ReferenceMajorService implements MajorService {
    @Override
    public int addMajor(String name, int departmentId) {
        try (Connection con = SQLDataSource.getInstance().getSQLConnection()) {
            String sql1 = "insert into major" +
                    "(name, departmentId)" +
                    "values (?, ?)";
            PreparedStatement preparedStatement = con.prepareStatement(sql1, PreparedStatement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, departmentId);
            preparedStatement.executeUpdate();
            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            if(resultSet.next()){
                return resultSet.getInt(1);
            }else {
                throw new EntityNotFoundException();
            }


        } catch (SQLException e) {
            e.printStackTrace();
            throw new IntegrityViolationException();
        }
    }

    @Override
    public void removeMajor(int majorId) {
        try (Connection con = SQLDataSource.getInstance().getSQLConnection()) {
            String sql1 = "delete from major where majorId = ?";
            PreparedStatement preparedStatement = con.prepareStatement(sql1);
            preparedStatement.setInt(1, majorId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IntegrityViolationException();
        }
    }

    @Override
    public List<Major> getAllMajors() {
        List<Major> result = new ArrayList<>();
        try (Connection con = SQLDataSource.getInstance().getSQLConnection()) {
            String sql1 = "select * from major";
            PreparedStatement preparedStatement = con.prepareStatement(sql1);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int majorId = resultSet.getInt(1);
                String name = resultSet.getString(2);
                int departmentId = resultSet.getInt(3);

                Major major = new Major();
                major.id = majorId;
                major.name = name;
                ReferenceDepartmentService department = new ReferenceDepartmentService();
                major.department = department.getDepartment(departmentId);

                result.add(major);
            }
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IntegrityViolationException();
        }
    }

    @Override
    public Major getMajor(int majorId) {
        try (Connection con = SQLDataSource.getInstance().getSQLConnection()) {
            Major major = new Major();
            String sql1 = "select * from major where majorId = ?";
            PreparedStatement preparedStatement = con.prepareStatement(sql1);
            preparedStatement.setInt(1, majorId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                major.id = resultSet.getInt(1);
                major.name = resultSet.getString(2);
                ReferenceDepartmentService department = new ReferenceDepartmentService();
                major.department = department.getDepartment(resultSet.getInt(3));
                return major;
            } else return null;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IntegrityViolationException();
        }
    }

    @Override
    public void addMajorCompulsoryCourse(int majorId, String courseId){
        try (Connection con = SQLDataSource.getInstance().getSQLConnection()) {
            String sql1 = "insert into majorCourse" +
                    "(majorId, courseId, isCompulsory)" +
                    "values (?, ?, ?)";
            PreparedStatement preparedStatement = con.prepareStatement(sql1);

            preparedStatement.setInt(1, majorId);
            preparedStatement.setString(2, courseId);
            preparedStatement.setBoolean(3, true);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IntegrityViolationException();
        }
    }

    @Override
    public void addMajorElectiveCourse(int majorId, String courseId) {
        try (Connection con = SQLDataSource.getInstance().getSQLConnection()) {
            String sql1 = "insert into majorCourse" +
                    "(majorId, courseId, isCompulsory)" +
                    "values (?, ?, ?)";
            PreparedStatement preparedStatement = con.prepareStatement(sql1);

            preparedStatement.setInt(1, majorId);
            preparedStatement.setString(2, courseId);
            preparedStatement.setBoolean(3, false);
            preparedStatement.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new IntegrityViolationException();
        }
    }
}
