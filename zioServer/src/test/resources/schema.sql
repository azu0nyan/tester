--
-- PostgreSQL database dump
--

-- Dumped from database version 15.3 (Ubuntu 15.3-0ubuntu0.23.04.1)
-- Dumped by pg_dump version 15.3 (Ubuntu 15.3-0ubuntu0.23.04.1)

-- Started on 2023-08-20 22:07:22 MSK

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
-- TOC entry 6 (class 2615 OID 16389)
-- Name: tester; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA tester;


ALTER SCHEMA tester OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 215 (class 1259 OID 16390)
-- Name: answer; Type: TABLE; Schema: tester; Owner: postgres
--

CREATE TABLE tester.answer (
    id integer NOT NULL,
    problemid integer NOT NULL,
    answer text NOT NULL,
    status jsonb NOT NULL,
    answeredat timestamp without time zone NOT NULL
);


ALTER TABLE tester.answer OWNER TO postgres;

--
-- TOC entry 216 (class 1259 OID 16395)
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
-- TOC entry 3498 (class 0 OID 0)
-- Dependencies: 216
-- Name: Answer_id_seq; Type: SEQUENCE OWNED BY; Schema: tester; Owner: postgres
--

ALTER SEQUENCE tester."Answer_id_seq" OWNED BY tester.answer.id;


--
-- TOC entry 218 (class 1259 OID 16399)
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
-- TOC entry 219 (class 1259 OID 16402)
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
-- TOC entry 3499 (class 0 OID 0)
-- Dependencies: 219
-- Name: CourseTemplateForGroup_id_seq; Type: SEQUENCE OWNED BY; Schema: tester; Owner: postgres
--

ALTER SEQUENCE tester."CourseTemplateForGroup_id_seq" OWNED BY tester.coursetemplateforgroup.id;


--
-- TOC entry 217 (class 1259 OID 16396)
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
-- TOC entry 220 (class 1259 OID 16403)
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
-- TOC entry 3500 (class 0 OID 0)
-- Dependencies: 220
-- Name: Course_id_seq; Type: SEQUENCE OWNED BY; Schema: tester; Owner: postgres
--

ALTER SEQUENCE tester."Course_id_seq" OWNED BY tester.course.id;


--
-- TOC entry 224 (class 1259 OID 16418)
-- Name: problem; Type: TABLE; Schema: tester; Owner: postgres
--

CREATE TABLE tester.problem (
    id integer NOT NULL,
    courseid integer NOT NULL,
    templatealias character varying(256) NOT NULL,
    seed integer NOT NULL,
    score jsonb NOT NULL
);


ALTER TABLE tester.problem OWNER TO postgres;

--
-- TOC entry 225 (class 1259 OID 16423)
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
-- TOC entry 3501 (class 0 OID 0)
-- Dependencies: 225
-- Name: Problem_id_seq; Type: SEQUENCE OWNED BY; Schema: tester; Owner: postgres
--

ALTER SEQUENCE tester."Problem_id_seq" OWNED BY tester.problem.id;


--
-- TOC entry 226 (class 1259 OID 16424)
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
    lastlogin timestamp without time zone,
    role jsonb NOT NULL
);


ALTER TABLE tester.registereduser OWNER TO postgres;

--
-- TOC entry 227 (class 1259 OID 16429)
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
-- TOC entry 3502 (class 0 OID 0)
-- Dependencies: 227
-- Name: RegisteredUser_id_seq; Type: SEQUENCE OWNED BY; Schema: tester; Owner: postgres
--

ALTER SEQUENCE tester."RegisteredUser_id_seq" OWNED BY tester.registereduser.id;


--
-- TOC entry 233 (class 1259 OID 32777)
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
-- TOC entry 232 (class 1259 OID 32776)
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
-- TOC entry 3503 (class 0 OID 0)
-- Dependencies: 232
-- Name: Session_id_seq; Type: SEQUENCE OWNED BY; Schema: tester; Owner: postgres
--

ALTER SEQUENCE tester."Session_id_seq" OWNED BY tester.session.id;


--
-- TOC entry 228 (class 1259 OID 16430)
-- Name: usergroup; Type: TABLE; Schema: tester; Owner: postgres
--

CREATE TABLE tester.usergroup (
    id integer NOT NULL,
    title text NOT NULL,
    description text NOT NULL
);


ALTER TABLE tester.usergroup OWNER TO postgres;

