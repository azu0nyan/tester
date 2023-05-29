-- Database generated with pgModeler (PostgreSQL Database Modeler).
-- pgModeler version: 1.0.0-beta1
-- PostgreSQL version: 15.0
-- Project Site: pgmodeler.io
-- Model Author: ---

-- Database creation must be performed outside a multi lined SQL file. 
-- These commands were put in this file only as a convenience.
-- 
-- object: new_database | type: DATABASE --
-- DROP DATABASE IF EXISTS new_database;
CREATE DATABASE new_database;
-- ddl-end --


-- object: tester | type: SCHEMA --
-- DROP SCHEMA IF EXISTS tester CASCADE;
CREATE SCHEMA tester;
-- ddl-end --
ALTER SCHEMA tester OWNER TO postgres;
-- ddl-end --

SET search_path TO pg_catalog,public,tester;
-- ddl-end --

-- object: tester."RegisteredUser" | type: TABLE --
-- DROP TABLE IF EXISTS tester."RegisteredUser" CASCADE;
CREATE TABLE tester."RegisteredUser" (
	id serial NOT NULL,
	login character varying(256) NOT NULL,
	"passwordHash" character varying(256) NOT NULL,
	"passwordSalt" character varying(256) NOT NULL,
	"firstName" character varying(256),
	"lastName" character varying(256),
	email character varying(256),
	"registeredAt" timestamp NOT NULL,
	"lastLogin" timestamp,
	role jsonb NOT NULL,
	CONSTRAINT user_pk PRIMARY KEY (id)
);
-- ddl-end --
ALTER TABLE tester."RegisteredUser" OWNER TO postgres;
-- ddl-end --

-- object: tester."Group" | type: TABLE --
-- DROP TABLE IF EXISTS tester."Group" CASCADE;
CREATE TABLE tester."Group" (
	id serial NOT NULL,
	title text NOT NULL,
	description text NOT NULL,
	CONSTRAINT group_pk PRIMARY KEY (id)
);
-- ddl-end --
ALTER TABLE tester."Group" OWNER TO postgres;
-- ddl-end --

-- object: tester."Course" | type: TABLE --
-- DROP TABLE IF EXISTS tester."Course" CASCADE;
CREATE TABLE tester."Course" (
	id serial NOT NULL,
	"userId" integer NOT NULL,
	"templateAlias" character varying(256) NOT NULL,
	seed integer NOT NULL,
	status jsonb NOT NULL,
	CONSTRAINT course_pk PRIMARY KEY (id)
);
-- ddl-end --
ALTER TABLE tester."Course" OWNER TO postgres;
-- ddl-end --

-- object: tester."UserToGroup" | type: TABLE --
-- DROP TABLE IF EXISTS tester."UserToGroup" CASCADE;
CREATE TABLE tester."UserToGroup" (
	id serial NOT NULL,
	"userId" integer NOT NULL,
	"groupId" integer NOT NULL,
	"enteredAt" timestamp NOT NULL,
	"leavedAt" timestamp,
	CONSTRAINT "unique" UNIQUE ("userId","groupId"),
	CONSTRAINT "UserToGroup_pk" PRIMARY KEY (id)
);
-- ddl-end --
ALTER TABLE tester."UserToGroup" OWNER TO postgres;
-- ddl-end --

-- object: tester."CourseTemplateForGroup" | type: TABLE --
-- DROP TABLE IF EXISTS tester."CourseTemplateForGroup" CASCADE;
CREATE TABLE tester."CourseTemplateForGroup" (
	id serial NOT NULL,
	"groupId" integer NOT NULL,
	"templateAlias" character varying(256),
	"forceStartForGroupMembers" boolean NOT NULL,
	CONSTRAINT "CourseTemplateForGroup_pk" PRIMARY KEY (id)
);
-- ddl-end --
ALTER TABLE tester."CourseTemplateForGroup" OWNER TO postgres;
-- ddl-end --

-- object: tester."CustomCourseTemplate" | type: TABLE --
-- DROP TABLE IF EXISTS tester."CustomCourseTemplate" CASCADE;
CREATE TABLE tester."CustomCourseTemplate" (
	id serial NOT NULL,
	"templateAlias" character varying(256) NOT NULL,
	description text NOT NULL,
	"courseData" json NOT NULL,
	CONSTRAINT "CustomCourseTemplate_pk" PRIMARY KEY (id)
);
-- ddl-end --
ALTER TABLE tester."CustomCourseTemplate" OWNER TO postgres;
-- ddl-end --

