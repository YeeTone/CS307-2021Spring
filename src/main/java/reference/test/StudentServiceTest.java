package reference.test;

import cn.edu.sustech.cs307.dto.Course;
import cn.edu.sustech.cs307.dto.grade.Grade;
import cn.edu.sustech.cs307.dto.grade.HundredMarkGrade;
import cn.edu.sustech.cs307.dto.prerequisite.AndPrerequisite;
import cn.edu.sustech.cs307.dto.prerequisite.CoursePrerequisite;
import cn.edu.sustech.cs307.dto.prerequisite.OrPrerequisite;
import cn.edu.sustech.cs307.service.*;
import reference.service.*;
import reference.util.DatabaseClearUtil;

import java.sql.Date;
import java.time.DayOfWeek;
import java.util.*;

public class StudentServiceTest {
    private static final CourseService COURSE_SERVICE =new ReferenceCourseService();
    private static final SemesterService SEMESTER_SERVICE =new ReferenceSemesterService();
    private static final InstructorService INSTRUCTOR_SERVICE=new ReferenceInstructorService();
    private static final StudentService STUDENT_SERVICE=new ReferenceStudentService();
    private static final MajorService MAJOR_SERVICE=new ReferenceMajorService();
    private static final DepartmentService DEPARTMENT_SERVICE=new ReferenceDepartmentService();

    static {
        DatabaseClearUtil.clearDatabase();
    }

    public static void main(String[] args) {
        getEnrolledCoursesAndGradesTest();
    }

    private static void addStudentTest(){

        int cse=DEPARTMENT_SERVICE.addDepartment("CSE");
        int cs=MAJOR_SERVICE.addMajor("CS",cse);
        STUDENT_SERVICE.addStudent(11910104,cs,"YeeTone","WANG", Date.valueOf("2019-08-15"));
        STUDENT_SERVICE.addStudent(11910103,cs,"Qinfu","QING",Date.valueOf("2019-08-14"));
    }

    private static void enrollCourseTest1(){

    }

    private static void enrollCourseTest2(){
        int cse=DEPARTMENT_SERVICE.addDepartment("CSE");
        int cs=MAJOR_SERVICE.addMajor("CS",cse);
        STUDENT_SERVICE.addStudent(11910104,cs,"YeeTone","WANG", Date.valueOf("2019-08-15"));

        COURSE_SERVICE.addCourse("CS102A","JavaA",3,64,
                Course.CourseGrading.HUNDRED_MARK_SCORE,null);
        COURSE_SERVICE.addCourse("CS102B","JavaB",3,64,
                Course.CourseGrading.HUNDRED_MARK_SCORE, null);
        COURSE_SERVICE.addCourse("CS203","DSAA",3,64,
                Course.CourseGrading.HUNDRED_MARK_SCORE,null);

        int fall2020=SEMESTER_SERVICE.addSemester("2020Fall",Date.valueOf("2020-09-01"),Date.valueOf("2021-02-01"));
        int spring2021=SEMESTER_SERVICE.addSemester("2021Spring",Date.valueOf("2021-03-01"),Date.valueOf("2021-07-01"));

        int cs102a1=COURSE_SERVICE.addCourseSection("CS102A",fall2020,"Lab1",45);
        int cs102a2=COURSE_SERVICE.addCourseSection("CS102B",spring2021,"Lab2",45);

        StudentService.EnrollResult enroll1 =STUDENT_SERVICE.enrollCourse(11910104,cs102a1);
        StudentService.EnrollResult enroll2 =STUDENT_SERVICE.enrollCourse(11910104,cs102a1);

        System.out.println(enroll1);
        System.out.println(enroll2);

    }