--
-- TOC entry 229 (class 1259 OID 16435)
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
-- TOC entry 3504 (class 0 OID 0)
-- Dependencies: 229
-- Name: UserGroup_id_seq; Type: SEQUENCE OWNED BY; Schema: tester; Owner: postgres
--

ALTER SEQUENCE tester."UserGroup_id_seq" OWNED BY tester.usergroup.id;


--
-- TOC entry 230 (class 1259 OID 16436)
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
-- TOC entry 231 (class 1259 OID 16439)
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
-- TOC entry 3505 (class 0 OID 0)
-- Dependencies: 231
-- Name: UserToGroup_id_seq; Type: SEQUENCE OWNED BY; Schema: tester; Owner: postgres
--

ALTER SEQUENCE tester."UserToGroup_id_seq" OWNED BY tester.usertogroup.id;


--
-- TOC entry 235 (class 1259 OID 32813)
-- Name: answerrejection; Type: TABLE; Schema: tester; Owner: postgres
--

CREATE TABLE tester.answerrejection (
    answerid integer NOT NULL,
    rejectedat timestamp without time zone NOT NULL,
    message text
);


ALTER TABLE tester.answerrejection OWNER TO postgres;

--
-- TOC entry 236 (class 1259 OID 32830)
-- Name: answerreview; Type: TABLE; Schema: tester; Owner: postgres
--

CREATE TABLE tester.answerreview (
    answerid integer NOT NULL,
    text text NOT NULL,
    reviewerid integer NOT NULL
);


ALTER TABLE tester.answerreview OWNER TO postgres;

--
-- TOC entry 237 (class 1259 OID 32849)
-- Name: answerverifiactionconfirmation; Type: TABLE; Schema: tester; Owner: postgres
--

CREATE TABLE tester.answerverifiactionconfirmation (
    answerid integer NOT NULL,
    confirmedat timestamp without time zone NOT NULL,
    confirmedbyid integer
);


ALTER TABLE tester.answerverifiactionconfirmation OWNER TO postgres;

--
-- TOC entry 234 (class 1259 OID 32806)
-- Name: answerverification; Type: TABLE; Schema: tester; Owner: postgres
--

CREATE TABLE tester.answerverification (
    answerid integer NOT NULL,
    verifiedat timestamp without time zone NOT NULL,
    systemmessage text
);


ALTER TABLE tester.answerverification OWNER TO postgres;

--
-- TOC entry 221 (class 1259 OID 16404)
-- Name: coursetemplate; Type: TABLE; Schema: tester; Owner: postgres
--

CREATE TABLE tester.coursetemplate (
    alias text NOT NULL,
    description text NOT NULL,
    coursedata json NOT NULL
);


ALTER TABLE tester.coursetemplate OWNER TO postgres;

--
-- TOC entry 222 (class 1259 OID 16409)
-- Name: coursetemplateproblem; Type: TABLE; Schema: tester; Owner: postgres
--

CREATE TABLE tester.coursetemplateproblem (
    problemalias text NOT NULL,
    coursealias text NOT NULL
);


ALTER TABLE tester.coursetemplateproblem OWNER TO postgres;

--
-- TOC entry 223 (class 1259 OID 16413)
-- Name: problemtemplate; Type: TABLE; Schema: tester; Owner: postgres
--

CREATE TABLE tester.problemtemplate (
    alias text NOT NULL,
    title text NOT NULL,
    html text NOT NULL,
    answerfield jsonb NOT NULL,
    initialscore jsonb NOT NULL
);


ALTER TABLE tester.problemtemplate OWNER TO postgres;

--
-- TOC entry 3298 (class 2604 OID 16440)
-- Name: answer id; Type: DEFAULT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.answer ALTER COLUMN id SET DEFAULT nextval('tester."Answer_id_seq"'::regclass);


--
-- TOC entry 3299 (class 2604 OID 16441)
-- Name: course id; Type: DEFAULT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.course ALTER COLUMN id SET DEFAULT nextval('tester."Course_id_seq"'::regclass);


--
-- TOC entry 3300 (class 2604 OID 16442)
-- Name: coursetemplateforgroup id; Type: DEFAULT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.coursetemplateforgroup ALTER COLUMN id SET DEFAULT nextval('tester."CourseTemplateForGroup_id_seq"'::regclass);


--
-- TOC entry 3301 (class 2604 OID 16444)
-- Name: problem id; Type: DEFAULT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.problem ALTER COLUMN id SET DEFAULT nextval('tester."Problem_id_seq"'::regclass);


