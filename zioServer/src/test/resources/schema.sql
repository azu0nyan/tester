--
-- PostgreSQL database dump
--

-- Dumped from database version 15.4 (Ubuntu 15.4-1.pgdg23.04+1)
-- Dumped by pg_dump version 15.4 (Ubuntu 15.4-1.pgdg23.04+1)

-- Started on 2023-10-01 18:03:55 MSK

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 6 (class 2615 OID 20338)
-- Name: tester; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA tester;


ALTER SCHEMA tester OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 215 (class 1259 OID 20339)
-- Name: answer; Type: TABLE; Schema: tester; Owner: postgres
--

CREATE TABLE tester.answer (
    id integer NOT NULL,
    problemid integer NOT NULL,
    answer text NOT NULL,
    answeredat timestamp without time zone NOT NULL
);


ALTER TABLE tester.answer OWNER TO postgres;

--
-- TOC entry 216 (class 1259 OID 20344)
-- Name: Answer_id_seq; Type: SEQUENCE; Schema: tester; Owner: postgres
--

CREATE SEQUENCE tester."Answer_id_seq"
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE tester."Answer_id_seq" OWNER TO postgres;

--
-- TOC entry 3536 (class 0 OID 0)
-- Dependencies: 216
-- Name: Answer_id_seq; Type: SEQUENCE OWNED BY; Schema: tester; Owner: postgres
--

ALTER SEQUENCE tester."Answer_id_seq" OWNED BY tester.answer.id;


--
-- TOC entry 217 (class 1259 OID 20345)
-- Name: coursetemplateforgroup; Type: TABLE; Schema: tester; Owner: postgres
--

CREATE TABLE tester.coursetemplateforgroup (
    id integer NOT NULL,
    groupid integer NOT NULL,
    templatealias character varying(256) NOT NULL,
    forcestartforgroupmembers boolean NOT NULL
);


ALTER TABLE tester.coursetemplateforgroup OWNER TO postgres;

--
-- TOC entry 218 (class 1259 OID 20348)
-- Name: CourseTemplateForGroup_id_seq; Type: SEQUENCE; Schema: tester; Owner: postgres
--

CREATE SEQUENCE tester."CourseTemplateForGroup_id_seq"
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE tester."CourseTemplateForGroup_id_seq" OWNER TO postgres;

--
-- TOC entry 3537 (class 0 OID 0)
-- Dependencies: 218
-- Name: CourseTemplateForGroup_id_seq; Type: SEQUENCE OWNED BY; Schema: tester; Owner: postgres
--

ALTER SEQUENCE tester."CourseTemplateForGroup_id_seq" OWNED BY tester.coursetemplateforgroup.id;


--
-- TOC entry 219 (class 1259 OID 20349)
-- Name: course; Type: TABLE; Schema: tester; Owner: postgres
--

CREATE TABLE tester.course (
    id integer NOT NULL,
    userid integer NOT NULL,
    templatealias character varying(256) NOT NULL,
    seed integer NOT NULL,
    startedat timestamp without time zone,
    endedat timestamp without time zone
);


ALTER TABLE tester.course OWNER TO postgres;

--
-- TOC entry 220 (class 1259 OID 20352)
-- Name: Course_id_seq; Type: SEQUENCE; Schema: tester; Owner: postgres
--

CREATE SEQUENCE tester."Course_id_seq"
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE tester."Course_id_seq" OWNER TO postgres;

--
-- TOC entry 3538 (class 0 OID 0)
-- Dependencies: 220
-- Name: Course_id_seq; Type: SEQUENCE OWNED BY; Schema: tester; Owner: postgres
--

ALTER SEQUENCE tester."Course_id_seq" OWNED BY tester.course.id;


--
-- TOC entry 221 (class 1259 OID 20353)
-- Name: problem; Type: TABLE; Schema: tester; Owner: postgres
--