    private static void enrollCourseTest3(){
        int cse=DEPARTMENT_SERVICE.addDepartment("CSE");
        int cs=MAJOR_SERVICE.addMajor("CS",cse);
        STUDENT_SERVICE.addStudent(11910104,cs,"YeeTone","WANG", Date.valueOf("2019-08-15"));

        COURSE_SERVICE.addCourse("CS102A","JavaA",3,64,
                Course.CourseGrading.HUNDRED_MARK_SCORE,null);
        COURSE_SERVICE.addCourse("CS102B","JavaB",3,64,
                Course.CourseGrading.HUNDRED_MARK_SCORE, null);
        COURSE_SERVICE.addCourse("CS203","DSAA",3,64,
                Course.CourseGrading.HUNDRED_MARK_SCORE,null);

        int fall2020=SEMESTER_SERVICE.addSemester("2020Fall",Date.valueOf("2020-09-01"),Date.valueOf("2021-02-01"));
        int spring2021=SEMESTER_SERVICE.addSemester("2021Spring",Date.valueOf("2021-03-01"),Date.valueOf("2021-07-01"));

        int cs102a1=COURSE_SERVICE.addCourseSection("CS102A",fall2020,"Lab1",45);
        int cs102a2=COURSE_SERVICE.addCourseSection("CS102A",spring2021,"Lab2",45);

        STUDENT_SERVICE.addEnrolledCourseWithGrade(11910104,cs102a1,new HundredMarkGrade((short)(61)));
        StudentService.EnrollResult enroll=STUDENT_SERVICE.enrollCourse(11910104,cs102a2);

        System.out.println(enroll);

    }

    private static void enrollCourseTest4(){
        //Prerequisite fullfilled.

        int cse=DEPARTMENT_SERVICE.addDepartment("CSE");
        int cs=MAJOR_SERVICE.addMajor("CS",cse);
        STUDENT_SERVICE.addStudent(11910104,cs,"YeeTone","WANG", Date.valueOf("2019-08-15"));

        //(CS102A And CS102B) or CS203
        COURSE_SERVICE.addCourse("CS102A","JavaA",3,64,
                Course.CourseGrading.HUNDRED_MARK_SCORE,null);
        COURSE_SERVICE.addCourse("CS102B","JavaB",3,64,
                Course.CourseGrading.HUNDRED_MARK_SCORE, null);
        COURSE_SERVICE.addCourse("CS203","DSAA",3,64,
                Course.CourseGrading.HUNDRED_MARK_SCORE,null);

        int fall2020=SEMESTER_SERVICE.addSemester("2020Fall",Date.valueOf("2020-09-01"),Date.valueOf("2021-02-01"));
        int cs102a1=COURSE_SERVICE.addCourseSection("CS102A",fall2020,"Lab1",45);
        int cs102b2=COURSE_SERVICE.addCourseSection("CS102B",fall2020,"Lab2",45);
        int cs203h=COURSE_SERVICE.addCourseSection("CS203",fall2020,"LabH",50);

        STUDENT_SERVICE.addEnrolledCourseWithGrade(11910104,cs203h,new HundredMarkGrade((short) 99));
        //STUDENT_SERVICE.addEnrolledCourseWithGrade(11910104,cs102b2,new HundredMarkGrade((short) 98));

        CoursePrerequisite cs102a=new CoursePrerequisite("CS102A");
        CoursePrerequisite cs102b=new CoursePrerequisite("CS102B");
        AndPrerequisite and1=new AndPrerequisite(Arrays.asList(cs102a,cs102b));

        CoursePrerequisite cs203=new CoursePrerequisite("CS203");
        OrPrerequisite or2=new OrPrerequisite(Arrays.asList(and1,cs203));

        COURSE_SERVICE.addCourse("CS302","OS",3,64,
                Course.CourseGrading.HUNDRED_MARK_SCORE,or2);

        int cs302OS=COURSE_SERVICE.addCourseSection("CS302",fall2020,"Lab0",50);

        StudentService.EnrollResult enroll=STUDENT_SERVICE.enrollCourse(11910104,cs302OS);
        System.out.println(enroll);
    }

