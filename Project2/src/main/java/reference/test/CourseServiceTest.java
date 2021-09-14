package reference.test;

import cn.edu.sustech.cs307.dto.*;
import cn.edu.sustech.cs307.dto.prerequisite.AndPrerequisite;
import cn.edu.sustech.cs307.dto.prerequisite.CoursePrerequisite;
import cn.edu.sustech.cs307.dto.prerequisite.OrPrerequisite;
import cn.edu.sustech.cs307.service.*;
import reference.service.*;
import reference.util.DatabaseClearUtil;

import java.sql.Date;
import java.time.DayOfWeek;
import java.util.*;

public class CourseServiceTest {

    private static final CourseService COURSE_SERVICE =new ReferenceCourseService();
    private static final SemesterService SEMESTER_SERVICE =new ReferenceSemesterService();
    private static final InstructorService INSTRUCTOR_SERVICE=new ReferenceInstructorService();
    private static final DepartmentService DEPARTMENT_SERVICE=new ReferenceDepartmentService();
    private static final MajorService MAJOR_SERVICE=new ReferenceMajorService();
    private static final StudentService STUDENT_SERVICE=new ReferenceStudentService();

    static {
        DatabaseClearUtil.clearDatabase();
    }

    public static void main(String[] args){
        getEnrolledStudentsInSemesterTest();
    }

    private static void addCourseTest(){

        COURSE_SERVICE.addCourse("CS102A","JavaA",3,64,
                Course.CourseGrading.HUNDRED_MARK_SCORE,null);

        COURSE_SERVICE.addCourse("CS202","Organization",3,64,
                Course.CourseGrading.HUNDRED_MARK_SCORE,null);

        COURSE_SERVICE.addCourse("CS203","DSAA-A",3,64,
                Course.CourseGrading.HUNDRED_MARK_SCORE,null);

        COURSE_SERVICE.addCourse("CS102B","JavaB",3,64,
                Course.CourseGrading.HUNDRED_MARK_SCORE,null);

        CoursePrerequisite cs202=new CoursePrerequisite("CS202");
        CoursePrerequisite cs102b=new CoursePrerequisite("CS102B");
        CoursePrerequisite cs102a=new CoursePrerequisite("CS102A");
        CoursePrerequisite dsaa=new CoursePrerequisite("CS203");
        //CoursePrerequisite cs101a=new CoursePrerequisite("CS101A");

        OrPrerequisite or1=new OrPrerequisite(Arrays.asList(cs102a,cs102b));
        AndPrerequisite and2=new AndPrerequisite(Arrays.asList(or1,dsaa));
        OrPrerequisite or3=new OrPrerequisite(Arrays.asList(and2,cs202));

        COURSE_SERVICE.addCourse("CS401","Robot",3,64,
                Course.CourseGrading.HUNDRED_MARK_SCORE,or3);
    }

    private static int addCourseSectionTest(){

        COURSE_SERVICE.addCourse("CS102A","JavaA",3,64,
                Course.CourseGrading.HUNDRED_MARK_SCORE,null);

        int fall2020= SEMESTER_SERVICE.addSemester("2020Fall", Date.valueOf("2020-09-01"),Date.valueOf("2021-02-01"));
        System.out.println("fall2020 = " + fall2020);

        return COURSE_SERVICE.addCourseSection("CS102A",fall2020,"Lecture",175);
    }

