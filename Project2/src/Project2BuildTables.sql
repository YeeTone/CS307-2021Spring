create table if not exists course
(
    courseid                  varchar(50) not null
        constraint course_pkey
            primary key,
    coursename                varchar(50),
    credit                    integer,
    classhour                 integer,
    ispf                      boolean,
    maxprerequisitegroupcount integer
);

create table if not exists semester
(
    name       varchar(20),
    semesterid serial not null
        constraint semester_pkey
            primary key,
    begin_time date,
    end_time   date
);

create table if not exists coursesection
(
    courseid      varchar(50)
        constraint coursesection_courseid_fkey
            references course
            on delete cascade,
    semesterid    bigint
        constraint coursesection_semesterid_fkey
            references semester
            on delete cascade,
    sectionid     serial not null
        constraint coursesection_pkey
            primary key,
    sectionname   varchar(50),
    totalcapacity integer
);

create table if not exists instructor
(
    userid    integer not null
        constraint instructor_pkey
            primary key,
    firstname varchar(30),
    lastname  varchar(30)
);

create table if not exists coursesectionclass
(
    sectionid    bigint
        constraint coursesectionclass_sectionid_fkey
            references coursesection
            on delete cascade,
    instructorid bigint
        constraint coursesectionclass_instructorid_fkey
            references instructor
            on delete cascade,
    dayofweek    integer,
    week         integer,
    classbegin   integer,
    classend     integer,
    location     varchar(50),
    classid      serial not null
);

create table if not exists department
(
    name         varchar(50)
        constraint department_departmentname_uq
            unique,
    departmentid serial not null
        constraint department_pkey
            primary key
);

create table if not exists major
(
    majorid      serial not null
        constraint major_pkey
            primary key,
    name         varchar(50)
        constraint major_majorname_uq
            unique,
    departmentid bigint
        constraint major_departmentid_fkey
            references department
            on delete cascade
);

create table if not exists majorcourse
(
    majorid      bigint      not null
        constraint majorcourse_majorid_fkey
            references major
            on delete cascade,
    courseid     varchar(50) not null
        constraint majorcourse_courseid_fkey
            references course
            on delete cascade,
    iscompulsory boolean,
    constraint pk
        primary key (majorid, courseid)
);

create table if not exists students
(
    userid       integer not null
        constraint students_pkey
            primary key,
    majorid      bigint
        constraint students_majorid_fkey
            references major
            on delete cascade,
    firstname    varchar(30),
    lastname     varchar(30),
    enrolleddate date
);

create table if not exists studentcourseselection
(
    studentid integer
        constraint studentcourseselection_studentid_fkey
            references students
            on delete cascade,
    sectionid bigint
        constraint studentcourseselection_sectionid_fkey
            references coursesection
            on delete cascade
);

create index if not exists student_course_selection_index
    on studentcourseselection (studentid, sectionid);

create index if not exists studentcourseselection_index2
    on studentcourseselection (studentid);

create table if not exists prerequisite
(
    id                   serial      not null
        constraint prerequisite_pkey
            primary key,
    course_id            varchar(50) not null
        constraint fk1
            references course
            on delete cascade,
    prerequisitecourseid varchar(50) not null
        constraint fk2
            references course
            on delete cascade,
    group_id             integer     not null
);

create table if not exists studentpfcourse
(
    studentid integer
        constraint studentpfcourse_studentid_fkey
            references students
            on delete cascade,
    sectionid bigint
        constraint studentpfcourse_sectionid_fkey
            references coursesection
            on delete cascade,
    grade     varchar(10),
    constraint spf_uq
        unique (studentid, sectionid)
);

create index if not exists spf_index
    on studentpfcourse (studentid, grade);

create table if not exists student100course
(
    studentid integer
        constraint student100course_studentid_fkey
            references students
            on delete cascade,
    sectionid bigint
        constraint student100course_sectionid_fkey
            references coursesection
            on delete cascade,
    grade     integer,
    constraint s100_uq
        unique (studentid, sectionid)
);