CREATE TABLE tester.problem (
    id integer NOT NULL,
    courseid integer NOT NULL,
    templatealias character varying(256) NOT NULL,
    seed integer NOT NULL,
    score jsonb NOT NULL,
    maxattempts integer,
    deadline timestamp without time zone,
    scorenormalized double precision DEFAULT 0 NOT NULL,
    requireconfirmation boolean DEFAULT false NOT NULL,
    addedat timestamp without time zone DEFAULT (now())::timestamp without time zone NOT NULL
);


ALTER TABLE tester.problem OWNER TO postgres;

--
-- TOC entry 222 (class 1259 OID 20361)
-- Name: Problem_id_seq; Type: SEQUENCE; Schema: tester; Owner: postgres
--

CREATE SEQUENCE tester."Problem_id_seq"
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE tester."Problem_id_seq" OWNER TO postgres;

--
-- TOC entry 3539 (class 0 OID 0)
-- Dependencies: 222
-- Name: Problem_id_seq; Type: SEQUENCE OWNED BY; Schema: tester; Owner: postgres
--

ALTER SEQUENCE tester."Problem_id_seq" OWNED BY tester.problem.id;


--
-- TOC entry 223 (class 1259 OID 20362)
-- Name: registereduser; Type: TABLE; Schema: tester; Owner: postgres
--

CREATE TABLE tester.registereduser (
    id integer NOT NULL,
    login character varying(256) NOT NULL,
    passwordhash character varying(256) NOT NULL,
    passwordsalt character varying(256) NOT NULL,
    firstname character varying(256) NOT NULL,
    lastname character varying(256) NOT NULL,
    email character varying(256),
    registeredat timestamp without time zone NOT NULL,
    lastlogin timestamp without time zone
);


ALTER TABLE tester.registereduser OWNER TO postgres;

--
-- TOC entry 224 (class 1259 OID 20367)
-- Name: RegisteredUser_id_seq; Type: SEQUENCE; Schema: tester; Owner: postgres
--

CREATE SEQUENCE tester."RegisteredUser_id_seq"
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE tester."RegisteredUser_id_seq" OWNER TO postgres;

--
-- TOC entry 3540 (class 0 OID 0)
-- Dependencies: 224
-- Name: RegisteredUser_id_seq; Type: SEQUENCE OWNED BY; Schema: tester; Owner: postgres
--

ALTER SEQUENCE tester."RegisteredUser_id_seq" OWNED BY tester.registereduser.id;


--
-- TOC entry 225 (class 1259 OID 20368)
-- Name: session; Type: TABLE; Schema: tester; Owner: postgres
--

CREATE TABLE tester.session (
    id integer NOT NULL,
    userid bigint NOT NULL,
    token text NOT NULL,
    ip text,
    useragent text,
    platform text,
    locale text,
    startat timestamp without time zone NOT NULL,
    endat timestamp without time zone NOT NULL,
    valid boolean NOT NULL
);


ALTER TABLE tester.session OWNER TO postgres;

--
-- TOC entry 226 (class 1259 OID 20373)
-- Name: Session_id_seq; Type: SEQUENCE; Schema: tester; Owner: postgres
--

CREATE SEQUENCE tester."Session_id_seq"
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE tester."Session_id_seq" OWNER TO postgres;

--
-- TOC entry 3541 (class 0 OID 0)
-- Dependencies: 226
-- Name: Session_id_seq; Type: SEQUENCE OWNED BY; Schema: tester; Owner: postgres
--

ALTER SEQUENCE tester."Session_id_seq" OWNED BY tester.session.id;


--
-- TOC entry 227 (class 1259 OID 20374)
-- Name: usergroup; Type: TABLE; Schema: tester; Owner: postgres
--

CREATE TABLE tester.usergroup (
    id integer NOT NULL,
    title text NOT NULL,
    description text NOT NULL
);


ALTER TABLE tester.usergroup OWNER TO postgres;

--
-- TOC entry 228 (class 1259 OID 20379)
-- Name: UserGroup_id_seq; Type: SEQUENCE; Schema: tester; Owner: postgres
--

