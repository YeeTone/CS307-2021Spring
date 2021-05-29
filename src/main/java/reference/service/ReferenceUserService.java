
package reference.service;


import cn.edu.sustech.cs307.database.SQLDataSource;
import cn.edu.sustech.cs307.dto.Instructor;
import cn.edu.sustech.cs307.dto.Student;
import cn.edu.sustech.cs307.dto.User;
import cn.edu.sustech.cs307.exception.IntegrityViolationException;
import cn.edu.sustech.cs307.service.UserService;

import javax.annotation.ParametersAreNonnullByDefault;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ParametersAreNonnullByDefault
public class ReferenceUserService implements UserService {
    @Override
    public void removeUser(int userId) {
        try (Connection con = SQLDataSource.getInstance().getSQLConnection()) {
            String sql1 = "delete from instructor where userId = ?";
            PreparedStatement preparedStatement = con.prepareStatement(sql1);
            preparedStatement.setInt(1, userId);
            preparedStatement.executeUpdate();
            String sql2 = "delete from students where userId = ?";
            PreparedStatement preparedStatement1 = con.prepareStatement(sql2);
            preparedStatement1.setInt(1, userId);
            preparedStatement1.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IntegrityViolationException();
        }
    }

    @Override
    public List<User> getAllUsers() {
        List<User> result = new ArrayList<>();
        try (Connection con = SQLDataSource.getInstance().getSQLConnection()) {
            String sql1 = "select * from instructor";
            PreparedStatement preparedStatement = con.prepareStatement(sql1);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int userId = resultSet.getInt(1);
                String firstName = resultSet.getString(2);
                String lastName = resultSet.getString(3);

                User user = new Instructor();
                user.id = userId;
                user.fullName = firstName + lastName;
                result.add(user);
            }

            String sql2 = "select * from students";
            PreparedStatement preparedStatement1 = con.prepareStatement(sql2);
            ResultSet resultSet1 = preparedStatement1.executeQuery();
            while (resultSet1.next()) {
                int userId = resultSet1.getInt(1);
                int majorId = resultSet1.getInt(2);
                String firstName = resultSet1.getString(3);
                String lastName = resultSet1.getString(4);
                Date enrollDate = resultSet1.getDate(5);
                User user = new Student();
                user.id = userId;
                user.fullName = firstName + lastName;
                result.add(user);
            }
            return result;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return result;
        }
    }

    @Override
    public cn.edu.sustech.cs307.dto.User getUser(int userId) {
        try (Connection con = SQLDataSource.getInstance().getSQLConnection()) {
            User user1 = new Instructor();
            User user2 = new Student();
            String sql1 = "select * from instructor where userId = ?";
            String sql2 = "select * from students where userId = ?";
            PreparedStatement preparedStatement = con.prepareStatement(sql1);
            preparedStatement.setInt(1, userId);
            ResultSet resultSet1 = preparedStatement.executeQuery();
            PreparedStatement preparedStatement1 = con.prepareStatement(sql2);
            preparedStatement1.setInt(1, userId);
            ResultSet resultSet2 = preparedStatement1.executeQuery();
            if (resultSet1.next()) {
                user1.id = resultSet1.getInt(1);
                user1.fullName = resultSet1.getString(2) + resultSet1.getString(3);
                return user1;
            } else {
                user2.id = resultSet2.getInt(1);
                user2.fullName = resultSet2.getString(3) + resultSet2.getString(4);
                return user2;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new IntegrityViolationException();
        }
    }
}
