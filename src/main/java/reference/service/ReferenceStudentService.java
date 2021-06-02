package reference.service;

import cn.edu.sustech.cs307.database.SQLDataSource;
import cn.edu.sustech.cs307.dto.*;
import cn.edu.sustech.cs307.dto.grade.Grade;
import cn.edu.sustech.cs307.dto.grade.HundredMarkGrade;
import cn.edu.sustech.cs307.dto.grade.PassOrFailGrade;
import cn.edu.sustech.cs307.exception.EntityNotFoundException;
import cn.edu.sustech.cs307.exception.IntegrityViolationException;
import cn.edu.sustech.cs307.service.StudentService;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import java.sql.*;
import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ParametersAreNonnullByDefault
public class ReferenceStudentService implements StudentService {
    @Override
    public void addStudent(int userId, int majorId, String firstName, String lastName, Date enrolledDate) {
        try(Connection conn= SQLDataSource.getInstance().getSQLConnection()){
            String sql="insert into students" +
                    "(userid, majorid, firstname, lastname, enrolleddate) " +
                    "values(?,?,?,?,?);";
            PreparedStatement p= conn.prepareStatement(sql);

            p.setInt(1,userId);
            p.setInt(2,majorId);
            p.setString(3,firstName);
            p.setString(4,lastName);
            p.setDate(5,enrolledDate);

            p.executeUpdate();

        }catch (SQLException e){
            throw new IntegrityViolationException();
        }
    }

    //TODO: Unfinished
    @Override
    public List<CourseSearchEntry> searchCourse
            (int studentId, int semesterId, @Nullable String searchCid, @Nullable String searchName,
             @Nullable String searchInstructor, @Nullable DayOfWeek searchDayOfWeek,
             @Nullable Short searchClassTime, @Nullable List<String> searchClassLocations,
             CourseType searchCourseType, boolean ignoreFull, boolean ignoreConflict,
             boolean ignorePassed, boolean ignoreMissingPrerequisites,
             int pageSize, int pageIndex) {
        return null;
    }

    @Override
    public EnrollResult enrollCourse(int studentId, int sectionId) {
        try(Connection conn=SQLDataSource.getInstance().getSQLConnection()){
            if(!isCourseFound(conn,studentId,sectionId)){
                return EnrollResult.COURSE_NOT_FOUND;
            }

            if(hasAlreadyEnrolled(conn,studentId,sectionId)){
                return EnrollResult.ALREADY_ENROLLED;
            }

            if(hasAlreadyPassed(conn,studentId,sectionId)){
                return EnrollResult.ALREADY_PASSED;
            }

            if(!hasPrerequisiteFulfilled(conn,studentId,sectionId)){
                return EnrollResult.PREREQUISITES_NOT_FULFILLED;
            }

            if(hasCourseConflictFound(conn,studentId,sectionId)){
                return EnrollResult.COURSE_CONFLICT_FOUND;
            }

            if(isCourseFull(conn,studentId,sectionId)){
                return EnrollResult.COURSE_IS_FULL;
            }

            String sql="insert into studentcourseselection(studentid, sectionid) values(?,?);";
            PreparedStatement p=conn.prepareStatement(sql);

            p.setInt(1,studentId);
            p.setInt(2,sectionId);

            p.executeUpdate();
            return EnrollResult.SUCCESS;
        }catch (SQLException e){
            e.printStackTrace();
            return EnrollResult.UNKNOWN_ERROR;
        }
    }

    private static boolean isCourseFound(Connection conn,int studentId,int sectionId){
        try {
            String sql="select * from coursesection where sectionid=?";
            PreparedStatement p=conn.prepareStatement(sql);

            p.setInt(1,sectionId);

            ResultSet rs=p.executeQuery();
            return rs.next();
        }catch (SQLException e){
            return false;
        }
    }

    private static boolean hasAlreadyEnrolled(Connection conn,int studentId,int sectionId){
        try {
            String sql="select 'a' from studentcourseselection where studentid=? and sectionid=?";
            PreparedStatement p=conn.prepareStatement(sql);

            p.setInt(1,studentId);
            p.setInt(2,sectionId);

            ResultSet rs=p.executeQuery();

            return rs.next();
        }catch (SQLException e){
            return false;
        }
    }

    private static boolean hasAlreadyPassed(Connection conn,int studentId,int sectionId){
        try {
            String sql="select * from " +
                    "((select c.courseid from student100course " +
                    "inner join coursesection c on c.sectionid = student100course.sectionid" +
                    " where studentid=?and grade>=60)" +
                    "union all " +
                    "(select c2.courseid from studentpfcourse " +
                    "inner join coursesection c2 on c2.sectionid = studentpfcourse.sectionid" +
                    " where studentid=? and grade='P')) as allPassed " +
                    "inner join coursesection cs on cs.courseid=allPassed.courseid and cs.sectionid=?";
            PreparedStatement p=conn.prepareStatement(sql);

            p.setInt(1,studentId);
            p.setInt(2,studentId);
            p.setInt(3,sectionId);

            System.out.println(p);

            ResultSet rs=p.executeQuery();

            return rs.next();
        }catch (SQLException e){
            return false;
        }
    }

