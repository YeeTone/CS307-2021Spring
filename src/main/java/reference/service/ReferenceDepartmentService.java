
package reference.service;

import cn.edu.sustech.cs307.database.SQLDataSource;
import cn.edu.sustech.cs307.dto.Department;
import cn.edu.sustech.cs307.exception.IntegrityViolationException;
import cn.edu.sustech.cs307.service.DepartmentService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReferenceDepartmentService implements DepartmentService {
    @Override
    public int addDepartment(String name) {
        try (Connection con = SQLDataSource.getInstance().getSQLConnection()) {
            String sql1 = "insert into department" +
                    "(name)" + "values (?)";
            PreparedStatement preparedStatement = con.prepareStatement(sql1);
            preparedStatement.setString(1, name);
            preparedStatement.executeUpdate();
            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            return resultSet.getInt(2);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new IntegrityViolationException();
        }
    }

    @Override
    public void removeDepartment(int departmentId) {
        try (Connection con = SQLDataSource.getInstance().getSQLConnection()) {
            String sql1 = "delete from department where departmentId = ?";
            PreparedStatement preparedStatement = con.prepareStatement(sql1);
            preparedStatement.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new IntegrityViolationException();
        }
    }

    @Override
    public List<Department> getAllDepartments() {
        List<Department> result = new ArrayList<>();
        try (Connection con = SQLDataSource.getInstance().getSQLConnection()) {
            String sql1 = "select * from department";
            PreparedStatement preparedStatement = con.prepareStatement(sql1);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String name = resultSet.getString(1);
                int departmentId = resultSet.getInt(2);

                Department department = new Department();
                department.name = name;
                department.id = departmentId;

                result.add(department);
            }
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            return result;
        }
    }

    @Override
    public cn.edu.sustech.cs307.dto.Department getDepartment(int departmentId) {
        try (Connection con = SQLDataSource.getInstance().getSQLConnection()) {
            Department department = new Department();
            String sql1 = "select * from department where departmentId = ?";
            PreparedStatement preparedStatement = con.prepareStatement(sql1);
            preparedStatement.setInt(1, departmentId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                department.name  = resultSet.getString(1);
                department.id = resultSet.getInt(2);
                return department;
            } else return null;
        } catch (SQLException e) {
            e.printStackTrace();;
            throw new IntegrityViolationException();
        }
    }
}