    private static int addCourseSectionClassTest(){
        INSTRUCTOR_SERVICE.addInstructor(11910104,"YeeTone","WANG");

        COURSE_SERVICE.addCourse("CS102A","JavaA",3
                ,64, Course.CourseGrading.HUNDRED_MARK_SCORE,null);

        int fall2020=SEMESTER_SERVICE.addSemester("2020Fall", Date.valueOf("2020-09-01"),Date.valueOf("2021-02-01"));

        int cs102aLecture=COURSE_SERVICE.addCourseSection("CS102A",fall2020,"Lecture",175);
        int cs102aLab=COURSE_SERVICE.addCourseSection("CS102A",fall2020,"Lab",45);

        Short[]week=new Short[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
        Set<Short> weekSet=new HashSet<>(Arrays.asList(week));

        int classId=COURSE_SERVICE.addCourseSectionClass(cs102aLecture,11910104,
                DayOfWeek.MONDAY,weekSet,(short) 3,(short) 4,"217Room");

        COURSE_SERVICE.addCourseSectionClass(cs102aLab,11910104,DayOfWeek.THURSDAY,weekSet,(short) 5,(short) 6,
                "217Room");

        return classId;
    }

    private static void removeCourseTest() throws InterruptedException {
        addCourseTest();
        Thread.sleep(30000);
        COURSE_SERVICE.removeCourse("CS102A");
    }

    private static void removeCourseSectionTest() throws InterruptedException {

        int addedSection =addCourseSectionTest();
        Thread.sleep(30000);

        COURSE_SERVICE.removeCourseSection(addedSection);

    }

    private static void removeCourseSectionClassTest()throws InterruptedException{
        int classId=addCourseSectionClassTest();
        Thread.sleep(30000);
        COURSE_SERVICE.removeCourseSectionClass(classId);
    }

    private static void getAllCourseTest(){
        COURSE_SERVICE.addCourse("CS205","C/C++",3,64,
                Course.CourseGrading.HUNDRED_MARK_SCORE,null);

        COURSE_SERVICE.addCourse("CS102A","JavaA",3,64,
                Course.CourseGrading.HUNDRED_MARK_SCORE,null);

        COURSE_SERVICE.addCourse("CS201","Discrete Math",3,48,
                Course.CourseGrading.HUNDRED_MARK_SCORE,null);

        List<Course> answer=new ArrayList<>();
        Course cs102a=new Course();
        cs102a.name="JavaA";
        cs102a.id="CS102A";
        cs102a.credit=3;
        cs102a.classHour=64;
        cs102a.grading= Course.CourseGrading.HUNDRED_MARK_SCORE;

        Course cs201=new Course();
        cs201.name="Discrete Math";
        cs201.id="CS201";
        cs201.credit=3;
        cs201.classHour=48;
        cs201.grading= Course.CourseGrading.HUNDRED_MARK_SCORE;

        Course cs205=new Course();
        cs205.name="C/C++";
        cs205.id="CS205";
        cs205.credit=3;
        cs205.classHour=64;
        cs205.grading= Course.CourseGrading.HUNDRED_MARK_SCORE;

        answer.add(cs205);
        answer.add(cs102a);
        answer.add(cs201);

        System.out.println(answer.equals(COURSE_SERVICE.getAllCourses()));
    }

    private static void getCourseSectionsInSemesterTest(){

        int spring2020=SEMESTER_SERVICE.addSemester("2020Spring",Date.valueOf("2020-03-01"),Date.valueOf("2020-07-01"));
        int fall2020=SEMESTER_SERVICE.addSemester("2020Fall",Date.valueOf("2020-09-01"),Date.valueOf("2021-02-01"));

        COURSE_SERVICE.addCourse("CS102A","JavaA",3,64, Course.CourseGrading.HUNDRED_MARK_SCORE,null);
        int spring2020Lecture=COURSE_SERVICE.addCourseSection("CS102A",spring2020,"Lecture",160);
        int fall2020Lecture=COURSE_SERVICE.addCourseSection("CS102A",fall2020,"Lecture",175);
        int spring2020Lab1=COURSE_SERVICE.addCourseSection("CS102A",spring2020,"Lab1",45);
        int fall2020Lab2=COURSE_SERVICE.addCourseSection("CS102A",fall2020,"Lab2",45);

        List<CourseSection> answer=new ArrayList<>();

        CourseSection cs102a_20Spring_lecture =new CourseSection();
        cs102a_20Spring_lecture.totalCapacity=160;
        cs102a_20Spring_lecture.name="Lecture";
        cs102a_20Spring_lecture.id=spring2020Lecture;
        cs102a_20Spring_lecture.leftCapacity=160;

        CourseSection cs102a_20Spring_lab =new CourseSection();
        cs102a_20Spring_lab.totalCapacity=45;
        cs102a_20Spring_lab.name="Lab1";
        cs102a_20Spring_lab.id=spring2020Lab1;
        cs102a_20Spring_lab.leftCapacity=45;

        answer.add(cs102a_20Spring_lecture);
        answer.add(cs102a_20Spring_lab);

        List<CourseSection> result=COURSE_SERVICE.getCourseSectionsInSemester("CS102A",spring2020);

        System.out.println(answer.equals(result));

    }

    private static void getCourseBySectionTest(){
        COURSE_SERVICE.addCourse("CS102A","JavaA",3,64,
                Course.CourseGrading.HUNDRED_MARK_SCORE,null);

        CoursePrerequisite cp=new CoursePrerequisite("CS102A");

        COURSE_SERVICE.addCourse("CS203","DSAA",3,64,
                Course.CourseGrading.HUNDRED_MARK_SCORE,cp);
        int fall2020=SEMESTER_SERVICE.addSemester("2020Fall",Date.valueOf("2020-09-01"),Date.valueOf("2021-02-01"));

        int dsaaLab1=COURSE_SERVICE.addCourseSection("CS203",fall2020,"DSAA Lab1",50);

        Course dsaa=new Course();
        dsaa.credit=3;
        dsaa.id="CS203";
        dsaa.classHour=64;
        dsaa.name="DSAA";
        dsaa.grading= Course.CourseGrading.HUNDRED_MARK_SCORE;

        System.out.println(dsaa.equals(COURSE_SERVICE.getCourseBySection(dsaaLab1)));
    }

    private static void getCourseSectionClassesTest(){
        COURSE_SERVICE.addCourse("CS102A","JavaA",3,
                64, Course.CourseGrading.HUNDRED_MARK_SCORE,null);
        int fall2020=SEMESTER_SERVICE.addSemester("2020Fall",Date.valueOf("2020-09-01"),Date.valueOf("2021-02-01"));

        int fall2020Lecture=COURSE_SERVICE.addCourseSection("CS102A",fall2020,"Lecture",160);
        INSTRUCTOR_SERVICE.addInstructor(11910104,"YeeTone","WANG");

        //List<Short>singleWeek=Arrays.asList(new Short[]{1,3,5,7,9,11,13,15});
        Set<Short> singleWeek=new HashSet<>(Arrays.asList(new Short[]{1,3,5,7,9,11,13,15}));
        int sectionClassId1=COURSE_SERVICE.addCourseSectionClass(fall2020Lecture,11910104,
                DayOfWeek.MONDAY,singleWeek,(short) 3,(short) 4,"217Room");

        //List<Short>fullWeek=Arrays.asList(new Short[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16});
        Set<Short>fullWeek=new HashSet<>(Arrays.asList(new Short[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16}));

        int sectionClassId2=COURSE_SERVICE.addCourseSectionClass(fall2020Lecture,11910104,
                DayOfWeek.TUESDAY,fullWeek,(short) 5,(short) 6,"218Room");

        List<CourseSectionClass> answer=new ArrayList<>();
        Instructor yeetone=new Instructor();
        yeetone.id=11910104;
        yeetone.fullName="YeeTone WANG";

        CourseSectionClass c1=new CourseSectionClass();
        c1.weekList=singleWeek;
        c1.id=sectionClassId1;
        c1.location="217Room";
        c1.dayOfWeek=DayOfWeek.MONDAY;
        c1.classBegin=3;
        c1.classEnd=4;
        c1.instructor=yeetone;
        answer.add(c1);

        CourseSectionClass c2=new CourseSectionClass();
        c2.weekList=fullWeek;
        c2.id=sectionClassId2;
        c2.location="218Room";
        c2.dayOfWeek=DayOfWeek.TUESDAY;
        c2.classBegin=5;
        c2.classEnd=6;
        c2.instructor=yeetone;
        answer.add(c2);

        List<CourseSectionClass> result=COURSE_SERVICE.getCourseSectionClasses(fall2020Lecture);

        System.out.println(answer.equals(result));

    }

    private static void getEnrolledStudentsInSemesterTest(){
        int cse=DEPARTMENT_SERVICE.addDepartment("CSE");
        int cs=MAJOR_SERVICE.addMajor("CS",cse);

        int math=DEPARTMENT_SERVICE.addDepartment("MATH");
        int ma=MAJOR_SERVICE.addMajor("MA",math);

        STUDENT_SERVICE.addStudent(11910104,cs,"YeeTone","WANG",Date.valueOf("2019-08-15"));
        STUDENT_SERVICE.addStudent(11610100,ma,"王","融",Date.valueOf("2016-08-15"));

        COURSE_SERVICE.addCourse("CS203","DSAA",3,64,
                Course.CourseGrading.HUNDRED_MARK_SCORE,null);
        int fall2020=SEMESTER_SERVICE.addSemester("2020Fall",Date.valueOf("2020-08-15"),Date.valueOf("2021-01-15"));

        int dsaaLab1=COURSE_SERVICE.addCourseSection("CS203",fall2020,"Lab1",50);
        StudentService.EnrollResult enroll1= STUDENT_SERVICE.enrollCourse(11910104,dsaaLab1);
        StudentService.EnrollResult enroll2=STUDENT_SERVICE.enrollCourse(11610100,dsaaLab1);

        System.out.println(enroll1);
        System.out.println(enroll2);

        Department csDepartment=new Department();
        csDepartment.name="CSE";
        csDepartment.id=cse;
        Major csMajor=new Major();
        csMajor.name="CS";
        csMajor.id =cs;
        csMajor.department=csDepartment;

        Department mathDepartment=new Department();
        mathDepartment.name="MATH";
        mathDepartment.id=math;
        Major maMajor=new Major();
        maMajor.name="MA";
        maMajor.id=ma;
        maMajor.department=mathDepartment;

        Student s1=new Student();
        s1.major=csMajor;
        s1.fullName="YeeTone WANG";
        s1.id=11910104;
        s1.enrolledDate=Date.valueOf("2019-08-15");

        Student s2=new Student();
        s2.major=maMajor;
        s2.fullName="王融";
        s2.id=11610100;
        s2.enrolledDate=Date.valueOf("2016-08-15");

        List<Student> answer=new ArrayList<>();
        answer.add(s1);
        answer.add(s2);

        List<Student> result=COURSE_SERVICE.getEnrolledStudentsInSemester("CS203",fall2020);

        System.out.println(answer.equals(result));
    }


}