    private static boolean hasPrerequisiteFulfilled(Connection conn,int studentId,int sectionId){
        try {
            String sql="select 'a' from students where isprerequisitefullfilled(?,?)";
            PreparedStatement p=conn.prepareStatement(sql);

            p.setInt(1,studentId);
            p.setInt(2,sectionId);
            System.out.println(p);

            ResultSet rs=p.executeQuery();
            return rs.next();
        }catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    private static boolean hasCourseConflictFound(Connection conn,int studentId,int sectionId){
        try{
            String sql="select sameTimeCourse.sectionid from( " +
                    "select otherClasses.* from " +
                    "(select c.* " +
                    "from coursesection as cs " +
                    "inner join coursesectionclass c on cs.sectionid = c.sectionid " +
                    "where cs.sectionid=?) as currentCourseSection " +
                    "inner join coursesectionclass as otherClasses " +
                    "    on otherClasses.classbegin<=currentCourseSection.classbegin " +
                    "and otherClasses.classend>=currentCourseSection.classend " +
                    "and otherClasses.dayofweek=currentCourseSection.dayofweek " +
                    "    ) as sameTimeCourse " +
                    "inner join studentcourseselection as selection " +
                    "on selection.sectionid=sameTimeCourse.sectionid " +
                    "where selection.studentid=?";
            PreparedStatement p=conn.prepareStatement(sql);

            p.setInt(1,sectionId);
            p.setInt(2,studentId);

            ResultSet rs=p.executeQuery();

            return rs.next();
        }catch (SQLException e){
            e.printStackTrace();
            throw new IntegrityViolationException();
        }
    }

    private static boolean isCourseFull(Connection conn,int studentId,int sectionId){

        try{
            String sql="select count(*),max(cs.totalcapacity) over(partition by cs.sectionid) from studentcourseselection as scs " +
                    "inner join coursesection cs on scs.sectionid = cs.sectionid where cs.sectionid=? group by cs.totalcapacity,cs.sectionid ";
            PreparedStatement p=conn.prepareStatement(sql);
            p.setInt(1,sectionId);
            System.out.println(p);

            ResultSet rs=p.executeQuery();

            if(rs.next()){
                return rs.getInt(1)>=rs.getInt(2);
            }else {
                return false;
            }
        }catch (SQLException e){
            e.printStackTrace();
            throw new IntegrityViolationException();
        }
    }

    @Override
    public void dropCourse(int studentId, int sectionId) throws IllegalStateException {
        try(Connection conn=SQLDataSource.getInstance().getSQLConnection()){
            String sql="delete from studentcourseselection where studentid=? and sectionid=?";
            PreparedStatement p=conn.prepareStatement(sql);

            p.setInt(1,studentId);
            p.setInt(2,sectionId);

            p.executeUpdate();
        }catch (SQLException e){
            throw new IntegrityViolationException();
        }
    }

    @Override
    public void addEnrolledCourseWithGrade(int studentId, int sectionId, @Nullable Grade grade) {
        try(Connection conn=SQLDataSource.getInstance().getSQLConnection()){

            boolean isPF= checkCourseGradeType(conn,sectionId,grade);

            String sql;
            if((!isPF && grade==null)||grade instanceof HundredMarkGrade){
                sql="insert into student100course(studentid, sectionid, grade) " +
                        "values(?,?,?);";
            }else if((isPF && grade==null)||grade instanceof PassOrFailGrade){
                sql="insert into studentpfcourse (studentid, sectionid, grade) " +
                        "values (?,?,?);";
            }else {
                throw new IntegrityViolationException();
            }
            PreparedStatement p= conn.prepareStatement(sql);

            p.setInt(1,studentId);
            p.setInt(2,sectionId);

            if(grade==null){
                p.setString(3,null);
            }else if(grade instanceof HundredMarkGrade){
                p.setInt(3,((HundredMarkGrade) grade).mark);
            }else {
                p.setString(3,grade==PassOrFailGrade.PASS?"P":"F");
            }

            p.executeUpdate();

        }catch (SQLException e){
            e.printStackTrace();
            throw new IntegrityViolationException();
        }
    }

    private boolean checkCourseGradeType(Connection conn,int sectionId,@Nullable Grade grade){
        try{

            String sql="select c.ispf from coursesection as cs " +
                    "inner join course c on c.courseid = cs.courseid " +
                    "where cs.sectionid=?";
            PreparedStatement p=conn.prepareStatement(sql);

            p.setInt(1,sectionId);

            ResultSet rs=p.executeQuery();

            if(!rs.next()){
                throw new IntegrityViolationException();
            }else{
                return rs.getBoolean(1);
            }

        }catch (SQLException e){
            throw new IntegrityViolationException();
        }

    }

    @Override
    public void setEnrolledCourseGrade(int studentId, int sectionId, Grade grade) {
        try(Connection conn=SQLDataSource.getInstance().getSQLConnection()){
            checkCourseGradeType(conn,sectionId,grade);

            String sql;
            if(grade instanceof HundredMarkGrade){
                sql="update student100course set grade=? where studentid=? and sectionid=?";
            }else if(grade instanceof PassOrFailGrade){
                sql="update studentpfcourse set grade=? where studentid=? and sectionid=?";
            }else {
                return;
            }
            PreparedStatement p=conn.prepareStatement(sql);

            if(grade instanceof HundredMarkGrade){
                p.setInt(1,((HundredMarkGrade)grade).mark);
            }else {
                p.setString(1,grade==PassOrFailGrade.PASS?"P":"F");
            }

            p.setInt(2,studentId);
            p.setInt(3,sectionId);

            p.executeUpdate();

        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    @Override
    public Map<Course, Grade> getEnrolledCoursesAndGrades(int studentId, @Nullable Integer semesterId) {

        try(Connection conn=SQLDataSource.getInstance().getSQLConnection()){
            Map<Course,Grade> resultMap=new HashMap<>();
            String sql;
            if(semesterId==null){
                sql="select c.courseid,c.coursename,c.credit,c.classhour,c.ispf,allCourse.grade from " +
                        "((select studentid, sectionid, grade::varchar from student100course as s100 where studentid=?) union all " +
                        "(select studentid, sectionid, grade::varchar from studentpfcourse as spf where studentid=?)) as allCourse" +
                        " inner join coursesection as cs on cs.sectionid=allCourse.sectionid" +
                        " inner join course c on c.courseid = cs.courseid;";
            }else {
                sql="select c.courseid,c.coursename,c.credit,c.classhour,c.ispf,allCourse.grade from " +
                        "((select studentid, sectionid, grade::varchar from student100course as s100 where studentid=?) union all " +
                        "(select studentid, sectionid, grade::varchar from studentpfcourse as spf where studentid=?)) as allCourse" +
                        " inner join coursesection as cs on cs.sectionid=allCourse.sectionid" +
                        " inner join course c on c.courseid = cs.courseid" +
                        " where cs.semesterid=?;";
            }

            PreparedStatement p=conn.prepareStatement(sql);

            p.setInt(1,studentId);
            p.setInt(2,studentId);

            if(semesterId!=null){
                p.setInt(3, semesterId);
            }

            ResultSet rs=p.executeQuery();

            while (rs.next()){
                Course c=new Course();
                c.id=rs.getString(1);
                c.name=rs.getString(2);
                c.credit=rs.getInt(3);
                c.classHour=rs.getInt(4);
                c.grading=rs.getBoolean(5)? Course.CourseGrading.PASS_OR_FAIL: Course.CourseGrading.HUNDRED_MARK_SCORE;

                String gr=rs.getString(6);

                Grade g;

                if(gr==null){
                    g=null;
                } else if(c.grading== Course.CourseGrading.PASS_OR_FAIL){
                    g=gr.equals("P")?PassOrFailGrade.PASS:PassOrFailGrade.FAIL;
                }else {
                    g=new HundredMarkGrade(Short.parseShort(gr));
                }

                resultMap.put(c,g);
            }
            if(resultMap.isEmpty()){
                throw new EntityNotFoundException();
            }else {
                return resultMap;
            }
        }catch (SQLException e){
            throw new IntegrityViolationException();
        }
    }

    //TODO: Unfinished
    @Override
    public CourseTable getCourseTable(int studentId, Date date) {
        try(Connection conn=SQLDataSource.getInstance().getSQLConnection()){
            return null;
        }catch (SQLException e){
            throw new IntegrityViolationException();
        }

    }

    @Override
    public boolean passedPrerequisitesForCourse(int studentId, String courseId) {
        try(Connection conn=SQLDataSource.getInstance().getSQLConnection()){
            String sql="select isprerequisitefullfilledbycourse(?,?);";
            PreparedStatement p=conn.prepareStatement(sql);

            p.setInt(1,studentId);
            p.setString(2,courseId);

            ResultSet rs=p.executeQuery();

            if(rs.next()){
                return rs.getBoolean(1);
            }else {
                throw new IntegrityViolationException();
            }
        }catch (SQLException e){
            throw new IntegrityViolationException();
        }
    }

    @Override
    public Major getStudentMajor(int studentId) {
        try (Connection conn=SQLDataSource.getInstance().getSQLConnection()){
            String sql="select d.departmentid,d.name, m.majorid, m.name from major m " +
                    "inner join students s on m.majorid = s.majorid " +
                    "inner join department d on d.departmentid = m.departmentid " +
                    "where s.userid=?";
            PreparedStatement p= conn.prepareStatement(sql);

            p.setInt(1,studentId);

            ResultSet rs=p.executeQuery();
            if(rs.next()){
                Department d=new Department();
                d.id=rs.getInt(1);
                d.name=rs.getString(2);

                Major m=new Major();
                m.id=rs.getInt(3);
                m.name=rs.getString(4);
                m.department=d;

                return m;
            }else {
                throw new EntityNotFoundException();
            }
        }catch (SQLException e){
            throw new IntegrityViolationException();
        }
    }
}