-- object: tester."CustomCourseTemplateProblemAlias" | type: TABLE --
-- DROP TABLE IF EXISTS tester."CustomCourseTemplateProblemAlias" CASCADE;
CREATE TABLE tester."CustomCourseTemplateProblemAlias" (
	"courseId" integer NOT NULL,
	"problemAlias" character varying(256) NOT NULL,
	CONSTRAINT unique_pair UNIQUE ("courseId","problemAlias")
);
-- ddl-end --
ALTER TABLE tester."CustomCourseTemplateProblemAlias" OWNER TO postgres;
-- ddl-end --

-- object: tester."CustomProblemTemplate" | type: TABLE --
-- DROP TABLE IF EXISTS tester."CustomProblemTemplate" CASCADE;
CREATE TABLE tester."CustomProblemTemplate" (
	alias character varying(256) NOT NULL,
	title character varying(256) NOT NULL,
	html text NOT NULL,
	"answerField" json NOT NULL,
	"initialScore" json NOT NULL,
	CONSTRAINT "CustomProblemTemplate_pk" PRIMARY KEY (alias)
);
-- ddl-end --
ALTER TABLE tester."CustomProblemTemplate" OWNER TO postgres;
-- ddl-end --

-- object: tester."Problem" | type: TABLE --
-- DROP TABLE IF EXISTS tester."Problem" CASCADE;
CREATE TABLE tester."Problem" (
	id serial NOT NULL,
	"courseId" integer NOT NULL,
	"templateAlias" character varying(256) NOT NULL,
	seed integer NOT NULL,
	score jsonb,
	CONSTRAINT "Problem_pk" PRIMARY KEY (id)
);
-- ddl-end --
ALTER TABLE tester."Problem" OWNER TO postgres;
-- ddl-end --

-- object: tester."Answer" | type: TABLE --
-- DROP TABLE IF EXISTS tester."Answer" CASCADE;
CREATE TABLE tester."Answer" (
	id serial NOT NULL,
	"problemId" integer NOT NULL,
	answer text NOT NULL,
	status jsonb NOT NULL,
	"answeredAt" timestamp NOT NULL,
	CONSTRAINT "Answer_pk" PRIMARY KEY (id)
);
-- ddl-end --
ALTER TABLE tester."Answer" OWNER TO postgres;
-- ddl-end --

-- object: "user" | type: CONSTRAINT --
-- ALTER TABLE tester."Course" DROP CONSTRAINT IF EXISTS "user" CASCADE;
ALTER TABLE tester."Course" ADD CONSTRAINT "user" FOREIGN KEY ("userId")
REFERENCES tester."RegisteredUser" (id) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;
-- ddl-end --

-- object: group_fk | type: CONSTRAINT --
-- ALTER TABLE tester."UserToGroup" DROP CONSTRAINT IF EXISTS group_fk CASCADE;
ALTER TABLE tester."UserToGroup" ADD CONSTRAINT group_fk FOREIGN KEY ("groupId")
REFERENCES tester."Group" (id) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;
-- ddl-end --

-- object: user_fk | type: CONSTRAINT --
-- ALTER TABLE tester."UserToGroup" DROP CONSTRAINT IF EXISTS user_fk CASCADE;
ALTER TABLE tester."UserToGroup" ADD CONSTRAINT user_fk FOREIGN KEY ("userId")
REFERENCES tester."RegisteredUser" (id) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;
-- ddl-end --

-- object: group_fk | type: CONSTRAINT --
-- ALTER TABLE tester."CourseTemplateForGroup" DROP CONSTRAINT IF EXISTS group_fk CASCADE;
ALTER TABLE tester."CourseTemplateForGroup" ADD CONSTRAINT group_fk FOREIGN KEY ("groupId")
REFERENCES tester."Group" (id) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;
-- ddl-end --

-- object: custom_course_ref | type: CONSTRAINT --
-- ALTER TABLE tester."CustomCourseTemplateProblemAlias" DROP CONSTRAINT IF EXISTS custom_course_ref CASCADE;
ALTER TABLE tester."CustomCourseTemplateProblemAlias" ADD CONSTRAINT custom_course_ref FOREIGN KEY ("courseId")
REFERENCES tester."CustomCourseTemplate" (id) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;
-- ddl-end --

-- object: course_fk | type: CONSTRAINT --
-- ALTER TABLE tester."Problem" DROP CONSTRAINT IF EXISTS course_fk CASCADE;
ALTER TABLE tester."Problem" ADD CONSTRAINT course_fk FOREIGN KEY ("courseId")
REFERENCES tester."Course" (id) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;
-- ddl-end --

-- object: problem_fk | type: CONSTRAINT --
-- ALTER TABLE tester."Answer" DROP CONSTRAINT IF EXISTS problem_fk CASCADE;
ALTER TABLE tester."Answer" ADD CONSTRAINT problem_fk FOREIGN KEY ("problemId")
REFERENCES tester."Problem" (id) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;
-- ddl-end --