--
-- TOC entry 3302 (class 2604 OID 16445)
-- Name: registereduser id; Type: DEFAULT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.registereduser ALTER COLUMN id SET DEFAULT nextval('tester."RegisteredUser_id_seq"'::regclass);


--
-- TOC entry 3305 (class 2604 OID 32780)
-- Name: session id; Type: DEFAULT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.session ALTER COLUMN id SET DEFAULT nextval('tester."Session_id_seq"'::regclass);


--
-- TOC entry 3303 (class 2604 OID 16446)
-- Name: usergroup id; Type: DEFAULT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.usergroup ALTER COLUMN id SET DEFAULT nextval('tester."UserGroup_id_seq"'::regclass);


--
-- TOC entry 3304 (class 2604 OID 16447)
-- Name: usertogroup id; Type: DEFAULT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.usertogroup ALTER COLUMN id SET DEFAULT nextval('tester."UserToGroup_id_seq"'::regclass);


--
-- TOC entry 3307 (class 2606 OID 16507)
-- Name: answer Answer_pk; Type: CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.answer
    ADD CONSTRAINT "Answer_pk" PRIMARY KEY (id);


--
-- TOC entry 3311 (class 2606 OID 16509)
-- Name: coursetemplateforgroup CourseTemplateForGroup_pk; Type: CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.coursetemplateforgroup
    ADD CONSTRAINT "CourseTemplateForGroup_pk" PRIMARY KEY (id);


--
-- TOC entry 3315 (class 2606 OID 16513)
-- Name: problemtemplate CustomProblemTemplate_pk; Type: CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.problemtemplate
    ADD CONSTRAINT "CustomProblemTemplate_pk" PRIMARY KEY (alias);


--
-- TOC entry 3317 (class 2606 OID 16515)
-- Name: problem Problem_pk; Type: CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.problem
    ADD CONSTRAINT "Problem_pk" PRIMARY KEY (id);


--
-- TOC entry 3327 (class 2606 OID 32784)
-- Name: session Session_pkey; Type: CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.session
    ADD CONSTRAINT "Session_pkey" PRIMARY KEY (id);


--
-- TOC entry 3323 (class 2606 OID 16517)
-- Name: usertogroup UserToGroup_pk; Type: CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.usertogroup
    ADD CONSTRAINT "UserToGroup_pk" PRIMARY KEY (id);


--
-- TOC entry 3331 (class 2606 OID 32819)
-- Name: answerrejection answerrejection_pkey; Type: CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.answerrejection
    ADD CONSTRAINT answerrejection_pkey PRIMARY KEY (answerid);


--
-- TOC entry 3333 (class 2606 OID 32848)
-- Name: answerreview answerreview_answerid_reviewerid_key; Type: CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.answerreview
    ADD CONSTRAINT answerreview_answerid_reviewerid_key UNIQUE (answerid, reviewerid);


--
-- TOC entry 3335 (class 2606 OID 32836)
-- Name: answerreview answerreview_pkey; Type: CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.answerreview
    ADD CONSTRAINT answerreview_pkey PRIMARY KEY (answerid);


--
-- TOC entry 3337 (class 2606 OID 32853)
-- Name: answerverifiactionconfirmation answerverifiactionconfirmation_pkey; Type: CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.answerverifiactionconfirmation
    ADD CONSTRAINT answerverifiactionconfirmation_pkey PRIMARY KEY (answerid);


--
-- TOC entry 3329 (class 2606 OID 32812)
-- Name: answerverification answerverification_pkey; Type: CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.answerverification
    ADD CONSTRAINT answerverification_pkey PRIMARY KEY (answerid);


--
-- TOC entry 3309 (class 2606 OID 16519)
-- Name: course course_pk; Type: CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.course
    ADD CONSTRAINT course_pk PRIMARY KEY (id);


--
-- TOC entry 3313 (class 2606 OID 32795)
-- Name: coursetemplate coursetemplate_pkey; Type: CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.coursetemplate
    ADD CONSTRAINT coursetemplate_pkey PRIMARY KEY (alias);


--
-- TOC entry 3321 (class 2606 OID 16521)
-- Name: usergroup group_pk; Type: CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.usergroup
    ADD CONSTRAINT group_pk PRIMARY KEY (id);


