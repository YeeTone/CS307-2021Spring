create table if not exists college
(
    college_id serial unique ,
    college_chinese_name varchar(20) not null unique,
    college_english_name varchar(50) not null unique,
    constraint fk primary key (college_english_name,college_chinese_name)
);

create table if not exists student
(
    SID        varchar(8) primary key,
    name       varchar(10) not null,
    gender     varchar(3) check ( gender in ('F', 'M') or gender is null ),
    college_id bigint
        constraint fk
            references college (college_id)
);
delete from student;
create unique index cn_index on college(college_chinese_name);
alter table student drop constraint fk;
alter table student add constraint fk foreign key (college_id) references college(college_id);

create table if not exists department
(
    department_id   serial primary key,
    department_name varchar(50) unique not null
);

create table if not exists course
(
    id               serial primary key,
    course_id        varchar(10) unique not null,
    department_id    bigint             not null,
    constraint fk foreign key (department_id) references department (department_id),
    course_credits   integer            not null,
    course_hour      integer            not null,
    max_prerequisite integer
);


create table if not exists teacher
(
    id serial,
    teacher_id   varchar(8) unique primary key not null,
    teacher_name varchar(30) not null
);
alter table if exists teacher add unique (teacher_name);


create table if not exists location
(
    location_id   serial primary key,
    location_name varchar(20) unique not null
);

create table if not exists classes
(
    class_name     varchar(20),
    courses_id     varchar(10),
    constraint pk primary key (class_name, courses_id),
    constraint fk1 foreign key (courses_id) references course (course_id),
    total_capacity integer    not null,
    teacher_id     varchar(8) not null,
    constraint fk2 foreign key (teacher_id) references teacher (teacher_id),
    location_id    integer    not null,
    constraint fk3 foreign key (location_id) references location (location_id)
    /*state          bigint     not null,
    constraint fk4 foreign key (state) references state (state_id)*/
);

create table if not exists prerequisite
(
    id              serial primary key,
    course_id       varchar(10) not null,
    constraint fk1 foreign key (course_id) references course (course_id),
    prerequisite_id varchar(10) not null,
    constraint fk2 foreign key (course_id) references course (course_id),
    constraint uq unique (course_id, prerequisite_id),
    group_id        integer     not null
);


create table if not exists course_selection
(
    selection_id serial primary key,
    course_id    varchar(10) not null,
    constraint fk1 foreign key (course_id) references course (course_id),
    student_id   varchar(8)  not null,
    constraint fk2 foreign key (student_id) references student (SID),
    constraint course_sid_uq unique (course_id, student_id)

);

with temp as
(select college_id as cid from
        college where college_chinese_name='')
insert into student(sid,name,gender,college_id)
values ('1000000','yeetone','M',temp.cid);

select * from student where SID='11000889';
create index sindex on student(SID);
update student set SID='11000889' where name='周慢精';
drop index sindex;

begin transaction;
delete from student where name >='王';
--where student_id='11000028';

rollback transaction ;

create extension extension_name;