CREATE SEQUENCE tester."UserGroup_id_seq"
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE tester."UserGroup_id_seq" OWNER TO postgres;

--
-- TOC entry 3542 (class 0 OID 0)
-- Dependencies: 228
-- Name: UserGroup_id_seq; Type: SEQUENCE OWNED BY; Schema: tester; Owner: postgres
--

ALTER SEQUENCE tester."UserGroup_id_seq" OWNED BY tester.usergroup.id;


--
-- TOC entry 229 (class 1259 OID 20380)
-- Name: usertogroup; Type: TABLE; Schema: tester; Owner: postgres
--

CREATE TABLE tester.usertogroup (
    id integer NOT NULL,
    userid integer NOT NULL,
    groupid integer NOT NULL,
    enteredat timestamp without time zone NOT NULL,
    leavedat timestamp without time zone
);


ALTER TABLE tester.usertogroup OWNER TO postgres;

--
-- TOC entry 230 (class 1259 OID 20383)
-- Name: UserToGroup_id_seq; Type: SEQUENCE; Schema: tester; Owner: postgres
--

CREATE SEQUENCE tester."UserToGroup_id_seq"
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE tester."UserToGroup_id_seq" OWNER TO postgres;

--
-- TOC entry 3543 (class 0 OID 0)
-- Dependencies: 230
-- Name: UserToGroup_id_seq; Type: SEQUENCE OWNED BY; Schema: tester; Owner: postgres
--

ALTER SEQUENCE tester."UserToGroup_id_seq" OWNED BY tester.usertogroup.id;


--
-- TOC entry 242 (class 1259 OID 28529)
-- Name: admin; Type: TABLE; Schema: tester; Owner: postgres
--

CREATE TABLE tester.admin (
    userid integer NOT NULL
);


ALTER TABLE tester.admin OWNER TO postgres;

--
-- TOC entry 231 (class 1259 OID 20384)
-- Name: answerrejection; Type: TABLE; Schema: tester; Owner: postgres
--

CREATE TABLE tester.answerrejection (
    answerid integer NOT NULL,
    rejectedat timestamp without time zone NOT NULL,
    message text,
    rejectedby integer
);


ALTER TABLE tester.answerrejection OWNER TO postgres;

--
-- TOC entry 232 (class 1259 OID 20389)
-- Name: answerreview; Type: TABLE; Schema: tester; Owner: postgres
--

CREATE TABLE tester.answerreview (
    answerid integer NOT NULL,
    text text NOT NULL,
    reviewerid integer NOT NULL
);


ALTER TABLE tester.answerreview OWNER TO postgres;

--
-- TOC entry 234 (class 1259 OID 20397)
-- Name: answerverification; Type: TABLE; Schema: tester; Owner: postgres
--

CREATE TABLE tester.answerverification (
    answerid integer NOT NULL,
    verifiedat timestamp without time zone NOT NULL,
    systemmessage text,
    scorenormalized double precision NOT NULL,
    score jsonb NOT NULL
);


ALTER TABLE tester.answerverification OWNER TO postgres;

--
-- TOC entry 233 (class 1259 OID 20394)
-- Name: answerverificationconfirmation; Type: TABLE; Schema: tester; Owner: postgres
--

CREATE TABLE tester.answerverificationconfirmation (
    answerid integer NOT NULL,
    confirmedat timestamp without time zone NOT NULL,
    confirmedbyid integer
);


ALTER TABLE tester.answerverificationconfirmation OWNER TO postgres;

--
-- TOC entry 235 (class 1259 OID 20402)
-- Name: coursetemplate; Type: TABLE; Schema: tester; Owner: postgres
--

CREATE TABLE tester.coursetemplate (
    alias text NOT NULL,
    description text NOT NULL,
    coursedata json NOT NULL
);


ALTER TABLE tester.coursetemplate OWNER TO postgres;