    private static void enrollCourseTest5(){
        int cse=DEPARTMENT_SERVICE.addDepartment("CSE");
        int cs=MAJOR_SERVICE.addMajor("CS",cse);
        STUDENT_SERVICE.addStudent(11910104,cs,"YeeTone","WANG", Date.valueOf("2019-08-15"));

        INSTRUCTOR_SERVICE.addInstructor(11911831,"Tong","ZHANG");

        //(CS102A And CS102B) or CS203
        COURSE_SERVICE.addCourse("CS102A","JavaA",3,64,
                Course.CourseGrading.HUNDRED_MARK_SCORE,null);
        COURSE_SERVICE.addCourse("CS102B","JavaB",3,64,
                Course.CourseGrading.HUNDRED_MARK_SCORE, null);
        COURSE_SERVICE.addCourse("CS203","DSAA",3,64,
                Course.CourseGrading.HUNDRED_MARK_SCORE,null);

        int fall2020=SEMESTER_SERVICE.addSemester("2020Fall",Date.valueOf("2020-09-01"),Date.valueOf("2021-02-01"));

        int cs102aLab1=COURSE_SERVICE.addCourseSection("CS102A",fall2020,"Lab1",45);
        int dsaaLab2=COURSE_SERVICE.addCourseSection("CS203",fall2020,"Lab2",45);

        //List<Short>fullWeek=Arrays.asList(new Short[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15});
        Set<Short>fullWeek=new HashSet<>(Arrays.asList(new Short[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15}));
        int javaLesson1=COURSE_SERVICE.addCourseSectionClass(cs102aLab1,11911831, DayOfWeek.MONDAY,fullWeek,(short) 1,(short) 2,"Lychee");
        int javaLesson2=COURSE_SERVICE.addCourseSectionClass(cs102aLab1,11911831,DayOfWeek.TUESDAY,fullWeek,(short) 3,(short) 4,"Lychee");

        StudentService.EnrollResult enroll1= STUDENT_SERVICE.enrollCourse(11910104,cs102aLab1);

        System.out.println(enroll1);

        //List<Short> singleWeek=Arrays.asList(new Short[]{1,3,5,7,9,11,13,15});
        Set<Short> singleWeek=new HashSet<>(Arrays.asList(new Short[]{1,3,5,7,9,11,13,15}));
        int dsaaLesson1=COURSE_SERVICE.addCourseSectionClass(dsaaLab2,11911831,DayOfWeek.TUESDAY,singleWeek,(short)3,(short) 4,"Lychee");
        int dsaaLesson2=COURSE_SERVICE.addCourseSectionClass(dsaaLab2,11911831,DayOfWeek.FRIDAY,singleWeek,(short)3,(short) 4,"Lychee");

        StudentService.EnrollResult enroll2=STUDENT_SERVICE.enrollCourse(11910104,dsaaLab2);

        System.out.println(enroll2);



    }

    private static void enrollCourseTest6(){
        int cse=DEPARTMENT_SERVICE.addDepartment("CSE");
        int cs=MAJOR_SERVICE.addMajor("CS",cse);
        STUDENT_SERVICE.addStudent(11910104,cs,"YeeTone","WANG", Date.valueOf("2019-08-15"));
        STUDENT_SERVICE.addStudent(11911831,cs,"Tong","ZHANG", Date.valueOf("2019-08-15"));

        COURSE_SERVICE.addCourse("CS102A","JavaA_only1",3,64,
                Course.CourseGrading.HUNDRED_MARK_SCORE,null);

        int fall2020=SEMESTER_SERVICE.addSemester("2020Fall",Date.valueOf("2020-09-01"),Date.valueOf("2021-02-01"));

        int cs102a1=COURSE_SERVICE.addCourseSection("CS102A",fall2020,"Lab1",1);

        StudentService.EnrollResult enroll1= STUDENT_SERVICE.enrollCourse(11910104,cs102a1);
        StudentService.EnrollResult enroll2=STUDENT_SERVICE.enrollCourse(11911831,cs102a1);

        System.out.println(enroll1);
        System.out.println(enroll2);
    }