--
-- TOC entry 3325 (class 2606 OID 16525)
-- Name: usertogroup unique_pair_co; Type: CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.usertogroup
    ADD CONSTRAINT unique_pair_co UNIQUE (userid, groupid);


--
-- TOC entry 3319 (class 2606 OID 16527)
-- Name: registereduser user_pk; Type: CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.registereduser
    ADD CONSTRAINT user_pk PRIMARY KEY (id);


--
-- TOC entry 3344 (class 2606 OID 32785)
-- Name: session SessionToUserID; Type: FK CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.session
    ADD CONSTRAINT "SessionToUserID" FOREIGN KEY (userid) REFERENCES tester.registereduser(id);


--
-- TOC entry 3346 (class 2606 OID 32825)
-- Name: answerrejection answerrejection_answerid_fkey; Type: FK CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.answerrejection
    ADD CONSTRAINT answerrejection_answerid_fkey FOREIGN KEY (answerid) REFERENCES tester.answer(id) NOT VALID;


--
-- TOC entry 3347 (class 2606 OID 32837)
-- Name: answerreview answerreview_answerid_fkey; Type: FK CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.answerreview
    ADD CONSTRAINT answerreview_answerid_fkey FOREIGN KEY (answerid) REFERENCES tester.answer(id);


--
-- TOC entry 3348 (class 2606 OID 32842)
-- Name: answerreview answerreview_reviewerid_fkey; Type: FK CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.answerreview
    ADD CONSTRAINT answerreview_reviewerid_fkey FOREIGN KEY (reviewerid) REFERENCES tester.registereduser(id) NOT VALID;


--
-- TOC entry 3349 (class 2606 OID 32859)
-- Name: answerverifiactionconfirmation answerverifiactionconfirmation_answerid_fkey; Type: FK CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.answerverifiactionconfirmation
    ADD CONSTRAINT answerverifiactionconfirmation_answerid_fkey FOREIGN KEY (answerid) REFERENCES tester.answer(id);


--
-- TOC entry 3350 (class 2606 OID 32854)
-- Name: answerverifiactionconfirmation answerverifiactionconfirmation_confirmedbyid_fkey; Type: FK CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.answerverifiactionconfirmation
    ADD CONSTRAINT answerverifiactionconfirmation_confirmedbyid_fkey FOREIGN KEY (confirmedbyid) REFERENCES tester.registereduser(id);


--
-- TOC entry 3345 (class 2606 OID 32820)
-- Name: answerverification answerverification_answerid_fkey; Type: FK CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.answerverification
    ADD CONSTRAINT answerverification_answerid_fkey FOREIGN KEY (answerid) REFERENCES tester.answer(id) NOT VALID;


--
-- TOC entry 3341 (class 2606 OID 32801)
-- Name: problem course_fk; Type: FK CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.problem
    ADD CONSTRAINT course_fk FOREIGN KEY (courseid) REFERENCES tester.course(id) ON DELETE CASCADE NOT VALID;


--
-- TOC entry 3342 (class 2606 OID 16538)
-- Name: usertogroup group_fk; Type: FK CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.usertogroup
    ADD CONSTRAINT group_fk FOREIGN KEY (groupid) REFERENCES tester.usergroup(id);


--
-- TOC entry 3340 (class 2606 OID 16543)
-- Name: coursetemplateforgroup group_fk; Type: FK CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.coursetemplateforgroup
    ADD CONSTRAINT group_fk FOREIGN KEY (groupid) REFERENCES tester.usergroup(id);


--
-- TOC entry 3338 (class 2606 OID 32796)
-- Name: answer problem_fk; Type: FK CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.answer
    ADD CONSTRAINT problem_fk FOREIGN KEY (problemid) REFERENCES tester.problem(id) ON DELETE CASCADE NOT VALID;


--
-- TOC entry 3339 (class 2606 OID 16553)
-- Name: course user_fk; Type: FK CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.course
    ADD CONSTRAINT user_fk FOREIGN KEY (userid) REFERENCES tester.registereduser(id);


--
-- TOC entry 3343 (class 2606 OID 16558)
-- Name: usertogroup user_fk; Type: FK CONSTRAINT; Schema: tester; Owner: postgres
--

ALTER TABLE ONLY tester.usertogroup
    ADD CONSTRAINT user_fk FOREIGN KEY (userid) REFERENCES tester.registereduser(id);


-- Completed on 2023-08-20 22:07:22 MSK

--
-- PostgreSQL database dump complete
--