--
-- TOC entry 236 (class 1259 OID 20407)
-- Name: coursetemplateproblem; Type: TABLE; Schema: tester; Owner: postgres
--

CREATE TABLE tester.coursetemplateproblem (
    problemalias text NOT NULL,
    coursealias text NOT NULL
);


ALTER TABLE tester.coursetemplateproblem OWNER TO postgres;

--
-- TOC entry 237 (class 1259 OID 20412)
-- Name: problemtemplate; Type: TABLE; Schema: tester; Owner: postgres
--

CREATE TABLE tester.problemtemplate (
    alias text NOT NULL,
    title text NOT NULL,
    html text NOT NULL,
    answerfield jsonb NOT NULL,
    initialscore jsonb NOT NULL,
    requireconfirmation boolean DEFAULT false NOT NULL,
    maxattempts integer,
    verificatoralias text
);


ALTER TABLE tester.problemtemplate OWNER TO postgres;

--
-- TOC entry 238 (class 1259 OID 20418)
-- Name: teacher; Type: TABLE; Schema: tester; Owner: postgres
--

CREATE TABLE tester.teacher (
    userid integer NOT NULL
);


ALTER TABLE tester.teacher OWNER TO postgres;

--
-- TOC entry 239 (class 1259 OID 20421)
-- Name: teachercoursetemplate; Type: TABLE; Schema: tester; Owner: postgres
--

CREATE TABLE tester.teachercoursetemplate (
    teacherid integer NOT NULL,
    coursetemplatealias text NOT NULL
);


ALTER TABLE tester.teachercoursetemplate OWNER TO postgres;

--
-- TOC entry 240 (class 1259 OID 20426)
-- Name: teacherproblemtemplate; Type: TABLE; Schema: tester; Owner: postgres
--

CREATE TABLE tester.teacherproblemtemplate (
    teacherid integer NOT NULL,
    templatealias text NOT NULL
);


ALTER TABLE tester.teacherproblemtemplate OWNER TO postgres;

--
-- TOC entry 241 (class 1259 OID 20431)
-- Name: teachertogroup; Type: TABLE; Schema: tester; Owner: postgres
--

CREATE TABLE tester.teachertogroup (
    teacherid integer NOT NULL,
    groupid integer NOT NULL
);


ALTER TABLE tester.teachertogroup OWNER TO postgres;

--
-- TOC entry 3319 (class 2604 OID 20434)
-- Name: answer id; Type: DEFAULT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.answer ALTER COLUMN id SET DEFAULT nextval('tester."Answer_id_seq"'::regclass);


--
-- TOC entry 3321 (class 2604 OID 20435)
-- Name: course id; Type: DEFAULT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.course ALTER COLUMN id SET DEFAULT nextval('tester."Course_id_seq"'::regclass);


--
-- TOC entry 3320 (class 2604 OID 20436)
-- Name: coursetemplateforgroup id; Type: DEFAULT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.coursetemplateforgroup ALTER COLUMN id SET DEFAULT nextval('tester."CourseTemplateForGroup_id_seq"'::regclass);


--
-- TOC entry 3322 (class 2604 OID 20437)
-- Name: problem id; Type: DEFAULT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.problem ALTER COLUMN id SET DEFAULT nextval('tester."Problem_id_seq"'::regclass);


--
-- TOC entry 3326 (class 2604 OID 20438)
-- Name: registereduser id; Type: DEFAULT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.registereduser ALTER COLUMN id SET DEFAULT nextval('tester."RegisteredUser_id_seq"'::regclass);


--
-- TOC entry 3327 (class 2604 OID 20439)
-- Name: session id; Type: DEFAULT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.session ALTER COLUMN id SET DEFAULT nextval('tester."Session_id_seq"'::regclass);


--
-- TOC entry 3328 (class 2604 OID 20440)
-- Name: usergroup id; Type: DEFAULT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.usergroup ALTER COLUMN id SET DEFAULT nextval('tester."UserGroup_id_seq"'::regclass);


