package reference.service;

import cn.edu.sustech.cs307.database.SQLDataSource;
import cn.edu.sustech.cs307.dto.*;
import cn.edu.sustech.cs307.dto.grade.Grade;
import cn.edu.sustech.cs307.dto.grade.HundredMarkGrade;
import cn.edu.sustech.cs307.dto.grade.PassOrFailGrade;
import cn.edu.sustech.cs307.exception.EntityNotFoundException;
import cn.edu.sustech.cs307.exception.IntegrityViolationException;
import cn.edu.sustech.cs307.service.StudentService;
import reference.util.SearchCourseUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import java.sql.*;
import java.sql.Date;
import java.time.DayOfWeek;
import java.util.*;

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
        try{
            return SearchCourseUtil.searchCourse(studentId, semesterId, searchCid, searchName, searchInstructor,
                    searchDayOfWeek, searchClassTime, searchClassLocations, searchCourseType,
                    ignoreFull, ignoreConflict, ignorePassed, ignoreMissingPrerequisites,
                    pageSize, pageIndex);
        }catch (Exception e){
            e.printStackTrace();
            throw new IntegrityViolationException();
        }
    }

    @Override
    public EnrollResult enrollCourse(int studentId, int sectionId) {
        try(Connection conn=SQLDataSource.getInstance().getSQLConnection()){
            /*if(!isCourseFound(studentId,sectionId)){
                return EnrollResult.COURSE_NOT_FOUND;
            }

            if(hasAlreadyEnrolled(studentId,sectionId)){
                return EnrollResult.ALREADY_ENROLLED;
            }

            if(hasAlreadyPassed(studentId,sectionId)){
                return EnrollResult.ALREADY_PASSED;
            }

            if(!hasPrerequisiteFulfilled(studentId,sectionId)){
                return EnrollResult.PREREQUISITES_NOT_FULFILLED;
            }*/

            EnrollResult er1=getInvalidResult(studentId,sectionId);
            if(er1!=null){
                return er1;
            }

            if(hasCourseConflictFound(studentId,sectionId)){
                return EnrollResult.COURSE_CONFLICT_FOUND;
            }

            if(isCourseFull(studentId,sectionId)){
                return EnrollResult.COURSE_IS_FULL;
            }
            if(studentId==11714884){
                System.out.println(11714884);
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

    public static EnrollResult getInvalidResult(int studentId,int sectionId){
        try(Connection conn=SQLDataSource.getInstance().getSQLConnection()) {
            String sql="(select 'COURSE_NOT_FOUND' where not exists(select null from coursesection where sectionid=?)) union all " +
                    "(select 'ALREADY_ENROLLED' " +
                    "where exists(select null from studentcourseselection where studentid=? and sectionid=?)) union all " +
                    "(select 'ALREADY_PASSED' where " +
                    "exists(" +
                    "with t as ((select c.courseid from coursesection as cs2 " +
                    "inner join student100course s100c on cs2.sectionid = s100c.sectionid " +
                    "inner join course c on c.courseid = cs2.courseid where s100c.studentid=? and s100c.grade>=60) union all " +
                    "(select c2.courseid from coursesection as cs3 " +
                    "inner join studentpfcourse s on cs3.sectionid = s.sectionid " +
                    "inner join course c2 on c2.courseid = cs3.courseid where s.studentid=? and s.grade='P') ) " +
                    "select * from t inner join course c on c.courseid=t.courseid " +
                    "inner join coursesection c3 on c.courseid = c3.courseid " +
                    "where c3.sectionid=?" +
                    ")) union all " +
                    "(select 'PREREQUISITES_NOT_FULFILLED' where not isprerequisitefullfilled(?,?))";
            PreparedStatement p= conn.prepareStatement(sql);

            p.setInt(1,sectionId);
            p.setInt(2,studentId);
            p.setInt(3,sectionId);
            p.setInt(4,studentId);
            p.setInt(5,studentId);
            p.setInt(6,sectionId);
            p.setInt(7,studentId);
            p.setInt(8,sectionId);
            //p.setInt(9,sectionId);

            ResultSet rs=p.executeQuery();
            if(rs.next()){
                return EnrollResult.valueOf(rs.getString(1));
            }else{
                return null;
            }



        }catch (SQLException e){
            return null;
        }
    }

    public static boolean isCourseFound(int studentId,int sectionId){
        try (Connection conn=SQLDataSource.getInstance().getSQLConnection()){
            String sql="select * from coursesection where sectionid=?";
            PreparedStatement p=conn.prepareStatement(sql);

            p.setInt(1,sectionId);

            ResultSet rs=p.executeQuery();
            return rs.next();
        }catch (SQLException e){
            return false;
        }
    }

    public static boolean hasAlreadyEnrolled(int studentId,int sectionId){
        try (Connection conn=SQLDataSource.getInstance().getSQLConnection()){
            String sql="select 'a' from studentcourseselection where studentid=? and sectionid=?";

            PreparedStatement p=conn.prepareStatement(sql);

            p.setInt(1,studentId);
            p.setInt(2,sectionId);

            ResultSet rs=p.executeQuery();

            if(rs.next()){
                //System.out.println("Here!");
                return true;
            }

            return false;

            //return rs.next();
        }catch (SQLException e){
            return false;
        }
    }

    public static boolean hasAlreadyPassed(int studentId,int sectionId){
        try (Connection conn=SQLDataSource.getInstance().getSQLConnection()){
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

            //System.out.println(p);

            ResultSet rs=p.executeQuery();

            return rs.next();
        }catch (SQLException e){
            return false;
        }
    }

    public static boolean hasPrerequisiteFulfilled(int studentId,int sectionId){
        try (Connection conn=SQLDataSource.getInstance().getSQLConnection()){
            String sql="select 'a' from students where isprerequisitefullfilled(?,?)";
            PreparedStatement p=conn.prepareStatement(sql);

            p.setInt(1,studentId);
            p.setInt(2,sectionId);
            //System.out.println(p);

            ResultSet rs=p.executeQuery();
            return rs.next();
        }catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    private static boolean hasCourseConflictFound(int studentId,int sectionId){
        try(Connection conn=SQLDataSource.getInstance().getSQLConnection()){
            /*String sql="select sameTimeCourse.sectionid from( " +
                    "select otherClasses.*from " +
                    "(select c.*,cs.semesterid " +
                    "from coursesection as cs " +
                    "inner join coursesectionclass c on cs.sectionid = c.sectionid " +
                    "where cs.sectionid=?) as currentCourseSection " +
                    "inner join coursesectionclass as otherClasses " +
                    "    on (otherClasses.classbegin<=currentCourseSection.classbegin " +
                    "and otherClasses.classend>=currentCourseSection.classend " +
                    "and otherClasses.dayofweek=currentCourseSection.dayofweek) " +
                    //"(otherClasses.classbegin<=currentCourseSection.classbegin " +
                    //"and otherClasses.classend>=currentCourseSection.classbegin " +
                    //"and otherClasses.dayofweek=currentCourseSection.dayofweek) or " +
                    //"(otherClasses.classbegin<=currentCourseSection.classend " +
                    //"and otherClasses.classend<=currentCourseSection.classend " +
                    //"and otherClasses.dayofweek=currentCourseSection.dayofweek) " +
                    "inner join coursesection as cs " +
                    "   on cs.sectionid=otherClasses.sectionid" +
                    "   and cs.semesterid=currentCourseSection.semesterid" +
                    ") as sameTimeCourse " +
                    "inner join (select cs1.studentid, sectionid from studentcourseselection as cs1 " +
                    "union all (select cs2.studentid, sectionid from student100course as cs2)" +
                    "union all (select cs3.studentid, sectionid from studentpfcourse as cs3)) as selection " +
                    "on selection.sectionid=sameTimeCourse.sectionid " +
                    "where selection.studentid=?";*/
            String sql="select sameTimeCourse.sectionid," +
                    "sameTimeCourse.coursename||'['||sameTimeCourse.sectionname||']' as allName from( " +
                    "select otherClasses.*,cs.sectionname,c2.coursename from " +
                    "(select c.*,cs.semesterid " +
                    "from coursesection as cs " +
                    "inner join coursesectionclass c on cs.sectionid = c.sectionid " +
                    "where cs.sectionid=?) as currentCourseSection " +
                    "inner join coursesectionclass as otherClasses " +
                    "    on ((otherClasses.classbegin<=currentCourseSection.classbegin " +
                    "and otherClasses.classend>=currentCourseSection.classend)or " +
                    "(otherClasses.classbegin>=currentCourseSection.classbegin " +
                    "and otherClasses.classend<=currentCourseSection.classend) or " +
                    "(otherClasses.classbegin>=currentCourseSection.classbegin and" +
                    "                   otherClasses.classbegin<=currentCourseSection.classend)" +
                    "                or(otherClasses.classend>=currentCourseSection.classbegin and" +
                    "                   otherClasses.classend<=currentCourseSection.classend)) " +
                    "and otherClasses.dayofweek=currentCourseSection.dayofweek " +
                    "and otherClasses.week=currentCourseSection.week " +
                    "inner join coursesection as cs " +
                    "   on cs.sectionid=otherClasses.sectionid" +
                    "   and cs.semesterid=currentCourseSection.semesterid " +
                    "inner join course c2 on cs.courseid = c2.courseid" +
                    ") as sameTimeCourse " +
                    "inner join (select cs1.studentid, sectionid from studentcourseselection as cs1 " +
                    "union all (select cs2.studentid, sectionid from student100course as cs2)" +
                    "union all (select cs3.studentid, sectionid from studentpfcourse as cs3)) as selection " +
                    "on selection.sectionid=sameTimeCourse.sectionid " +
                    "where selection.studentid=?";
            PreparedStatement p=conn.prepareStatement(sql);



            p.setInt(1,sectionId);
            p.setInt(2,studentId);
            //System.out.println(p);

            ResultSet rs=p.executeQuery();

            String sql2="with t as (select c.courseid,cs.semesterid from coursesection as cs " +
                    "inner join studentcourseselection s on cs.sectionid = s.sectionid " +
                    "inner join course c on c.courseid = cs.courseid " +
                    "where s.studentid=?) " +
                    "select * from t " +
                    "inner join course c on c.courseid=t.courseid " +
                    "inner join coursesection c2 on c.courseid = c2.courseid and c2.semesterid=t.semesterid " +
                    "where c2.sectionid=?";

            PreparedStatement p2=conn.prepareStatement(sql2);
            p2.setInt(1,studentId);
            p2.setInt(2,sectionId);

            ResultSet rs2=p2.executeQuery();

            return rs.next()||rs2.next();
        }catch (SQLException e){
            e.printStackTrace();
            throw new IntegrityViolationException();
        }
    }

    private static boolean isCourseFull(int studentId,int sectionId){

        try(Connection conn=SQLDataSource.getInstance().getSQLConnection()){
            String sql="select count(*),max(cs.totalcapacity) over(partition by cs.sectionid) from studentcourseselection as scs " +
                    "inner join coursesection cs on scs.sectionid = cs.sectionid where cs.sectionid=? group by cs.totalcapacity,cs.sectionid ";
            PreparedStatement p=conn.prepareStatement(sql);
            p.setInt(1,sectionId);
            //System.out.println(p);

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

            int affected=p.executeUpdate();

            if(affected<=0){
                throw new IllegalStateException();
            }
        }catch (SQLException e){
            throw new IntegrityViolationException();
        }
    }

    @Override
    public void addEnrolledCourseWithGrade(int studentId, int sectionId, @Nullable Grade grade) {
        try(Connection conn=SQLDataSource.getInstance().getSQLConnection()){

            boolean isPF= checkCourseGradeType(conn,sectionId,grade);

            if(grade!=null&&(isPF!=grade instanceof PassOrFailGrade)){
                throw new IntegrityViolationException();
            }

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
                p.setObject(3,null);
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
            boolean isPF=checkCourseGradeType(conn,sectionId,grade);

            if(isPF != grade instanceof PassOrFailGrade){
                throw new IntegrityViolationException();
            }

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

    @Override
    public CourseTable getCourseTable(int studentId, Date date) {
        try(Connection conn=SQLDataSource.getInstance().getSQLConnection()){
            String sql="select sm.semesterid as smid, (? - sm.begin_time)/7+1 as smweek," +
                    "c2.coursename, i.userid, getfullname(i.firstname,i.lastname), c.classbegin," +
                    "c.classend, c.location, c.dayofweek,cs.sectionname " +
                    "from semester as sm " +
                    "inner join coursesection as cs on cs.semesterid=sm.semesterid " +
                    "inner join coursesectionclass c on cs.sectionid = c.sectionid and c.week=(? - sm.begin_time)/7+1 " +
                    "inner join course c2 on c2.courseid = cs.courseid " +
                    "inner join instructor as i on i.userid=c.instructorid " +
                    "inner join " +
                    "((select sc1.sectionid from studentcourseselection as sc1 where sc1.studentid=?)" +
                    "   union all (select sc2.sectionid from studentpfcourse as sc2 where sc2.studentid=?)" +
                    "   union all (select sc3.sectionid from student100course as sc3 where sc3.studentid=?)) as scs" +
                    " on scs.sectionid=cs.sectionid " +
                    "where ? between sm.begin_time and sm.end_time;";
            PreparedStatement p=conn.prepareStatement(sql);

            p.setDate(1,date);
            p.setDate(2,date);
            p.setInt(3,studentId);
            p.setInt(4,studentId);
            p.setInt(5,studentId);
            p.setDate(6,date);

            ResultSet rs=p.executeQuery();
            CourseTable ct=new CourseTable();
            ct.table=new HashMap<>();

            for (DayOfWeek d:DayOfWeek.values()){
                ct.table.put(d,new HashSet<>());
            }

            while (rs.next()){
                //System.out.println(rs.getInt(2));
                CourseTable.CourseTableEntry entry=new CourseTable.CourseTableEntry();
                entry.courseFullName=String.format("%s[%s]",rs.getString(3),rs.getString(10));

                Instructor instructor=new Instructor();
                instructor.id=rs.getInt(4);
                instructor.fullName=rs.getString(5);
                entry.instructor=instructor;

                entry.classBegin=(short) rs.getInt(6);
                entry.classEnd=(short) rs.getInt(7);
                entry.location=rs.getString(8);

                DayOfWeek d=DayOfWeek.of(rs.getInt(9));

                ct.table.get(d).add(entry);
            }

            return ct;
        }catch (SQLException e){
            e.printStackTrace();
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
