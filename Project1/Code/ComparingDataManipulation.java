package JsonParse;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class ComparingDataManipulation implements DataManipulation{
    private static int group=-1;
    private static int teacherIDCNT=30000000;
    private static Connection con = null;
    private static ResultSet resultSet;

    private String host = "localhost";
    private String dbname = "Project1Test";
    private String user = "yeetone";
    private String pwd = "******";
    private String port = "5432";

    private static HashMap<String,Long> collegeHash;
    private static ArrayList<String>allInput=new ArrayList<>();
    public static void main(String[] args) {
        test3();
    }

    public static void test1(){
        //Running Time: 347950;
        try {
            DataManipulation dm = new ComparingDataManipulation();
            dm.openDatasource(); // 打开数据库
            BufferedReader reader = new BufferedReader(new FileReader(
                    "D:\\计算机\\数据库\\Project\\release of project 1\\data\\output1_4.json"));
            String line;
            long t1=System.currentTimeMillis();
            while ((line= reader.readLine())!=null){
                try{
                    Student student=new Student(line);
                    String sql="insert into student (sid, name, gender, college_id) " +
                            "values ('"+student.getSid()+"','"+student.getName()
                            +"','"+student.getGender()+"',"+findCollegeIDByName(student.getCollege_chinese_name())+")";
                    Statement statement=con.createStatement();
                    statement.executeUpdate(sql);
                }catch (SQLException e){
                    e.printStackTrace();
                }
            }
            long t2=System.currentTimeMillis();
            System.out.println(t2-t1);

            /*for (College c:College.cnNameCollegeHashMap.values()){
                String sql="insert into college (college_chinese_name, college_english_name) " +
                        "values ('"+c.getCollege_chinese_name()+"','"+
                        c.getCollege_english_name().substring(0,c.getCollege_english_name().length()-1)+"');";
                System.out.println(sql);
                Statement statement=con.createStatement();


                statement.execute(sql);
            }*/



            dm.closeDatasource();
        }
        catch (IllegalArgumentException | IOException e) {
            System.err.println(e.getMessage());
        }
    }
    public static void test2(){
        //289657
        try {
            DataManipulation dm = new ComparingDataManipulation();
            dm.openDatasource(); // 打开数据库
            BufferedReader reader = new BufferedReader(new FileReader(
                    "D:\\计算机\\数据库\\Project\\release of project 1\\data\\output1_4.json"));
            String line;
            long t1=System.currentTimeMillis();
            int result=0;
            while ((line= reader.readLine())!=null){
                try{
                    Student student=new Student(line);
                    String sql="insert into student (sid, name, gender, college_id) " +
                            "values (?,?,?,?);";
                    Long cid=findCollegeIDByName(student.getCollege_chinese_name());
                    if(cid==null){
                        continue;
                    }
                    PreparedStatement preparedStatement=con.prepareStatement(sql);
                    preparedStatement.setString(1,student.getSid());
                    preparedStatement.setString(2,student.getName());
                    preparedStatement.setString(3,student.getGender());
                    preparedStatement.setLong(4,cid);
                    //System.out.println(preparedStatement.toString());
                    result=preparedStatement.executeUpdate();
                }catch (SQLException e){
                    e.printStackTrace();
                }
            }
            long t2=System.currentTimeMillis();
            System.out.println(t2-t1);

            /*for (College c:College.cnNameCollegeHashMap.values()){
                String sql="insert into college (college_chinese_name, college_english_name) " +
                        "values ('"+c.getCollege_chinese_name()+"','"+
                        c.getCollege_english_name().substring(0,c.getCollege_english_name().length()-1)+"');";
                System.out.println(sql);
                Statement statement=con.createStatement();


                statement.execute(sql);
            }*/



            dm.closeDatasource();
        }
        catch (IllegalArgumentException | IOException e) {
            System.err.println(e.getMessage());
        }
    }
    public static void test3(){
        try {
            DataManipulation dm = new ComparingDataManipulation();
            dm.openDatasource(); // 打开数据库
            BufferedReader reader = new BufferedReader(new FileReader(
                    "D:\\计算机\\数据库\\Project\\release of project 1\\data\\output1_4.json"));
            String line;
            long t1=System.currentTimeMillis();
            int result=0;
            String sql="insert into student (sid, name, gender, college_id) " +
                    "values (?,?,?,?);";
            PreparedStatement preparedStatement=con.prepareStatement(sql);
            while ((line= reader.readLine())!=null){
                try{
                    Student student=new Student(line);

                    Long cid=findCollegeIDByName(student.getCollege_chinese_name());
                    if(cid==null){
                        continue;
                    }

                    preparedStatement.setString(1,student.getSid());
                    preparedStatement.setString(2,student.getName());
                    preparedStatement.setString(3,student.getGender());
                    preparedStatement.setLong(4,cid);
                    //System.out.println(preparedStatement.toString());
                    result=preparedStatement.executeUpdate();
                }catch (SQLException e){
                    e.printStackTrace();
                }
            }
            long t2=System.currentTimeMillis();
            System.out.println(t2-t1);

            /*for (College c:College.cnNameCollegeHashMap.values()){
                String sql="insert into college (college_chinese_name, college_english_name) " +
                        "values ('"+c.getCollege_chinese_name()+"','"+
                        c.getCollege_english_name().substring(0,c.getCollege_english_name().length()-1)+"');";
                System.out.println(sql);
                Statement statement=con.createStatement();


                statement.execute(sql);
            }*/



            dm.closeDatasource();
        }
        catch (IllegalArgumentException | IOException|SQLException e) {
            System.err.println(e.getMessage());
        }
    }
    public static void test4(){
        try {
            DataManipulation dm = new ComparingDataManipulation();
            dm.openDatasource(); // 打开数据库
            BufferedReader reader = new BufferedReader(new FileReader(
                    "D:\\计算机\\数据库\\Project\\release of project 1\\data\\output1_4.json"));
            String line;
            long t1=System.currentTimeMillis();
            int result=0;
            String sql="insert into student (sid, name, gender, college_id) " +
                    "values (?,?,?,?);";
            PreparedStatement preparedStatement=con.prepareStatement(sql);
            con.setAutoCommit(false);
            int count=0;

            while ((line= reader.readLine())!=null){
                try{
                    Student student=new Student(line);

                    Long cid=College.cnNameCollegeHashMap.get(student.getCollege_chinese_name()).college_id;
                    if(cid==null){
                        continue;
                    }

                    preparedStatement.setString(1,student.getSid());
                    preparedStatement.setString(2,student.getName());
                    preparedStatement.setString(3,student.getGender());
                    preparedStatement.setLong(4,cid);
                    //System.out.println(preparedStatement.toString());
                    preparedStatement.addBatch();
                    count++;
                    if (count%20000==0){
                        preparedStatement.executeBatch();
                        con.commit();
                    }

                }catch (SQLException e){
                    e.printStackTrace();
                }
            }
            preparedStatement.executeBatch();
            con.commit();
            long t2=System.currentTimeMillis();
            System.out.println(t2-t1);

            /*for (College c:College.cnNameCollegeHashMap.values()){
                String sql="insert into college (college_chinese_name, college_english_name) " +
                        "values ('"+c.getCollege_chinese_name()+"','"+
                        c.getCollege_english_name().substring(0,c.getCollege_english_name().length()-1)+"');";
                System.out.println(sql);
                Statement statement=con.createStatement();


                statement.execute(sql);
            }*/



            dm.closeDatasource();
        }
        catch (IllegalArgumentException | IOException|SQLException e) {
            System.err.println(e.getMessage());
        }
    }
    public static void test5(){
        try {
            DataManipulation dm = new ComparingDataManipulation();
            dm.openDatasource(); // 打开数据库
            BufferedReader reader = new BufferedReader(new FileReader(
                    "D:\\计算机\\数据库\\Project\\release of project 1\\data\\output1_4.json"));
            String line;
            long t1=System.currentTimeMillis();
            int result=0;
            String sql="insert into student (sid, name, gender, college_id) " +
                    "values (?,?,?,?);";
            PreparedStatement preparedStatement=con.prepareStatement(sql);
            con.setAutoCommit(false);
            int count=0;

            HashMap<String,Long>collegeHash=getCollegeHashMap();

            while ((line= reader.readLine())!=null){
                try{
                    Student student=new Student(line);

                    Long cid=collegeHash.get(student.getCollege_chinese_name());
                    if(cid==null){
                        continue;
                    }

                    preparedStatement.setString(1,student.getSid());
                    preparedStatement.setString(2,student.getName());
                    preparedStatement.setString(3,student.getGender());
                    preparedStatement.setLong(4,cid);
                    //System.out.println(preparedStatement.toString());
                    preparedStatement.addBatch();
                    count++;
                    if (count%20000==0){
                        preparedStatement.executeBatch();
                        con.commit();
                    }

                }catch (SQLException e){
                    e.printStackTrace();
                }
            }
            preparedStatement.executeBatch();
            con.commit();
            long t2=System.currentTimeMillis();
            System.out.println(t2-t1);

            /*for (College c:College.cnNameCollegeHashMap.values()){
                String sql="insert into college (college_chinese_name, college_english_name) " +
                        "values ('"+c.getCollege_chinese_name()+"','"+
                        c.getCollege_english_name().substring(0,c.getCollege_english_name().length()-1)+"');";
                System.out.println(sql);
                Statement statement=con.createStatement();


                statement.execute(sql);
            }*/



            dm.closeDatasource();
        }
        catch (IllegalArgumentException | IOException|SQLException e) {
            System.err.println(e.getMessage());
        }
    }
    public static void test6(){
        try {
            DataManipulation dm = new ComparingDataManipulation();
            dm.openDatasource(); // 打开数据库
            BufferedReader reader = new BufferedReader(new FileReader(
                    "D:\\计算机\\数据库\\Project\\release of project 1\\data\\output1_4.json"));
            String line;
            long t1=System.currentTimeMillis();
            int result=0;
            String sql="insert into student (sid, name, gender, college_id) " +
                    "values (?,?,?,?),(?,?,?,?),(?,?,?,?),(?,?,?,?)";
            PreparedStatement preparedStatement=con.prepareStatement(sql);
            con.setAutoCommit(false);
            int count=0;

            HashMap<String,Long>collegeHash=getCollegeHashMap();

            int index=1;
            while ((line= reader.readLine())!=null){
                try{
                    Student student=new Student(line);

                    Long cid=collegeHash.get(student.getCollege_chinese_name());
                    if(cid==null){
                        continue;
                    }

                    preparedStatement.setString(index++,student.getSid());
                    preparedStatement.setString(index++,student.getName());
                    preparedStatement.setString(index++,student.getGender());
                    preparedStatement.setLong(index++,cid);
                    //System.out.println(preparedStatement.toString());
                    if(index>=16){
                        index=1;
                        preparedStatement.addBatch();
                    }
                    count++;
                    if (count%20000==0){
                        preparedStatement.executeBatch();
                        con.commit();
                    }

                }catch (SQLException e){
                    e.printStackTrace();
                }
            }
            preparedStatement.executeBatch();
            con.commit();
            long t2=System.currentTimeMillis();
            System.out.println(t2-t1);

            /*for (College c:College.cnNameCollegeHashMap.values()){
                String sql="insert into college (college_chinese_name, college_english_name) " +
                        "values ('"+c.getCollege_chinese_name()+"','"+
                        c.getCollege_english_name().substring(0,c.getCollege_english_name().length()-1)+"');";
                System.out.println(sql);
                Statement statement=con.createStatement();


                statement.execute(sql);
            }*/



            dm.closeDatasource();
        }
        catch (IllegalArgumentException | IOException|SQLException e) {
            System.err.println(e.getMessage());
        }
    }
    public static void test7(){
        try {
            DataManipulation dm = new ComparingDataManipulation();
            dm.openDatasource(); // 打开数据库
            BufferedReader reader = new BufferedReader(new FileReader(
                    "D:\\计算机\\数据库\\Project\\release of project 1\\data\\output1_4.json"));
            String line;
            long t1=System.currentTimeMillis();

            con.setAutoCommit(false);

            collegeHash=getCollegeHashMap();

            int count=0;
            while ((line= reader.readLine())!=null){
                allInput.add(line);
                count++;
            }
            int cpus=Runtime.getRuntime().availableProcessors();
            int start=0,end=count/cpus;
            Thread[]threads2=new Thread[cpus];
            MyPostgreSQLThread[]threads=new MyPostgreSQLThread[cpus];
            for (int i = 0; i < cpus; i++) {
                threads[i]=new MyPostgreSQLThread(start,end);
                start=end+1;
                if(i!=cpus-2){
                    end+=count/cpus;
                }else {
                    end=count-2;
                }
                threads2[i]=new Thread(threads[i]);
                threads2[i].start();
                threads2[i].join();
            }


            long t2=System.currentTimeMillis();
            System.out.println(t2-t1);

            /*for (College c:College.cnNameCollegeHashMap.values()){
                String sql="insert into college (college_chinese_name, college_english_name) " +
                        "values ('"+c.getCollege_chinese_name()+"','"+
                        c.getCollege_english_name().substring(0,c.getCollege_english_name().length()-1)+"');";
                System.out.println(sql);
                Statement statement=con.createStatement();


                statement.execute(sql);
            }*/


            dm.closeDatasource();
        }
        catch (IllegalArgumentException | IOException | SQLException | InterruptedException e) {
            System.err.println(e.getMessage());
        }
    }
    public static void test8(){
        try {
            DataManipulation dm = new ComparingDataManipulation();
            dm.openDatasource(); // 打开数据库
            BufferedReader reader = new BufferedReader(new FileReader(
                    "D:\\计算机\\数据库\\Project\\release of project 1\\data\\output1_4.json"));
            String line;
            long t1=System.currentTimeMillis();

            con.setAutoCommit(false);

            collegeHash=getCollegeHashMap();

            int count=0;
            while ((line= reader.readLine())!=null){
                allInput.add(line);
                count++;
            }
            int cpus=Runtime.getRuntime().availableProcessors()*1000;
            int start=0,end=count/cpus;
            Thread[]threads2=new Thread[cpus];
            MyPostgreSQLThread[]threads=new MyPostgreSQLThread[cpus];
            for (int i = 0; i < cpus; i++) {
                threads[i]=new MyPostgreSQLThread(start,end);
                start=end+1;
                if(i!=cpus-2){
                    end+=count/cpus;
                }else {
                    end=count-1;
                }
                threads2[i]=new Thread(threads[i]);
                threads2[i].start();
                threads2[i].join();
            }


            long t2=System.currentTimeMillis();
            System.out.println(t2-t1);

            /*for (College c:College.cnNameCollegeHashMap.values()){
                String sql="insert into college (college_chinese_name, college_english_name) " +
                        "values ('"+c.getCollege_chinese_name()+"','"+
                        c.getCollege_english_name().substring(0,c.getCollege_english_name().length()-1)+"');";
                System.out.println(sql);
                Statement statement=con.createStatement();


                statement.execute(sql);
            }*/


            dm.closeDatasource();
        }
        catch (IllegalArgumentException | IOException | SQLException | InterruptedException e) {
            System.err.println(e.getMessage());
        }
    }
    private static class MyPostgreSQLThread implements Runnable{
        int start;
        int end;
        public MyPostgreSQLThread(int s,int e){
            this.start=s;
            this.end=e;
        }

        @Override
        public void run() {
            try{
                String sql="insert into student (sid, name, gender, college_id) " +
                        "values (?,?,?,?)";
                PreparedStatement preparedStatement=con.prepareStatement(sql);
                int index=1;
                int count=0;
                for (int i=start;i<=end;i++){

                    String line= allInput.get(i);
                    Student student=new Student(line);

                    Long cid=collegeHash.get(student.getCollege_chinese_name());
                    if(cid==null){
                        continue;
                    }
                    preparedStatement.setString(index++,student.getSid());
                    preparedStatement.setString(index++,student.getName());
                    preparedStatement.setString(index++,student.getGender());
                    preparedStatement.setLong(index++,cid);

                    if(index>=4){
                        index=1;
                        preparedStatement.addBatch();
                    }
                    count++;
                    if (count%20000==0){
                        preparedStatement.executeBatch();
                        con.commit();
                    }
                }
                preparedStatement.executeBatch();
                con.commit();
            }
            catch (SQLException e){
                e.printStackTrace();
            }

        }
    }

    private static synchronized void myCommit(){
        try{
            con.commit();
        }catch (SQLException e){
            e.printStackTrace();;
        }
    }

    public static Long findCollegeIDByName(String name){
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

    public static HashMap<String,Long>getCollegeHashMap(){
        HashMap<String,Long>collegeHashMap=new HashMap<>();
        try{
            String sql="select * from college";
            PreparedStatement preparedStatement=con.prepareStatement(sql);
            resultSet= preparedStatement.executeQuery();
            while (resultSet.next()){
                collegeHashMap.put(resultSet.getString(2),resultSet.getLong(1));
            }
        }catch (SQLException e){

        }
        return collegeHashMap;
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
    public int addOneTeacher(String teacher) {
        return 0;
    }

    @Override
    public int addOneCourse(Course c) {
        return 0;
    }

    @Override
    public int addOneLocation(String location) {
        return 0;
    }

    @Override
    public int addOneCourseDept(String courseDept) {
        return 0;
    }

    @Override
    public int addOneCollege(College college) {

        return 0;
    }

    @Override
    public int addOneStudent(Student student) {
        return 0;
    }

    @Override
    public int addOneCourseSelection(Course_selection_info course_selection_info) {
        return 0;
    }

    @Override
    public String continentsWithCountryCount() {
        return null;
    }

    @Override
    public void addOneCoursePrerequisite(Course c, PrerequisiteTreeNode root) {

    }

}