--
-- TOC entry 3329 (class 2604 OID 20441)
-- Name: usertogroup id; Type: DEFAULT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.usertogroup ALTER COLUMN id SET DEFAULT nextval('tester."UserToGroup_id_seq"'::regclass);


--
-- TOC entry 3332 (class 2606 OID 20443)
-- Name: answer Answer_pk; Type: CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.answer
    ADD CONSTRAINT "Answer_pk" PRIMARY KEY (id);


--
-- TOC entry 3334 (class 2606 OID 20445)
-- Name: coursetemplateforgroup CourseTemplateForGroup_pk; Type: CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.coursetemplateforgroup
    ADD CONSTRAINT "CourseTemplateForGroup_pk" PRIMARY KEY (id);


--
-- TOC entry 3362 (class 2606 OID 20447)
-- Name: problemtemplate CustomProblemTemplate_pk; Type: CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.problemtemplate
    ADD CONSTRAINT "CustomProblemTemplate_pk" PRIMARY KEY (alias);


--
-- TOC entry 3338 (class 2606 OID 20449)
-- Name: problem Problem_pk; Type: CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.problem
    ADD CONSTRAINT "Problem_pk" PRIMARY KEY (id);


--
-- TOC entry 3342 (class 2606 OID 20451)
-- Name: session Session_pkey; Type: CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.session
    ADD CONSTRAINT "Session_pkey" PRIMARY KEY (id);


--
-- TOC entry 3346 (class 2606 OID 20453)
-- Name: usertogroup UserToGroup_pk; Type: CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.usertogroup
    ADD CONSTRAINT "UserToGroup_pk" PRIMARY KEY (id);


--
-- TOC entry 3366 (class 2606 OID 28533)
-- Name: admin admin_pkey; Type: CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.admin
    ADD CONSTRAINT admin_pkey PRIMARY KEY (userid);


--
-- TOC entry 3350 (class 2606 OID 20455)
-- Name: answerrejection answerrejection_pkey; Type: CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.answerrejection
    ADD CONSTRAINT answerrejection_pkey PRIMARY KEY (answerid);


--
-- TOC entry 3352 (class 2606 OID 20457)
-- Name: answerreview answerreview_answerid_reviewerid_key; Type: CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.answerreview
    ADD CONSTRAINT answerreview_answerid_reviewerid_key UNIQUE (answerid, reviewerid);


--
-- TOC entry 3354 (class 2606 OID 20459)
-- Name: answerreview answerreview_pkey; Type: CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.answerreview
    ADD CONSTRAINT answerreview_pkey PRIMARY KEY (answerid);


--
-- TOC entry 3356 (class 2606 OID 20461)
-- Name: answerverificationconfirmation answerverifiactionconfirmation_pkey; Type: CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.answerverificationconfirmation
    ADD CONSTRAINT answerverifiactionconfirmation_pkey PRIMARY KEY (answerid);


--
-- TOC entry 3358 (class 2606 OID 20463)
-- Name: answerverification answerverification_pkey; Type: CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.answerverification
    ADD CONSTRAINT answerverification_pkey PRIMARY KEY (answerid);


--
-- TOC entry 3336 (class 2606 OID 20465)
-- Name: course course_pk; Type: CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.course
    ADD CONSTRAINT course_pk PRIMARY KEY (id);


--
-- TOC entry 3360 (class 2606 OID 20467)
-- Name: coursetemplate coursetemplate_pkey; Type: CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.coursetemplate
    ADD CONSTRAINT coursetemplate_pkey PRIMARY KEY (alias);


--
-- TOC entry 3344 (class 2606 OID 20469)
-- Name: usergroup group_pk; Type: CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.usergroup
    ADD CONSTRAINT group_pk PRIMARY KEY (id);


--
-- TOC entry 3364 (class 2606 OID 20471)
-- Name: teacher teacher_pkey; Type: CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.teacher
    ADD CONSTRAINT teacher_pkey PRIMARY KEY (userid);


