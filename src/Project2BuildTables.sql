create table if not exists course(
    courseId varchar(50) primary key ,
    courseName varchar(50),
    credit integer,
    classHour integer,
    isPF bool,
    maxPrerequisiteGroupCount integer
);

/*alter table course drop coursePreId;
alter table course add column maxPrerequisiteGroupCount integer;*/

create table if not exists semester(
    name varchar(20),
    semesterId serial primary key,
    begin_time date,
    end_time date
);

create table if not exists courseSection (
    courseId varchar(50) references course(courseId) on delete cascade ,
    semesterId bigint references Semester(semesterId) on delete cascade ,
    sectionId serial primary key ,
    sectionName varchar(50),
    totalCapacity integer
);

create table if not exists instructor(
    userId integer primary key ,
    firstName varchar(30),
    lastName varchar(30)
);

create table if not exists courseSectionClass (
    sectionId bigint references courseSection(sectionId) on delete cascade,
    instructorId bigint references instructor(userId) on delete cascade,
    dayOfWeek  integer,
    week integer,
    classBegin integer,
    classEnd integer,
    location varchar(50),
    classId serial
);
create table if not exists department (
    name varchar(50),
    departmentId serial primary key
);

create table if not exists major (
    majorId serial primary key ,
    name varchar(50),
    departmentId bigint references department(departmentId) on delete cascade
);

create table if not exists majorCourse(
    majorId bigint references major(majorId),
    courseId varchar(50) references course(courseId),
    isCompulsory bool,
    constraint pk primary key (majorId, courseId)
);

create table if not exists students(
    userId integer primary key ,
    majorId bigint references major(majorid),
    firstName varchar(30),
    lastName varchar(30),
    enrolledDate date
);

create table if not exists studentCourseSelection (
    studentId integer references students(userId) on delete cascade ,
    sectionId bigint references courseSection(sectionId) on delete cascade
);

create table if not exists studentPfCourse(
    studentId integer references students(userId) on delete cascade,
    sectionId bigint references courseSection(sectionId) on delete cascade,
    grade varchar(10)
);

create table if not exists student100Course(
    studentId integer references students(userId) on delete cascade,
    sectionId bigint references courseSection(sectionId) on delete cascade ,
    grade integer
);

create table if not exists prerequisite
(
    id              serial primary key,
    course_id       varchar(50) not null,
    constraint fk1 foreign key (course_id) references course (courseId) on delete cascade ,
    prerequisiteCourseId varchar(50) not null,
    constraint fk2 foreign key (prerequisiteCourseId) references course (courseId) on delete cascade ,
    group_id        integer     not null
);
