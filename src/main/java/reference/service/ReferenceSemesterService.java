package Reference;

import cn.edu.sustech.cs307.database.SQLDataSource;
import cn.edu.sustech.cs307.dto.Semester;
import cn.edu.sustech.cs307.exception.IntegrityViolationException;
import cn.edu.sustech.cs307.service.SemesterService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReferenceSemesterService implements SemesterService {

    @Override
    public int addSemester(String name, Date begin, Date end) {
        try (Connection con = SQLDataSource.getInstance().getSQLConnection()) {
            String sql1 = "insert into Semester" +
                    "(name, begin_time, end_time)" +
                    "values(?, ?, ?)";
            PreparedStatement preparedStatement = con.prepareStatement(sql1);

            preparedStatement.setString(1, name);
            preparedStatement.setDate(2, begin);
            preparedStatement.setDate(3, end);
            preparedStatement.executeUpdate();
            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            return resultSet.getInt(2);
        } catch (SQLException throwable) {
            throwable.printStackTrace();
            throw new IntegrityViolationException();
        }
    }

    @Override
    public void removeSemester(int semesterId) {
        try (Connection con = SQLDataSource.getInstance().getSQLConnection()) {
            String sql1 = "delete from semester where semesterId = ?";
            PreparedStatement preparedStatement = con.prepareStatement(sql1);
            preparedStatement.setInt(1, semesterId);
            preparedStatement.executeUpdate();
        } catch (SQLException throwable) {
            throwable.printStackTrace();
            throw new IntegrityViolationException();
        }

    }

    @Override
    public List<Semester> getAllSemesters() {
        List<Semester> result = new ArrayList<>();
        try (Connection con = SQLDataSource.getInstance().getSQLConnection()) {
            String sql = "select * from semester";
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int semesterId = resultSet.getInt(2);
                String name = resultSet.getString(1);
                Date begin_time = resultSet.getDate(3);
                Date end_time = resultSet.getDate(4);

                Semester semester = new Semester();
                semester.id = semesterId;
                semester.begin = begin_time;
                semester.end = end_time;
                semester.name = name;

                result.add(semester);
            }
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            return result;
        }

    }

    @Override
    public cn.edu.sustech.cs307.dto.Semester getSemester(int semesterId){
        try (Connection con = SQLDataSource.getInstance().getSQLConnection()) {
            Semester semester = new Semester();
            String sql = "select * from semester where semesterId = ?";
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setInt(1, semesterId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                semester.id = resultSet.getInt(2);
                semester.name = resultSet.getString(1);
                semester.begin = resultSet.getDate(3);
                semester.end = resultSet.getDate(4);
                return semester;
            } else return null;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IntegrityViolationException();
        }

    }
}