--
-- TOC entry 3348 (class 2606 OID 20473)
-- Name: usertogroup unique_pair_co; Type: CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.usertogroup
    ADD CONSTRAINT unique_pair_co UNIQUE (userid, groupid);


--
-- TOC entry 3340 (class 2606 OID 20475)
-- Name: registereduser user_pk; Type: CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.registereduser
    ADD CONSTRAINT user_pk PRIMARY KEY (id);


--
-- TOC entry 3371 (class 2606 OID 20476)
-- Name: session SessionToUserID; Type: FK CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.session
    ADD CONSTRAINT "SessionToUserID" FOREIGN KEY (userid) REFERENCES tester.registereduser(id);


--
-- TOC entry 3388 (class 2606 OID 28534)
-- Name: admin admin_userid_fkey; Type: FK CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.admin
    ADD CONSTRAINT admin_userid_fkey FOREIGN KEY (userid) REFERENCES tester.registereduser(id);


--
-- TOC entry 3374 (class 2606 OID 20481)
-- Name: answerrejection answerrejection_answerid_fkey; Type: FK CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.answerrejection
    ADD CONSTRAINT answerrejection_answerid_fkey FOREIGN KEY (answerid) REFERENCES tester.answer(id) ON DELETE CASCADE NOT VALID;


--
-- TOC entry 3375 (class 2606 OID 20486)
-- Name: answerrejection answerrejection_rejectedBy_fkey; Type: FK CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.answerrejection
    ADD CONSTRAINT "answerrejection_rejectedBy_fkey" FOREIGN KEY (rejectedby) REFERENCES tester.teacher(userid) ON DELETE SET NULL NOT VALID;


--
-- TOC entry 3376 (class 2606 OID 20491)
-- Name: answerreview answerreview_answerid_fkey; Type: FK CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.answerreview
    ADD CONSTRAINT answerreview_answerid_fkey FOREIGN KEY (answerid) REFERENCES tester.answer(id) ON UPDATE CASCADE NOT VALID;


--
-- TOC entry 3377 (class 2606 OID 20496)
-- Name: answerreview answerreview_reviewerid_fkey; Type: FK CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.answerreview
    ADD CONSTRAINT answerreview_reviewerid_fkey FOREIGN KEY (reviewerid) REFERENCES tester.registereduser(id) ON DELETE SET NULL NOT VALID;


--
-- TOC entry 3378 (class 2606 OID 20501)
-- Name: answerverificationconfirmation answerverifiactionconfirmation_answerid_fkey; Type: FK CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.answerverificationconfirmation
    ADD CONSTRAINT answerverifiactionconfirmation_answerid_fkey FOREIGN KEY (answerid) REFERENCES tester.answer(id) ON DELETE CASCADE NOT VALID;


--
-- TOC entry 3379 (class 2606 OID 20506)
-- Name: answerverificationconfirmation answerverifiactionconfirmation_confirmedbyid_fkey; Type: FK CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.answerverificationconfirmation
    ADD CONSTRAINT answerverifiactionconfirmation_confirmedbyid_fkey FOREIGN KEY (confirmedbyid) REFERENCES tester.registereduser(id) ON DELETE SET NULL NOT VALID;


--
-- TOC entry 3380 (class 2606 OID 20511)
-- Name: answerverification answerverification_answerid_fkey; Type: FK CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.answerverification
    ADD CONSTRAINT answerverification_answerid_fkey FOREIGN KEY (answerid) REFERENCES tester.answer(id) ON DELETE CASCADE NOT VALID;


--
-- TOC entry 3370 (class 2606 OID 20516)
-- Name: problem course_fk; Type: FK CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.problem
    ADD CONSTRAINT course_fk FOREIGN KEY (courseid) REFERENCES tester.course(id) ON DELETE CASCADE NOT VALID;


