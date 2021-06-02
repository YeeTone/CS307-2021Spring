package reference.util;

import cn.edu.sustech.cs307.database.SQLDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseClearUtil extends Util{
    private DatabaseClearUtil(){
        super();
    }

    public static void clearDatabase(){
        try(Connection conn= SQLDataSource.getInstance().getSQLConnection()){
            String[]sqls={"delete from prerequisite;",
                    "delete from course;",
                    "delete from semester;",
                    "delete from instructor;",
                    "delete from coursesection;",
                    "delete from coursesectionclass;",
                    "delete from major",
                    "delete from students",
                    "delete from studentcourseselection",
                    "delete from student100course",
                    "delete from studentpfcourse",
                    "alter sequence prerequisite_id_seq restart with 1;",
                    "alter sequence coursesection_sectionid_seq restart with 1;",
                    "alter sequence semester_semesterid_seq restart with 1;",
                    "alter sequence coursesectionclass_classid_seq restart with 1;"
            };

            for (String s:sqls){
                PreparedStatement p=conn.prepareStatement(s);
                p.executeUpdate();
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
}
