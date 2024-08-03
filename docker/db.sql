--
-- PostgreSQL database dump
--

-- Dumped from database version 16.2
-- Dumped by pg_dump version 16.2

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

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: groups; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.groups (
    id integer NOT NULL,
    name text NOT NULL,
    createdat timestamp without time zone NOT NULL
);


ALTER TABLE public.groups OWNER TO postgres;

--
-- Name: group_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.groups ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.group_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: members; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.members (
    id integer NOT NULL,
    userid integer NOT NULL,
    groupid integer NOT NULL
);


ALTER TABLE public.members OWNER TO postgres;

--
-- Name: members_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.members ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.members_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: messages; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.messages (
    id integer NOT NULL,
    groupid integer NOT NULL,
    sender integer NOT NULL,
    createdat timestamp without time zone NOT NULL,
    edited boolean NOT NULL,
    text text NOT NULL
);


ALTER TABLE public.messages OWNER TO postgres;

--
-- Name: messages_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.messages ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.messages_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: unreadmessages; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.unreadmessages (
    id integer NOT NULL,
    userid integer NOT NULL,
    messageid integer NOT NULL
);


ALTER TABLE public.unreadmessages OWNER TO postgres;

--
-- Name: unreadmessages_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.unreadmessages ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.unreadmessages_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    id integer NOT NULL,
    name text NOT NULL,
    password text NOT NULL,
    login text NOT NULL,
    createdat timestamp without time zone NOT NULL,
    loginedat timestamp without time zone NOT NULL,
    settings text
);


ALTER TABLE public.users OWNER TO postgres;

--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.users ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: usersblocked; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.usersblocked (
    id integer NOT NULL,
    userid integer NOT NULL,
    blockedid integer NOT NULL
);


ALTER TABLE public.usersblocked OWNER TO postgres;

--
-- Name: usersblocked_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.usersblocked ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.usersblocked_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: groups group_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.groups
    ADD CONSTRAINT group_pk PRIMARY KEY (id);


--
-- Name: members members_unique; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.members
    ADD CONSTRAINT members_unique UNIQUE (userid, groupid);


--
-- Name: messages messages_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.messages
    ADD CONSTRAINT messages_pk PRIMARY KEY (id);


--
-- Name: members newtable_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.members
    ADD CONSTRAINT newtable_pk PRIMARY KEY (id);


--
-- Name: unreadmessages unreadmessages_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.unreadmessages
    ADD CONSTRAINT unreadmessages_pk PRIMARY KEY (id);


--
-- Name: unreadmessages unreadmessages_unique; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.unreadmessages
    ADD CONSTRAINT unreadmessages_unique UNIQUE (userid, messageid);


--
-- Name: users users_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pk PRIMARY KEY (id);


--
-- Name: users users_unique; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_unique UNIQUE (login);


--
-- Name: usersblocked usersblocked_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usersblocked
    ADD CONSTRAINT usersblocked_pk PRIMARY KEY (id);


--
-- Name: usersblocked usersblocked_unique; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usersblocked
    ADD CONSTRAINT usersblocked_unique UNIQUE (userid, blockedid);


--
-- Name: members members_group_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.members
    ADD CONSTRAINT members_group_fk FOREIGN KEY (groupid) REFERENCES public.groups(id) ON DELETE CASCADE;


--
-- Name: members members_users_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.members
    ADD CONSTRAINT members_users_fk FOREIGN KEY (userid) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: messages messages_groups_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.messages
    ADD CONSTRAINT messages_groups_fk FOREIGN KEY (groupid) REFERENCES public.groups(id) ON DELETE CASCADE;


--
-- Name: messages messages_users_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.messages
    ADD CONSTRAINT messages_users_fk FOREIGN KEY (sender) REFERENCES public.users(id);


--
-- Name: unreadmessages unreadmessages_messages_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.unreadmessages
    ADD CONSTRAINT unreadmessages_messages_fk FOREIGN KEY (messageid) REFERENCES public.messages(id) ON DELETE CASCADE;


--
-- Name: unreadmessages unreadmessages_users_fk_1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.unreadmessages
    ADD CONSTRAINT unreadmessages_users_fk_1 FOREIGN KEY (userid) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: usersblocked usersblocked_users_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usersblocked
    ADD CONSTRAINT usersblocked_users_fk FOREIGN KEY (userid) REFERENCES public.users(id);


--
-- Name: usersblocked usersblocked_users_fk_1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usersblocked
    ADD CONSTRAINT usersblocked_users_fk_1 FOREIGN KEY (blockedid) REFERENCES public.users(id);


--
-- PostgreSQL database dump complete
--

