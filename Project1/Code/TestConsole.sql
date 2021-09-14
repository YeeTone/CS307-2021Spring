select * from course where course_name like '线性代数%';

alter table classes alter column class_name type varchar(50);

alter table course alter column course_credits type double precision;

alter table course alter column course_name type varchar(30);

alter table classes add constraint pk primary key(class_name,courses_id);

create table if not exists teacher_class(
    teacher_id varchar(8),
    constraint fk1 foreign key(teacher_id) references teacher(teacher_id),
    course_id varchar(10),
    class_name varchar(20),
    constraint fk2 foreign key (course_id,class_name) references classes(courses_id,class_name),
    constraint uk unique (teacher_id,course_id,class_name)
);
alter table teacher_class alter column class_name type varchar(50);

select * from classes where courses_id='IPE103';
update classes set courses_id=trim(courses_id);

--alter table teacher_class drop constraint fk2;
--alter table classes drop constraint fk1;
update course set course_id=upper(trim(course_id)),course_name=upper(trim(course_name));
update classes set courses_id=upper(trim(courses_id)),class_name=trim(class_name);
update teacher_class set course_id=upper(trim(course_id));

alter table classes add constraint fk2 foreign key (courses_id)references course(course_id);
alter table teacher_class add constraint fk2 foreign key (course_id,class_name)references classes(courses_id,class_name);
alter table location alter column location_name type varchar(50);

update college set college_english_name=replace(college_english_name,')','');
alter table classes add column classEnd int;

drop table if exists class_weekList;
create table if not exists class_weekList(
    class_weekListID serial primary key,
    course_id varchar(10),
    class_name varchar(50),
    location_id int,
    constraint fk1 foreign key(course_id) references course(course_id),
    constraint fk2 foreign key (location_id)references location(location_id),
    week int not null,
    havingLesson varchar(2) not null,
    classStart int not null,
    classEnd int not null,
    weekday int not null,
    constraint uk5 unique
    (course_id,class_name,location_id,week,havingLesson,classStart,classEnd,weekday)
);
alter table class_weekList add constraint fk2 foreign key (location_id)references location(location_id);

select * from class_weekList where course_id='ME303';

with temp as(select course_id,min(group_id) as minN
from prerequisite group by course_id)
update prerequisite set group_id=
    (group_id-temp.minN+1) from temp
where temp.course_id=prerequisite.course_id;

select count(*)from class_weekList;

create index sid_index on course_selection(student_id);
drop index sid_index;
select * from course_selection where student_id='11000028';


begin transaction;
alter table course_selection drop constraint fk2;
alter table course_selection drop constraint course_sid_uq;
update course_selection set student_id='99999999';
--where student_id='11000028';

rollback transaction;

begin transaction;
alter table course_selection disable trigger all;
alter table course_selection drop constraint fk1;
alter table course_selection drop constraint fk2;
alter table course_selection drop constraint course_sid_uq;
alter table course_selection drop constraint course_selection_pkey;
select * from course_selection inner join student s on s.sid = course_selection.student_id;
/*...*/
rollback transaction ;