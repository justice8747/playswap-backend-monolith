--
-- PostgreSQL database dump
--

-- Dumped from database version 17.0
-- Dumped by pg_dump version 17.0

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
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
-- Name: access_tokens; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.access_tokens (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    user_id uuid,
    access_token text NOT NULL,
    refresh_token text,
    provider character varying(50),
    expires_at timestamp without time zone NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.users (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    created_at timestamp without time zone DEFAULT now(),
    updated_at timestamp without time zone DEFAULT now(),
    spotify_id text,
    youtube_id text,
    email character varying(100),
    password text,
    is_google_user boolean DEFAULT false,
    is_guest boolean DEFAULT true
);


--
-- Data for Name: access_tokens; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.access_tokens (id, user_id, access_token, refresh_token, provider, expires_at, created_at) FROM stdin;
a67c05e6-0901-4409-b7a9-ff38eaf2a152	9121df4d-4686-4bba-95bd-efeaff3d9c2b	dummy_spotify_access_token	dummy_spotify_refresh_token	spotify	2026-02-19 07:05:13.955	2026-01-28 09:14:32.099778
ed16aea6-a6cc-4aad-ad6b-89c6ddc126e1	9121df4d-4686-4bba-95bd-efeaff3d9c2b	dummy_youtube_access_token	dummy_youtube_refresh_token	youtube	2026-02-19 07:05:12.97	2026-01-28 09:01:56.351813
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.users (id, created_at, updated_at, spotify_id, youtube_id, email, password, is_google_user, is_guest) FROM stdin;
9121df4d-4686-4bba-95bd-efeaff3d9c2b	2026-01-28 09:01:56.344946	2026-01-28 09:01:56.344946	316uofqrxyurpqsvd6h5aynrjqwq	105035616235361315106	\N	\N	f	t
\.


--
-- Name: access_tokens access_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.access_tokens
    ADD CONSTRAINT access_tokens_pkey PRIMARY KEY (id);


--
-- Name: access_tokens unique_user_provider; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.access_tokens
    ADD CONSTRAINT unique_user_provider UNIQUE (user_id, provider);


--
-- Name: users users_email_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: users users_spotify_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_spotify_id_key UNIQUE (spotify_id);


--
-- Name: users users_youtube_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_youtube_id_key UNIQUE (youtube_id);


--
-- Name: access_tokens access_tokens_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.access_tokens
    ADD CONSTRAINT access_tokens_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

