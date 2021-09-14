package JsonParse;

import java.sql.*;
import java.util.List;

public class DatabaseManipulation implements DataManipulation {
    private static int group=0;
    private static int teacherIDCNT=30000000;
    private Connection con = null;
    private ResultSet resultSet;

    private String host = "localhost";
    private String dbname = "project1db";
    private String user = "yeetone";
    private String pwd = "******";
    private String port = "5432";

    public void resetGroup(){
        group=0;
    }

    public static int getGroup() {
        return group;
    }

    public double findDeptIDByName(String name){
        String sql="select * from department where department_name = ?";
        try{
            PreparedStatement preparedStatement=con.prepareStatement(sql);
            preparedStatement.setString(1,name);
            //Statement statement=con.createStatement();
            resultSet=preparedStatement.executeQuery();
            if(resultSet.next()){
                return resultSet.getDouble("department_id");
                //sb.append(resultSet.getInt("department_id"));
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return 0;
    }

    public int findTeacherIDByName(String name){
        String sql="select * from teacher where teacher_name = ?";
        try{
            PreparedStatement preparedStatement=con.prepareStatement(sql);
            preparedStatement.setString(1,name);
            //Statement statement=con.createStatement();
            resultSet=preparedStatement.executeQuery();
            if(resultSet.next()){
                return resultSet.getInt("teacher_id");
                //sb.append(resultSet.getInt("department_id"));
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return 0;
    }

    public int findLocationIDByName(String name){
        String sql="select * from location where location_name = ?";
        try{
            PreparedStatement preparedStatement=con.prepareStatement(sql);
            preparedStatement.setString(1,name);
            //Statement statement=con.createStatement();
            resultSet=preparedStatement.executeQuery();
            if(resultSet.next()){
                return resultSet.getInt("location_id");
                //sb.append(resultSet.getInt("department_id"));
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return 0;
    }

    public void checkIfAllCourse_ClassEnterDB(List<Course>courses){
        String sql="select class_name,courses_id from classes where class_name=? and courses_id=?;";
        for(Course c:courses){
            try{
                PreparedStatement preparedStatement=con.prepareStatement(sql);
                preparedStatement.setString(1,c.getClassName().trim());
                preparedStatement.setString(2,c.getCourseId().trim().toUpperCase());
                //Statement statement=con.createStatement();
                resultSet=preparedStatement.executeQuery();
                if(!resultSet.next()){
                    throw new SQLException();
                }
            }catch (SQLException e){
                //e.printStackTrace();
            }
        }
    }
    @Override
    public void openDatasource() {
        try {
            Class.forName("org.postgresql.Driver");

        } catch (Exception e) {
            System.err.println("Cannot find the PostgreSQL driver. Check CLASSPATH.");
            System.exit(1);
        }

        try {
            String url = "jdbc:postgresql://" + host + ":" + port + "/" + dbname;
            con = DriverManager.getConnection(url, user, pwd);

        } catch (SQLException e) {
            System.err.println("Database connection failed");
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    @Override
    public void closeDatasource() {
        if (con != null) {
            try {
                con.close();
                con = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int addOneTeacher(String teacher){
        int result = 0;
        String sql="insert into teacher(teacher_id,teacher_name) values(?,?)";

        try{
            PreparedStatement preparedStatement=con.prepareStatement(sql);
            preparedStatement.setInt(1,++teacherIDCNT);
            preparedStatement.setString(2,teacher);
            System.out.println(preparedStatement.toString());
            result=preparedStatement.executeUpdate();
        }
        catch (SQLException e){
            e.printStackTrace();
            teacherIDCNT--;
        }


        return result;
    }

    public int addOneLocation(String location){
        int result = 0;
        String sql="insert into location(location_name) values(?)";
        try{
            PreparedStatement preparedStatement=con.prepareStatement(sql);
            preparedStatement.setString(1,location);
            System.out.println(preparedStatement.toString());
            result= preparedStatement.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
        }
        return result;
    }

    public int addOneCourseDept(String courseDept){
        int result = 0;
        String sql="insert into department(department_name) values(?)";
        try{
            PreparedStatement preparedStatement=con.prepareStatement(sql);
            preparedStatement.setString(1,courseDept);
            System.out.println(preparedStatement.toString());
            result=preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public int addOneCollege(College college) {
        int result = 0;
        String sql = "insert into college (college_chinese_name, college_english_name) " +
                "values (?,?)";

        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setString(1, college.getCollege_chinese_name());
            preparedStatement.setString(2, college.getCollege_english_name());
            result = preparedStatement.executeUpdate();

        } catch (SQLException e) {
            // e.printStackTrace();
        }
        return result;
    }


    @Override
    public int addOneCourse(Course c){
        int result = 0;
        String sql="insert into course(course_id,department_id,course_credits,course_hour,max_prerequisite,course_name) " +
                "values(?,?,?,?,?,?)";
        try{

            PreparedStatement preparedStatement=con.prepareStatement(sql);
            preparedStatement.setString(1,c.getCourseId());
            preparedStatement.setDouble(2,findDeptIDByName(c.getCourseDept()));
            preparedStatement.setDouble(3,c.getCourseCredit());
            preparedStatement.setInt(4,c.getCourseHour());
            preparedStatement.setInt(5,0);
            preparedStatement.setString(6,c.getCourseName()
                    .replaceAll("（","(").replaceAll("）",")"));
            if(c.getCourseName().equals("SUSTech English III")){
                System.out.println(preparedStatement.toString());
            }

            result=preparedStatement.executeUpdate();
            System.out.println("result = " + result);
        }catch (SQLException e){
            if(c.getCourseName().equals("SUSTech English III")){
                //System.out.println("IPE303 Error!");
                e.printStackTrace();
            }

        }
        return result;
    }

    public int addOneClass(Course c) {
        int result = 0;
        try {
            String sql = "insert into classes(class_name,courses_id,total_capacity,location_id,havinglessons) " +
                    "values(?,?,?,?,?)";
            int[][] lessons = new int[c.getClassList().length][16];

            for (int i = 0; i < c.getClassList().length; i++) {
                for (int j = 0; j < c.getClassList()[i].getWeekList().length; j++) {
                    int lesson = c.getClassList()[i].getWeekList()[j];
                    lessons[i][lesson - 1]++;
                }
            }

            String[] lessonsInfo = new String[c.getClassList().length];
            for (int i = 0; i < lessonsInfo.length; i++) {
                StringBuilder b = new StringBuilder();
                for (int j = 0; j < 16; j++) {
                    if (lessons[i][j] == 0) {
                        b.append("N");
                    } else {
                        b.append("Y");
                    }
                }
                lessonsInfo[i] = b.toString();
            }


            try {
                PreparedStatement preparedStatement = con.prepareStatement(sql);
                preparedStatement.setString(1, c.getClassName().trim().toUpperCase());
                preparedStatement.setString(2, c.getCourseId().trim().toUpperCase());
                preparedStatement.setInt(3, c.getTotalCapacity());
                preparedStatement.setInt(4, findLocationIDByName(c.getClassList()[0].getLocation()));
                if(c.getCourseId().trim().equals("MED307")){
                    System.out.println("result = " + c.getClassList()[0].getLocation());
                }
                preparedStatement.setArray(5, con.createArrayOf("varchar", lessonsInfo));
                //System.out.println(preparedStatement.toString());
                result = preparedStatement.executeUpdate();
                if(c.getCourseId().trim().equals("MED307")){
                    System.out.println("result = " + result);
                }
            } catch (SQLException e) {
                if(c.getCourseId().trim().equals("MED307")){
                    e.printStackTrace();
                }

            }
        } catch (NullPointerException e) {
            return 0;
        }


        return result;
    }

    public int addOneTeacherClass(Course c){
        int result = 0;
        String sql="insert into teacher_class(teacher_id,course_id,class_name)" +
                "values(?,?,?)";
        if(c.getTeacher()==null){
            try {
                PreparedStatement preparedStatement = con.prepareStatement(sql);
                preparedStatement.setString(1,null);
                preparedStatement.setString(2, c.getCourseId().trim());
                preparedStatement.setString(3, c.getClassName().trim());
                System.out.println(preparedStatement.toString());
                result = preparedStatement.executeUpdate();
            } catch (SQLException e) {
                //e.printStackTrace();
                return 0;
            }
            System.out.println("OK!");
            return 0;
        }
        String[]teachers=c.getTeacher().split(",");
        for (String teacher : teachers) {
            try {
                PreparedStatement preparedStatement = con.prepareStatement(sql);
                preparedStatement.setInt(1, findTeacherIDByName(teacher));
                preparedStatement.setString(2, c.getCourseId().trim());
                preparedStatement.setString(3, c.getClassName().trim());
                System.out.println(preparedStatement.toString());
                result = preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public Long findCollegeIDByName(String name){
        String sql="select * from College where college_chinese_name = ?";
        try{
            PreparedStatement preparedStatement=con.prepareStatement(sql);
            preparedStatement.setString(1,name);
            //Statement statement=con.createStatement();
            resultSet=preparedStatement.executeQuery();
            if(resultSet.next()){
                return resultSet.getLong("college_id");
                //sb.append(resultSet.getInt("department_id"));
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int addOneStudent(Student student) {
        int result = 0;
        String sql = "insert into student (sid, name, gender, college_id) " +
                "values (?,?,?,?)";

        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setString(1, student.getSid());
            preparedStatement.setString(2, student.getName());
            preparedStatement.setString(3, student.getGender());

            preparedStatement.setLong(4, findCollegeIDByName(student.getCollege_chinese_name()));
            //     preparedStatement.setInt(4, Integer.parseInt(movieInfo[3]));
            //  System.out.println(preparedStatement.toString());

            result = preparedStatement.executeUpdate();

        } catch (SQLException e) {
            //  e.printStackTrace();
        }
        return result;
    }
    @Override
    public int addOneCourseSelection(Course_selection_info course_selection_info) {
        int result = 0;
        String sql = "insert into course_selection (course_id, student_id) " +
                "values (?,?)";
        String[] courses = course_selection_info.getCourses_id();
        String student_id = course_selection_info.getStudent_id();
        for (int i = 0; i < courses.length; i++) {

            try {
                PreparedStatement preparedStatement = con.prepareStatement(sql);
                preparedStatement.setString(1, courses[i]);
                preparedStatement.setString(2, student_id);
                result = preparedStatement.executeUpdate();

            } catch (SQLException e) {
                //e.printStackTrace();
            }
        }
        return result;
    }

    public int addClassWeekList(Course c){
        int result=0;
        for (ClassList cl:c.getClassList()){
            int[]yn16=new int[16];
            StringBuilder b=new StringBuilder();
            for (int yn:cl.getWeekList()){
                yn16[yn-1]++;
            }
            for (int yn:yn16){
                if(yn>0){
                    b.append('Y');
                }else {
                    b.append('N');
                }
            }
            String classTime=cl.getClassTime().trim();
            int divide=classTime.indexOf('-');
            int start=Integer.parseInt(classTime.substring(0,divide));
            int end=Integer.parseInt(classTime.substring(divide+1));

            for (int i=0;i<16;i++){
                try{
                    String sql="insert into class_weekList(course_id,class_name,week,havinglesson,classStart,classEnd,weekday,location_id)" +
                            "values (?,?,?,?,?,?,?,?)";
                    PreparedStatement preparedStatement=con.prepareStatement(sql);
                    preparedStatement.setString(1,c.getCourseId().trim().toUpperCase());
                    preparedStatement.setString(2,c.getClassName().trim().toUpperCase());
                    preparedStatement.setInt(3,i+1);
                    preparedStatement.setString(4,String.valueOf(b.charAt(i)));
                    preparedStatement.setInt(5,start);
                    preparedStatement.setInt(6,end);
                    preparedStatement.setInt(7,cl.getWeekday());
                    preparedStatement.setInt(8,findLocationIDByName(cl.getLocation()));
                    result=preparedStatement.executeUpdate();
                }catch (SQLException e){
                    e.printStackTrace();
                }
            }

        }
        return result;
    }

    @Override
    public String continentsWithCountryCount() {
        StringBuilder sb = new StringBuilder();
        String sql = "select continent, count(*) countryNumber from countries group by continent;";
        try {
            Statement statement = con.createStatement();
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                sb.append(resultSet.getString("continent")).append("\t");
                sb.append(resultSet.getString("countryNumber"));
                sb.append(System.lineSeparator());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    @Override
    public void addOneCoursePrerequisite(Course c,PrerequisiteTreeNode root) {
        if(root==null){
            return;
        }

        switch (root.getType()) {
            case "and":
                if (root.getLeftChild() != null && !root.getLeftChild().getType().equals("and")) {
                    group++;
                }
                addOneCoursePrerequisite(c, root.getLeftChild());

                if (root.getRightChild() != null && !root.getRightChild().getType().equals("and")) {
                    group++;
                }
                addOneCoursePrerequisite(c, root.getRightChild());
                break;
            case "or":
                addOneCoursePrerequisite(c, root.getLeftChild());
                addOneCoursePrerequisite(c, root.getRightChild());
                break;
            case "course":
                String sql = "insert into prerequisite(course_id,prerequisite_id,group_id)values(?,?,?);";
                String course_id = getCourseIdByName(root.getName());
                if(course_id==null){
                    return;
                }
                try {
                    PreparedStatement preparedStatement = con.prepareStatement(sql);
                    preparedStatement.setString(1, c.getCourseId());
                    preparedStatement.setString(2, course_id);
                    root.setGroup(group);
                    preparedStatement.setInt(3, group+1);
                    //System.out.println(preparedStatement.toString());
                    //preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    public String getCourseIdByName(String c_name){
        int result=0;
        String sql="select course_id from course where course_name=?";
        try{
            PreparedStatement preparedStatement=con.prepareStatement(sql);
            preparedStatement.setString(1,c_name.trim().toUpperCase());
            //System.out.println(preparedStatement.toString());
            resultSet=preparedStatement.executeQuery();
            if(resultSet.next()){
                return resultSet.getString("course_id");
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }
}
