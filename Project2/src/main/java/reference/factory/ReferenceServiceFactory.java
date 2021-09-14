package reference.factory;

import cn.edu.sustech.cs307.factory.ServiceFactory;
import cn.edu.sustech.cs307.service.*;
import reference.service.*;

public class ReferenceServiceFactory extends ServiceFactory {
    public ReferenceServiceFactory(){
        registerService(CourseService.class, new ReferenceCourseService());
        registerService(DepartmentService.class, new ReferenceDepartmentService());
        registerService(InstructorService.class, new ReferenceInstructorService());
        registerService(MajorService.class, new ReferenceMajorService());
        registerService(SemesterService.class, new ReferenceSemesterService());
        registerService(StudentService.class, new ReferenceStudentService());
        registerService(UserService.class, new ReferenceUserService());
    }
}