    private static void dropCourseTest(){
        int cse=DEPARTMENT_SERVICE.addDepartment("CSE");
        int cs=MAJOR_SERVICE.addMajor("CS",cse);
        STUDENT_SERVICE.addStudent(11910104,cs,"YeeTone","WANG", Date.valueOf("2019-08-15"));
        STUDENT_SERVICE.addStudent(11911831,cs,"Tong","ZHANG", Date.valueOf("2019-08-15"));

        COURSE_SERVICE.addCourse("CS102A","JavaA",3,64,
                Course.CourseGrading.HUNDRED_MARK_SCORE,null);
        COURSE_SERVICE.addCourse("CS101A","IntroductionA",3,64,
                Course.CourseGrading.HUNDRED_MARK_SCORE,null);

        int fall2020=SEMESTER_SERVICE.addSemester("2020Fall",Date.valueOf("2020-09-01"),Date.valueOf("2021-02-01"));
        int spring2021=SEMESTER_SERVICE.addSemester("2021Spring",Date.valueOf("2021-03-01"),Date.valueOf("2021-07-01"));

        int cs102a1=COURSE_SERVICE.addCourseSection("CS102A",fall2020,"Lab1",40);
        int cs101a=COURSE_SERVICE.addCourseSection("CS101A",fall2020,"Lecture",160);

        INSTRUCTOR_SERVICE.addInstructor(11712310,"Ziqin","WANG");
        INSTRUCTOR_SERVICE.addInstructor(11712019,"Tiancheng","YU");

        Set<Short>fullWeek=new HashSet<>(Arrays.asList(new Short[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15}));
        COURSE_SERVICE.addCourseSectionClass(cs102a1,11712310,DayOfWeek.MONDAY,fullWeek,(short) 3,(short) 4,
                "Lychee1");
        COURSE_SERVICE.addCourseSectionClass(cs102a1,11712019,DayOfWeek.TUESDAY,fullWeek,(short) 3,(short) 4,
                "Lychee6");

        Set<Short>singleWeek=new HashSet<>(Arrays.asList(new Short[]{1,3,5,7,9,11,13,15}));
        COURSE_SERVICE.addCourseSectionClass(cs101a,11712019,DayOfWeek.MONDAY,singleWeek,(short)3,(short) 4,
                "Building1");

        StudentService.EnrollResult enroll1=STUDENT_SERVICE.enrollCourse(11910104,cs102a1);
        StudentService.EnrollResult enroll2=STUDENT_SERVICE.enrollCourse(11910104,cs101a);
        System.out.println(enroll1);
        System.out.println(enroll2);

        STUDENT_SERVICE.dropCourse(11910104,cs102a1);
        StudentService.EnrollResult enroll3=STUDENT_SERVICE.enrollCourse(11910104,cs101a);

        System.out.println(enroll3);


    }

    private static void addEnrolledCourseWithGradeTest(){
        int cse=DEPARTMENT_SERVICE.addDepartment("CSE");
        int cs=MAJOR_SERVICE.addMajor("CS",cse);

        COURSE_SERVICE.addCourse("CS102A","JavaA",3,64, Course.CourseGrading.HUNDRED_MARK_SCORE,null);

        int fall2020=SEMESTER_SERVICE.addSemester("2020Fall",Date.valueOf("2020-09-01"),Date.valueOf("2021-02-01"));

        INSTRUCTOR_SERVICE.addInstructor(11910104,"YeeTone","WANG");
        int cs102aLab1=COURSE_SERVICE.addCourseSection("CS102A",fall2020,"Lab1",50);

        STUDENT_SERVICE.addStudent(11911831,cs,"Tong","ZHANG",Date.valueOf("2019-08-15"));

        STUDENT_SERVICE.addEnrolledCourseWithGrade(11911831,cs102aLab1, null);
        STUDENT_SERVICE.addEnrolledCourseWithGrade(11911831,cs102aLab1,new HundredMarkGrade((short) 99));
    }