create index if not exists s100c_index
    on student100course (studentid, grade);

create or replace function getfullname(firstname character varying, lastname character varying) returns character varying
    language plpgsql
as
$$
begin
    if (firstname ~ '[a-zA-z]' and lastName ~ '[a-zA-z]') then
        return firstname || ' ' || lastname;
    else
        return firstname || lastname;
    end if;
end
$$;

create or replace function isprerequisitefullfilled(studentidx integer, sectionidx integer) returns boolean
    language plpgsql
as
$$
declare
    maxGroup    int= -1;
    isOK        boolean= false;
    currentIsOK boolean= false;
begin
    select max(c.maxPrerequisiteGroupCount)
    into maxGroup
    from course as c
             inner join courseSection cS on c.courseId = cS.courseId
    where cS.sectionId = sectionIdx;

    if (maxGroup = 0 or maxGroup is null) then
        return true;
    end if;


    for i in 1 .. maxGroup
        loop
            if (exists(select *
                       from (((select cs.courseId
                               from coursE as cs
                                        inner join prerequisite as p
                                                   on p.prerequisitecourseid = cs.courseid
                               where p.group_id = i)
                              except
                              ((select c3.courseId
                                from coursesection as cs2
                                         inner join course c3
                                                    on c3.courseId = cs2.courseId
                                         inner join student100Course s100C
                                                    on cs2.sectionId = s100C.sectionId and s100C.grade >= 60
                                where s100C.studentId = studentIdx
                               )
                               union all
                               (select c4.courseId
                                from coursesection as cs3
                                         inner join course c4
                                                    on cs3.courseId = c4.courseId
                                         inner join studentPfCourse sPC
                                                    on cs3.sectionId = sPC.sectionId and sPC.grade = 'P'
                                where sPC.studentId = studentIdx)))) as notFullfilled)) then
                currentIsOK := false;
                raise notice 'not fulfilled in the group %',i;
            else
                raise notice 'fulfilled in the group %',i;
                currentIsOK := true;
            end if;

            isOK := isOK or currentIsOK;

        end loop;

    return isOK;
end;
$$;

create or replace function isprerequisitefullfilledbycourse(studentidx integer, courseidx character varying) returns boolean
    language plpgsql
as
$$
declare
    maxGroup    int     := -1;
    isOK        boolean := false;
    currentIsOK boolean := false;
begin
    select maxPrerequisiteGroupCount
    into maxGroup
    from course
    where courseId = courseIdx;

    if (maxGroup is null or maxGroup = -1) then
        raise exception 'No such course found!';
    end if;

    if (maxGroup = 0) then
        raise notice 'No Group!';
        return true;
    end if;

    for i in 1.. maxGroup
        loop
            if (exists(select *
                       from (select cs.courseId
                             from course as cs
                                      inner join prerequisite as p
                                                 on p.prerequisitecourseid = cs.courseId
                             where p.group_id = i
                               and p.course_id = courseIdx) as allPre
                           except ((select c3.courseId
                                    from course c3
                                             inner join courseSection cs2
                                                        on c3.courseId = cs2.courseId
                                             inner join student100Course s100C
                                                        on cs2.sectionid = s100C.sectionid and s100C.grade >= 60
                                    where s100C.studentid = studentIdx)
                                   union all
                                   (select c4.courseId
                                    from courseSection as cs3
                                             inner join course c4
                                                        on cs3.courseId = c4.courseId
                                             inner join studentPfCourse sPC
                                                        on cs3.sectionId = sPC.sectionId and sPC.grade = 'P'
                                    where spc.studentId = studentIdx)))) then
                currentIsOK := false;
            else
                --raise notice 'Here!';
                currentIsOK := true;
            end if;

            isOK := isOK or currentIsOK;

        end loop;

    return isOK;
end

$$;