--
-- TOC entry 3372 (class 2606 OID 20521)
-- Name: usertogroup group_fk; Type: FK CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.usertogroup
    ADD CONSTRAINT group_fk FOREIGN KEY (groupid) REFERENCES tester.usergroup(id);


--
-- TOC entry 3368 (class 2606 OID 20526)
-- Name: coursetemplateforgroup group_fk; Type: FK CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.coursetemplateforgroup
    ADD CONSTRAINT group_fk FOREIGN KEY (groupid) REFERENCES tester.usergroup(id);


--
-- TOC entry 3367 (class 2606 OID 20531)
-- Name: answer problem_fk; Type: FK CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.answer
    ADD CONSTRAINT problem_fk FOREIGN KEY (problemid) REFERENCES tester.problem(id) ON DELETE CASCADE NOT VALID;


--
-- TOC entry 3381 (class 2606 OID 20536)
-- Name: teacher teacher_userid_fkey; Type: FK CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.teacher
    ADD CONSTRAINT teacher_userid_fkey FOREIGN KEY (userid) REFERENCES tester.registereduser(id);


--
-- TOC entry 3382 (class 2606 OID 20541)
-- Name: teachercoursetemplate teachercoursetemplate_coursetemplatealias_fkey; Type: FK CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.teachercoursetemplate
    ADD CONSTRAINT teachercoursetemplate_coursetemplatealias_fkey FOREIGN KEY (coursetemplatealias) REFERENCES tester.coursetemplate(alias) ON UPDATE CASCADE ON DELETE CASCADE NOT VALID;


--
-- TOC entry 3383 (class 2606 OID 20546)
-- Name: teachercoursetemplate teachercoursetemplate_teacherid_fkey; Type: FK CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.teachercoursetemplate
    ADD CONSTRAINT teachercoursetemplate_teacherid_fkey FOREIGN KEY (teacherid) REFERENCES tester.teacher(userid) ON DELETE CASCADE NOT VALID;


--
-- TOC entry 3384 (class 2606 OID 20551)
-- Name: teacherproblemtemplate teacherproblemtemplate_teacherid_fkey; Type: FK CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.teacherproblemtemplate
    ADD CONSTRAINT teacherproblemtemplate_teacherid_fkey FOREIGN KEY (teacherid) REFERENCES tester.teacher(userid) ON DELETE CASCADE;


--
-- TOC entry 3385 (class 2606 OID 20556)
-- Name: teacherproblemtemplate teacherproblemtemplate_templatealias_fkey; Type: FK CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.teacherproblemtemplate
    ADD CONSTRAINT teacherproblemtemplate_templatealias_fkey FOREIGN KEY (templatealias) REFERENCES tester.problemtemplate(alias) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3386 (class 2606 OID 20561)
-- Name: teachertogroup teachertogroup_groupid_fkey; Type: FK CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.teachertogroup
    ADD CONSTRAINT teachertogroup_groupid_fkey FOREIGN KEY (groupid) REFERENCES tester.usergroup(id) NOT VALID;


--
-- TOC entry 3387 (class 2606 OID 20566)
-- Name: teachertogroup teachertogroup_teacherid_fkey; Type: FK CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.teachertogroup
    ADD CONSTRAINT teachertogroup_teacherid_fkey FOREIGN KEY (teacherid) REFERENCES tester.teacher(userid) NOT VALID;


--
-- TOC entry 3369 (class 2606 OID 20571)
-- Name: course user_fk; Type: FK CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.course
    ADD CONSTRAINT user_fk FOREIGN KEY (userid) REFERENCES tester.registereduser(id);


--
-- TOC entry 3373 (class 2606 OID 20576)
-- Name: usertogroup user_fk; Type: FK CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.usertogroup
    ADD CONSTRAINT user_fk FOREIGN KEY (userid) REFERENCES tester.registereduser(id);


-- Completed on 2023-10-01 18:03:55 MSK

--
-- PostgreSQL database dump complete
--