    private static void setEnrolledCourseGradeTest(){
        int cse=DEPARTMENT_SERVICE.addDepartment("CSE");
        int cs=MAJOR_SERVICE.addMajor("CS",cse);

        COURSE_SERVICE.addCourse("CS102A","JavaA",3,64, Course.CourseGrading.HUNDRED_MARK_SCORE,null);

        int fall2020=SEMESTER_SERVICE.addSemester("2020Fall",Date.valueOf("2020-09-01"),Date.valueOf("2021-02-01"));

        INSTRUCTOR_SERVICE.addInstructor(11910104,"YeeTone","WANG");
        int cs102aLab1=COURSE_SERVICE.addCourseSection("CS102A",fall2020,"Lab1",50);

        STUDENT_SERVICE.addStudent(11911831,cs,"Tong","ZHANG",Date.valueOf("2019-08-15"));

        STUDENT_SERVICE.addEnrolledCourseWithGrade(11911831,cs102aLab1,null);
        //STUDENT_SERVICE.setEnrolledCourseGrade(11911831,cs102aLab1,new HundredMarkGrade((short) 99));
    }

    private static void getEnrolledCoursesAndGradesTest(){
        int cse=DEPARTMENT_SERVICE.addDepartment("CSE");
        int cs=MAJOR_SERVICE.addMajor("CS",cse);

        COURSE_SERVICE.addCourse("CS102A","JavaA",3,64, Course.CourseGrading.HUNDRED_MARK_SCORE,null);
        COURSE_SERVICE.addCourse("CS203","DSAA",3,64, Course.CourseGrading.HUNDRED_MARK_SCORE,null);

        int fall2020=SEMESTER_SERVICE.addSemester("2020Fall",Date.valueOf("2020-09-01"),Date.valueOf("2021-02-01"));
        int spring2021=SEMESTER_SERVICE.addSemester("2021Spring",Date.valueOf("2021-03-01"),Date.valueOf("2021-07-01"));

        INSTRUCTOR_SERVICE.addInstructor(11712310,"Ziqin","WANG");

        int javaAFall=COURSE_SERVICE.addCourseSection("CS102A",fall2020,"JavaA-Fall",50);
        int dsaaSpring=COURSE_SERVICE.addCourseSection("CS203",spring2021,"DSAA-Spring",50);

        Set<Short>fullWeek=new HashSet<>(Arrays.asList(new Short[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15}));
        int ziqinFall=COURSE_SERVICE.addCourseSectionClass(javaAFall,11712310,DayOfWeek.MONDAY,fullWeek,(short) 3,(short) 4,"Lychee");
        int ziqinSpring=COURSE_SERVICE.addCourseSectionClass(dsaaSpring,11712310,DayOfWeek.MONDAY,fullWeek,(short) 3,(short) 4,"Lychee");

        STUDENT_SERVICE.addStudent(11910104,cs,"YeeTone","WANG",Date.valueOf("2019-08-15"));

        STUDENT_SERVICE.addEnrolledCourseWithGrade(11910104,javaAFall,new HundredMarkGrade((short) 99));
        STUDENT_SERVICE.addEnrolledCourseWithGrade(11910104,dsaaSpring,new HundredMarkGrade((short) 97));

        Map<Course,Grade> enrolledMapFall2020 =STUDENT_SERVICE.getEnrolledCoursesAndGrades(11910104,fall2020);
        Map<Course,Grade> answer =new HashMap<>();

        Course javaA=new Course();
        javaA.id="CS102A";
        javaA.name="JavaA";
        javaA.credit=3;
        javaA.classHour=64;
        javaA.grading= Course.CourseGrading.HUNDRED_MARK_SCORE;

        Grade javaAGrade =new HundredMarkGrade((short) 99);

        answer.put(javaA, javaAGrade);

        System.out.println(enrolledMapFall2020.equals(answer));

        Course dsaa=new Course();
        dsaa.id="CS203";
        dsaa.name="DSAA";
        dsaa.credit=3;
        dsaa.classHour=64;
        dsaa.grading= Course.CourseGrading.HUNDRED_MARK_SCORE;

        Grade dsaaGrade=new HundredMarkGrade((short) 97);
        answer.put(dsaa,dsaaGrade);

        Map<Course,Grade>enrolledMapAll=STUDENT_SERVICE.getEnrolledCoursesAndGrades(11910104,null);
        System.out.println(answer.equals(enrolledMapAll));

    }
}
