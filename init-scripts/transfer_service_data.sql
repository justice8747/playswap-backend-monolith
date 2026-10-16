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
-- Name: song_mappings; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.song_mappings (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    source_platform character varying(50) NOT NULL,
    source_track_id character varying(255) NOT NULL,
    destination_platform character varying(50) NOT NULL,
    destination_track_id character varying(255) NOT NULL,
    title character varying(255),
    artist character varying(255),
    confidence_score integer DEFAULT 0,
    last_used_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: transfer_items; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.transfer_items (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    job_id uuid,
    source_track_id character varying(255) NOT NULL,
    source_metadata jsonb,
    target_track_id character varying(255),
    target_metadata jsonb,
    status character varying(50) DEFAULT 'pending'::character varying,
    confidence_score integer DEFAULT 0,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: transfer_jobs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.transfer_jobs (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    session_id uuid,
    source_playlist_id character varying(255) NOT NULL,
    source_playlist_name character varying(255),
    target_playlist_id character varying(255),
    status character varying(50) DEFAULT 'pending'::character varying,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: transfer_sessions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.transfer_sessions (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    user_id uuid,
    source_platform character varying(50),
    target_platform character varying(50),
    status character varying(50) DEFAULT 'analyzing'::character varying
);


--
-- Name: transferhistory; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.transferhistory (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    user_id uuid,
    source_platform character varying(50),
    target_platform character varying(50),
    playlist_id text,
    status character varying(50)
);


--
-- Data for Name: song_mappings; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.song_mappings (id, source_platform, source_track_id, destination_platform, destination_track_id, title, artist, confidence_score, last_used_at, created_at) FROM stdin;
a093170c-bd89-40f5-9e15-94a18223a370	youtube	MD-EM4vApl4	spotify	5rTC3tKShDyYK0wk0rq96X	Running	Chike	100	2026-02-16 03:29:00.114813	2026-02-10 06:22:27.531146
0534d931-98f3-4a31-a44e-28ace1baf748	spotify	3GZD6HmiNUhxXYf8Gch723	youtube	J3DWAJGaf7o	Lost	{"Frank Ocean"}	100	2026-02-10 06:21:03.926286	2026-02-07 06:24:08.134091
b47288b7-62b9-4cc6-a342-c663aa3699e0	spotify	4PMqSO5qyjpvzhlLI5GnID	youtube	UB6AAoP67Fs	Good Days	{"SZA"}	90	2026-02-10 06:21:03.933531	2026-02-07 06:24:08.783974
fe7d34f4-53fd-4486-9c3b-04dc2662509e	spotify	4b7vk8SRcYgnxpk0JOIS7r	youtube	qz6lh2pVit8	Drugs You Should Try It	{"Travis Scott"}	90	2026-02-10 06:21:03.944577	2026-02-07 06:24:09.579453
a2432b78-78eb-4ce3-94ce-40960998ae71	spotify	6jdOi5U5LBzQrc4c1VT983	youtube	sbLqX7cUKoM	Hold On, We're Going Home	{"Drake","Majid Jordan"}	100	2026-02-10 06:21:03.951838	2026-02-07 06:24:11.143509
1f0b73b7-d6fd-46ce-b72e-557a6c2675cf	spotify	45XhKYRRkyeqoW3teSOkCM	youtube	G8-3EX34QvY	Wild Thoughts	{"DJ Khaled","Rihanna","Bryson Tiller"}	95	2026-02-10 06:21:03.962547	2026-02-07 06:24:12.606875
9b06eb82-c6c1-4ff0-85d6-08e219f238fe	spotify	5TRPicyLGbAF2LGBFbHGvO	youtube	ZAz3rnLGthg	Flashing Lights	{"Kanye West","Dwele"}	90	2026-02-10 06:21:03.969915	2026-02-07 06:24:13.250897
5ca2d699-f2bd-419a-93a2-4e1a038e3f6e	spotify	4wVOKKEHUJxHCFFNUWDn0B	youtube	ZzuXhUZUDyc	Chicago Freestyle	{"Drake","GIVĒON"}	90	2026-02-10 06:21:08.9023	2026-02-07 06:24:13.773427
4258fec7-d882-4e42-8059-cbc6fb1081fd	spotify	51EC3I1nQXpec4gDk0mQyP	youtube	h35g2e9aIIk	90210	{"Travis Scott","Kacy Hill"}	90	2026-02-10 06:21:08.933267	2026-02-07 06:24:14.474854
e1b5c0e9-028c-44a0-b2e3-f41ef36fb97f	spotify	4MXhiYIRDMGAuvZc5IFTwC	youtube	6iPjiNV9vEk	ASTROTHUNDER	{"Travis Scott"}	90	2026-02-10 06:21:08.948758	2026-02-07 06:24:15.388794
c09d5573-6a37-4c18-a6ed-cd001fd49849	spotify	4Wjhj0WjkyECccfHVIgaTq	youtube	87plLWRWNZA	Trust Issues	{"Drake"}	90	2026-02-10 06:21:08.984224	2026-02-07 06:24:20.401811
5bcc38fa-2e76-4e8c-a5c3-2f801935ae70	spotify	2b6rV6eUYrGE3uW1aGOJEw	youtube	9IPeQN3RcPE	Too Good To Be True	{"FYM ENT","Queen Izzy","Pizzo Da L.P"}	87	2026-02-10 06:21:09.00265	2026-02-07 06:24:21.195224
c17f4444-c025-4ff3-84e0-c84873ff98bb	spotify	4cQrSREMqBSvJ8ZzBZjVb8	youtube	PWPafG8wEvc	Mile High	{"James Blake","Travis Scott","Metro Boomin"}	87	2026-02-10 06:21:09.017734	2026-02-07 06:24:21.882842
32536dc9-9868-49d4-9d5d-9404c7b936e0	spotify	45J4avUb9Ni0bnETYaYFVJ	youtube	HfWLgELllZs	luther	{"Kendrick Lamar","SZA"}	87	2026-02-10 06:21:11.632976	2026-02-07 06:24:23.936301
648561b9-53b2-44ae-b0da-b2f8796801ae	spotify	37TOSNWwKVhHxO5GwKwIlk	youtube	7EG4H5qxRyk	Feel Good	{"okay coleman!"}	90	2026-02-10 06:21:11.641888	2026-02-07 06:24:22.870284
26565a2b-9bae-4591-870f-654ffec23aeb	spotify	2scd9lw4ljzDwMfG3WW4gD	youtube	qsYrpNHQ6hg	Who Do We Think We Are	{"John Legend","Rick Ross"}	87	2026-02-10 06:21:11.648631	2026-02-07 06:24:24.602156
64df1e31-93f2-4a81-9142-279d265d66db	spotify	59nOXPmaKlBfGMDeOVGrIK	youtube	mcN92dALZ_o	WAIT FOR U	{"Future","Drake","Tems"}	90	2026-02-10 06:21:11.664591	2026-02-07 06:24:26.583538
3c1d6856-6d5c-4de3-99cc-38435695e7aa	spotify	2qYbtIf8dRkBkKIcZVGFDq	youtube	djhkd1Xlqa0	Drive	{"SZA"}	90	2026-02-10 06:21:11.656647	2026-02-07 06:24:25.724116
ba124c64-deba-4484-b9e5-7cb6bd41698a	spotify	1gTANFZlqRitzVx7YSenzA	youtube	lsZRoypKIX8	White Dress	{"Kanye West"}	90	2026-02-10 06:21:11.671918	2026-02-07 06:24:27.149214
ba4f9b72-3bfd-43d5-9d9a-9093f97aaf00	spotify	0PJIbOdMs3bd5AT8liULMQ	youtube	cstIR7VI7Ak	oui	{"Jeremih"}	87	2026-02-10 06:21:11.679672	2026-02-07 06:24:27.672311
c1bc4d48-ae65-4b5d-bfe8-53218c3c349b	spotify	0XO2hckt5aHvvwH7FFdVYF	youtube	C_KkyUAl-Z8	Drama	{"Roy Woods","Drake"}	87	2026-02-10 06:21:11.688281	2026-02-07 06:24:28.469165
48b1d983-b4b6-4c75-856e-9f708381fbc5	spotify	14PBM2V8PjSXWye927FUtL	youtube	CJCINYVapXA	4AM	{"Nino Uptown"}	87	2026-02-10 06:21:11.695524	2026-02-07 06:24:28.97937
23ddfc6c-1aca-459c-8f98-f44a9f643b1a	spotify	5wUUWVRvrciJqFitZF8R0P	youtube	xYAvW3bmRD0	Recognize	{"PARTYNEXTDOOR","Drake"}	87	2026-02-10 06:21:11.70432	2026-02-07 06:24:29.513804
ad7d8899-7521-4dd2-992f-8cfa52800e3f	spotify	7H5Bv8j9chJ4CWTuoXF4EK	youtube	7XBBU2fFbIY	Different Species	{"Offset","Gunna"}	87	2026-02-10 06:21:14.098529	2026-02-07 06:24:33.836856
db308416-369f-4bd3-8fc2-a9f46a05a9a4	spotify	4qPhFTSrBnBJyUDGj12KAc	youtube	FF8okFt4bGg	Indecision	{"Sampha"}	87	2026-02-10 06:21:14.10605	2026-02-07 06:24:34.366987
19d12d01-b3d5-4fd3-bf41-8e1844ab9b6c	spotify	7fBv7CLKzipRk6EC6TWHOB	youtube	G5XpJP7f_SE	The Hills	{"The Weeknd"}	90	2026-02-10 06:21:14.113908	2026-02-07 06:24:35.051926
cef14c40-01b6-49d9-b7ba-89d0e8b1073d	spotify	53WWD2BBC6Jcip8wy5CniE	youtube	eUsQzDa7qyU	cLOUDs	{"J. Cole"}	90	2026-02-10 06:21:14.123256	2026-02-07 06:24:35.852756
133f5fd9-dd9a-47dd-9b40-1655020695bf	spotify	2Gyc6e2cLxA5hoX1NOvYnU	youtube	MJZ-HAJDONw	Say It	{"Tory Lanez"}	87	2026-02-10 06:21:14.132063	2026-02-07 06:24:36.663917
8ae5c352-c9fb-4144-99d1-170d9d3513e5	spotify	0Hlv1Wzw8iXH6qUnlygVZE	youtube	fz4bRC5Xsfk	baddays	{"JaWill"}	87	2026-02-10 06:21:14.141812	2026-02-07 06:24:37.38081
0927c75f-b9a5-4adf-b1b5-f10c252c904a	spotify	1KwIDHEK3CYbHPJUTMxxNu	youtube	exfNMKltGKg	Do You Have A Destination?	{"Mac Miller"}	87	2026-02-10 06:21:14.14752	2026-02-07 06:24:38.20215
2ea0e580-6669-4070-8524-d14e86cfd647	spotify	3ym9gIBrWP3LKjiH2kxyr5	youtube	ACaWQdMDyG0	GREY	{"Saba"}	84	2026-02-10 06:21:14.162514	2026-02-07 06:24:38.798373
6968648e-17a2-4ea1-a427-5afe2aa9ea5d	spotify	3XgGQ1wjo5khvq2UImjyNF	youtube	sqgerBkCCWM	ALL MINE	{"Brent Faiyaz"}	87	2026-02-10 06:21:14.169174	2026-02-07 06:24:39.679235
173a43f6-b1af-4359-9834-6eb75943e4c7	spotify	6uvX1SaTHs2j7x8XImLcro	youtube	Br8p20iiAlg	the bullet you called home	{"Kleia Sage"}	100	2026-02-10 06:21:14.178963	2026-02-07 06:24:41.313956
1b771619-1fbe-446d-8900-6b5b14658a42	spotify	4qNV5FkgIlovZzZe7jo55q	youtube	NLw5hoe3OFY	Rise	{"Biancallove"}	84	2026-02-10 06:21:14.18491	2026-02-07 06:24:41.870283
43087bbd-154d-42cc-8f30-eb4373621fa6	spotify	0NUqi0ps17YpLUC3kgsZq0	youtube	MZOY9xLD5oo	DIE TRYING	{"PARTYNEXTDOOR","Drake","Yebba"}	90	2026-02-10 06:21:14.194444	2026-02-07 06:24:42.480078
e80397f1-8f14-4437-bf57-898ebbfe9c19	spotify	43PuMrRfbyyuz4QpZ3oAwN	youtube	LXWoYPx9fBU	Exchange	{"Bryson Tiller"}	90	2026-02-10 06:21:14.201004	2026-02-07 06:24:43.148846
3a6b42ad-44dd-4da7-becf-4d73681f36df	spotify	1I1Son2KONbLjUlnjgs9Yc	youtube	KSy_gu8dKnI	stack these bills	{"Saint Raphael"}	87	2026-02-10 06:21:14.208934	2026-02-07 06:24:43.746689
6c54693b-92fd-4541-b0ff-90f11a1755fa	spotify	1AHtLZpjQvz77ATlkXxP8V	youtube	A-KjPkdtdD4	elastic	{"Garrett Zoukis","ADÉ"}	87	2026-02-10 06:21:14.215806	2026-02-07 06:24:44.187141
ffa74282-995d-4e14-9b73-6de21e6e1f0d	spotify	4iMzpYxUO5RiZkGmNKRCMh	youtube	dT_AvPxGAOA	Kody Blu 31	{"JID"}	90	2026-02-10 06:21:14.226927	2026-02-07 06:24:44.803858
fa8f3e3a-b6b5-49cb-9c3b-1eb9d6a98ca1	spotify	5a0fB9A7P0NWS8ovlrThbr	youtube	90rDFh7XsyQ	Emotionless	{"Cole Fisher"}	90	2026-02-10 06:21:14.233922	2026-02-07 06:24:45.863592
6ab80ff2-df83-4d1b-bca2-7ae2f122526c	spotify	17uMdIXoCfzHidMJEo3bqi	youtube	g_FQXop__vU	Long Way	{"Nelchy"}	87	2026-02-10 06:21:14.243543	2026-02-07 06:24:46.411266
809146d7-86c1-4a72-a9ba-8821e884d5cd	spotify	0q75NwOoFiARAVp4EXU4Bs	youtube	Ar6u90btpJQ	Love Galore	{"SZA","Travis Scott"}	90	2026-02-10 06:21:14.251006	2026-02-07 06:24:47.085809
52509841-8af1-4da1-96dc-f78390379b40	spotify	1SGvjfc85yzqKXsfKcCxn2	youtube	wIz-a0qNDtE	heart pt. 6	{"Kendrick Lamar"}	90	2026-02-10 06:21:14.258649	2026-02-07 06:24:47.777063
96ae560e-2cc4-4cfb-b887-3996c24e3713	spotify	4YdRnTnXYwgI48kkzWy6uk	youtube	Uc7XoL25AIA	BUSY / SIRENS	{"Saba","theMIND"}	82	2026-02-10 06:21:14.264916	2026-02-07 06:24:48.331401
6e692825-ce8a-4577-809c-b22a88adf12e	spotify	3iNKi8zJTEpIeMBNaFY3y3	youtube	hPVSR50O0oo	Swervin	{"Raina Simone"}	87	2026-02-10 06:21:14.271964	2026-02-07 06:24:49.255017
283c630b-1bc7-4a8f-94ae-dd711c2aeafd	spotify	2J6OF7CkpdQGSfm1wdclqn	youtube	FFu_lJ3EvAA	Clouded	{"Brent Faiyaz"}	87	2026-02-10 06:21:14.278948	2026-02-07 06:24:49.865028
4e18cfdf-a2ba-43b0-b160-d46c583a44bb	spotify	4Hqh0dS4x07zuRw6eBTO7p	youtube	yM2QRNtkGDg	20/20	{"Lil Tjay"}	87	2026-02-10 06:21:14.286987	2026-02-07 06:24:50.668909
01c2183e-80b1-4505-978f-ee881ab0a5e8	spotify	66kQ7wr4d2LwwSjr7HXcyr	youtube	JQbjS0_ZfJ0	All The Stars	{"Kendrick Lamar","SZA"}	87	2026-02-10 06:21:03.884331	2026-02-07 06:24:02.920527
570f2b45-2c9b-4983-87a9-a861e6f2f91b	spotify	5rgrBsAFYMun6yhtnLKRPz	youtube	c4M9U6D3uKI	Right My Wrongs	{"Bryson Tiller"}	100	2026-02-10 06:21:03.899236	2026-02-07 06:24:04.793299
f43be803-78e9-4bac-9a2f-14e718b0a0a5	spotify	7zFXmv6vqI4qOt4yGf3jYZ	youtube	xyUQKDE57Ag	Get You	{"Daniel Caesar","Kali Uchis"}	87	2026-02-10 06:21:03.91013	2026-02-07 06:24:05.759383
6bc8918e-32fd-41ff-921f-15885c9857b6	spotify	3B54sVLJ402zGa6Xm4YGNe	youtube	LkvAjalYnWw	Unforgettable	{"French Montana","Swae Lee"}	90	2026-02-10 06:21:03.918782	2026-02-07 06:24:06.493678
c3557470-1fcd-45e9-9f00-80fb4f2e6b64	spotify	3azJifCSqg9fRij2yKIbWz	youtube	07fhkAoCnig	The Color Violet	{"Tory Lanez"}	87	2026-02-10 06:21:14.33062	2026-02-07 06:24:54.891718
cc6a224d-9335-45bc-9255-dd50af5a7b12	spotify	2WC4sK0ryyysQhtDok9Ytr	youtube	CikjiSG8eRM	From Time	{"Drake","Jhené Aiko"}	90	2026-02-10 06:21:14.33822	2026-02-07 06:24:55.596591
f65ad04e-cd95-47ff-8295-7fdbb543b6cf	spotify	2xbI8Vmyv3TkpTdywpPyNw	youtube	u4XyXLQxQbQ	Selfish	{"PnB Rock"}	90	2026-02-10 06:21:14.345742	2026-02-07 06:24:56.366941
ee0bd863-f11f-49b2-9088-c90ea15eafe3	spotify	5kYSScXYOZYyi0fxramiW6	youtube	GNhq1xCWs8M	BEAUTIFUL	{"DJ Khaled","Future","SZA"}	90	2026-02-10 06:21:14.353555	2026-02-07 06:24:57.011506
e1693282-2019-450b-9b52-95884e16839a	spotify	2fXwCWkh6YG5zU1IyvQrbs	youtube	XueuILZeosI	Broken Clocks	{"SZA"}	90	2026-02-10 06:21:14.36062	2026-02-07 06:24:57.753446
7640929c-80c4-412b-9382-88e7aeef1a85	spotify	2KvHC9z14GSl4YpkNMX384	youtube	zhY_0DoQCQs	Do Not Disturb	{"Drake"}	90	2026-02-10 06:21:14.368069	2026-02-07 06:24:58.344195
69375cc5-12a0-4541-ae6a-d7d5f63c751a	spotify	2g8HN35AnVGIk7B8yMucww	youtube	stKOaz60VS8	Big Poppa	{"The Notorious B.I.G."}	87	2026-02-10 06:21:14.378085	2026-02-07 06:24:59.067018
c9c087db-017e-4b7f-a65a-829351cd3e77	spotify	6PGoSes0D9eUDeeAafB2As	youtube	w6NxHj3L_XY	LOVE. FEAT. ZACARI.	{"Kendrick Lamar","Zacari"}	87	2026-02-10 06:21:14.385338	2026-02-07 06:24:59.725039
c9e451b2-b760-44db-94b0-2c204e090172	spotify	3xby7fOyqmeON8jsnom0AT	youtube	NnOH3_NAw34	Nightcrawler	{"Travis Scott","Swae Lee","Chief Keef"}	90	2026-02-10 06:21:14.393832	2026-02-07 06:25:00.333906
f4928d2d-7cbb-421a-84d4-93a2d456a6e2	spotify	2e3Ea0o24lReQFR4FA7yXH	youtube	6tjlU4w4fSo	Love Yourz	{"J. Cole"}	90	2026-02-10 06:21:14.400475	2026-02-07 06:25:01.014482
3e7b1a0f-d011-4062-a171-01b0f8e679db	spotify	3QLjDkgLh9AOEHlhQtDuhs	youtube	VWOc39wC5-8	Best I Ever Had	{"Drake"}	90	2026-02-10 06:21:14.410072	2026-02-07 06:25:01.649274
1183f75c-3492-4e4f-9690-b82953e2fa33	spotify	2uwnP6tZVVmTovzX5ELooy	youtube	OGcYLf-g_oM	Power Trip	{"J. Cole","Miguel"}	90	2026-02-10 06:21:14.417087	2026-02-07 06:25:02.329624
4e0a9d8c-8ecf-4e0f-8acb-7861b6eca831	spotify	2P3SLxeQHPqh8qKB6gtJY2	youtube	XWQJdnmpnhc	Poetic Justice	{"Kendrick Lamar","Drake"}	90	2026-02-10 06:21:14.426719	2026-02-07 06:25:03.200419
00b04f39-b0c5-4613-9647-037012b66f1c	spotify	4n4BflhWjCHIxrI4v7Xt9s	youtube	Uh78jXB7h3A	How Bout Now	{"Drake"}	90	2026-02-10 06:21:14.434879	2026-02-07 06:25:03.843835
c23740e7-b031-4aca-875c-9d316422e7e0	spotify	4TSDz7xZtxIXeKwiOuCe25	youtube	TtVhMHLmAow	Me U & Hennessy	{"DeJ Loaf","Lil Wayne"}	100	2026-02-10 06:21:14.443421	2026-02-07 06:25:05.515019
666c9a48-7135-4f12-b0d2-ed91d2344c0a	spotify	14SaZBTjxlorHJQxXh01Hu	youtube	TP3moIiyZFI	Girls Need Love	{"Summer Walker","Drake"}	87	2026-02-10 06:21:14.450637	2026-02-07 06:25:06.227167
09ea0d38-7342-4b84-9865-5e4276f5ac80	spotify	1OHoBC4icbuNhpSxP400sv	youtube	lOhZl4tD744	Preach	{"Drake","PARTYNEXTDOOR"}	90	2026-02-10 06:21:14.46015	2026-02-07 06:25:06.711338
25ac02b3-234e-489b-a187-bf77833b6ed5	spotify	5nkUIVKqOqdpB6ApKgEMkv	youtube	Dv7OVwv46YY	Stay Ready	{"Jhené Aiko","Kendrick Lamar"}	87	2026-02-10 06:21:14.467066	2026-02-07 06:25:07.19336
921a929f-cc2f-4df6-af03-cff4da4cb386	spotify	5c3luoCub1Z1Gg74NdNtuq	youtube	nXvpQjRnWw4	Belong to the City	{"PARTYNEXTDOOR"}	90	2026-02-10 06:21:14.475532	2026-02-07 06:25:07.772408
fe79dcfb-aa53-445c-a329-c4eb9dc62709	spotify	11pEKMLmavDu8fxOB5QjbQ	youtube	gZWFi96plXk	Pipe Down	{"Drake"}	90	2026-02-10 06:21:14.48117	2026-02-07 06:25:08.483087
6c8453a2-efdb-4007-a6a2-3adb1b01eb77	spotify	5nzhL67GpF9szXWg5y509A	youtube	hhzYbExfVIY	Close Friends	{"Lil Baby"}	84	2026-02-10 06:21:14.487859	2026-02-07 06:25:09.654341
0b7186ef-6f00-431b-8429-555127634560	spotify	6eT7xZZlB2mwyzJ2sUKG6w	youtube	UXTEJhIr6IY	White Iverson	{"Post Malone"}	90	2026-02-10 06:21:14.493974	2026-02-07 06:25:10.331819
1406c3a8-ced7-4bfe-8118-7d83292218c0	spotify	5SkRLpaGtvYPhw02vZhQQ9	youtube	W0VnPiyXSRQ	All Falls Down	{"Kanye West","Syleena Johnson"}	90	2026-02-10 06:21:14.500427	2026-02-07 06:25:11.078167
689306e6-0e94-41d5-90fb-8e5008cd5fdc	spotify	4jTiyLlOJVJj3mCr7yfPQD	youtube	poYUzSVlNEE	This Could Be Us	{"Rae Sremmurd"}	90	2026-02-10 06:21:14.508879	2026-02-07 06:25:11.681908
8f8a3242-5140-47ed-a691-5bd5fb10ebc7	spotify	410TZrK18uRjtsTunG14cl	youtube	9ny56n0rXE4	Middle of the Ocean	{"Drake"}	90	2026-02-10 06:21:14.515363	2026-02-07 06:25:12.39513
9b0b655a-749f-4157-91bb-03d8019aab2b	spotify	4FRW5Nza1Ym91BGV4nFWXI	youtube	96o83m9Cr6I	Lemon Pepper Freestyle	{"Drake","Rick Ross"}	90	2026-02-10 06:21:14.522425	2026-02-07 06:25:12.892988
db0a7eab-0ebf-4e92-8523-7b320357f2cc	spotify	6qBFSepqLCuh5tehehc1bd	youtube	IHp5Y4S3IJQ	Like I Want You	{"GIVĒON"}	100	2026-02-10 06:21:14.528464	2026-02-07 06:25:15.521342
ef98001b-8e14-44dc-a72f-df16953d472c	spotify	7dK9JJXrbDFc29n23BqUso	youtube	9Hy_OCiXbtE	Fall Thru	{"Meek Mill"}	100	2026-02-10 06:21:14.534476	2026-02-07 06:25:16.841875
acef6615-44e9-443b-a2ea-d2b199b8b4d5	spotify	1zi7xx7UVEFkmKfv06H8x0	youtube	FOqKN-ouAUE	One Dance	{"Drake","Wizkid","Kyla"}	84	2026-02-10 06:21:14.543082	2026-02-07 06:25:17.413726
f74e6870-a8b1-4682-b46b-f9877c049881	spotify	3s7MCdXyWmwjdcWh7GWXas	youtube	dTcyiwCbXSE	Violent Crimes	{"Kanye West"}	90	2026-02-10 06:21:14.548903	2026-02-07 06:25:18.021776
1f6cc292-30df-4713-89d6-baf148fd20ab	spotify	07KXEDMj78x68D884wgVEm	youtube	iGU66wsjIPA	High Fashion	{"Roddy Ricch","Mustard"}	87	2026-02-10 06:21:14.556021	2026-02-07 06:25:18.751985
79e3fa65-84f4-4628-b298-3d0213434b73	spotify	2FTeQQ5Rg9tBpgpTF5mnhE	youtube	VvBBcfZW8ho	Still Think About You	{"A Boogie Wit da Hoodie"}	100	2026-02-10 06:21:14.562583	2026-02-07 06:25:20.103799
d58b8c8d-1120-4382-b8d4-7dc47ebbf2e1	spotify	0At2qAoaVjIwWNAqrscXli	youtube	hCR3UbyQ9jc	Boo'd Up	{"Ella Mai"}	87	2026-02-10 06:21:14.568399	2026-02-07 06:25:20.773339
a62902ed-5bcb-4a65-99ae-e0a69e0ed48d	spotify	6REbwUNlppTfcnV4d4ZoZi	youtube	T_OWvLDIyno	Swimming Pools	{"Kendrick Lamar"}	95	2026-02-10 06:21:14.57711	2026-02-07 06:25:22.8712
872c5376-1ec2-4b77-97e0-b4f35c563192	spotify	5Psnhdkyanjpgc2P8A5TSM	youtube	w4MSbajRs_Y	Emotionless	{"Drake"}	90	2026-02-10 06:21:14.583414	2026-02-07 06:25:23.57264
c36fd0ea-c752-4453-bf84-df9965240145	spotify	3ESSGgWzRf1xvP7G5hHMhB	youtube	Cq74Ct7sc3o	Again	{"Fetty Wap"}	84	2026-02-10 06:21:14.592292	2026-02-07 06:25:24.205634
4f7ddfea-5a90-445c-8a1a-d8ca395e16fe	spotify	3O8NlPh2LByMU9lSRSHedm	youtube	PsW85VbSau0	Controlla	{"Drake"}	84	2026-02-10 06:21:14.598631	2026-02-07 06:25:24.773784
ddb105dc-249e-4538-bf35-25d15dc9010a	spotify	03w1u0L5fS0F7izSUkdqS2	youtube	uzANdtLhDUI	Feelins	{"PnB Rock"}	87	2026-02-10 06:21:14.605601	2026-02-07 06:25:25.405165
0adbae06-a6c5-4b0a-87a0-3d8a205c1850	spotify	2onCHFrhTinyUc6OawwU4Z	youtube	ky0A7XE2n6Y	Rich $ex	{"Future"}	90	2026-02-10 06:21:14.613059	2026-02-07 06:25:26.015775
3b984370-5d2c-4196-98fd-1ceb5dada8b2	spotify	206QiQ5OnKTZE3cnuMYu3H	youtube	FChdSKn0S9o	Lost One	{"JAY-Z","Chrisette Michele"}	95	2026-02-10 06:21:14.618337	2026-02-07 06:25:27.756593
55d59174-0b55-406f-a48a-a393109d4843	spotify	3pXF1nA74528Edde4of9CC	youtube	Yy4aGQF-p4g	Don't	{"Bryson Tiller"}	90	2026-02-10 06:21:14.626059	2026-02-07 06:25:28.692723
644ee3a6-7fd6-4ad2-9578-09c72ff95144	spotify	5QDGKcQLqOJvoiu8eScEaM	youtube	wU5Uhwj3zWg	Let Em' Know	{"Bryson Tiller"}	90	2026-02-10 06:21:14.63151	2026-02-07 06:25:29.211873
7ab1d020-a326-4211-8258-f1b686e15452	spotify	7JXZq0JgG2zTrSOAgY8VMC	youtube	AfRdRXCo3IU	Jungle	{"Drake"}	90	2026-02-10 06:21:14.637819	2026-02-07 06:25:29.75797
c6ba6f30-5b1e-4bfc-ba93-147f84030e66	spotify	1eUGmzzvahJjOSWgDHuRlv	youtube	_8YI4_o2oqM	Girl With The Tattoo Enter.lewd	{"Miguel"}	100	2026-02-10 06:21:14.644178	2026-02-10 05:09:28.366315
f96683cf-1744-4e68-af22-35595a3da923	spotify	7sPdi32gNLe1K8fpnr96mq	youtube	-DKPLrQWeaU	on my own	{"Leftii"}	87	2026-02-10 06:21:14.156185	2026-02-10 05:09:23.148887
e9a5243c-29ab-430e-bb93-74cf46bb5f2d	spotify	6Ius4TC0L3cN74HT7ENE6e	youtube	lTgvsJY4aYE	Wet Dreamz	{"J. Cole"}	90	2026-02-10 06:21:14.297321	2026-02-07 06:24:51.414581
83f25cd2-40b3-4845-b25b-2738c97d420c	spotify	5fEB6ZmVkg63GZg9qO86jh	youtube	eG3zE_rafJs	Break from Toronto	{"PARTYNEXTDOOR"}	90	2026-02-10 06:21:14.323133	2026-02-07 06:24:54.19981
522be305-6285-4563-a836-9b844489a2ab	spotify	6dFn6my1sHK2bcf23GlHwM	youtube	6B3YwcjQ_bU	2009	{"Mac Miller"}	87	2026-02-10 06:21:14.314888	2026-02-07 06:24:53.599611
3df06e3e-6828-446d-9f8e-2669c682ee8e	spotify	5uZm7EFtP5aoTJvx5gv9Xf	youtube	OoNedeRoflQ	Jungle	{"A Boogie Wit da Hoodie"}	100	2026-02-10 06:21:14.306344	2026-02-07 06:24:52.946663
66e7af82-5424-45cc-a1d7-e59bf2cc1f56	spotify	4BSR9I4ExlCJdXJo2GpBD5	youtube	JPOjiXoPmOk	Take Your Mask Off	{"Tyler, The Creator","Daniel Caesar","LaToiya Williams"}	90	2026-02-10 06:21:11.711442	2026-02-07 06:24:30.11378
ddbd7631-6144-40b1-963d-d0efa389e675	spotify	3i0J9e00YhlrKO7AqgdO8Z	youtube	A2-aUNmYNLM	She Used to Be Mine	{"Jessie Mueller"}	100	2026-02-19 05:06:21.544834	2026-02-07 06:22:55.052898
2da619b3-7c6f-46d3-9455-931984572c4c	spotify	1ia019RqDK2o4QiANR1Dyn	youtube	wp9G24LA0sA	Moment 4 Life	{"Nicki Minaj","Drake"}	90	2026-02-10 06:21:14.656602	2026-02-10 05:09:29.569757
67c348dd-f412-4396-9e5c-61db646978c0	spotify	0Doa5K0ifq5RgdTWngqoYS	youtube	NzEYlhJbTbE	Chip On My Shoulder	{"Rod Wave"}	83	2026-02-10 06:21:14.663302	2026-02-10 05:14:38.276186
1af9249f-51e8-4753-b288-7bee72cce939	spotify	0NHvTrD8Igke3fawcp3njY	youtube	1YEZxCNeiMI	Wus Good / Curious	{"PARTYNEXTDOOR"}	90	2026-02-10 06:21:14.649429	2026-02-10 05:09:28.951045
347cc07b-b4f1-4ff2-a83e-20c93a387a29	spotify	5qPfvvKDCoXG3rzgztsJQG	youtube	g4aHiYt2ubU	Ribbon In The Sky	{"Rod Wave"}	86	2026-02-10 06:21:14.668778	2026-02-10 05:14:39.004621
9a3441cd-c6e4-4dce-8d38-231bc402f967	spotify	4o4wEDRqotccDTXiQ7TORu	youtube	HhuGQUZJot8	Un	{"Alicia Keys","Drake"}	87	2026-02-10 06:21:14.676177	2026-02-10 05:14:39.626448
58bda96e-b2ea-4477-9d47-cace2185c037	spotify	0oufSLnKQDoBFX5mgkDCgR	youtube	pa5E4uA3ALY	Trust	{"Brent Faiyaz"}	87	2026-02-10 06:21:14.682163	2026-02-10 05:14:40.263771
5e873dfe-6b74-4fba-910a-c879f26985c9	spotify	3f95xQ6yw4S4ISl1zC7QSx	youtube	vLzREYJ1ucA	Documentary	{"YFN Lucci","Bigga Rankin"}	87	2026-02-10 06:21:14.698186	2026-02-10 05:14:42.561775
d902b11d-a136-4a53-83cc-56003faa222d	spotify	4IdAIZRVkwuQQnHpi529cn	youtube	9fT-o9J6-DQ	No Longer Friends	{"Bryson Tiller"}	90	2026-02-10 06:21:14.70849	2026-02-10 05:14:43.429346
096b5073-8bc6-47bf-8a51-9783455df5ef	spotify	5g30WoqK1Dv5Bnwpusp2vk	youtube	o8isoCFkCOA	For However Long	{"Bryson Tiller"}	90	2026-02-10 06:21:14.714994	2026-02-10 05:14:44.068101
60c45f45-3728-4441-87f4-65a6c3719e38	spotify	4YPUpGTxrGvnkwUCljlBpO	youtube	rQBftVxLJGI	Nonchalant	{"A Boogie Wit da Hoodie","Alkaline"}	87	2026-02-10 06:21:14.723195	2026-02-10 05:14:44.699835
c0fd19f1-0816-4650-a5bf-f4635642171d	spotify	2c1jezYXHqGN8C0AJhIE1S	youtube	Nn_md6We3V8	Make a Mil	{"PARTYNEXTDOOR"}	90	2026-02-10 06:21:14.730895	2026-02-10 05:14:45.289301
003fb0cb-97cd-4617-a4e6-9eca90662d67	spotify	5JUu0unA8VwhTZ9LkMWUVI	youtube	-I8oNLTQL-Q	Since Way Back	{"Drake","PARTYNEXTDOOR"}	90	2026-02-10 06:21:14.738966	2026-02-10 05:14:46.026055
89f099f3-b578-4887-aa5d-da087ac374cc	spotify	3JEJwjqLkg2Jbau5922CAb	youtube	1Eops1caFVM	Her Way	{"PARTYNEXTDOOR"}	90	2026-02-10 06:21:14.746773	2026-02-10 05:14:46.610927
a4d5bd80-bc3d-4d0c-9783-63f5804a0ad3	spotify	725fdnXEqS0Xs1sTvdNuHq	youtube	gDTtys70v0A	Ten Nine Fourteen	{"Bryson Tiller"}	90	2026-02-10 06:21:14.754204	2026-02-10 05:14:47.360488
16bd6c19-207f-4b26-b306-c1041caae481	spotify	3U3gHqRwjam6CxwW68m30z	youtube	6OQrEP0eMX4	Resume	{"Lil Tjay"}	100	2026-02-10 06:21:14.760751	2026-02-10 05:14:48.824736
59d03420-6562-4960-b882-55422236a134	spotify	4HISFDmENrT1reZ5nMUe4l	youtube	F-dWu9RhHwA	R e s e n t m e n t	{"PARTYNEXTDOOR"}	100	2026-02-10 06:21:14.766425	2026-02-10 05:14:50.319032
f9359743-145d-4c26-8a34-8a49b8529aef	spotify	56NDFbD0tCUawnqeU2wcvv	youtube	eC3_272UNYM	Girls Love Beyoncé	{"Drake","James Fauntleroy"}	90	2026-02-10 06:21:14.773621	2026-02-10 05:14:50.878641
fd6e34cc-5471-4071-8d20-9623b4eaf4e7	spotify	6LGwYMXXgURfaequXipzHx	youtube	_ST6ZRbhGiA	You Don't Know My Name	{"Alicia Keys"}	87	2026-02-10 06:21:14.779848	2026-02-10 05:14:51.471524
fe85b12b-f596-4b1f-aef9-20a0a924d17f	spotify	6cEguiQecbXrFlsnMi2ysr	youtube	6SA7X0ZuxpI	Come and See Me	{"PARTYNEXTDOOR","Drake"}	87	2026-02-10 06:21:14.784862	2026-02-10 05:14:52.155707
5304ad70-b6e6-484e-a66f-12c167e06cf5	spotify	4sMmYKC0ot3GTbl2RzHw7T	youtube	mQFjaRHye04	And This is Just The Intro	{"Tory Lanez"}	87	2026-02-10 06:21:14.792425	2026-02-10 05:14:52.794312
42187c31-f0af-4d38-8558-1511ed5913a5	spotify	2pg2TiYo9Rb8KeB5JjP7jS	youtube	3tnb2o-cV_0	Sativa	{"Jhené Aiko","Swae Lee"}	87	2026-02-10 06:21:14.798301	2026-02-10 05:14:53.304628
4710d59e-8df0-44b2-a264-9668febb786b	spotify	43xzI2dp9CTUsmZXjm1voH	youtube	Hg8jHpsYXQI	Over Here	{"PARTYNEXTDOOR","Drake"}	87	2026-02-10 06:21:14.804379	2026-02-10 05:14:53.87009
c3f54f77-e878-4a7e-96bc-117469153d35	spotify	2wBCrtJS3E3TimRZ5MElTI	youtube	zpoAMCwzFBA	The Worst	{"Jhené Aiko"}	90	2026-02-10 06:21:14.810956	2026-02-10 05:14:54.603733
ca7f22ce-37cd-4050-ad66-ec87a839beca	spotify	7umZiitjVsEjMQ6HNddpUI	youtube	mHM8Qa6zsVg	Love Is Only a Feeling	{"Joey Bada$$"}	100	2026-02-10 06:21:14.81651	2026-02-10 05:14:56.201605
eb8ca2ca-8f63-4a70-95a7-f48f6b5d80cb	spotify	7t1lBIr3WIEtqQEOdZFMUf	youtube	5H7CS-_RUe8	Look What You've Done	{"Drake"}	90	2026-02-10 06:21:14.822389	2026-02-10 05:14:56.806616
e655d091-9d45-4a1c-be19-a596f055ea85	spotify	4louS1zw5coBPLDyJLD5Sm	youtube	HBhO9hsRvvI	WEST DISTRICT	{"PARTYNEXTDOOR"}	100	2026-02-10 06:21:14.828353	2026-02-10 05:14:57.967403
cf7e5d33-8cd3-45b2-a2db-0d51fdf102ee	spotify	6oEVnWKgPqIEPc53OYDNqG	youtube	DQxmfHaaph0	Could've Been	{"H.E.R.","Bryson Tiller"}	87	2026-02-10 06:21:14.83377	2026-02-10 05:14:59.015271
4d661f01-210d-4c1f-9069-078d619592ab	spotify	2Gnsof1hvZzjE1xdLRpjtf	youtube	PMk8L9FNqnY	Over My Dead Body	{"Drake"}	90	2026-02-10 06:21:14.84113	2026-02-10 05:14:59.584386
57b90ae6-b403-426b-bc00-8dc580615f72	spotify	7ry34dFOuoK2c5GoYWWsl3	youtube	2lct2Tzo3-E	Nostalgia	{"Rod Wave","Wet"}	86	2026-02-10 06:21:14.847179	2026-02-10 05:15:00.121471
bb66ba07-4922-48bb-ad3b-2b32520cfdc8	spotify	6oLHyWvmk6bKrA91EIYZBp	youtube	Fig9ZBQaXK8	Wednesday Night Interlude	{"Drake","PARTYNEXTDOOR"}	90	2026-02-10 06:21:14.855143	2026-02-10 05:15:00.709578
a80c280b-726c-443b-88e4-0438b1504d59	spotify	3FM9mryo6yae4MogbZnd1A	youtube	7y65ieEuitE	Wishing	{"DJ Drama","Skeme","Lyquin","Chris Brown"}	81	2026-02-10 06:21:14.862141	2026-02-10 05:15:01.2113
0920b273-86c1-4448-8ee3-020a36515e95	spotify	4mwiRPRAUSSFD6lJ86m98B	youtube	l87PDUJRpAs	Stay Down	{"Brent Faiyaz"}	90	2026-02-10 06:21:14.867924	2026-02-10 05:15:01.704225
306c0c15-674e-4810-bdff-15da4dbe0a81	spotify	1A8RGlqkdjAfwkXtEgJ7wR	youtube	8-YJIK-NWAY	Sex on the Beach	{"PARTYNEXTDOOR"}	90	2026-02-10 06:21:14.876141	2026-02-10 05:15:02.250754
ac0cae37-f669-4879-b40c-1944325dd288	spotify	0w3Mfe4PIVjuFjJbe3OlYv	youtube	srtJIha2qjI	8am in Charlotte	{"Drake"}	90	2026-02-10 06:21:14.882385	2026-02-10 05:15:02.889362
86c57490-ab93-4fbe-8bb7-f63f4b87f8c9	spotify	3TOfcOKNuu6ZPZXJie5msH	youtube	QsyaN-QVVUQ	Canceled	{"Bryson Tiller"}	100	2026-02-10 06:21:14.890616	2026-02-10 05:15:04.580412
df5bb152-c762-42a0-898b-946f56743cec	spotify	5hJFhO9dvhJoDvUZZ9iWSw	youtube	7RBvizK9TlI	Adorn	{"Miguel"}	81	2026-02-10 06:21:14.898046	2026-02-10 05:15:05.076926
defd3be2-bbf6-4616-aa35-18d300d2e8c6	spotify	2TPmjAg6ua4womsSaiNpdk	youtube	9pq93-A-vQU	Thirsty	{"PARTYNEXTDOOR"}	90	2026-02-10 06:21:14.905315	2026-02-10 05:15:05.595805
df8aba5f-8011-49b9-9936-a24cdd25442b	spotify	4KjnaUNYPwGnJjoeTFlt91	youtube	o0yLdYf6OVY	Now Or Never	{"Kendrick Lamar","Mary J. Blige"}	87	2026-02-10 06:21:14.911897	2026-02-10 05:15:06.106298
7769d3e2-86c7-4015-bb99-63fbd43dbdaa	spotify	2YaDRtIlQiZ5WDDB2YuEOC	youtube	LcdcJ1jMW4k	You & The 6	{"Drake"}	90	2026-02-10 06:21:14.918184	2026-02-10 05:15:06.622213
c35d49b8-0cde-4cce-b9ec-ff7d65c24dca	spotify	7KA4W4McWYRpgf0fWsJZWB	youtube	EZE62LpaqHg	See You Again	{"Tyler, The Creator","Kali Uchis"}	95	2026-02-10 06:21:17.410426	2026-02-10 05:15:11.846112
a7355f24-d3f4-45ab-bbbb-777d622a6aef	spotify	15EPc80XuFrb2LmOzGjuRg	youtube	vR1-Rl7lq_M	Crew	{"GoldLink","Brent Faiyaz","Shy Glizzy"}	90	2026-02-10 06:21:17.428938	2026-02-10 05:15:12.47049
3d2ea774-d200-4b85-9238-ff3b177ca26c	spotify	2SWbnWSe1onmbllRAU46uo	youtube	DXtD8Qz24XY	PERSIAN RUGS	{"PARTYNEXTDOOR"}	100	2026-02-10 06:21:17.444716	2026-02-10 05:15:13.768473
96863941-b265-4bd1-822a-de320bec2ab2	spotify	5O59s7bCgTFsXDXlWecyQ1	youtube	98j39T5mrAk	Apparently	{"J. Cole"}	90	2026-02-10 06:21:17.454241	2026-02-10 05:15:14.519139
6932cc7e-a752-46e3-a7a8-b9829465df7a	spotify	236P5yLtfnHgTMxevc0q6F	youtube	44cICamRLwk	TBH	{"PARTYNEXTDOOR"}	90	2026-02-10 06:21:17.461069	2026-02-10 05:15:15.160762
02b661bd-0446-454b-b1e2-89e17876ff19	spotify	57wp7VFnV8X0pSVnYArGeJ	youtube	2MeftqZ_quo	Whatever She Wants	{"Bryson Tiller"}	90	2026-02-10 06:21:17.472324	2026-02-10 05:15:15.951011
1b83404b-43fe-407b-b428-4fa14324bbe6	spotify	5BIO6OOLusXv7vArG6zcO0	youtube	J6lqLaHzWkU	That Way	{"Wale","Jeremih","Rick Ross"}	87	2026-02-10 06:21:17.479522	2026-02-10 05:15:16.751726
9626f951-60e1-48f5-b231-76b42dfc9ba1	spotify	7xEMzWZo71RZbrBki6ZraY	youtube	fYsW4GoHwAo	I Been Drinking	{"Future"}	90	2026-02-10 06:21:17.492605	2026-02-10 05:15:17.64779
06a56827-f7eb-4435-9a9a-50c4614ba290	spotify	0llA0pYA6GpGk7fTjew0wO	youtube	bjHf7a9Gidc	The Resistance	{"Drake"}	90	2026-02-10 06:21:17.499284	2026-02-10 05:15:18.326117
ddfc84c2-0c32-4c2d-8e3f-4ace5eb3cf5b	spotify	4at3d5QWnlibMVN75ECDrp	youtube	Bh0L3_pchZQ	Pretty Little Fears	{"6LACK","J. Cole"}	87	2026-02-10 06:21:17.509856	2026-02-10 05:15:18.885237
b333b967-385f-4b7c-a6e6-522d475d591d	spotify	7eqoqGkKwgOaWNNHx90uEZ	youtube	Fx3b85eDQvw	Nights	{"Frank Ocean"}	100	2026-02-10 06:21:17.516793	2026-02-10 05:15:20.670386
64492963-ae7f-4e5d-b4c8-46961c2b1cde	spotify	2wQVmS0j4xcSbEK8CLEgwz	youtube	xqFKSu-BvnU	Born Sinner	{"J. Cole","James Fauntleroy"}	90	2026-02-10 06:21:17.530925	2026-02-10 05:15:22.596729
6efd5b51-d676-4e19-8ddc-699d6526c1a1	spotify	60ZMdbcC5wwmzbGr5XkDHd	youtube	A2mJ05KoVjA	Lights Please	{"J. Cole"}	100	2026-02-10 06:21:17.539932	2026-02-10 05:15:23.96484
94898add-48cf-414d-a1ae-1bcf8330c094	spotify	3Z2sglqDj1rDRMF5x0Sz2R	youtube	3VmDcu5lWXI	3005	{"Childish Gambino"}	95	2026-02-10 06:21:17.545958	2026-02-10 05:15:25.243531
062b29ca-0155-40b4-9c8e-8955c4573e09	spotify	515Xrd7u7EHy7xBLnoAO0A	youtube	rfbaoXuo0hk	Tired/Reflections	{"WESTSIDE BOOGIE"}	87	2026-02-10 06:21:17.553898	2026-02-10 05:15:26.053565
b2de7556-ec0f-42ba-825f-2f2aa2591e13	spotify	34j4OxJxKznBs88cjSL2j9	youtube	p4NvOKy7GOU	Never Let Me Down	{"Kanye West","JAY-Z","J. Ivy"}	90	2026-02-10 06:21:17.560444	2026-02-10 05:15:26.98458
d4af546d-cef1-477c-93ae-ab47211187a0	spotify	01z2fBGB8Hl3Jd3zXe4IXR	youtube	VzAjXdBJsEc	Come Back to Earth	{"Mac Miller"}	100	2026-02-10 06:21:17.566966	2026-02-10 05:15:28.173842
34034098-20f9-4067-a552-5e0609fc02bd	spotify	77v9kYcrCZV615E0P9WMrD	youtube	b7twYZLgqWM	Phoenix	{"A$AP Rocky"}	100	2026-02-10 06:21:17.577945	2026-02-10 05:15:29.451681
3a8e2ada-50cd-42e2-8a88-031ceca22f6e	spotify	5gNdqHSdG9eTbnvp8sZwUD	youtube	soGBh3B6oRE	All Along	{"Kid Cudi"}	90	2026-02-10 06:21:17.582737	2026-02-10 05:15:30.168686
02ac7448-535a-4341-a1f9-9d1822740b0f	spotify	0Epl79nHvdyTdZRAiWpJah	youtube	XCjTwALse0Q	REVOFEV	{"Kid Cudi"}	90	2026-02-10 06:21:17.593237	2026-02-10 05:15:30.697112
405e0e87-a6a9-4604-8ee1-64f0919ae3a6	spotify	6n3HGiq4v35D6eFOSwqYuo	youtube	Uc57OKGTDXk	Teenage Fever	{"Drake"}	90	2026-02-10 06:21:19.962228	2026-02-10 05:15:34.635492
a819c86c-f2af-4d63-9a3f-146e9f65daca	spotify	4l0UlMRbfxPS0Y3zHlpYou	youtube	xnMr8V-qh8E	Paper Cuts	{"Bas","J. Cole"}	90	2026-02-10 06:21:19.9982	2026-02-10 05:15:35.302497
b2d4bbb4-7e19-408e-9780-e35af443c539	spotify	2gCQ6k02Y2Dgd9r6yHilC7	youtube	GCUs15YIa3o	Wait On Me	{"Bas","FKJ"}	87	2026-02-10 06:21:20.024855	2026-02-10 05:15:36.080231
4a332383-3f1d-40f5-a60b-093fbca82641	spotify	0rcoSjQLh5QXhCmBYBdNDW	youtube	8F4pITS9fXs	The Hearse	{"Mustafa"}	87	2026-02-10 06:21:20.040811	2026-02-10 05:15:36.567292
6577994d-8a64-43ea-88fd-9bd3331997f0	spotify	5XFoaqV1Z8XaLJidchVWhh	youtube	ZqK2As0qimM	love.	{"Kid Cudi"}	90	2026-02-10 06:21:20.062173	2026-02-10 05:15:38.051187
639e1b9d-4958-4d5d-8a62-3e06b8173571	spotify	5q5MyfXey3ms6IyT6TYf5L	youtube	4FzXI6Z13g4	Night Job	{"Bas","J. Cole"}	90	2026-02-10 06:21:20.070462	2026-02-10 05:15:38.748092
c83e4f11-4118-4552-9784-6a3c03231d59	spotify	3BUWNzPWz2mDbptZmGEXpB	youtube	TKJJiF8QJRc	Let It All Work Out	{"Lil Wayne"}	90	2026-02-10 06:21:20.077505	2026-02-10 05:15:39.481373
af9d8035-c5aa-476b-adc1-aa88a3615ba7	spotify	4qS2KPWvsQzLvRa9oCHw41	youtube	XkQ1pltpQnw	You	{"Don Toliver","Travis Scott"}	87	2026-02-10 06:21:20.084106	2026-02-10 05:15:40.165258
c0fe80db-f0e8-40a2-9ebc-39bb5cf953a8	spotify	5Tutv8aK0ZJFciloVv6WDT	youtube	KaKsypkxGnI	CHAIN ON	{"BROCKHAMPTON","JPEGMAFIA"}	95	2026-02-10 06:21:20.092108	2026-02-10 05:15:41.598828
b17e85ec-21a3-4cc7-90d3-f37afcf36b00	spotify	7LBdv0ek7Tj2s0bgxmlrC6	youtube	U_IEiTWaEtM	CALLIGRAPHY	{"Saba"}	84	2026-02-10 06:21:20.100366	2026-02-10 05:15:42.27092
3d6df1a8-a0c4-4c02-8cce-9cb51252a76c	spotify	0WtDGnWL2KrMCk0mI1Gpwz	youtube	k49I5m1J6Is	Redbone	{"Childish Gambino"}	100	2026-02-10 06:21:20.11297	2026-02-10 05:15:43.743092
4d2d9a8a-b069-4046-aabe-7b83bf707879	spotify	7fQ1PCR4pZC3SyNSZHDQtT	youtube	b5unsCbNpJ4	trio	{"Gunna"}	90	2026-02-10 06:21:20.122485	2026-02-10 05:15:44.633186
75fe77c1-4178-4bc9-9061-38c0f4d3d09d	spotify	25cUhiAod71TIQSNicOaW3	youtube	I7eUlYgyn9I	Adorn	{"Miguel"}	100	2026-02-10 06:21:20.129397	2026-02-10 05:15:46.130327
9d0342dc-e134-48ce-be15-a7c78fc48d80	spotify	2AXyDKTC0kFvSiQPgje4ql	youtube	sWnlPAkE8M8	BACKSTREETS	{"Don Toliver","Teezo Touchdown"}	87	2026-02-10 06:21:20.139564	2026-02-10 05:15:46.679534
38d148bf-08b9-43ff-978e-dcb7b5d9ebc1	spotify	7i8ncEfBk3s0w5YoG43SV9	youtube	B_QlMts_lcA	PURPLE RAIN	{"Don Toliver","Future","Metro Boomin"}	87	2026-02-10 06:21:20.146555	2026-02-10 05:15:47.147741
69d8fd7f-73f0-4946-b299-7e8b2f3973e6	spotify	6Lo67fRKutuSH1FcweSgrD	youtube	lNiUlCqfKuw	530	{"¥$","Kanye West","Ty Dolla $ign"}	100	2026-02-10 06:21:20.15626	2026-02-10 05:15:48.266126
384f8583-42f1-424f-b453-bc5484552710	spotify	2UICNcwQNzeuirpkWIaRuF	youtube	n2YiUKvugaQ	Grey	{"Travis Scott"}	90	2026-02-10 06:21:20.163306	2026-02-10 05:15:48.923662
e1110c73-21b5-4ed6-9f7f-178988efeb00	spotify	4CkbpelT92r4dVkceDX9g1	youtube	EAdtal_4N2k	Leave Me Alone	{"BigXthaPlug"}	90	2026-02-10 06:21:20.170172	2026-02-10 05:15:49.539374
76a3a599-49e1-43af-b5d4-f46dd6d2e758	spotify	0VaeksJaXy5R1nvcTMh3Xk	youtube	dmKUqY4W4fg	Darling, I	{"Tyler, The Creator","Teezo Touchdown"}	82	2026-02-10 06:21:20.176955	2026-02-10 05:15:50.23733
f6cbfb15-9b18-42f2-90b8-bbbe66b92344	spotify	2WDLmMNQ44SDx9fHE50yJq	youtube	drb--5u8gTc	06 dreamin	{"Cordae"}	87	2026-02-10 06:21:20.184141	2026-02-10 05:15:50.765481
768a36bf-a4d1-43f4-a6d1-a776f722bec2	spotify	5dPZ04EhmBOdLlyRtPLqEW	youtube	5gJmpjRm1RA	Summer Drop	{"Cordae","Anderson .Paak"}	87	2026-02-10 06:21:20.19119	2026-02-10 05:15:51.368901
6fc5ccd6-eeea-4605-a7e4-cde9301e49d4	spotify	4K1Pg0FLno1ltzX3jeqT83	youtube	CHX3G7BjHyI	dodger blue	{"Kendrick Lamar","Wallie the Sensei","Siete7x","Roddy Ricch"}	90	2026-02-10 06:21:20.196885	2026-02-10 05:15:51.987821
dced85e3-80c2-473b-a7d8-7dbb9ee5f3d2	spotify	1JtAIBbCgomz38qPBSJzCn	youtube	lJqDlEYEUA8	DJ's Chord Organ	{"Mac Miller","SZA"}	87	2026-02-10 06:21:20.202856	2026-02-10 05:15:52.646047
5344be67-28ee-4a10-9ff7-d5b8468a09ed	spotify	4pu58kqegTnOxPNJpY3kr9	youtube	IMUNeQ3W2ew	Funny Papers	{"Mac Miller"}	87	2026-02-10 06:21:20.209005	2026-02-10 05:15:53.348202
28c15343-e619-4a3e-a3de-d2f04a12ac25	spotify	2v0AG62ZHtD3I4YmTb3WYM	youtube	jmopn_q_0TU	Niagara Falls	{"The Weeknd"}	90	2026-02-10 06:21:20.214025	2026-02-10 05:15:53.956907
a4e7eec9-36a8-4f66-9237-9ff8362c360f	spotify	1RMJOxR6GRPsBHL8qeC2ux	youtube	zNhtz0Lygik	Best Part	{"Daniel Caesar","H.E.R."}	95	2026-02-10 06:21:20.22228	2026-02-10 05:15:56.457227
411fa2ce-3347-4929-b885-c0051d1bbfef	spotify	1Mza2sr6tPhy6jjI3HB9fW	youtube	xBJ4YkQq1i8	Hold Me Down	{"Daniel Caesar"}	90	2026-02-10 06:21:20.229535	2026-02-10 05:15:57.110666
3f41f17e-951e-496e-8339-c0658cb0af98	spotify	0NWMPu3EzbUr0VBi0tIVGB	youtube	SHP-xh5NnVs	Toronto 2014	{"Daniel Caesar","Mustafa"}	87	2026-02-10 06:21:20.234244	2026-02-10 05:15:57.752587
f4433eb5-b739-4292-8c9f-c8de3b5b9806	spotify	71XJNtOdwmL1UWOQQvsKoH	youtube	gV26nFt8X_8	Always	{"Daniel Caesar"}	90	2026-02-10 06:21:20.244485	2026-02-10 05:15:58.38981
61030143-b0c5-49fb-bdfb-c20efcdd359d	spotify	1D4qUYEH0cs7zNcUXVw6IX	youtube	4-i5fwa2k1s	Winter Wonderland	{"J. Cole"}	90	2026-02-10 06:21:20.249656	2026-02-10 05:15:59.171374
17a346a6-c66d-4c27-8894-c909e96658fd	spotify	1DmnEYXa4WfbdhAPwNzgD8	youtube	Emn6BEK5SlE	30 for 30 Freestyle	{"Drake"}	90	2026-02-10 06:21:20.25722	2026-02-10 05:15:59.805539
5983186a-8dfc-49aa-a87b-51e8cafe9b70	spotify	3Yib1ZUZ1T25Z5TGpbCvZK	youtube	CTyRUel1xFY	Loaded Bases	{"Nipsey Hussle","CeeLo Green"}	87	2026-02-10 06:21:20.262931	2026-02-10 05:16:00.691027
eaf7123e-45ed-461f-a8db-3d12537ebcd7	spotify	4u43I0LP2Xf85OAS85eG0R	youtube	EwIqO9zTRi0	CN TOWER	{"PARTYNEXTDOOR","Drake"}	90	2026-02-10 06:21:20.267355	2026-02-10 05:16:01.305416
3aa4b7b4-358f-4e13-a84b-cd5ed98c57e6	spotify	3Ku0VYAQ4b6IomnonhjdVv	youtube	mZOf5_rIfK4	Rearrange My World	{"Daniel Caesar","Rex Orange County"}	87	2026-02-10 06:21:20.274882	2026-02-10 05:16:02.1533
2f3bd726-962e-4a34-90a4-9b22f72419c2	spotify	1iY2DB9Bpn1r8mBgoFLBOG	youtube	Y8LV9ZW8xcw	By Any Means	{"ScHoolboy Q"}	100	2026-02-10 06:21:20.280897	2026-02-10 05:16:03.913202
c857e985-b95b-449f-be7b-86e7931cd655	spotify	13HsOwrwTA5HgLuoaDHncP	youtube	uLXPCPkQBh0	RUNITUP	{"Tyler, The Creator","Teezo Touchdown"}	87	2026-02-10 06:21:22.645098	2026-02-10 05:16:06.871252
24d16551-72de-428d-a45e-8ca4d3768e35	spotify	4Hta7Oy5W9HD0UgT1P8PPV	youtube	Znwgjl4wuPo	RISE!	{"Tyler, The Creator","DAISY WORLD"}	87	2026-02-10 06:21:22.65013	2026-02-10 05:16:07.378901
577eafc9-8fe1-432b-9314-3289b7df9f5b	spotify	4JvTUper6kKrGUuOPsdhI2	youtube	HKeRvmhwXic	THE SHINING	{"Vince Staples"}	90	2026-02-10 06:21:22.65888	2026-02-10 05:16:07.964473
f578d173-15ac-4d49-b743-43c99700c0c7	spotify	4gTrWkQqn4xC2qcwQEIYg8	youtube	SSYWfq_pFTU	I THOUGHT YOU GOT ME	{"Russ","Benny The Butcher"}	95	2026-02-10 06:21:22.664568	2026-02-10 05:16:09.601959
3b39c517-50ee-42ef-a278-ebe93b82aed7	spotify	34xbyF0mABlqub5fj5RkC0	youtube	Ut0uSNeGf0Y	We Get High	{"Logic"}	84	2026-02-10 06:21:22.674647	2026-02-10 05:16:10.218377
c2caea30-45db-482a-b5ed-9968f57093b4	spotify	2asRb3cc3lsAxEJLBSGXnF	youtube	KkfUhe_37A4	Claymore	{"Isaiah Rashad","Smino"}	87	2026-02-10 06:21:22.680357	2026-02-10 05:16:10.705592
d7f8daaf-1cb1-4ddf-960b-dbb3e824a40b	spotify	7xfueRdAGnr8pKEIhkGcc8	youtube	M8y5oqHzfLM	Hate The Game	{"Young Thug"}	90	2026-02-10 06:21:22.691075	2026-02-10 05:16:11.559268
15e3b4c3-9f1e-4641-a347-3630d5b7dbb5	spotify	4S4Mfvv03M1cHgIOJcbUCL	youtube	hEq8jBUbbWE	In My Room	{"Frank Ocean"}	70	2026-02-10 06:21:08.961315	2026-02-07 07:03:21.349219
67768dd5-c727-4b30-9c9b-4afd2635676b	spotify	4RnvS1tmh1ERhjXX6dxv95	youtube	Aj1xlBrLvNU	Language	{"Paperboy Fabe","Brent Faiyaz"}	95	2026-02-10 06:21:14.691346	2026-02-10 05:14:41.803014
366f6f8a-f39a-43ed-9840-0103e5475b0f	spotify	5B0kgjHULYJhAQkK5XsMoC	youtube	NJea386275c	WUSYANAME	{"Tyler, The Creator","YoungBoy Never Broke Again","Ty Dolla $ign"}	87	2026-02-10 06:21:17.363782	2026-02-10 05:15:10.428559
beb0cedd-88b7-407d-847a-4fdf12aa2db5	spotify	79VMlcpvgHkxz0xwsTfc9d	youtube	zg1ZcDAmtcA	Thoughts from a Balcony	{"Mac Miller"}	100	2026-02-10 06:21:17.524805	2026-02-10 05:15:21.809081
f865ac92-b016-4726-9431-7e7fb50d889d	spotify	5CRnOqHHcREG9NPtq7xHge	youtube	WIoUgBtrtCw	Burn Bridges	{"Dreamville","Lute","Cozz","REASON","Arin Ray"}	87	2026-02-10 06:21:20.050504	2026-02-10 05:15:37.234939
c2ad0e57-1f01-4e3c-bf0d-03cefd9d1881	spotify	3DiCcOpGyqBzd2MDoUnD6K	youtube	DzLJCu95RKI	U&I	{"Flatbush Zombies","Dia"}	80	2026-02-10 06:21:22.696334	2026-02-10 05:16:12.207083
d11bc1ef-0282-440d-b537-e1dc9c355b42	spotify	5I4ipBNoOOXF20YY0pOjCx	youtube	79C0G6fBGUY	A Spike Lee Joint	{"Flatbush Zombies","Anthony Flammia"}	80	2026-02-10 06:21:22.704507	2026-02-10 05:16:12.704214
cef5c5a6-78b2-4957-8c3a-5cfac79e7617	spotify	2kglz28uEL0ZfZ6CwcoGWr	youtube	glB4XNl0_tg	Pussy On Repeat	{"Spillage Village","EARTHGANG","JID","6LACK"}	87	2026-02-10 06:21:22.709924	2026-02-10 05:16:13.309377
bbda78be-14e8-4f21-8919-3288edcea599	spotify	0mvnmLsfi9n5RjPIWzT6tY	youtube	Zf_TvbfP-TU	FIND YOUR WINGS	{"Tyler, The Creator","Roy Ayers","Syd","Kali Uchis"}	90	2026-02-10 06:21:22.714655	2026-02-10 05:16:13.991464
5b1881db-95a0-4466-99bd-a031cde2ac05	spotify	5isZz9H9yWJgOjFxrTeZm6	youtube	BhgGQuIH72s	Before I'm Gone	{"J. Cole"}	87	2026-02-10 06:21:22.722213	2026-02-10 05:16:14.708519
b10d0237-0eab-4e64-a2e9-b8a640b7879b	spotify	3jl8vmPdfQodceoYAmjrQs	youtube	5Tq95Zpr4sI	Home Soon	{"J. Cole"}	90	2026-02-10 06:21:22.726971	2026-02-10 05:16:15.36981
d206bcf2-f8e5-46a9-afa4-ec39995ce528	spotify	74QLfg69aVm4Afw3VGhDTY	youtube	BWtBckf8RIw	Port Antonio	{"J. Cole"}	90	2026-02-10 06:21:22.731723	2026-02-10 05:16:16.064868
c93c1b83-0599-4075-a753-188878da6a75	spotify	46CA1H0T1r7u5lDUtVveAh	youtube	X1A1fYk_ldM	FUCKING YOUNG / PERFECT	{"Tyler, The Creator","Charlie Wilson","Chaz Bundick","Syd","Kali Uchis"}	87	2026-02-10 06:21:22.739514	2026-02-10 05:16:16.873042
cad98635-a9c8-40b2-ae1f-b2fd09dbe495	spotify	3mTBcwM1BTiF6HR7D0SIcH	youtube	ntEbkWcWAJw	PROM/KING	{"Saba"}	100	2026-02-10 06:21:22.745038	2026-02-10 05:16:18.440214
ab93f4b8-75b4-4c82-980c-de86ea55081d	spotify	4NVNapccSX7E5JLiW0uQEy	youtube	Qz3LK267YVI	Pound Cake / Paris Morton Music 2	{"Drake","JAY-Z"}	90	2026-02-10 06:21:22.75088	2026-02-10 05:16:19.112511
64a4465e-6c76-42cb-b17c-b553331e76c4	spotify	3GXSywNvYLAVUCtjMHkKDh	youtube	CqBmVPI7ce8	E. Coli	{"The Alchemist","Earl Sweatshirt"}	84	2026-02-10 06:21:22.757716	2026-02-10 05:16:19.78042
408bd9fb-c084-4df8-b0e6-7478d4831996	spotify	1mh9eHVRdNhzryG43PXdW1	youtube	t-1CvBXEpcE	MUTT	{"Leon Thomas"}	90	2026-02-10 06:21:22.762601	2026-02-10 05:16:20.305998
e05c4302-67ab-480f-a579-b63ceee41607	spotify	4lsHZ92XCFOQfzJFBTluk8	youtube	Xb6A0lwA6rc	You Got It	{"Vedo"}	90	2026-02-10 06:21:22.766998	2026-02-10 05:16:20.868593
0d6ec2ee-055b-438c-bfeb-1529621a5732	spotify	4FIgHqXd4KkOwxrGeLDYM2	youtube	kFXHPfI2JoI	Texts Go Green	{"Drake"}	87	2026-02-10 06:21:22.773961	2026-02-10 05:16:21.375067
f7d6a54d-c816-4f0b-904a-999e2b71f452	spotify	314sW8ZsMHGiYkn2fAOW9e	youtube	fyvmWcNnCQs	Wholeheartedly	{"JID","Ty Dolla $ign","6LACK"}	90	2026-02-10 06:21:22.780524	2026-02-10 05:16:21.892893
a83086f4-bd87-4c45-9f50-ed266aca77c8	spotify	1Xin1kJWE4QGydizKryZZI	youtube	KT8ioO_pZmU	No Boo	{"JID","Jessie Reyez"}	90	2026-02-10 06:21:22.79286	2026-02-10 05:16:22.822721
7808bc62-e242-455a-ba13-111595da513c	spotify	1hZsH26IxBQAYyOZUKOuVF	youtube	H-wJta36SAQ	A Thousand Mountains	{"Freddie Gibbs","The Alchemist"}	87	2026-02-10 06:21:22.80031	2026-02-10 05:16:23.46729
6b64b581-224d-410e-bedc-a867ccebc966	spotify	6q22ySlGYH8PEcciOeN1Ki	youtube	l8-gibHm-R8	Aahhyeahh	{"Oliver Francis"}	100	2026-02-10 06:21:22.808181	2026-02-10 05:16:25.291817
66601222-09f9-48cd-a0e8-4c9a2e88610e	spotify	2OKtaNOAcj19WiWT9dKvGr	youtube	a4Gh6CetTSE	SPEEDIN' THROUGH THE RAIN	{"Joey Bada$$"}	87	2026-02-10 06:21:22.81458	2026-02-10 05:16:26.234584
94db0fe4-4e93-4fc0-935d-87a8e81c8401	spotify	3H3kzlDWxN9KEFuUtv39p2	youtube	TsdlyIlbqfc	Automatic	{"Dijon"}	87	2026-02-10 06:21:22.825108	2026-02-10 05:16:27.200872
d591de6d-c604-4550-b470-2affac30f043	spotify	1M3O7iJBvb5wCAYQmWGWsC	youtube	oDQxTW1xcVQ	Call On Me	{"Daniel Caesar"}	87	2026-02-10 06:21:22.833697	2026-02-10 05:16:27.868772
234063c0-d04c-4be0-bfa4-bc895e125f28	spotify	0xg25m2SiZJkAXEXAcURNb	youtube	-42N5181Fcg	Letters	{"Chance the Rapper","Rachel Robinson"}	87	2026-02-10 06:21:22.848757	2026-02-10 05:16:29.446443
a7153c42-258e-4fff-830d-d74a5eeea577	spotify	6qQggeOq8bqOAuXutY8nfN	youtube	5cX_B4YikLs	INFATUATION	{"Earl Sweatshirt"}	90	2026-02-10 06:21:22.858455	2026-02-10 05:16:30.112037
12411bf5-4103-42f8-af9f-6413ebf22c8f	spotify	71SEHJPt1iDwGkBhUOAXDP	youtube	VtEBRE9ul90	Survival	{"Drake"}	90	2026-02-10 06:21:22.866261	2026-02-10 05:16:30.605916
14934f21-7233-4464-a8e5-37f38a52d104	spotify	1U1ulJ1H9eXkrVTW8ot0T3	youtube	lENJ3mPuwdk	VCRs	{"JID","Vince Staples"}	90	2026-02-10 06:21:22.874741	2026-02-10 05:16:31.117174
9bf8f754-b570-4296-bc5d-e9b80e514c09	spotify	5ScbulRnixQ2XAdvrPMFjz	youtube	R-uMLq0lkPw	Get Along Better	{"Drake","Ty Dolla $ign"}	90	2026-02-10 06:21:22.88362	2026-02-10 05:16:31.806653
0e05d31d-ea26-45b3-a097-c5dd02c0f28f	spotify	43rqS032Um5iJo9sy2uDNu	youtube	O5yc94spPoQ	head above the surface	{"Garrett Zoukis"}	87	2026-02-10 06:21:22.89511	2026-02-10 05:16:32.655109
7068f513-46b5-4d17-b745-0fcd1cbd0cf5	spotify	3sJLoIc9y1b4gKAP8ZNsR9	youtube	EBzQ5U3mIXs	SAFETY	{"J. Cole"}	87	2026-02-10 06:21:22.903225	2026-02-10 05:16:33.182783
b3cfe61a-7d1d-40e7-a592-390da3ac8b1e	spotify	6jtqO14L18plBtpDTtUkFR	youtube	BQdmfuMKqvs	Bunce Road Blues	{"J. Cole","Future","Tems"}	87	2026-02-10 06:21:22.910127	2026-02-10 05:16:34.00511
e3a95080-2bb3-4640-a6c2-4a66b9afba7c	spotify	2aNaJLXE4SXbm9L23qpHTG	youtube	IJgCvnvO3Bc	The Let Out	{"J. Cole"}	87	2026-02-10 06:21:22.916717	2026-02-10 05:16:34.721374
f2572f4e-adef-4976-a0a4-162560e9521d	spotify	31qCy5ZaophVA81wtlwLc4	youtube	4cLa1c7Zzg8	Anyone	{"Justin Bieber"}	87	2026-02-10 06:21:24.460983	2026-02-10 06:21:24.460983
9e4c2676-efa8-4a26-8a7c-deaf8a9718df	spotify	0mBP9X2gPCuapvpZ7TGDk3	youtube	G8KgyvIwgFY	Left and Right	{"Charlie Puth","Jung Kook","BTS"}	87	2026-02-10 06:21:25.111269	2026-02-10 06:21:25.111269
8898f3e0-ba73-4fa9-9643-11ffd82c655f	spotify	0QBzMgT7NIeoCYy3sJCof1	youtube	YwXXnjMYrVw	Bam Bam	{"Camila Cabello","Ed Sheeran"}	87	2026-02-10 06:21:26.015017	2026-02-10 06:21:26.015017
662ae0ab-f2d4-415e-bbbe-000ad7b12edc	spotify	0z8hI3OPS8ADPWtoCjjLl6	youtube	_jQ4u2cNbrg	Summer of Love	{"Shawn Mendes","Tainy"}	87	2026-02-10 06:21:26.667887	2026-02-10 06:21:26.667887
f1f1967a-fa87-4a5f-89bf-2f4a6d15fdae	spotify	5902W4uHWzhtOff1UK7the	youtube	-ZFfIoBbOfg	When I’m Gone	{"Alesso","Katy Perry"}	87	2026-02-10 06:21:27.491737	2026-02-10 06:21:27.491737
2e555fad-5f56-4ca0-9293-86c070858699	spotify	50nfwKoDiSYg8zOCREWAm5	youtube	z2_Lrg6rRks	Shivers	{"Ed Sheeran"}	87	2026-02-10 06:21:28.080471	2026-02-10 06:21:28.080471
a2f3b707-ebed-4aee-82ab-e353121390d4	spotify	0zzVTGyRrWpQu8Fr28NRAv	youtube	TYGo1fToZ8Y	OK Not To Be OK	{"Marshmello","Demi Lovato"}	100	2026-02-10 06:21:30.339571	2026-02-10 06:21:30.339571
0e001017-ffb1-47e9-865a-a3a07685d487	spotify	1QxIFMs4M8qd5cbJJu80v4	youtube	urqKqv8TSpo	Skyline	{"Khalid"}	100	2026-02-10 06:21:31.857363	2026-02-10 06:21:31.857363
b912ddde-9ed6-4a83-b7e6-7e615d7ab9aa	spotify	1RF02Cf80mTaeNXG2P2boR	youtube	TfbK_sCRapM	21 Reasons	{"Nathan Dawe","Ella Henderson"}	87	2026-02-10 06:21:32.427571	2026-02-10 06:21:32.427571
e56294a0-3017-404d-8372-745e77748d59	youtube	zjbMpAhOzLI	spotify	1ySzohfiLo3l12pcqW40p3	Stranger	Simi	100	2026-02-16 03:29:00.09829	2026-02-10 06:22:26.91194
4ff68429-5a20-4c47-b468-b7aef38dc47d	youtube	3IUqoyTxEU8	spotify	2uIX8YMNjGMD7441kqyyNU	ocean eyes	Billie Eilish	100	2026-02-10 06:24:35.806479	2026-02-10 06:24:35.806479
b195ec6d-92aa-4e29-9cc8-5969143849e3	youtube	2qPlX1FMJXI	spotify	3Z0oQ8r78OUaHvGPiDBR3W	Into the Unknown	Idina Menzel	100	2026-02-10 06:24:36.589624	2026-02-10 06:24:36.589624
7394e0f6-dfaf-4469-b029-fd163f9d0d2f	youtube	oa7JwwdiA98	spotify	1rMlnD8S0hX7IHZXZDt3gH	Everything Matters	AURORA	100	2026-02-10 06:24:37.134477	2026-02-10 06:24:37.134477
f63963b8-b55a-4013-843f-94b7009e56b0	youtube	4zsVKROnQfY	spotify	58HvfVOeJY7lUuCqF0m3ly	MIDDLE OF THE NIGHT	Elley Duhé	100	2026-02-10 06:24:37.646532	2026-02-10 06:24:37.646532
daac71b8-82ec-4b94-a2b1-f42c0e0a3010	youtube	WMK3JXG3Fx0	spotify	1SOClUWhOi8vHZYMz3GluK	Infinity	Jaymes Young	100	2026-02-10 06:24:38.260574	2026-02-10 06:24:38.260574
8055df26-b834-40c2-87d9-5ba7c3da9f99	youtube	59nAgJZ5IoE	spotify	4rHZZAmHpZrA3iH5zx8frV	Mirrors	Justin Timberlake	100	2026-02-10 06:24:38.795303	2026-02-10 06:24:38.795303
31e4abd1-1097-456f-9f81-b5e022a9f682	youtube	AeedWEyMYYo	spotify	6usohdchdzW9oML7VC4Uhk	Lose Control	Teddy Swims	100	2026-02-10 06:24:39.512246	2026-02-10 06:24:39.512246
b294d50a-cc5c-49c7-8d65-9b474b8ab0a6	youtube	wo0bpfOSIPc	spotify	2TVSN0ed8AFnLUaWPXof2e	Angel of My Life	Paul Play Dairo	100	2026-02-16 03:29:00.184806	2026-02-10 06:22:31.493629
2b131831-86b8-47e6-9229-fbf6f8f9ce97	youtube	Z-dN-y8_Uzo	spotify	6rk87fdFvUTxJqaa5a7Od0	Obi Mu O	Obiora Obiwon	100	2026-02-16 03:29:00.19314	2026-02-10 06:22:31.976606
bf8bacc5-5d98-416f-b018-fc410269df97	youtube	Floqvslobvs	spotify	3GgYYPUOIwd1uMBvfv5bNW	Bibanke	Aṣa	100	2026-02-16 03:29:00.199768	2026-02-10 06:22:32.395261
599dc621-1ed8-4695-b811-fb5c953a7fdf	youtube	YiRV6RjWLck	spotify	1Oao2WEZDXfK4TEINsglKg	Pray for Me	Darey	100	2026-02-16 03:29:00.20667	2026-02-10 06:22:32.820346
849471ca-6ce9-48f3-8c8f-6e6d382f3be4	youtube	t6tqWadIbqU	spotify	2oo62jEC2X10AQZFVCRuWI	Not The Girl	Darey	100	2026-02-16 03:29:00.213559	2026-02-10 06:22:33.339004
600094ec-0c6d-4012-a114-eb470683a1b5	youtube	LfwAlJhXeHQ	spotify	3UqcIgObI7zZk8e7nvqmfc	Finesse	Pheelz	100	2026-02-12 05:00:39.258805	2026-02-11 00:28:38.525768
d16e26ce-5cf6-47be-8b30-356e8169bf39	spotify	2J2Z1SkXYghSajLibnQHOa	youtube	cfhwSpSDA9c	Unstoppable	{"Sia"}	100	2026-02-12 03:33:36.403909	2026-02-11 02:16:21.056196
85effbab-38db-4fc9-b333-54c038ca1d08	spotify	4VrWlk8IQxevMvERoX08iC	youtube	uzaYLK3k0DQ	Chandelier	{"Sia"}	100	2026-02-12 03:33:36.421488	2026-02-11 02:16:22.397712
72ba5a98-fd6f-4592-95b1-caf0e133cb15	spotify	27SdWb2rFzO6GWiYDBTD9j	youtube	J1b22l1kFKY	Cheap Thrills	{"Sia"}	87	2026-02-12 03:33:36.436636	2026-02-11 02:16:23.094596
16ae31bc-9e91-47be-88c7-737131200b4a	spotify	1rMlnD8S0hX7IHZXZDt3gH	youtube	oa7JwwdiA98	Everything Matters	{"AURORA","Pomme"}	95	2026-02-12 03:33:36.451312	2026-02-11 02:16:24.422849
6013df26-28f0-4017-aa9d-684c7383af87	youtube	kCCQtMx7V7k	spotify	6GkFuU6FVfx866bhM1T1Iz	Ole	Qing Madi	100	2026-02-12 05:00:39.276376	2026-02-11 00:17:26.696977
559cf56c-0ba6-4f9c-914d-ad8b770a93bf	youtube	hCAt-l4vUuM	spotify	731NDNG7VQOA2KBnS43jqr	PRAY	BNXN	100	2026-02-12 05:00:39.290621	2026-02-11 00:17:27.306434
3d45e903-d6f5-4116-9f85-383ccc0a2c0d	youtube	DfLS0A5IAhk	spotify	1z567QCaLLuRE7ZxQS5oYS	Feeling	LADIPOE	100	2026-02-12 05:00:39.296751	2026-02-11 00:17:27.916573
e4b12080-ee7a-411f-ab57-755c0bd83764	youtube	K2XCMnB6984	spotify	4jcrO2T6XJ1B7hXsAEJ3ve	Pidgin & English	BNXN	100	2026-02-12 05:00:39.30772	2026-02-11 00:17:28.446671
e4f6ed13-f8d9-451a-baf3-7f85f3b1d823	youtube	BaIVxGeRUiY	spotify	6D09vfVjCFlRxOGdmja0yp	I Alone	BNXN	100	2026-02-12 05:00:39.315094	2026-02-11 00:17:28.982302
bee6f8c7-439e-45c5-8a2e-e48de9bea264	youtube	aE5nfPCmrjQ	spotify	3sgensqwEH4E7icL0TWM1A	NO LONGER BENEFICIAL	Simi	100	2026-02-16 03:29:00.133234	2026-02-10 06:22:28.120816
3456f49f-f508-4380-bf66-2410599a86d2	spotify	1v1oIWf2Xgh54kIWuKsDf6	youtube	d_HlPboLRL8	Runaway	{"AURORA"}	87	2026-02-12 03:33:36.459734	2026-02-11 02:16:25.061327
d5964e2f-8419-4b3f-bf2b-97492cf14ec2	youtube	NaevY5LCZyc	spotify	1Dugqy9EGYV66nkp7yLiA4	Duduke	Simi	100	2026-02-16 03:29:00.146602	2026-02-10 06:22:28.703777
1046ec4b-9477-4641-9d2c-d7868b935a97	youtube	RFTuYO0EnJ0	spotify	3T3XVQvaJ09MckNdDE8hA8	Good Morning	Brymo	100	2026-02-16 03:29:00.219315	2026-02-10 06:22:33.75483
31c101ee-484e-420d-934a-0963fde10276	youtube	QynH_vd2_a0	spotify	4WS3bhnGgDjxwtAUWaPyNW	Ara	Brymo	100	2026-02-16 03:29:00.226925	2026-02-10 06:22:34.268955
72feacad-8862-4470-ab9c-3257ad745d32	youtube	mI5Qv3G1JMI	spotify	2t8CVuJb6W5E0FutItjhpd	Down	Brymo	100	2026-02-16 03:29:00.232422	2026-02-10 06:22:34.869074
9d554925-7ce7-4dfb-8751-7c70f11a7ff1	youtube	Efo9US_ZUgU	spotify	1rgK0UrJNwkaIptPI0a5kg	Need You	Fireboy DML	100	2026-02-16 03:29:00.238019	2026-02-10 06:22:35.381402
72d4c0b7-8e1f-42fb-9132-4af2509ac508	youtube	aJsKPgb6_N4	spotify	4kSTjIGLc8KM21Qbv3Y4UN	Need Somebody	Faze	91	2026-02-16 03:29:00.243752	2026-02-10 06:22:35.833043
bc5a04bc-709c-4aa7-8603-79a0f3028eb0	spotify	2MbgQKWhoii8bqsMBWWWgA	youtube	mWQACEqf4QY	Courage to Change	{"Sia"}	87	2026-02-12 03:33:36.469704	2026-02-11 02:16:25.640074
62253027-379c-4716-bd62-122446c6fcd9	youtube	OJk_XO0nS9A	spotify	5KWV7S3osWbeOMPHCeWVyK	Bambi	Jidenna	100	2026-02-16 03:29:00.154767	2026-02-10 06:22:29.3583
baa36cb0-03c2-4557-a3d7-30e7b96f315f	youtube	6rgiz8p_DmQ	spotify	0RVH9WtDKwNCQ1GMpPRFzj	Onyinye	Obiwon	100	2026-02-16 03:29:00.164525	2026-02-10 06:22:29.764213
e40ae855-bb60-479a-8b9b-69aa0bc006de	youtube	b24mOclhalM	spotify	3zamtBVBTOhYDizGtduNZQ	Ego	Djinee	73	2026-02-16 03:29:00.171627	2026-02-10 06:22:30.38975
4361d416-fea6-4d86-b581-c1610b658912	youtube	PwG5YGNvFr4	spotify	1PE0jpl3pt4CkmzB34q9ws	I No Dey Shame	Djinee	100	2026-02-16 03:29:00.178347	2026-02-10 06:22:30.837233
b82620ea-a56f-4c45-a48a-3a044472b0ec	spotify	4aPrZhKlDAh9aoSlXV4jZJ	youtube	18Hi_Q7HKyw	Just Around the Riverbend	{"Judy Kuhn"}	90	2026-02-19 05:06:21.559665	2026-02-07 06:22:56.637822
bf618759-df32-4595-b1fc-8807cc3524da	spotify	2ogKhhoMClkFXek7ZgxAhN	youtube	zAw3MLNU56s	Shameless	{"Camila Cabello"}	100	2026-02-12 03:33:36.654701	2026-02-11 02:16:44.967708
7ab1091c-f5d2-4a52-9936-c3011c1c9234	spotify	3yOlyBJuViE2YSGn3nVE1K	youtube	TduHf1tWDvI	My Oh My	{"Camila Cabello","DaBaby"}	87	2026-02-12 03:33:36.663379	2026-02-11 02:16:45.647471
ac4a5ee5-416a-4eea-a223-75fc6bd5dc03	spotify	4l0Mvzj72xxOpRrp6h8nHi	youtube	zlJDTxahav0	Lose You To Love Me	{"Selena Gomez"}	87	2026-02-12 03:33:36.730697	2026-02-11 02:16:54.367406
75d2a5f0-6e3b-4327-acf8-62b02bff474e	spotify	4crTd5bkKKISvU2w9eEc0r	youtube	HA0vlhJANow	Soul Sucker	{"Ellise"}	87	2026-02-12 03:33:36.551592	2026-02-11 02:16:34.984072
68d52c70-dc0d-4794-a4b4-d11339e6fb0b	spotify	3ZmEwYX2GbUUM9C2rpEhkm	youtube	WhG0-WO0vLE	Breadcrumbs	{"Ellise"}	87	2026-02-12 03:33:36.559239	2026-02-11 02:16:35.466665
05dca669-1e0c-44fb-ad53-2031180c39d6	spotify	1J14CdDAvBTE1AJYUOwl6C	youtube	51u5fnyrGj4	Arcade	{"Duncan Laurence"}	87	2026-02-12 03:33:36.735933	2026-02-11 02:16:54.928115
eadea3ed-6fa5-4ba4-912e-b775d993d9af	spotify	073ILU1C3APNRGF66VhtyA	youtube	6-OvO8ZuW98	Liar	{"Camila Cabello"}	87	2026-02-12 03:33:36.668375	2026-02-11 02:16:46.261826
9e74fa84-7a4b-41a9-be1f-0add19c3a325	spotify	6VpQdig9pdpTSIFItgkJV5	youtube	FITSPSA8gQs	Never Be the Same	{"Camila Cabello"}	87	2026-02-12 03:33:36.675742	2026-02-11 02:16:46.874848
36e27561-cd80-4199-8837-acb11f9e88e5	spotify	705dwKmZCd7ImAUyyD6Mpm	youtube	mcr-BYQBqXw	psychofreak	{"Camila Cabello","WILLOW"}	87	2026-02-12 03:33:36.683926	2026-02-11 02:16:47.48964
02842de9-f188-45b9-90f1-249672b9bf8e	spotify	0Gl5s8IhMmQE5YQwM8Qx1J	youtube	nT0CYXcre0c	Never Enough	{"Loren Allred"}	100	2026-02-12 03:33:36.741722	2026-02-11 02:16:56.120076
e35174ec-eb01-41d2-84df-be5cdf3f25c9	youtube	UG9ZvLPSWaI	spotify	77eZIuMTB9HrQ86L4ljqEX	Baby Riddim	FAVE	100	2026-02-12 04:51:23.736019	2026-02-12 04:51:23.736019
508826ea-ebd2-4b74-af8f-ea70b5d3594f	youtube	FzNxEQr62xY	spotify	0cnxJjQ8T4GSz87TaIkwDY	WeWe	Ruger	100	2026-02-12 04:51:24.117021	2026-02-12 04:51:24.117021
97905f7e-0773-4d3a-8302-793a2e31ead6	youtube	fUXSk0BoML4	spotify	56HnJIjgi41kpjh6L3Y1Ca	Risky	Davido	100	2026-02-12 04:51:24.642274	2026-02-12 04:51:24.642274
169006f0-b153-4144-b741-138fd7d21813	youtube	bH4UDgIzQgE	spotify	731NDNG7VQOA2KBnS43jqr	PRAY	BNXN	100	2026-02-12 04:51:25.057048	2026-02-12 04:51:25.057048
bcfc5f4c-8d8e-4db0-bcba-5982f6eaa7ff	youtube	NKqXl7ddKTQ	spotify	16aRxdbEl0smktrm0hK0eX	Great Guy	Asake	100	2026-02-12 04:51:25.506246	2026-02-12 04:51:25.506246
c12686d2-310f-4aab-9ca1-56218fbe5af2	youtube	m0hIGk6aNxY	spotify	5M1qjvTphJznaT4zdtDxNT	Iwuozor at Old School	Prince Chinedu F. Iwuozor	100	2026-02-16 03:30:46.901708	2026-02-16 03:30:46.901708
d3da1c46-b978-4a5c-a847-8619a446fa59	spotify	5zHX1aFxP5p10NI4L1nkai	youtube	4FrYREQ4XP8	You Keep Me Crawling	{"AURORA"}	87	2026-02-12 03:33:36.483384	2026-02-11 02:16:26.582839
bfb72079-f100-41ef-b48d-f6a8e0d37a0f	spotify	2rbDhOo9Fh61Bbu23T2qCk	youtube	Voo86mlxZvA	Always Remember Us This Way	{"Lady Gaga"}	100	2026-02-12 03:33:36.495853	2026-02-11 02:16:28.013395
2ada4a9e-a0b3-4325-950c-c2b81f9c10f2	spotify	3Z0oQ8r78OUaHvGPiDBR3W	youtube	2qPlX1FMJXI	Into the Unknown	{"Idina Menzel","AURORA"}	100	2026-02-12 03:33:36.503505	2026-02-11 02:16:29.188103
81d5d318-cab0-4bac-ac3f-107f6d41780b	spotify	0Ie5uiv54KgCr7P4sYDTHl	youtube	IJpeLGzJBGo	Baby It's Cold Outside	{"Idina Menzel","Michael Bublé"}	95	2026-02-12 03:33:36.514896	2026-02-11 02:16:30.435595
dbc74964-7240-4b3e-8f7c-2cea81def1e2	spotify	50WeOnXhM1H7AZEeIDoWfZ	youtube	7noPeO5sKHA	Show Yourself	{"Idina Menzel","Evan Rachel Wood"}	100	2026-02-12 03:33:36.520683	2026-02-11 02:16:31.704775
9a2bbc09-23ff-4d88-b6c9-98ec460ec697	spotify	0qcr5FMsEO85NAQjrlDRKo	youtube	qSU560anReg	Let It Go	{"Idina Menzel"}	95	2026-02-12 03:33:36.532751	2026-02-11 02:16:33.243663
1bf3570b-2a41-4eac-bcd3-16fb58b2f7a5	spotify	5Th9jSEXB7XdeQHXPkBvws	youtube	eMpLkkhPA70	Hateful Ever After	{"Ellise"}	87	2026-02-12 03:33:36.566728	2026-02-11 02:16:35.989997
228bba27-b672-453a-b043-758ee225d631	spotify	23KVefoON2iEz1dh940quQ	youtube	FX0NxcNPDOc	911	{"Ellise"}	87	2026-02-12 03:33:36.577904	2026-02-11 02:16:36.495875
4b0d44bb-6a5c-4991-947d-75b970b623f0	spotify	2f2hbFjim051DVx0o8o4rU	youtube	b_cFEOHaaDU	Nightmares	{"Ellise"}	84	2026-02-12 03:33:36.5878	2026-02-11 02:16:37.032338
77b108b5-0152-4fa1-9b75-e9edaa8bdbde	spotify	3yMC1KsTwh0ceXdIe4QQAQ	youtube	mzwg5Ldkt5Q	Fingers Crossed	{"Lauren Spencer Smith"}	87	2026-02-12 03:33:36.599166	2026-02-11 02:16:37.641802
3956133e-0dc4-4612-b466-869a41fabe35	spotify	6Z165JvPnS8PYvGW8oLGLc	youtube	J4UsVfwVIx8	Flowers	{"Lauren Spencer Smith"}	90	2026-02-12 03:33:36.610616	2026-02-11 02:16:38.256831
0b9e3c4a-f5f3-49e9-9ddf-75b00bb6e71e	spotify	23oYG6HAbahMQEtjk0M7GK	youtube	pTo_kZQ2rvs	Narcissist	{"Lauren Spencer Smith"}	90	2026-02-12 03:33:36.618701	2026-02-11 02:16:38.8292
c04ad351-a492-46e3-accd-9ecec72ba772	spotify	3hTSUqyG6ntAYXjgYOjS16	youtube	wAFfJl6wr-k	Single On The 25th	{"Lauren Spencer Smith"}	87	2026-02-12 03:33:36.62553	2026-02-11 02:16:39.379063
b5169137-b57b-4934-97cb-c2f42a4de6ae	spotify	2Obwg8XE4sWXLpJSDlxn1r	youtube	sF0BX5ECsKE	Back to Friends	{"Lauren Spencer Smith"}	98	2026-02-12 03:33:36.6316	2026-02-11 02:16:41.500358
ed7fd011-448d-494f-83e5-67a0632e84a5	spotify	2DB4DdfCFMw1iaR6JaR03a	youtube	nWr4r-3bHBc	Bam Bam	{"Camila Cabello","Ed Sheeran"}	87	2026-02-12 03:33:36.636504	2026-02-11 02:16:42.233139
db7a014d-73a0-4512-aac9-61a5c29dfe1e	spotify	6v3KW9xbzN5yKLt9YKDYA2	youtube	ahiXR3ZmraA	Señorita	{"Shawn Mendes","Camila Cabello"}	87	2026-02-12 03:33:36.644764	2026-02-11 02:16:42.78645
1518f0cb-64d1-4d3e-a44c-885e7fc46d54	spotify	1mXVgsBdtIVeCLJnSnmtdV	youtube	pmc93sA80HI	Too Good At Goodbyes	{"Sam Smith"}	100	2026-02-12 03:33:36.688858	2026-02-11 02:16:48.750014
f24f5689-7146-4efe-8652-e0c32691aea4	spotify	76cy1WJvNGJTj78UqeA5zr	youtube	j6jA6dCCToQ	IDGAF	{"Dua Lipa"}	90	2026-02-12 03:33:36.697397	2026-02-11 02:16:49.478515
b400ada6-14b9-4f20-b4b9-8e6fd0172a9b	spotify	2qxmye6gAegTMjLKEBoR3d	youtube	PKonqhZ5PsA	Let Me Down Slowly	{"Alec Benjamin"}	87	2026-02-12 03:33:36.702824	2026-02-11 02:16:50.134806
36e9e0ca-336e-41f3-916e-21ef84ed62eb	spotify	4vUmTMuQqjdnvlZmAH61Qk	youtube	q07RanslaGM	South of the Border	{"Ed Sheeran","Camila Cabello","Cardi B"}	87	2026-02-12 03:33:36.70873	2026-02-11 02:16:50.79694
8cc9320e-46aa-4655-aa7f-12168318541a	spotify	4umIPjkehX1r7uhmGvXiSV	youtube	RcSX0hOWcQ0	Intentions	{"Justin Bieber","Quavo"}	95	2026-02-12 03:33:36.714379	2026-02-11 02:16:52.138516
40c46fb8-7919-4c2c-a7b4-8ab8aa5a957d	spotify	52iLDrSoRtf9lcgFuLVfqE	youtube	r1Fx0tqK5Z4	Older	{"Sasha Alex Sloan"}	87	2026-02-12 03:33:36.719572	2026-02-11 02:16:53.056482
8b1d9fb6-d812-4775-96cc-b9be4f5eae66	spotify	0wI7QkCcs8FUQE1OkXUIqd	youtube	Em7vc8NWUNY	Scars To Your Beautiful	{"Alessia Cara"}	87	2026-02-12 03:33:36.725422	2026-02-11 02:16:53.70395
b35c731f-25ad-491b-8fa3-fd5285fad725	spotify	0EnS2KLYG9ig26yYltoXJf	youtube	_373lKEvQ34	Love Power	{"Idina Menzel"}	100	2026-02-12 03:33:36.538674	2026-02-11 02:16:34.505192
99b76797-5a42-49c7-b5a5-8895814064be	spotify	1rfofaqEpACxVEHIZBJe6W	youtube	HCjNJDNzw8Y	Havana	{"Camila Cabello","Young Thug"}	87	2026-02-12 03:33:36.649754	2026-02-11 02:16:43.390232
\.


--
-- Data for Name: transfer_items; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.transfer_items (id, job_id, source_track_id, source_metadata, target_track_id, target_metadata, status, confidence_score, created_at) FROM stdin;
330dc784-6569-453f-8dbc-2cf877f98b4f	2e6052bd-f046-45d6-8c97-37442649f2b3	2J2Z1SkXYghSajLibnQHOa	\N	cfhwSpSDA9c	\N	matched	100	2026-02-11 02:43:05.552972
4646bfe2-14c2-4a73-abfe-46d0ab447b02	2e6052bd-f046-45d6-8c97-37442649f2b3	4VrWlk8IQxevMvERoX08iC	\N	uzaYLK3k0DQ	\N	matched	100	2026-02-11 02:43:05.552972
15e97c29-6eb3-4401-9040-3591e9f5bb50	2e6052bd-f046-45d6-8c97-37442649f2b3	27SdWb2rFzO6GWiYDBTD9j	\N	J1b22l1kFKY	\N	matched	87	2026-02-11 02:43:05.552972
f4b893b6-05da-440c-bfb4-d53940310123	2e6052bd-f046-45d6-8c97-37442649f2b3	1rMlnD8S0hX7IHZXZDt3gH	\N	oa7JwwdiA98	\N	matched	95	2026-02-11 02:43:05.552972
85e5617c-77e4-457c-9119-723f10a002b0	2e6052bd-f046-45d6-8c97-37442649f2b3	1v1oIWf2Xgh54kIWuKsDf6	\N	d_HlPboLRL8	\N	matched	87	2026-02-11 02:43:05.552972
b9ebdcd4-1b21-4816-a32e-59386808ae66	2e6052bd-f046-45d6-8c97-37442649f2b3	2MbgQKWhoii8bqsMBWWWgA	\N	mWQACEqf4QY	\N	matched	87	2026-02-11 02:43:05.552972
93d9e8f7-3a0a-4d5a-817e-271be3332100	2e6052bd-f046-45d6-8c97-37442649f2b3	5zHX1aFxP5p10NI4L1nkai	\N	4FrYREQ4XP8	\N	matched	87	2026-02-11 02:43:05.552972
660f8570-56d3-4aa0-af8f-6bc9e620f581	2e6052bd-f046-45d6-8c97-37442649f2b3	2rbDhOo9Fh61Bbu23T2qCk	\N	Voo86mlxZvA	\N	matched	100	2026-02-11 02:43:05.552972
b5ee8a61-f30e-4a12-a15d-8555e40e3c3b	2e6052bd-f046-45d6-8c97-37442649f2b3	3Z0oQ8r78OUaHvGPiDBR3W	\N	2qPlX1FMJXI	\N	matched	100	2026-02-11 02:43:05.552972
d3f04eb7-9cd9-40e2-a152-0842d2230a89	2e6052bd-f046-45d6-8c97-37442649f2b3	0Ie5uiv54KgCr7P4sYDTHl	\N	IJpeLGzJBGo	\N	matched	95	2026-02-11 02:43:05.552972
bad802e0-6bed-4af4-b01b-ca9232f6c743	2e6052bd-f046-45d6-8c97-37442649f2b3	50WeOnXhM1H7AZEeIDoWfZ	\N	7noPeO5sKHA	\N	matched	100	2026-02-11 02:43:05.552972
6d599910-0f9f-4aa2-83d7-bfd0d714a3c5	2e6052bd-f046-45d6-8c97-37442649f2b3	0qcr5FMsEO85NAQjrlDRKo	\N	qSU560anReg	\N	matched	95	2026-02-11 02:43:05.552972
535a58d0-36c3-44b8-b3fb-11b6742350ba	2e6052bd-f046-45d6-8c97-37442649f2b3	0EnS2KLYG9ig26yYltoXJf	\N	_373lKEvQ34	\N	matched	100	2026-02-11 02:43:05.552972
ecddba34-a31f-4432-8867-e23ff0504864	2e6052bd-f046-45d6-8c97-37442649f2b3	4crTd5bkKKISvU2w9eEc0r	\N	HA0vlhJANow	\N	matched	87	2026-02-11 02:43:05.552972
8ac99086-cb35-4986-b1a3-187172bab3c9	2e6052bd-f046-45d6-8c97-37442649f2b3	3ZmEwYX2GbUUM9C2rpEhkm	\N	WhG0-WO0vLE	\N	matched	87	2026-02-11 02:43:05.552972
581eb070-5882-498f-b6d5-e5f0cfbc5c48	2e6052bd-f046-45d6-8c97-37442649f2b3	5Th9jSEXB7XdeQHXPkBvws	\N	eMpLkkhPA70	\N	matched	87	2026-02-11 02:43:05.552972
c52d2bfb-bcc8-4cb8-b399-908edfb2a09f	2e6052bd-f046-45d6-8c97-37442649f2b3	23KVefoON2iEz1dh940quQ	\N	FX0NxcNPDOc	\N	matched	87	2026-02-11 02:43:05.552972
639f2697-c7a4-4c5e-8e10-b4b160227df0	2e6052bd-f046-45d6-8c97-37442649f2b3	2f2hbFjim051DVx0o8o4rU	\N	b_cFEOHaaDU	\N	matched	84	2026-02-11 02:43:05.552972
d5d123fa-111a-4dda-a1eb-3b5bdd73f630	2e6052bd-f046-45d6-8c97-37442649f2b3	3yMC1KsTwh0ceXdIe4QQAQ	\N	mzwg5Ldkt5Q	\N	matched	87	2026-02-11 02:43:05.552972
12086b40-d076-4ebf-9bcb-63eb527bd781	2e6052bd-f046-45d6-8c97-37442649f2b3	6Z165JvPnS8PYvGW8oLGLc	\N	J4UsVfwVIx8	\N	matched	90	2026-02-11 02:43:05.552972
50cc5605-fc8b-47a8-9394-a1ae52dd9f44	2e6052bd-f046-45d6-8c97-37442649f2b3	23oYG6HAbahMQEtjk0M7GK	\N	pTo_kZQ2rvs	\N	matched	90	2026-02-11 02:43:05.552972
97c1a40d-4a14-4f88-ac54-30fe65525870	2e6052bd-f046-45d6-8c97-37442649f2b3	3hTSUqyG6ntAYXjgYOjS16	\N	wAFfJl6wr-k	\N	matched	87	2026-02-11 02:43:05.552972
ce590470-3434-4c85-bfe9-c9459c94319d	2e6052bd-f046-45d6-8c97-37442649f2b3	2Obwg8XE4sWXLpJSDlxn1r	\N	sF0BX5ECsKE	\N	matched	98	2026-02-11 02:43:05.552972
f0bdd90a-cee0-4182-9441-7fad20419d17	2e6052bd-f046-45d6-8c97-37442649f2b3	2DB4DdfCFMw1iaR6JaR03a	\N	nWr4r-3bHBc	\N	matched	87	2026-02-11 02:43:05.552972
3f76582b-8c3f-4873-a95f-d03565f9cd20	2e6052bd-f046-45d6-8c97-37442649f2b3	6v3KW9xbzN5yKLt9YKDYA2	\N	ahiXR3ZmraA	\N	matched	87	2026-02-11 02:43:05.552972
48773e8a-88ed-422f-b74b-e95a98afcf0a	2e6052bd-f046-45d6-8c97-37442649f2b3	1rfofaqEpACxVEHIZBJe6W	\N	HCjNJDNzw8Y	\N	matched	87	2026-02-11 02:43:05.552972
b56d1ff0-316b-46fe-8d20-aade6b2c708e	2e6052bd-f046-45d6-8c97-37442649f2b3	2ogKhhoMClkFXek7ZgxAhN	\N	zAw3MLNU56s	\N	matched	100	2026-02-11 02:43:05.552972
bc66b303-5187-4584-a73d-01d94e3e51b8	2e6052bd-f046-45d6-8c97-37442649f2b3	3yOlyBJuViE2YSGn3nVE1K	\N	TduHf1tWDvI	\N	matched	87	2026-02-11 02:43:05.552972
b40e3572-fd37-459e-934d-30476f74988b	2e6052bd-f046-45d6-8c97-37442649f2b3	073ILU1C3APNRGF66VhtyA	\N	6-OvO8ZuW98	\N	matched	87	2026-02-11 02:43:05.552972
6cae13cc-3f2b-453d-9e65-5a7cc7ebe141	2e6052bd-f046-45d6-8c97-37442649f2b3	6VpQdig9pdpTSIFItgkJV5	\N	FITSPSA8gQs	\N	matched	87	2026-02-11 02:43:05.552972
8f9890cb-e4c7-43e7-be79-ba174624f23b	2e6052bd-f046-45d6-8c97-37442649f2b3	705dwKmZCd7ImAUyyD6Mpm	\N	mcr-BYQBqXw	\N	matched	87	2026-02-11 02:43:05.552972
a2c873f5-7e71-4ef5-86a6-1ab196cdba8a	2e6052bd-f046-45d6-8c97-37442649f2b3	1mXVgsBdtIVeCLJnSnmtdV	\N	pmc93sA80HI	\N	matched	100	2026-02-11 02:43:05.552972
b7a09a12-6e69-4864-8aae-f70c47b69954	2e6052bd-f046-45d6-8c97-37442649f2b3	76cy1WJvNGJTj78UqeA5zr	\N	j6jA6dCCToQ	\N	matched	90	2026-02-11 02:43:05.552972
e2b92de9-f0e7-4427-bb98-f52ffb4a73ce	2e6052bd-f046-45d6-8c97-37442649f2b3	2qxmye6gAegTMjLKEBoR3d	\N	PKonqhZ5PsA	\N	matched	87	2026-02-11 02:43:05.552972
915b3943-19b6-4692-bc77-2128e2986a26	2e6052bd-f046-45d6-8c97-37442649f2b3	4vUmTMuQqjdnvlZmAH61Qk	\N	q07RanslaGM	\N	matched	87	2026-02-11 02:43:05.552972
73c3b0f4-77ed-4885-8196-5c80e6b628ee	2e6052bd-f046-45d6-8c97-37442649f2b3	4umIPjkehX1r7uhmGvXiSV	\N	RcSX0hOWcQ0	\N	matched	95	2026-02-11 02:43:05.552972
92ac7eb6-1b1f-469c-9cdd-d6421b1af93b	2e6052bd-f046-45d6-8c97-37442649f2b3	52iLDrSoRtf9lcgFuLVfqE	\N	r1Fx0tqK5Z4	\N	matched	87	2026-02-11 02:43:05.552972
70833fbe-6297-405c-b66c-5e7d0be4e8ff	2e6052bd-f046-45d6-8c97-37442649f2b3	0wI7QkCcs8FUQE1OkXUIqd	\N	Em7vc8NWUNY	\N	matched	87	2026-02-11 02:43:05.552972
ef0a540c-ab95-4152-8f27-4408bf93b6d6	2e6052bd-f046-45d6-8c97-37442649f2b3	4l0Mvzj72xxOpRrp6h8nHi	\N	zlJDTxahav0	\N	matched	87	2026-02-11 02:43:05.552972
82b77929-c77f-4cae-a702-0fd7a7a90664	2e6052bd-f046-45d6-8c97-37442649f2b3	1J14CdDAvBTE1AJYUOwl6C	\N	51u5fnyrGj4	\N	matched	87	2026-02-11 02:43:05.552972
8036b3b0-2831-4be9-951f-125c5697b352	2e6052bd-f046-45d6-8c97-37442649f2b3	0Gl5s8IhMmQE5YQwM8Qx1J	\N	nT0CYXcre0c	\N	matched	100	2026-02-11 02:43:05.552972
c23b5467-917a-4350-a0c7-6b5ff874204f	bfc1a6fa-6041-4f32-83ec-04a71f66fd8a	2J2Z1SkXYghSajLibnQHOa	\N	cfhwSpSDA9c	\N	matched	100	2026-02-12 02:13:16.385484
b6591ee0-60d7-4e50-9611-920785f91659	bfc1a6fa-6041-4f32-83ec-04a71f66fd8a	4VrWlk8IQxevMvERoX08iC	\N	uzaYLK3k0DQ	\N	matched	100	2026-02-12 02:13:16.385484
b0eb9190-f077-483c-be3e-65582028845d	bfc1a6fa-6041-4f32-83ec-04a71f66fd8a	27SdWb2rFzO6GWiYDBTD9j	\N	J1b22l1kFKY	\N	matched	87	2026-02-12 02:13:16.385484
b55df874-1407-4d88-bb33-4538e5644544	bfc1a6fa-6041-4f32-83ec-04a71f66fd8a	1rMlnD8S0hX7IHZXZDt3gH	\N	oa7JwwdiA98	\N	matched	95	2026-02-12 02:13:16.385484
32d7d195-f3ff-4460-9bda-0ae6b401fadb	bfc1a6fa-6041-4f32-83ec-04a71f66fd8a	1v1oIWf2Xgh54kIWuKsDf6	\N	d_HlPboLRL8	\N	matched	87	2026-02-12 02:13:16.385484
63329537-457e-42f0-ae51-6bd41e07c147	bfc1a6fa-6041-4f32-83ec-04a71f66fd8a	2MbgQKWhoii8bqsMBWWWgA	\N	mWQACEqf4QY	\N	matched	87	2026-02-12 02:13:16.385484
8ef9cc1e-65c3-40af-9a52-cb9cef606dce	bfc1a6fa-6041-4f32-83ec-04a71f66fd8a	5zHX1aFxP5p10NI4L1nkai	\N	4FrYREQ4XP8	\N	matched	87	2026-02-12 02:13:16.385484
32077d07-b9e2-4b6f-acbc-b216b51e7b2d	bfc1a6fa-6041-4f32-83ec-04a71f66fd8a	2rbDhOo9Fh61Bbu23T2qCk	\N	Voo86mlxZvA	\N	matched	100	2026-02-12 02:13:16.385484
30c905df-2484-4ffe-9981-bd08953cbc55	bfc1a6fa-6041-4f32-83ec-04a71f66fd8a	3Z0oQ8r78OUaHvGPiDBR3W	\N	2qPlX1FMJXI	\N	matched	100	2026-02-12 02:13:16.385484
f8de6572-9523-49fd-8598-3fb67be5b236	bfc1a6fa-6041-4f32-83ec-04a71f66fd8a	0Ie5uiv54KgCr7P4sYDTHl	\N	IJpeLGzJBGo	\N	matched	95	2026-02-12 02:13:16.385484
e73bd1e3-8122-42e6-abd8-9a9b883d9fb9	bfc1a6fa-6041-4f32-83ec-04a71f66fd8a	50WeOnXhM1H7AZEeIDoWfZ	\N	7noPeO5sKHA	\N	matched	100	2026-02-12 02:13:16.385484
76e48f7d-a564-467b-bf91-6511f9787f4a	bfc1a6fa-6041-4f32-83ec-04a71f66fd8a	0qcr5FMsEO85NAQjrlDRKo	\N	qSU560anReg	\N	matched	95	2026-02-12 02:13:16.385484
8a3c5d36-b6d4-420b-838b-59e766f06471	bfc1a6fa-6041-4f32-83ec-04a71f66fd8a	0EnS2KLYG9ig26yYltoXJf	\N	_373lKEvQ34	\N	matched	100	2026-02-12 02:13:16.385484
72a48e91-cff0-4633-a9dc-b7cb54305828	bfc1a6fa-6041-4f32-83ec-04a71f66fd8a	4crTd5bkKKISvU2w9eEc0r	\N	HA0vlhJANow	\N	matched	87	2026-02-12 02:13:16.385484
1cc2fce7-7266-4d50-96da-97ce29e6d095	bfc1a6fa-6041-4f32-83ec-04a71f66fd8a	3ZmEwYX2GbUUM9C2rpEhkm	\N	WhG0-WO0vLE	\N	matched	87	2026-02-12 02:13:16.385484
de6a125d-edc5-4a6c-b885-e6d985c46749	bfc1a6fa-6041-4f32-83ec-04a71f66fd8a	5Th9jSEXB7XdeQHXPkBvws	\N	eMpLkkhPA70	\N	matched	87	2026-02-12 02:13:16.385484
aa4b155b-0c82-4c74-b792-7cd78d4720d4	bfc1a6fa-6041-4f32-83ec-04a71f66fd8a	23KVefoON2iEz1dh940quQ	\N	FX0NxcNPDOc	\N	matched	87	2026-02-12 02:13:16.385484
a2852b69-5096-464c-93a3-ad69f8484779	bfc1a6fa-6041-4f32-83ec-04a71f66fd8a	2f2hbFjim051DVx0o8o4rU	\N	b_cFEOHaaDU	\N	matched	84	2026-02-12 02:13:16.385484
3944c756-1434-4918-8b2a-b0fe7eeddc11	bfc1a6fa-6041-4f32-83ec-04a71f66fd8a	3yMC1KsTwh0ceXdIe4QQAQ	\N	mzwg5Ldkt5Q	\N	matched	87	2026-02-12 02:13:16.385484
a954f8dc-bacb-47a4-b24f-a4e6c9021a0a	bfc1a6fa-6041-4f32-83ec-04a71f66fd8a	6Z165JvPnS8PYvGW8oLGLc	\N	J4UsVfwVIx8	\N	matched	90	2026-02-12 02:13:16.385484
518dec07-6f3c-4161-aec8-04fb1a185a6d	bfc1a6fa-6041-4f32-83ec-04a71f66fd8a	23oYG6HAbahMQEtjk0M7GK	\N	pTo_kZQ2rvs	\N	matched	90	2026-02-12 02:13:16.385484
6606d97b-d663-4187-9b4f-dbd6a416511f	bfc1a6fa-6041-4f32-83ec-04a71f66fd8a	3hTSUqyG6ntAYXjgYOjS16	\N	wAFfJl6wr-k	\N	matched	87	2026-02-12 02:13:16.385484
fdf5d644-b6a7-41d5-a4d6-3bb2cb0f1d50	bfc1a6fa-6041-4f32-83ec-04a71f66fd8a	2Obwg8XE4sWXLpJSDlxn1r	\N	sF0BX5ECsKE	\N	matched	98	2026-02-12 02:13:16.385484
c99868dd-6646-4fdd-afc1-70ab0d37d0bd	bfc1a6fa-6041-4f32-83ec-04a71f66fd8a	2DB4DdfCFMw1iaR6JaR03a	\N	nWr4r-3bHBc	\N	matched	87	2026-02-12 02:13:16.385484
a703995d-a7f9-42da-9012-e52f9a9b16eb	bfc1a6fa-6041-4f32-83ec-04a71f66fd8a	6v3KW9xbzN5yKLt9YKDYA2	\N	ahiXR3ZmraA	\N	matched	87	2026-02-12 02:13:16.385484
e76e249e-e359-487f-ab41-3eab1c497e8e	bfc1a6fa-6041-4f32-83ec-04a71f66fd8a	1rfofaqEpACxVEHIZBJe6W	\N	HCjNJDNzw8Y	\N	matched	87	2026-02-12 02:13:16.385484
1c4f0c95-c905-47e8-b864-cfb1f4cdbb4e	bfc1a6fa-6041-4f32-83ec-04a71f66fd8a	2ogKhhoMClkFXek7ZgxAhN	\N	zAw3MLNU56s	\N	matched	100	2026-02-12 02:13:16.385484
f115ad66-01ce-4547-8076-aae8d7c64a2c	bfc1a6fa-6041-4f32-83ec-04a71f66fd8a	3yOlyBJuViE2YSGn3nVE1K	\N	TduHf1tWDvI	\N	matched	87	2026-02-12 02:13:16.385484
c97d94f4-72dd-4e2e-b444-77e80d8e4ead	bfc1a6fa-6041-4f32-83ec-04a71f66fd8a	073ILU1C3APNRGF66VhtyA	\N	6-OvO8ZuW98	\N	matched	87	2026-02-12 02:13:16.385484
0d1966ee-c2f6-4f13-b784-4f39a5cbc669	bfc1a6fa-6041-4f32-83ec-04a71f66fd8a	6VpQdig9pdpTSIFItgkJV5	\N	FITSPSA8gQs	\N	matched	87	2026-02-12 02:13:16.385484
83610c68-1ab8-431a-bfe8-d440f58b1f34	bfc1a6fa-6041-4f32-83ec-04a71f66fd8a	705dwKmZCd7ImAUyyD6Mpm	\N	mcr-BYQBqXw	\N	matched	87	2026-02-12 02:13:16.385484
1b41defb-26ab-4a38-9608-6187963ef554	bfc1a6fa-6041-4f32-83ec-04a71f66fd8a	1mXVgsBdtIVeCLJnSnmtdV	\N	pmc93sA80HI	\N	matched	100	2026-02-12 02:13:16.385484
e9bd1ec0-804a-475d-8c46-142bd0233985	bfc1a6fa-6041-4f32-83ec-04a71f66fd8a	76cy1WJvNGJTj78UqeA5zr	\N	j6jA6dCCToQ	\N	matched	90	2026-02-12 02:13:16.385484
a0770a0f-fb7f-479b-a681-2b722750a478	bfc1a6fa-6041-4f32-83ec-04a71f66fd8a	2qxmye6gAegTMjLKEBoR3d	\N	PKonqhZ5PsA	\N	matched	87	2026-02-12 02:13:16.385484
b86dc722-6a0c-435b-81c3-8f7070f162c8	bfc1a6fa-6041-4f32-83ec-04a71f66fd8a	4vUmTMuQqjdnvlZmAH61Qk	\N	q07RanslaGM	\N	matched	87	2026-02-12 02:13:16.385484
4d58de20-8fd6-4287-9bd7-a9c6b5ac6e74	bfc1a6fa-6041-4f32-83ec-04a71f66fd8a	4umIPjkehX1r7uhmGvXiSV	\N	RcSX0hOWcQ0	\N	matched	95	2026-02-12 02:13:16.385484
17bcc87f-1fc7-4174-838d-54a231d63afd	bfc1a6fa-6041-4f32-83ec-04a71f66fd8a	52iLDrSoRtf9lcgFuLVfqE	\N	r1Fx0tqK5Z4	\N	matched	87	2026-02-12 02:13:16.385484
516e9f19-f91e-4719-9677-12fcabc76624	bfc1a6fa-6041-4f32-83ec-04a71f66fd8a	0wI7QkCcs8FUQE1OkXUIqd	\N	Em7vc8NWUNY	\N	matched	87	2026-02-12 02:13:16.385484
1d2dc42e-b1ed-4ca8-b8c9-0c6913aae6db	bfc1a6fa-6041-4f32-83ec-04a71f66fd8a	4l0Mvzj72xxOpRrp6h8nHi	\N	zlJDTxahav0	\N	matched	87	2026-02-12 02:13:16.385484
476f1ccd-1032-4a21-82ed-9399e5de0776	bfc1a6fa-6041-4f32-83ec-04a71f66fd8a	1J14CdDAvBTE1AJYUOwl6C	\N	51u5fnyrGj4	\N	matched	87	2026-02-12 02:13:16.385484
600d20ff-7dc5-4ae5-9468-5ece6b856a3e	bfc1a6fa-6041-4f32-83ec-04a71f66fd8a	0Gl5s8IhMmQE5YQwM8Qx1J	\N	nT0CYXcre0c	\N	matched	100	2026-02-12 02:13:16.385484
c0871364-589f-42ba-874c-a9130eb847ae	b6e1b364-3fd9-4348-bf7e-c99488c36037	2J2Z1SkXYghSajLibnQHOa	\N	cfhwSpSDA9c	\N	matched	100	2026-02-12 02:22:34.182255
5a10bf04-174c-40b8-b52b-7901a1011067	b6e1b364-3fd9-4348-bf7e-c99488c36037	4VrWlk8IQxevMvERoX08iC	\N	uzaYLK3k0DQ	\N	matched	100	2026-02-12 02:22:34.182255
37d78436-f667-4757-83a1-13497e6e08ef	b6e1b364-3fd9-4348-bf7e-c99488c36037	27SdWb2rFzO6GWiYDBTD9j	\N	J1b22l1kFKY	\N	matched	87	2026-02-12 02:22:34.182255
aef0b2e0-927b-461b-adfb-86b921dfc56a	b6e1b364-3fd9-4348-bf7e-c99488c36037	1rMlnD8S0hX7IHZXZDt3gH	\N	oa7JwwdiA98	\N	matched	95	2026-02-12 02:22:34.182255
3e36d887-53ef-41af-9e31-8e0f7d9b5c36	b6e1b364-3fd9-4348-bf7e-c99488c36037	1v1oIWf2Xgh54kIWuKsDf6	\N	d_HlPboLRL8	\N	matched	87	2026-02-12 02:22:34.182255
cf26aa6d-212c-462a-9cc6-17c016da411d	b6e1b364-3fd9-4348-bf7e-c99488c36037	2MbgQKWhoii8bqsMBWWWgA	\N	mWQACEqf4QY	\N	matched	87	2026-02-12 02:22:34.182255
e1d313c2-4f5d-4341-8fa8-b5829a4eeab5	b6e1b364-3fd9-4348-bf7e-c99488c36037	5zHX1aFxP5p10NI4L1nkai	\N	4FrYREQ4XP8	\N	matched	87	2026-02-12 02:22:34.182255
4a5b26d9-285d-41f7-a39e-60e422059e60	b6e1b364-3fd9-4348-bf7e-c99488c36037	2rbDhOo9Fh61Bbu23T2qCk	\N	Voo86mlxZvA	\N	matched	100	2026-02-12 02:22:34.182255
c0b8e32f-0eaf-47f1-9f8a-e764836d1b9d	b6e1b364-3fd9-4348-bf7e-c99488c36037	3Z0oQ8r78OUaHvGPiDBR3W	\N	2qPlX1FMJXI	\N	matched	100	2026-02-12 02:22:34.182255
905a9948-5264-4b89-ab1c-6217b067bffe	b6e1b364-3fd9-4348-bf7e-c99488c36037	0Ie5uiv54KgCr7P4sYDTHl	\N	IJpeLGzJBGo	\N	matched	95	2026-02-12 02:22:34.182255
43f5e2e4-42c4-4126-ae5b-e6673fb3e8ac	b6e1b364-3fd9-4348-bf7e-c99488c36037	50WeOnXhM1H7AZEeIDoWfZ	\N	7noPeO5sKHA	\N	matched	100	2026-02-12 02:22:34.182255
96170a54-42ab-4290-aaa7-d7b3810f2121	b6e1b364-3fd9-4348-bf7e-c99488c36037	0qcr5FMsEO85NAQjrlDRKo	\N	qSU560anReg	\N	matched	95	2026-02-12 02:22:34.182255
d39cde05-f6ad-413f-b2b7-f4c0f9a359f2	b6e1b364-3fd9-4348-bf7e-c99488c36037	0EnS2KLYG9ig26yYltoXJf	\N	_373lKEvQ34	\N	matched	100	2026-02-12 02:22:34.182255
01ba03a8-ca7d-4097-ac56-22e47d316f11	b6e1b364-3fd9-4348-bf7e-c99488c36037	4crTd5bkKKISvU2w9eEc0r	\N	HA0vlhJANow	\N	matched	87	2026-02-12 02:22:34.182255
0696f175-dd91-49c0-95ac-d8b9e6ed934e	b6e1b364-3fd9-4348-bf7e-c99488c36037	3ZmEwYX2GbUUM9C2rpEhkm	\N	WhG0-WO0vLE	\N	matched	87	2026-02-12 02:22:34.182255
8957320e-a53d-4b10-acdf-99809eef00c0	b6e1b364-3fd9-4348-bf7e-c99488c36037	5Th9jSEXB7XdeQHXPkBvws	\N	eMpLkkhPA70	\N	matched	87	2026-02-12 02:22:34.182255
200b5887-1d37-4697-bc7e-24d6906ba506	b6e1b364-3fd9-4348-bf7e-c99488c36037	23KVefoON2iEz1dh940quQ	\N	FX0NxcNPDOc	\N	matched	87	2026-02-12 02:22:34.182255
26f62b43-7de1-4747-9169-230b9f023c42	b6e1b364-3fd9-4348-bf7e-c99488c36037	2f2hbFjim051DVx0o8o4rU	\N	b_cFEOHaaDU	\N	matched	84	2026-02-12 02:22:34.182255
f25f3ae5-aea9-4bff-883e-6b04ef84baed	b6e1b364-3fd9-4348-bf7e-c99488c36037	3yMC1KsTwh0ceXdIe4QQAQ	\N	mzwg5Ldkt5Q	\N	matched	87	2026-02-12 02:22:34.182255
c0327cc5-2c09-4090-8284-c36d6e3f038b	b6e1b364-3fd9-4348-bf7e-c99488c36037	6Z165JvPnS8PYvGW8oLGLc	\N	J4UsVfwVIx8	\N	matched	90	2026-02-12 02:22:34.182255
60a89896-2760-4092-9978-ec161548e8fe	b6e1b364-3fd9-4348-bf7e-c99488c36037	23oYG6HAbahMQEtjk0M7GK	\N	pTo_kZQ2rvs	\N	matched	90	2026-02-12 02:22:34.182255
b5844843-e1c4-4c9a-8394-261b71dafc25	b6e1b364-3fd9-4348-bf7e-c99488c36037	3hTSUqyG6ntAYXjgYOjS16	\N	wAFfJl6wr-k	\N	matched	87	2026-02-12 02:22:34.182255
7c32f92b-a625-4c65-b800-ec0b0e983c71	b6e1b364-3fd9-4348-bf7e-c99488c36037	2Obwg8XE4sWXLpJSDlxn1r	\N	sF0BX5ECsKE	\N	matched	98	2026-02-12 02:22:34.182255
09436d1f-e70a-4eab-a08c-b8faa96de242	b6e1b364-3fd9-4348-bf7e-c99488c36037	2DB4DdfCFMw1iaR6JaR03a	\N	nWr4r-3bHBc	\N	matched	87	2026-02-12 02:22:34.182255
27705821-619e-4ac9-a6ef-f66cc42a0141	b6e1b364-3fd9-4348-bf7e-c99488c36037	6v3KW9xbzN5yKLt9YKDYA2	\N	ahiXR3ZmraA	\N	matched	87	2026-02-12 02:22:34.182255
de2fb1ca-51e4-4da6-911a-e334cd7e939b	b6e1b364-3fd9-4348-bf7e-c99488c36037	1rfofaqEpACxVEHIZBJe6W	\N	HCjNJDNzw8Y	\N	matched	87	2026-02-12 02:22:34.182255
a19fd752-1716-4aaa-8f69-04f3b98251c5	b6e1b364-3fd9-4348-bf7e-c99488c36037	2ogKhhoMClkFXek7ZgxAhN	\N	zAw3MLNU56s	\N	matched	100	2026-02-12 02:22:34.182255
0006036b-abbd-408f-9282-20f979e80985	b6e1b364-3fd9-4348-bf7e-c99488c36037	3yOlyBJuViE2YSGn3nVE1K	\N	TduHf1tWDvI	\N	matched	87	2026-02-12 02:22:34.182255
a3e69ae8-14f0-4b81-9cf8-0966959efc62	b6e1b364-3fd9-4348-bf7e-c99488c36037	073ILU1C3APNRGF66VhtyA	\N	6-OvO8ZuW98	\N	matched	87	2026-02-12 02:22:34.182255
896fcf9c-c2c5-47b2-a473-ddce39d9e2d7	b6e1b364-3fd9-4348-bf7e-c99488c36037	6VpQdig9pdpTSIFItgkJV5	\N	FITSPSA8gQs	\N	matched	87	2026-02-12 02:22:34.182255
98b2b1d9-487a-496b-8e44-3ea752258038	b6e1b364-3fd9-4348-bf7e-c99488c36037	705dwKmZCd7ImAUyyD6Mpm	\N	mcr-BYQBqXw	\N	matched	87	2026-02-12 02:22:34.182255
7beff46b-56c4-4be4-b2a7-189fc183d1b0	b6e1b364-3fd9-4348-bf7e-c99488c36037	1mXVgsBdtIVeCLJnSnmtdV	\N	pmc93sA80HI	\N	matched	100	2026-02-12 02:22:34.182255
3b8cc8ee-fe6b-4ac3-b6a4-66d82371e88a	b6e1b364-3fd9-4348-bf7e-c99488c36037	76cy1WJvNGJTj78UqeA5zr	\N	j6jA6dCCToQ	\N	matched	90	2026-02-12 02:22:34.182255
17dea99b-7270-4085-b9e1-ed43321cbed3	b6e1b364-3fd9-4348-bf7e-c99488c36037	2qxmye6gAegTMjLKEBoR3d	\N	PKonqhZ5PsA	\N	matched	87	2026-02-12 02:22:34.182255
548b8a0d-e4cb-424d-b10f-57e3ffcf4909	b6e1b364-3fd9-4348-bf7e-c99488c36037	4vUmTMuQqjdnvlZmAH61Qk	\N	q07RanslaGM	\N	matched	87	2026-02-12 02:22:34.182255
764235d8-0f69-4533-88c9-8b92305bf784	b6e1b364-3fd9-4348-bf7e-c99488c36037	4umIPjkehX1r7uhmGvXiSV	\N	RcSX0hOWcQ0	\N	matched	95	2026-02-12 02:22:34.182255
96026a27-5384-4a26-b5e6-1c2516f9c749	b6e1b364-3fd9-4348-bf7e-c99488c36037	52iLDrSoRtf9lcgFuLVfqE	\N	r1Fx0tqK5Z4	\N	matched	87	2026-02-12 02:22:34.182255
a909212e-6a51-46a3-8828-1a660e8e4903	b6e1b364-3fd9-4348-bf7e-c99488c36037	0wI7QkCcs8FUQE1OkXUIqd	\N	Em7vc8NWUNY	\N	matched	87	2026-02-12 02:22:34.182255
c87916fb-9e3e-463b-8d4b-2b1385a0018d	b6e1b364-3fd9-4348-bf7e-c99488c36037	4l0Mvzj72xxOpRrp6h8nHi	\N	zlJDTxahav0	\N	matched	87	2026-02-12 02:22:34.182255
aabb85aa-b707-4180-af7a-95e323d71f88	b6e1b364-3fd9-4348-bf7e-c99488c36037	1J14CdDAvBTE1AJYUOwl6C	\N	51u5fnyrGj4	\N	matched	87	2026-02-12 02:22:34.182255
fb3d17a9-d471-434d-8742-4e9336e2804b	b6e1b364-3fd9-4348-bf7e-c99488c36037	0Gl5s8IhMmQE5YQwM8Qx1J	\N	nT0CYXcre0c	\N	matched	100	2026-02-12 02:22:34.182255
ae0df86d-2319-47fa-81b1-b77482588190	2ca9b045-a083-40ef-b933-a070cc7594ce	2J2Z1SkXYghSajLibnQHOa	\N	cfhwSpSDA9c	\N	matched	100	2026-02-12 02:37:49.398212
d6906e98-c45d-423a-8d13-f14aebe20820	2ca9b045-a083-40ef-b933-a070cc7594ce	4VrWlk8IQxevMvERoX08iC	\N	uzaYLK3k0DQ	\N	matched	100	2026-02-12 02:37:49.398212
36566e1a-bbdc-44f7-8bda-3a1cee29ddc1	2ca9b045-a083-40ef-b933-a070cc7594ce	27SdWb2rFzO6GWiYDBTD9j	\N	J1b22l1kFKY	\N	matched	87	2026-02-12 02:37:49.398212
e6fc354c-9d0b-4c12-949d-c0f474ab3a03	2ca9b045-a083-40ef-b933-a070cc7594ce	1rMlnD8S0hX7IHZXZDt3gH	\N	oa7JwwdiA98	\N	matched	95	2026-02-12 02:37:49.398212
66c623c5-d904-4994-98e1-14b5e0fb1151	2ca9b045-a083-40ef-b933-a070cc7594ce	1v1oIWf2Xgh54kIWuKsDf6	\N	d_HlPboLRL8	\N	matched	87	2026-02-12 02:37:49.398212
a972a160-c49c-4745-8a6e-9b87d6139b64	2ca9b045-a083-40ef-b933-a070cc7594ce	2MbgQKWhoii8bqsMBWWWgA	\N	mWQACEqf4QY	\N	matched	87	2026-02-12 02:37:49.398212
e4e230ac-78d2-40d6-b829-e352f1bffa2d	2ca9b045-a083-40ef-b933-a070cc7594ce	5zHX1aFxP5p10NI4L1nkai	\N	4FrYREQ4XP8	\N	matched	87	2026-02-12 02:37:49.398212
3c5fdb8d-4fbf-4539-99ca-190046795d78	2ca9b045-a083-40ef-b933-a070cc7594ce	2rbDhOo9Fh61Bbu23T2qCk	\N	Voo86mlxZvA	\N	matched	100	2026-02-12 02:37:49.398212
1a47798e-57c3-490b-8e87-b38732225cf5	2ca9b045-a083-40ef-b933-a070cc7594ce	3Z0oQ8r78OUaHvGPiDBR3W	\N	2qPlX1FMJXI	\N	matched	100	2026-02-12 02:37:49.398212
a13b46f2-8995-40da-bd30-378b812a15bb	2ca9b045-a083-40ef-b933-a070cc7594ce	0Ie5uiv54KgCr7P4sYDTHl	\N	IJpeLGzJBGo	\N	matched	95	2026-02-12 02:37:49.398212
2c7db2d9-ae04-4498-bc80-b406ae22ffeb	2ca9b045-a083-40ef-b933-a070cc7594ce	50WeOnXhM1H7AZEeIDoWfZ	\N	7noPeO5sKHA	\N	matched	100	2026-02-12 02:37:49.398212
858efce1-75b8-4ca1-b987-85f1979b5058	2ca9b045-a083-40ef-b933-a070cc7594ce	0qcr5FMsEO85NAQjrlDRKo	\N	qSU560anReg	\N	matched	95	2026-02-12 02:37:49.398212
186af8e0-412d-4c19-8ddf-ce444deb57cd	2ca9b045-a083-40ef-b933-a070cc7594ce	0EnS2KLYG9ig26yYltoXJf	\N	_373lKEvQ34	\N	matched	100	2026-02-12 02:37:49.398212
8938a385-9979-46e0-b649-4c77d24491f9	2ca9b045-a083-40ef-b933-a070cc7594ce	4crTd5bkKKISvU2w9eEc0r	\N	HA0vlhJANow	\N	matched	87	2026-02-12 02:37:49.398212
520c4ff6-a07c-40d8-8a0a-7b617f2cdea8	2ca9b045-a083-40ef-b933-a070cc7594ce	3ZmEwYX2GbUUM9C2rpEhkm	\N	WhG0-WO0vLE	\N	matched	87	2026-02-12 02:37:49.398212
f0e8a47b-f2ec-4ab0-bf69-3042d932fc4d	2ca9b045-a083-40ef-b933-a070cc7594ce	5Th9jSEXB7XdeQHXPkBvws	\N	eMpLkkhPA70	\N	matched	87	2026-02-12 02:37:49.398212
3be95642-7c1c-482a-b6a1-9f0c21c17987	2ca9b045-a083-40ef-b933-a070cc7594ce	23KVefoON2iEz1dh940quQ	\N	FX0NxcNPDOc	\N	matched	87	2026-02-12 02:37:49.398212
c4ec1300-47b3-45c9-9168-a8a9768abdd9	2ca9b045-a083-40ef-b933-a070cc7594ce	2f2hbFjim051DVx0o8o4rU	\N	b_cFEOHaaDU	\N	matched	84	2026-02-12 02:37:49.398212
729d7334-bce6-496b-aa52-926a6381ad07	2ca9b045-a083-40ef-b933-a070cc7594ce	3yMC1KsTwh0ceXdIe4QQAQ	\N	mzwg5Ldkt5Q	\N	matched	87	2026-02-12 02:37:49.398212
4f409755-6319-46b2-bf65-88cf7663c55d	2ca9b045-a083-40ef-b933-a070cc7594ce	6Z165JvPnS8PYvGW8oLGLc	\N	J4UsVfwVIx8	\N	matched	90	2026-02-12 02:37:49.398212
3bb347f0-cf3c-4fcc-ba9d-06265c9955b2	2ca9b045-a083-40ef-b933-a070cc7594ce	23oYG6HAbahMQEtjk0M7GK	\N	pTo_kZQ2rvs	\N	matched	90	2026-02-12 02:37:49.398212
44fc6e94-269b-4eae-b11f-f380587922fd	2ca9b045-a083-40ef-b933-a070cc7594ce	3hTSUqyG6ntAYXjgYOjS16	\N	wAFfJl6wr-k	\N	matched	87	2026-02-12 02:37:49.398212
851890f8-3554-4158-bafb-c5e6cba73c0f	2ca9b045-a083-40ef-b933-a070cc7594ce	2Obwg8XE4sWXLpJSDlxn1r	\N	sF0BX5ECsKE	\N	matched	98	2026-02-12 02:37:49.398212
d4a5f5bb-4cbc-493a-9eed-ec20996b62bd	2ca9b045-a083-40ef-b933-a070cc7594ce	2DB4DdfCFMw1iaR6JaR03a	\N	nWr4r-3bHBc	\N	matched	87	2026-02-12 02:37:49.398212
61b02d93-4eb4-4be6-8e05-cd3b84661946	2ca9b045-a083-40ef-b933-a070cc7594ce	6v3KW9xbzN5yKLt9YKDYA2	\N	ahiXR3ZmraA	\N	matched	87	2026-02-12 02:37:49.398212
9c44c086-3a99-4aea-baa7-2620e97c3562	2ca9b045-a083-40ef-b933-a070cc7594ce	1rfofaqEpACxVEHIZBJe6W	\N	HCjNJDNzw8Y	\N	matched	87	2026-02-12 02:37:49.398212
29f95a95-80d7-44ec-8aca-1b51be82fde0	2ca9b045-a083-40ef-b933-a070cc7594ce	2ogKhhoMClkFXek7ZgxAhN	\N	zAw3MLNU56s	\N	matched	100	2026-02-12 02:37:49.398212
2ea56a5c-c2af-4356-8598-24aac7af64cb	2ca9b045-a083-40ef-b933-a070cc7594ce	3yOlyBJuViE2YSGn3nVE1K	\N	TduHf1tWDvI	\N	matched	87	2026-02-12 02:37:49.398212
b5ed79a7-0ce8-4cc8-9038-4c8003a0ad9d	2ca9b045-a083-40ef-b933-a070cc7594ce	073ILU1C3APNRGF66VhtyA	\N	6-OvO8ZuW98	\N	matched	87	2026-02-12 02:37:49.398212
2e2ca0c0-ae18-401f-a28b-990a027574f6	2ca9b045-a083-40ef-b933-a070cc7594ce	6VpQdig9pdpTSIFItgkJV5	\N	FITSPSA8gQs	\N	matched	87	2026-02-12 02:37:49.398212
76672672-95a2-45e6-a84c-f91844866577	2ca9b045-a083-40ef-b933-a070cc7594ce	705dwKmZCd7ImAUyyD6Mpm	\N	mcr-BYQBqXw	\N	matched	87	2026-02-12 02:37:49.398212
3fe48a7b-5fcd-431d-8138-0f42bb655bde	2ca9b045-a083-40ef-b933-a070cc7594ce	1mXVgsBdtIVeCLJnSnmtdV	\N	pmc93sA80HI	\N	matched	100	2026-02-12 02:37:49.398212
502743ba-5c3d-4760-b596-7472e54cdbe0	2ca9b045-a083-40ef-b933-a070cc7594ce	76cy1WJvNGJTj78UqeA5zr	\N	j6jA6dCCToQ	\N	matched	90	2026-02-12 02:37:49.398212
908e7945-bfba-4ae9-ac20-36df48620193	2ca9b045-a083-40ef-b933-a070cc7594ce	2qxmye6gAegTMjLKEBoR3d	\N	PKonqhZ5PsA	\N	matched	87	2026-02-12 02:37:49.398212
8b193ee2-83be-4405-8739-d13e12648744	2ca9b045-a083-40ef-b933-a070cc7594ce	4vUmTMuQqjdnvlZmAH61Qk	\N	q07RanslaGM	\N	matched	87	2026-02-12 02:37:49.398212
2c30e0e0-f891-4277-bbf3-16208a204fb9	2ca9b045-a083-40ef-b933-a070cc7594ce	4umIPjkehX1r7uhmGvXiSV	\N	RcSX0hOWcQ0	\N	matched	95	2026-02-12 02:37:49.398212
e5ca2a85-b7a0-4333-9920-434a2ff2b96b	2ca9b045-a083-40ef-b933-a070cc7594ce	52iLDrSoRtf9lcgFuLVfqE	\N	r1Fx0tqK5Z4	\N	matched	87	2026-02-12 02:37:49.398212
120e519e-3680-4221-b638-de87a19a49ce	2ca9b045-a083-40ef-b933-a070cc7594ce	0wI7QkCcs8FUQE1OkXUIqd	\N	Em7vc8NWUNY	\N	matched	87	2026-02-12 02:37:49.398212
f5664e34-1aa4-4426-9ec2-19c279f83548	2ca9b045-a083-40ef-b933-a070cc7594ce	4l0Mvzj72xxOpRrp6h8nHi	\N	zlJDTxahav0	\N	matched	87	2026-02-12 02:37:49.398212
2cdd54c8-58e1-4acf-bbee-adbc1ce917ac	2ca9b045-a083-40ef-b933-a070cc7594ce	1J14CdDAvBTE1AJYUOwl6C	\N	51u5fnyrGj4	\N	matched	87	2026-02-12 02:37:49.398212
4e5399d3-0d04-484a-8d43-c71861d008d1	2ca9b045-a083-40ef-b933-a070cc7594ce	0Gl5s8IhMmQE5YQwM8Qx1J	\N	nT0CYXcre0c	\N	matched	100	2026-02-12 02:37:49.398212
45c76df6-00b8-44d2-830e-f78fe360734f	c95bb82e-e96a-446f-a973-f814ea1cfc39	2J2Z1SkXYghSajLibnQHOa	\N	cfhwSpSDA9c	\N	matched	100	2026-02-12 02:38:26.220383
69130144-02b9-4c21-8f08-cac1ad2df963	c95bb82e-e96a-446f-a973-f814ea1cfc39	4VrWlk8IQxevMvERoX08iC	\N	uzaYLK3k0DQ	\N	matched	100	2026-02-12 02:38:26.220383
b2cca565-eafd-45d7-9d2e-b675d8e49b5c	c95bb82e-e96a-446f-a973-f814ea1cfc39	27SdWb2rFzO6GWiYDBTD9j	\N	J1b22l1kFKY	\N	matched	87	2026-02-12 02:38:26.220383
719cc406-3c76-4eb4-98db-6a6cbd916119	c95bb82e-e96a-446f-a973-f814ea1cfc39	1rMlnD8S0hX7IHZXZDt3gH	\N	oa7JwwdiA98	\N	matched	95	2026-02-12 02:38:26.220383
66296887-f77c-4d61-a8b1-bf8c8963a070	c95bb82e-e96a-446f-a973-f814ea1cfc39	1v1oIWf2Xgh54kIWuKsDf6	\N	d_HlPboLRL8	\N	matched	87	2026-02-12 02:38:26.220383
9a73e518-06d6-4d71-bdeb-3109ea31cf7c	c95bb82e-e96a-446f-a973-f814ea1cfc39	2MbgQKWhoii8bqsMBWWWgA	\N	mWQACEqf4QY	\N	matched	87	2026-02-12 02:38:26.220383
561ebf67-f05f-4d29-b803-f7ec1374baad	c95bb82e-e96a-446f-a973-f814ea1cfc39	5zHX1aFxP5p10NI4L1nkai	\N	4FrYREQ4XP8	\N	matched	87	2026-02-12 02:38:26.220383
23409233-1208-4e62-a374-e12de4a47b78	c95bb82e-e96a-446f-a973-f814ea1cfc39	2rbDhOo9Fh61Bbu23T2qCk	\N	Voo86mlxZvA	\N	matched	100	2026-02-12 02:38:26.220383
e03610bc-b1d7-4d4d-bcf6-9a713400f05e	c95bb82e-e96a-446f-a973-f814ea1cfc39	3Z0oQ8r78OUaHvGPiDBR3W	\N	2qPlX1FMJXI	\N	matched	100	2026-02-12 02:38:26.220383
9985abf8-e3ee-4fdf-ba0f-e1229661b585	c95bb82e-e96a-446f-a973-f814ea1cfc39	0Ie5uiv54KgCr7P4sYDTHl	\N	IJpeLGzJBGo	\N	matched	95	2026-02-12 02:38:26.220383
ec74359d-0362-4fa4-becc-2e338fdb5718	c95bb82e-e96a-446f-a973-f814ea1cfc39	50WeOnXhM1H7AZEeIDoWfZ	\N	7noPeO5sKHA	\N	matched	100	2026-02-12 02:38:26.220383
6f27e680-ff32-4c02-b402-add71f3da760	c95bb82e-e96a-446f-a973-f814ea1cfc39	0qcr5FMsEO85NAQjrlDRKo	\N	qSU560anReg	\N	matched	95	2026-02-12 02:38:26.220383
f45a3d67-2efb-4b0f-a2ac-31c74358f448	c95bb82e-e96a-446f-a973-f814ea1cfc39	0EnS2KLYG9ig26yYltoXJf	\N	_373lKEvQ34	\N	matched	100	2026-02-12 02:38:26.220383
354cbce6-3e65-4ec3-b8e0-2075a8d73d9e	c95bb82e-e96a-446f-a973-f814ea1cfc39	4crTd5bkKKISvU2w9eEc0r	\N	HA0vlhJANow	\N	matched	87	2026-02-12 02:38:26.220383
ed1d358c-55d9-44a5-81c5-2671b9aa6360	c95bb82e-e96a-446f-a973-f814ea1cfc39	3ZmEwYX2GbUUM9C2rpEhkm	\N	WhG0-WO0vLE	\N	matched	87	2026-02-12 02:38:26.220383
60e7ef4c-856c-48e1-ac4f-1619785acf59	c95bb82e-e96a-446f-a973-f814ea1cfc39	5Th9jSEXB7XdeQHXPkBvws	\N	eMpLkkhPA70	\N	matched	87	2026-02-12 02:38:26.220383
803ade34-3c4f-4955-b1db-6fc9508f7f64	c95bb82e-e96a-446f-a973-f814ea1cfc39	23KVefoON2iEz1dh940quQ	\N	FX0NxcNPDOc	\N	matched	87	2026-02-12 02:38:26.220383
08e72071-0528-4083-862d-6d2f845661bd	c95bb82e-e96a-446f-a973-f814ea1cfc39	2f2hbFjim051DVx0o8o4rU	\N	b_cFEOHaaDU	\N	matched	84	2026-02-12 02:38:26.220383
cc67761f-7f94-4308-a720-a71506fb32e0	c95bb82e-e96a-446f-a973-f814ea1cfc39	3yMC1KsTwh0ceXdIe4QQAQ	\N	mzwg5Ldkt5Q	\N	matched	87	2026-02-12 02:38:26.220383
d0129eef-d769-4e30-b02f-2dab9889c01f	c95bb82e-e96a-446f-a973-f814ea1cfc39	6Z165JvPnS8PYvGW8oLGLc	\N	J4UsVfwVIx8	\N	matched	90	2026-02-12 02:38:26.220383
a7f6b5ec-b561-4df4-8b1c-a03fe8f1aa14	c95bb82e-e96a-446f-a973-f814ea1cfc39	23oYG6HAbahMQEtjk0M7GK	\N	pTo_kZQ2rvs	\N	matched	90	2026-02-12 02:38:26.220383
f52fe390-2af0-4d40-a3ce-31b890e60c4b	c95bb82e-e96a-446f-a973-f814ea1cfc39	3hTSUqyG6ntAYXjgYOjS16	\N	wAFfJl6wr-k	\N	matched	87	2026-02-12 02:38:26.220383
ff73d152-da2b-4bef-bba7-4f1c1acec62b	c95bb82e-e96a-446f-a973-f814ea1cfc39	2Obwg8XE4sWXLpJSDlxn1r	\N	sF0BX5ECsKE	\N	matched	98	2026-02-12 02:38:26.220383
8b094078-fd1b-4814-8ac5-5a78c563845e	c95bb82e-e96a-446f-a973-f814ea1cfc39	2DB4DdfCFMw1iaR6JaR03a	\N	nWr4r-3bHBc	\N	matched	87	2026-02-12 02:38:26.220383
3617ce71-2685-4174-9149-6c55d06f4f6b	c95bb82e-e96a-446f-a973-f814ea1cfc39	6v3KW9xbzN5yKLt9YKDYA2	\N	ahiXR3ZmraA	\N	matched	87	2026-02-12 02:38:26.220383
3ddaff0e-2659-4123-b24b-cb2cc6d5ee3b	c95bb82e-e96a-446f-a973-f814ea1cfc39	1rfofaqEpACxVEHIZBJe6W	\N	HCjNJDNzw8Y	\N	matched	87	2026-02-12 02:38:26.220383
e757d7fb-8b9c-4001-ba30-eb34bf5c0a81	c95bb82e-e96a-446f-a973-f814ea1cfc39	2ogKhhoMClkFXek7ZgxAhN	\N	zAw3MLNU56s	\N	matched	100	2026-02-12 02:38:26.220383
59447ce6-8946-4fdf-ae93-543527400d8d	c95bb82e-e96a-446f-a973-f814ea1cfc39	3yOlyBJuViE2YSGn3nVE1K	\N	TduHf1tWDvI	\N	matched	87	2026-02-12 02:38:26.220383
fb772b02-eaeb-49be-a6c0-42f9795e8300	c95bb82e-e96a-446f-a973-f814ea1cfc39	073ILU1C3APNRGF66VhtyA	\N	6-OvO8ZuW98	\N	matched	87	2026-02-12 02:38:26.220383
9d426103-c041-4940-9651-cbaa417c8401	c95bb82e-e96a-446f-a973-f814ea1cfc39	6VpQdig9pdpTSIFItgkJV5	\N	FITSPSA8gQs	\N	matched	87	2026-02-12 02:38:26.220383
90106681-a492-44e2-a398-3bf4c69c64e7	c95bb82e-e96a-446f-a973-f814ea1cfc39	705dwKmZCd7ImAUyyD6Mpm	\N	mcr-BYQBqXw	\N	matched	87	2026-02-12 02:38:26.220383
57ef4947-6fcd-4c2c-a938-e979382fa68d	c95bb82e-e96a-446f-a973-f814ea1cfc39	1mXVgsBdtIVeCLJnSnmtdV	\N	pmc93sA80HI	\N	matched	100	2026-02-12 02:38:26.220383
fc5b3e54-df0e-4634-96d7-51c6686038a9	c95bb82e-e96a-446f-a973-f814ea1cfc39	76cy1WJvNGJTj78UqeA5zr	\N	j6jA6dCCToQ	\N	matched	90	2026-02-12 02:38:26.220383
a9528db9-90f3-4a82-9312-12167c8c8e74	c95bb82e-e96a-446f-a973-f814ea1cfc39	2qxmye6gAegTMjLKEBoR3d	\N	PKonqhZ5PsA	\N	matched	87	2026-02-12 02:38:26.220383
3ae99031-caec-4c1d-91a0-7733b74efa61	c95bb82e-e96a-446f-a973-f814ea1cfc39	4vUmTMuQqjdnvlZmAH61Qk	\N	q07RanslaGM	\N	matched	87	2026-02-12 02:38:26.220383
fb9071af-e3b0-4aad-a603-f30d197cbc2f	c95bb82e-e96a-446f-a973-f814ea1cfc39	4umIPjkehX1r7uhmGvXiSV	\N	RcSX0hOWcQ0	\N	matched	95	2026-02-12 02:38:26.220383
95af5124-6f01-4cdc-a250-e57c34a59faa	c95bb82e-e96a-446f-a973-f814ea1cfc39	52iLDrSoRtf9lcgFuLVfqE	\N	r1Fx0tqK5Z4	\N	matched	87	2026-02-12 02:38:26.220383
9a66adad-d93d-4e0c-807e-6eb55b7f4aaa	c95bb82e-e96a-446f-a973-f814ea1cfc39	0wI7QkCcs8FUQE1OkXUIqd	\N	Em7vc8NWUNY	\N	matched	87	2026-02-12 02:38:26.220383
cde2063a-0fb3-4b9f-beb3-bf68c9401b0a	c95bb82e-e96a-446f-a973-f814ea1cfc39	4l0Mvzj72xxOpRrp6h8nHi	\N	zlJDTxahav0	\N	matched	87	2026-02-12 02:38:26.220383
e0ed1e82-0fe0-4756-bcda-110887d13c86	c95bb82e-e96a-446f-a973-f814ea1cfc39	1J14CdDAvBTE1AJYUOwl6C	\N	51u5fnyrGj4	\N	matched	87	2026-02-12 02:38:26.220383
3bf9afd2-6e0a-499b-91db-960efcd46c7e	c95bb82e-e96a-446f-a973-f814ea1cfc39	0Gl5s8IhMmQE5YQwM8Qx1J	\N	nT0CYXcre0c	\N	matched	100	2026-02-12 02:38:26.220383
79dd8363-e0b2-474c-85a8-4b600cbef6e8	2b5b4967-35ac-432a-873a-8bec7b56bdff	2J2Z1SkXYghSajLibnQHOa	\N	cfhwSpSDA9c	\N	matched	100	2026-02-12 02:38:50.223247
9bc30a1b-fbce-411d-b618-d328a081e9c4	2b5b4967-35ac-432a-873a-8bec7b56bdff	4VrWlk8IQxevMvERoX08iC	\N	uzaYLK3k0DQ	\N	matched	100	2026-02-12 02:38:50.223247
f9291a73-0ee3-4409-9bad-6eb60e3c63e6	2b5b4967-35ac-432a-873a-8bec7b56bdff	27SdWb2rFzO6GWiYDBTD9j	\N	J1b22l1kFKY	\N	matched	87	2026-02-12 02:38:50.223247
dce2f844-d655-4216-8a74-b2dde7bb5ca4	2b5b4967-35ac-432a-873a-8bec7b56bdff	1rMlnD8S0hX7IHZXZDt3gH	\N	oa7JwwdiA98	\N	matched	95	2026-02-12 02:38:50.223247
297d004e-be10-44fb-ad9d-42a37572cc03	2b5b4967-35ac-432a-873a-8bec7b56bdff	1v1oIWf2Xgh54kIWuKsDf6	\N	d_HlPboLRL8	\N	matched	87	2026-02-12 02:38:50.223247
976ba064-da54-4905-aa7d-df0e804f92d1	2b5b4967-35ac-432a-873a-8bec7b56bdff	2MbgQKWhoii8bqsMBWWWgA	\N	mWQACEqf4QY	\N	matched	87	2026-02-12 02:38:50.223247
13a7e022-d996-4457-b533-75e3c5df3716	2b5b4967-35ac-432a-873a-8bec7b56bdff	5zHX1aFxP5p10NI4L1nkai	\N	4FrYREQ4XP8	\N	matched	87	2026-02-12 02:38:50.223247
484595a3-f199-4db4-ae8c-213c674acdb9	2b5b4967-35ac-432a-873a-8bec7b56bdff	2rbDhOo9Fh61Bbu23T2qCk	\N	Voo86mlxZvA	\N	matched	100	2026-02-12 02:38:50.223247
23b86131-6cfd-4049-8a8b-7d15d41cb2cf	2b5b4967-35ac-432a-873a-8bec7b56bdff	3Z0oQ8r78OUaHvGPiDBR3W	\N	2qPlX1FMJXI	\N	matched	100	2026-02-12 02:38:50.223247
c89776df-8806-4198-89a4-ba773ef92185	2b5b4967-35ac-432a-873a-8bec7b56bdff	0Ie5uiv54KgCr7P4sYDTHl	\N	IJpeLGzJBGo	\N	matched	95	2026-02-12 02:38:50.223247
56e8dc65-cab2-4b3c-abc1-9f42e5ccd50d	2b5b4967-35ac-432a-873a-8bec7b56bdff	50WeOnXhM1H7AZEeIDoWfZ	\N	7noPeO5sKHA	\N	matched	100	2026-02-12 02:38:50.223247
5064fc53-0052-4079-b0fe-d89e9ce698d3	2b5b4967-35ac-432a-873a-8bec7b56bdff	0qcr5FMsEO85NAQjrlDRKo	\N	qSU560anReg	\N	matched	95	2026-02-12 02:38:50.223247
a1b047b3-96ce-4997-8d6c-5c3fc95babd4	2b5b4967-35ac-432a-873a-8bec7b56bdff	0EnS2KLYG9ig26yYltoXJf	\N	_373lKEvQ34	\N	matched	100	2026-02-12 02:38:50.223247
067102fd-4d36-4608-ab71-9118dfbd4229	2b5b4967-35ac-432a-873a-8bec7b56bdff	4crTd5bkKKISvU2w9eEc0r	\N	HA0vlhJANow	\N	matched	87	2026-02-12 02:38:50.223247
d36bb70d-8e94-42ad-a0a9-db7da29d25ba	2b5b4967-35ac-432a-873a-8bec7b56bdff	3ZmEwYX2GbUUM9C2rpEhkm	\N	WhG0-WO0vLE	\N	matched	87	2026-02-12 02:38:50.223247
0b070ff5-29ad-4f74-97c9-bd2a1fe1a438	2b5b4967-35ac-432a-873a-8bec7b56bdff	5Th9jSEXB7XdeQHXPkBvws	\N	eMpLkkhPA70	\N	matched	87	2026-02-12 02:38:50.223247
f78a29ab-8a17-4d29-9764-52acdd53713b	2b5b4967-35ac-432a-873a-8bec7b56bdff	23KVefoON2iEz1dh940quQ	\N	FX0NxcNPDOc	\N	matched	87	2026-02-12 02:38:50.223247
33ef2b2e-5c3a-4817-ba93-7d928624a469	2b5b4967-35ac-432a-873a-8bec7b56bdff	2f2hbFjim051DVx0o8o4rU	\N	b_cFEOHaaDU	\N	matched	84	2026-02-12 02:38:50.223247
e02b2e87-14d7-412d-8a2f-40efe9b22fbe	2b5b4967-35ac-432a-873a-8bec7b56bdff	3yMC1KsTwh0ceXdIe4QQAQ	\N	mzwg5Ldkt5Q	\N	matched	87	2026-02-12 02:38:50.223247
81765664-50b9-4341-9aa4-b72a16eada63	2b5b4967-35ac-432a-873a-8bec7b56bdff	6Z165JvPnS8PYvGW8oLGLc	\N	J4UsVfwVIx8	\N	matched	90	2026-02-12 02:38:50.223247
e94cc7fa-8036-4005-8a82-a97dd84e42cb	2b5b4967-35ac-432a-873a-8bec7b56bdff	23oYG6HAbahMQEtjk0M7GK	\N	pTo_kZQ2rvs	\N	matched	90	2026-02-12 02:38:50.223247
a358e647-fdee-4993-90c9-a10321164ca1	2b5b4967-35ac-432a-873a-8bec7b56bdff	3hTSUqyG6ntAYXjgYOjS16	\N	wAFfJl6wr-k	\N	matched	87	2026-02-12 02:38:50.223247
dbf32033-97e5-422b-ad18-959b0b342ed1	2b5b4967-35ac-432a-873a-8bec7b56bdff	2Obwg8XE4sWXLpJSDlxn1r	\N	sF0BX5ECsKE	\N	matched	98	2026-02-12 02:38:50.223247
3428396f-aa7b-4710-a3e2-bce2808304ea	2b5b4967-35ac-432a-873a-8bec7b56bdff	2DB4DdfCFMw1iaR6JaR03a	\N	nWr4r-3bHBc	\N	matched	87	2026-02-12 02:38:50.223247
46ba00e4-aebf-41ca-8659-d7b4146eb5e6	2b5b4967-35ac-432a-873a-8bec7b56bdff	6v3KW9xbzN5yKLt9YKDYA2	\N	ahiXR3ZmraA	\N	matched	87	2026-02-12 02:38:50.223247
345b896e-762f-4579-9a2e-56b2659edf36	2b5b4967-35ac-432a-873a-8bec7b56bdff	1rfofaqEpACxVEHIZBJe6W	\N	HCjNJDNzw8Y	\N	matched	87	2026-02-12 02:38:50.223247
2548476f-031d-4d8c-bf5e-b4886e0f8766	2b5b4967-35ac-432a-873a-8bec7b56bdff	2ogKhhoMClkFXek7ZgxAhN	\N	zAw3MLNU56s	\N	matched	100	2026-02-12 02:38:50.223247
ecf57df4-841f-4f9d-a483-66298066dfd2	2b5b4967-35ac-432a-873a-8bec7b56bdff	3yOlyBJuViE2YSGn3nVE1K	\N	TduHf1tWDvI	\N	matched	87	2026-02-12 02:38:50.223247
738a13d0-6269-4da6-8095-5d7bb543b6d1	2b5b4967-35ac-432a-873a-8bec7b56bdff	073ILU1C3APNRGF66VhtyA	\N	6-OvO8ZuW98	\N	matched	87	2026-02-12 02:38:50.223247
f758848f-d9c6-40d6-92e8-47d06374b751	2b5b4967-35ac-432a-873a-8bec7b56bdff	6VpQdig9pdpTSIFItgkJV5	\N	FITSPSA8gQs	\N	matched	87	2026-02-12 02:38:50.223247
5d22e5c3-c400-427c-ad80-3e9adb2f3146	2b5b4967-35ac-432a-873a-8bec7b56bdff	705dwKmZCd7ImAUyyD6Mpm	\N	mcr-BYQBqXw	\N	matched	87	2026-02-12 02:38:50.223247
7b566f0d-5b38-4568-a4bc-0f7d50feafcf	2b5b4967-35ac-432a-873a-8bec7b56bdff	1mXVgsBdtIVeCLJnSnmtdV	\N	pmc93sA80HI	\N	matched	100	2026-02-12 02:38:50.223247
44d0fe3b-857a-4d76-b710-0bf248f66a59	2b5b4967-35ac-432a-873a-8bec7b56bdff	76cy1WJvNGJTj78UqeA5zr	\N	j6jA6dCCToQ	\N	matched	90	2026-02-12 02:38:50.223247
b2292865-5481-47af-86be-49054b736b83	2b5b4967-35ac-432a-873a-8bec7b56bdff	2qxmye6gAegTMjLKEBoR3d	\N	PKonqhZ5PsA	\N	matched	87	2026-02-12 02:38:50.223247
0679f894-9248-474d-aa0b-1afea62001f3	2b5b4967-35ac-432a-873a-8bec7b56bdff	4vUmTMuQqjdnvlZmAH61Qk	\N	q07RanslaGM	\N	matched	87	2026-02-12 02:38:50.223247
5089ff9f-7638-4a68-a677-535e4025e8aa	2b5b4967-35ac-432a-873a-8bec7b56bdff	4umIPjkehX1r7uhmGvXiSV	\N	RcSX0hOWcQ0	\N	matched	95	2026-02-12 02:38:50.223247
4ea1e1db-6652-437d-936a-a05a45736393	2b5b4967-35ac-432a-873a-8bec7b56bdff	52iLDrSoRtf9lcgFuLVfqE	\N	r1Fx0tqK5Z4	\N	matched	87	2026-02-12 02:38:50.223247
b9d4ac41-ddbc-49a1-81a9-3f63536c37a0	2b5b4967-35ac-432a-873a-8bec7b56bdff	0wI7QkCcs8FUQE1OkXUIqd	\N	Em7vc8NWUNY	\N	matched	87	2026-02-12 02:38:50.223247
ce401037-3611-45d1-a098-8bac23d2593c	2b5b4967-35ac-432a-873a-8bec7b56bdff	4l0Mvzj72xxOpRrp6h8nHi	\N	zlJDTxahav0	\N	matched	87	2026-02-12 02:38:50.223247
7bb14014-d28e-46a0-b116-8875f8efb5ec	2b5b4967-35ac-432a-873a-8bec7b56bdff	1J14CdDAvBTE1AJYUOwl6C	\N	51u5fnyrGj4	\N	matched	87	2026-02-12 02:38:50.223247
b7810f4f-33e8-4100-b24b-b98e88854172	2b5b4967-35ac-432a-873a-8bec7b56bdff	0Gl5s8IhMmQE5YQwM8Qx1J	\N	nT0CYXcre0c	\N	matched	100	2026-02-12 02:38:50.223247
a7c568f4-adb7-4254-ac63-c0091e4b2045	cb69538d-e593-442d-893e-67f68c1c27ab	2J2Z1SkXYghSajLibnQHOa	\N	cfhwSpSDA9c	\N	matched	100	2026-02-12 02:39:18.401606
583b1131-5846-4d7d-8e8c-da6ff3edf811	cb69538d-e593-442d-893e-67f68c1c27ab	4VrWlk8IQxevMvERoX08iC	\N	uzaYLK3k0DQ	\N	matched	100	2026-02-12 02:39:18.401606
e004850f-19cf-4e7d-8362-08833ec40a82	cb69538d-e593-442d-893e-67f68c1c27ab	27SdWb2rFzO6GWiYDBTD9j	\N	J1b22l1kFKY	\N	matched	87	2026-02-12 02:39:18.401606
811281f4-d82e-4085-8443-5156cf28b7ee	cb69538d-e593-442d-893e-67f68c1c27ab	1rMlnD8S0hX7IHZXZDt3gH	\N	oa7JwwdiA98	\N	matched	95	2026-02-12 02:39:18.401606
165785eb-5531-4907-b02d-f9713303e58f	cb69538d-e593-442d-893e-67f68c1c27ab	1v1oIWf2Xgh54kIWuKsDf6	\N	d_HlPboLRL8	\N	matched	87	2026-02-12 02:39:18.401606
955048f0-5d30-43f7-b61b-12597b93cb21	cb69538d-e593-442d-893e-67f68c1c27ab	2MbgQKWhoii8bqsMBWWWgA	\N	mWQACEqf4QY	\N	matched	87	2026-02-12 02:39:18.401606
ee2d7c59-aa33-49f3-a5f7-04efe6cf9854	cb69538d-e593-442d-893e-67f68c1c27ab	5zHX1aFxP5p10NI4L1nkai	\N	4FrYREQ4XP8	\N	matched	87	2026-02-12 02:39:18.401606
67613ba5-1125-4190-8be2-b15d5064f0f8	cb69538d-e593-442d-893e-67f68c1c27ab	2rbDhOo9Fh61Bbu23T2qCk	\N	Voo86mlxZvA	\N	matched	100	2026-02-12 02:39:18.401606
bb18a5eb-302e-44d9-ac0a-8600da094318	cb69538d-e593-442d-893e-67f68c1c27ab	3Z0oQ8r78OUaHvGPiDBR3W	\N	2qPlX1FMJXI	\N	matched	100	2026-02-12 02:39:18.401606
77dcb4a0-a10b-4869-93ee-5991b425b80f	cb69538d-e593-442d-893e-67f68c1c27ab	0Ie5uiv54KgCr7P4sYDTHl	\N	IJpeLGzJBGo	\N	matched	95	2026-02-12 02:39:18.401606
0c8c7b9d-19a6-4bca-9954-23363c3b0aca	cb69538d-e593-442d-893e-67f68c1c27ab	50WeOnXhM1H7AZEeIDoWfZ	\N	7noPeO5sKHA	\N	matched	100	2026-02-12 02:39:18.401606
565f3762-5c32-4251-9979-f1ee5867a126	cb69538d-e593-442d-893e-67f68c1c27ab	0qcr5FMsEO85NAQjrlDRKo	\N	qSU560anReg	\N	matched	95	2026-02-12 02:39:18.401606
0740449f-280e-401c-b899-fb20e1fb4952	cb69538d-e593-442d-893e-67f68c1c27ab	0EnS2KLYG9ig26yYltoXJf	\N	_373lKEvQ34	\N	matched	100	2026-02-12 02:39:18.401606
f21c225d-b15c-404c-bf53-d4f387312606	cb69538d-e593-442d-893e-67f68c1c27ab	4crTd5bkKKISvU2w9eEc0r	\N	HA0vlhJANow	\N	matched	87	2026-02-12 02:39:18.401606
24837365-2727-481b-a106-a437d582055d	cb69538d-e593-442d-893e-67f68c1c27ab	3ZmEwYX2GbUUM9C2rpEhkm	\N	WhG0-WO0vLE	\N	matched	87	2026-02-12 02:39:18.401606
b67f712a-0d81-4f2e-8d10-6a0a2e521e1d	cb69538d-e593-442d-893e-67f68c1c27ab	5Th9jSEXB7XdeQHXPkBvws	\N	eMpLkkhPA70	\N	matched	87	2026-02-12 02:39:18.401606
0b5167a6-59ca-48f1-bb37-381c4d8809f7	cb69538d-e593-442d-893e-67f68c1c27ab	23KVefoON2iEz1dh940quQ	\N	FX0NxcNPDOc	\N	matched	87	2026-02-12 02:39:18.401606
4d6a1126-5485-44e4-8689-abb0f5950552	cb69538d-e593-442d-893e-67f68c1c27ab	2f2hbFjim051DVx0o8o4rU	\N	b_cFEOHaaDU	\N	matched	84	2026-02-12 02:39:18.401606
a572ae77-6ebb-46f9-bae4-e9c283f437b0	cb69538d-e593-442d-893e-67f68c1c27ab	3yMC1KsTwh0ceXdIe4QQAQ	\N	mzwg5Ldkt5Q	\N	matched	87	2026-02-12 02:39:18.401606
d4ddba33-79c5-4801-9991-b05a8c6f8b0f	cb69538d-e593-442d-893e-67f68c1c27ab	6Z165JvPnS8PYvGW8oLGLc	\N	J4UsVfwVIx8	\N	matched	90	2026-02-12 02:39:18.401606
d38b779f-fff7-4e32-9e22-0b6ae742faed	cb69538d-e593-442d-893e-67f68c1c27ab	23oYG6HAbahMQEtjk0M7GK	\N	pTo_kZQ2rvs	\N	matched	90	2026-02-12 02:39:18.401606
efc8ae6d-17ca-4ba7-a27c-870a765105cb	cb69538d-e593-442d-893e-67f68c1c27ab	3hTSUqyG6ntAYXjgYOjS16	\N	wAFfJl6wr-k	\N	matched	87	2026-02-12 02:39:18.401606
2912cb33-db6e-4cfa-992b-d86ef30af11b	cb69538d-e593-442d-893e-67f68c1c27ab	2Obwg8XE4sWXLpJSDlxn1r	\N	sF0BX5ECsKE	\N	matched	98	2026-02-12 02:39:18.401606
4acf833a-eb56-4c7b-8b5f-1ef9686347dc	cb69538d-e593-442d-893e-67f68c1c27ab	2DB4DdfCFMw1iaR6JaR03a	\N	nWr4r-3bHBc	\N	matched	87	2026-02-12 02:39:18.401606
33d9341d-56e4-44eb-b806-008161121806	cb69538d-e593-442d-893e-67f68c1c27ab	6v3KW9xbzN5yKLt9YKDYA2	\N	ahiXR3ZmraA	\N	matched	87	2026-02-12 02:39:18.401606
7725d9af-8bdc-44f0-a6cd-8d6f44a93c93	cb69538d-e593-442d-893e-67f68c1c27ab	1rfofaqEpACxVEHIZBJe6W	\N	HCjNJDNzw8Y	\N	matched	87	2026-02-12 02:39:18.401606
de843304-a0bc-4877-83a5-1e2c3ae8b50c	cb69538d-e593-442d-893e-67f68c1c27ab	2ogKhhoMClkFXek7ZgxAhN	\N	zAw3MLNU56s	\N	matched	100	2026-02-12 02:39:18.401606
7f9d5e10-66dd-466e-84fd-36f69af5b603	cb69538d-e593-442d-893e-67f68c1c27ab	3yOlyBJuViE2YSGn3nVE1K	\N	TduHf1tWDvI	\N	matched	87	2026-02-12 02:39:18.401606
5f6bcb57-10c1-4376-9460-ed8b00f9c4a4	cb69538d-e593-442d-893e-67f68c1c27ab	073ILU1C3APNRGF66VhtyA	\N	6-OvO8ZuW98	\N	matched	87	2026-02-12 02:39:18.401606
87f8767a-f5d9-4fa6-8c78-ea162c7eaaf7	cb69538d-e593-442d-893e-67f68c1c27ab	6VpQdig9pdpTSIFItgkJV5	\N	FITSPSA8gQs	\N	matched	87	2026-02-12 02:39:18.401606
468f5de4-1b0e-488d-8324-7c95f6eadae3	cb69538d-e593-442d-893e-67f68c1c27ab	705dwKmZCd7ImAUyyD6Mpm	\N	mcr-BYQBqXw	\N	matched	87	2026-02-12 02:39:18.401606
aa69cf19-ee15-4684-9a27-615eafff4291	cb69538d-e593-442d-893e-67f68c1c27ab	1mXVgsBdtIVeCLJnSnmtdV	\N	pmc93sA80HI	\N	matched	100	2026-02-12 02:39:18.401606
1d2439ee-2bc7-450d-9898-ceadc6973033	cb69538d-e593-442d-893e-67f68c1c27ab	76cy1WJvNGJTj78UqeA5zr	\N	j6jA6dCCToQ	\N	matched	90	2026-02-12 02:39:18.401606
6ec6fac8-6456-45e5-8b78-d0f2cd74f354	cb69538d-e593-442d-893e-67f68c1c27ab	2qxmye6gAegTMjLKEBoR3d	\N	PKonqhZ5PsA	\N	matched	87	2026-02-12 02:39:18.401606
6bc3980c-9b6b-4674-bef1-82f2b84df785	cb69538d-e593-442d-893e-67f68c1c27ab	4vUmTMuQqjdnvlZmAH61Qk	\N	q07RanslaGM	\N	matched	87	2026-02-12 02:39:18.401606
4b226fae-3780-4143-a4ee-668fa7d840b2	cb69538d-e593-442d-893e-67f68c1c27ab	4umIPjkehX1r7uhmGvXiSV	\N	RcSX0hOWcQ0	\N	matched	95	2026-02-12 02:39:18.401606
528b94a0-a81b-4a6a-9efa-01146b7dd42c	cb69538d-e593-442d-893e-67f68c1c27ab	52iLDrSoRtf9lcgFuLVfqE	\N	r1Fx0tqK5Z4	\N	matched	87	2026-02-12 02:39:18.401606
bc4a678f-c9c8-4ec5-b697-4fd77eac9b9f	cb69538d-e593-442d-893e-67f68c1c27ab	0wI7QkCcs8FUQE1OkXUIqd	\N	Em7vc8NWUNY	\N	matched	87	2026-02-12 02:39:18.401606
84afab85-3f2f-410a-9942-e3eed1ed05f7	cb69538d-e593-442d-893e-67f68c1c27ab	4l0Mvzj72xxOpRrp6h8nHi	\N	zlJDTxahav0	\N	matched	87	2026-02-12 02:39:18.401606
06f6edd3-ee48-40c2-a313-3d000057fa28	cb69538d-e593-442d-893e-67f68c1c27ab	1J14CdDAvBTE1AJYUOwl6C	\N	51u5fnyrGj4	\N	matched	87	2026-02-12 02:39:18.401606
3715a2c4-01c2-4d8f-b2f7-c60f755ab1ad	cb69538d-e593-442d-893e-67f68c1c27ab	0Gl5s8IhMmQE5YQwM8Qx1J	\N	nT0CYXcre0c	\N	matched	100	2026-02-12 02:39:18.401606
b9b6d20e-d856-4f2a-a243-4bdee415e1cf	571cce4a-2dd0-46db-99d1-14ffd986eb1b	2J2Z1SkXYghSajLibnQHOa	\N	cfhwSpSDA9c	\N	matched	100	2026-02-12 03:05:11.072065
244e2ca8-3ebf-421a-a553-14a44c9a7147	571cce4a-2dd0-46db-99d1-14ffd986eb1b	4VrWlk8IQxevMvERoX08iC	\N	uzaYLK3k0DQ	\N	matched	100	2026-02-12 03:05:11.072065
b3a90a58-613c-48f4-bd06-b8b5f8bf705f	571cce4a-2dd0-46db-99d1-14ffd986eb1b	27SdWb2rFzO6GWiYDBTD9j	\N	J1b22l1kFKY	\N	matched	87	2026-02-12 03:05:11.072065
18b6214c-9d04-43b0-b559-e7617375a021	571cce4a-2dd0-46db-99d1-14ffd986eb1b	1rMlnD8S0hX7IHZXZDt3gH	\N	oa7JwwdiA98	\N	matched	95	2026-02-12 03:05:11.072065
15519f88-900b-44f5-991c-422b5a9dcc2f	571cce4a-2dd0-46db-99d1-14ffd986eb1b	1v1oIWf2Xgh54kIWuKsDf6	\N	d_HlPboLRL8	\N	matched	87	2026-02-12 03:05:11.072065
dee9368e-44e4-4ce2-b975-c9da7c4f2540	571cce4a-2dd0-46db-99d1-14ffd986eb1b	2MbgQKWhoii8bqsMBWWWgA	\N	mWQACEqf4QY	\N	matched	87	2026-02-12 03:05:11.072065
c54f22b8-77cc-45ff-91c0-62ffff529784	571cce4a-2dd0-46db-99d1-14ffd986eb1b	5zHX1aFxP5p10NI4L1nkai	\N	4FrYREQ4XP8	\N	matched	87	2026-02-12 03:05:11.072065
baf222a1-dbdb-489e-98f2-169e041e2f97	571cce4a-2dd0-46db-99d1-14ffd986eb1b	2rbDhOo9Fh61Bbu23T2qCk	\N	Voo86mlxZvA	\N	matched	100	2026-02-12 03:05:11.072065
75f19512-7930-412b-823e-4f7702657d15	571cce4a-2dd0-46db-99d1-14ffd986eb1b	3Z0oQ8r78OUaHvGPiDBR3W	\N	2qPlX1FMJXI	\N	matched	100	2026-02-12 03:05:11.072065
b0a76066-8780-4c8c-99a7-1eb4a99ec67d	571cce4a-2dd0-46db-99d1-14ffd986eb1b	0Ie5uiv54KgCr7P4sYDTHl	\N	IJpeLGzJBGo	\N	matched	95	2026-02-12 03:05:11.072065
6a322a8a-5750-4c66-bb21-b6139b67555f	571cce4a-2dd0-46db-99d1-14ffd986eb1b	50WeOnXhM1H7AZEeIDoWfZ	\N	7noPeO5sKHA	\N	matched	100	2026-02-12 03:05:11.072065
6ff79099-3b7d-4ed9-8374-3ec9eb2c2378	571cce4a-2dd0-46db-99d1-14ffd986eb1b	0qcr5FMsEO85NAQjrlDRKo	\N	qSU560anReg	\N	matched	95	2026-02-12 03:05:11.072065
4ca4e9f0-e3d5-40d6-84c7-4712b5ff1183	571cce4a-2dd0-46db-99d1-14ffd986eb1b	0EnS2KLYG9ig26yYltoXJf	\N	_373lKEvQ34	\N	matched	100	2026-02-12 03:05:11.072065
0477e6e3-d4b6-442b-a8d1-3d43f3570e5e	571cce4a-2dd0-46db-99d1-14ffd986eb1b	4crTd5bkKKISvU2w9eEc0r	\N	HA0vlhJANow	\N	matched	87	2026-02-12 03:05:11.072065
5e592623-b7fc-4b81-9163-103b9c06a9e4	571cce4a-2dd0-46db-99d1-14ffd986eb1b	3ZmEwYX2GbUUM9C2rpEhkm	\N	WhG0-WO0vLE	\N	matched	87	2026-02-12 03:05:11.072065
d6255afb-0568-4835-b7fb-1a8aeccb4e0b	571cce4a-2dd0-46db-99d1-14ffd986eb1b	5Th9jSEXB7XdeQHXPkBvws	\N	eMpLkkhPA70	\N	matched	87	2026-02-12 03:05:11.072065
52e7e6f8-534e-4df8-bc60-07f9eb177f05	571cce4a-2dd0-46db-99d1-14ffd986eb1b	23KVefoON2iEz1dh940quQ	\N	FX0NxcNPDOc	\N	matched	87	2026-02-12 03:05:11.072065
604d5f01-693d-49de-9c4c-160207e935f4	571cce4a-2dd0-46db-99d1-14ffd986eb1b	2f2hbFjim051DVx0o8o4rU	\N	b_cFEOHaaDU	\N	matched	84	2026-02-12 03:05:11.072065
4722dfa8-7f80-494e-9794-ac5b40f7e770	571cce4a-2dd0-46db-99d1-14ffd986eb1b	3yMC1KsTwh0ceXdIe4QQAQ	\N	mzwg5Ldkt5Q	\N	matched	87	2026-02-12 03:05:11.072065
659f401f-40fc-465a-b603-b47f04388ba1	571cce4a-2dd0-46db-99d1-14ffd986eb1b	6Z165JvPnS8PYvGW8oLGLc	\N	J4UsVfwVIx8	\N	matched	90	2026-02-12 03:05:11.072065
7d331878-164b-4edb-9b40-0c5aa0eeddac	571cce4a-2dd0-46db-99d1-14ffd986eb1b	23oYG6HAbahMQEtjk0M7GK	\N	pTo_kZQ2rvs	\N	matched	90	2026-02-12 03:05:11.072065
dc86aedc-95d9-4aa9-bbfd-ada779727d6a	571cce4a-2dd0-46db-99d1-14ffd986eb1b	3hTSUqyG6ntAYXjgYOjS16	\N	wAFfJl6wr-k	\N	matched	87	2026-02-12 03:05:11.072065
2ab49f01-e50d-48e7-9bb1-1f182c2d5082	571cce4a-2dd0-46db-99d1-14ffd986eb1b	2Obwg8XE4sWXLpJSDlxn1r	\N	sF0BX5ECsKE	\N	matched	98	2026-02-12 03:05:11.072065
88ad3957-301c-4061-b602-c4ef33199a9a	571cce4a-2dd0-46db-99d1-14ffd986eb1b	2DB4DdfCFMw1iaR6JaR03a	\N	nWr4r-3bHBc	\N	matched	87	2026-02-12 03:05:11.072065
8cbf7d8a-04e6-4da9-bf05-cdab331a4939	571cce4a-2dd0-46db-99d1-14ffd986eb1b	6v3KW9xbzN5yKLt9YKDYA2	\N	ahiXR3ZmraA	\N	matched	87	2026-02-12 03:05:11.072065
898a8904-fe31-4f52-944a-e8645be44409	571cce4a-2dd0-46db-99d1-14ffd986eb1b	1rfofaqEpACxVEHIZBJe6W	\N	HCjNJDNzw8Y	\N	matched	87	2026-02-12 03:05:11.072065
354458c5-f4d2-4cec-97cd-bd2a99a9e421	571cce4a-2dd0-46db-99d1-14ffd986eb1b	2ogKhhoMClkFXek7ZgxAhN	\N	zAw3MLNU56s	\N	matched	100	2026-02-12 03:05:11.072065
83431056-3338-485c-b44a-e2a5b4f12d22	571cce4a-2dd0-46db-99d1-14ffd986eb1b	3yOlyBJuViE2YSGn3nVE1K	\N	TduHf1tWDvI	\N	matched	87	2026-02-12 03:05:11.072065
08b96c3c-d54d-4657-8aa4-15c781221f8f	571cce4a-2dd0-46db-99d1-14ffd986eb1b	073ILU1C3APNRGF66VhtyA	\N	6-OvO8ZuW98	\N	matched	87	2026-02-12 03:05:11.072065
da99b3ad-183d-4088-b8a9-86b1dae53fed	571cce4a-2dd0-46db-99d1-14ffd986eb1b	6VpQdig9pdpTSIFItgkJV5	\N	FITSPSA8gQs	\N	matched	87	2026-02-12 03:05:11.072065
07914905-87a3-4edb-8cc6-4b11cb4430dc	571cce4a-2dd0-46db-99d1-14ffd986eb1b	705dwKmZCd7ImAUyyD6Mpm	\N	mcr-BYQBqXw	\N	matched	87	2026-02-12 03:05:11.072065
37fea2ce-7caf-4180-84de-67df4de44c71	571cce4a-2dd0-46db-99d1-14ffd986eb1b	1mXVgsBdtIVeCLJnSnmtdV	\N	pmc93sA80HI	\N	matched	100	2026-02-12 03:05:11.072065
0aa4dd4e-cd69-4858-a684-19c83f85b298	571cce4a-2dd0-46db-99d1-14ffd986eb1b	76cy1WJvNGJTj78UqeA5zr	\N	j6jA6dCCToQ	\N	matched	90	2026-02-12 03:05:11.072065
43bb457b-ce29-4210-9802-35eb9cbc0517	571cce4a-2dd0-46db-99d1-14ffd986eb1b	2qxmye6gAegTMjLKEBoR3d	\N	PKonqhZ5PsA	\N	matched	87	2026-02-12 03:05:11.072065
08782569-1320-4637-95eb-dc49cdd0c88e	571cce4a-2dd0-46db-99d1-14ffd986eb1b	4vUmTMuQqjdnvlZmAH61Qk	\N	q07RanslaGM	\N	matched	87	2026-02-12 03:05:11.072065
6248816b-a77f-4d5d-a319-017ab8a2a9d2	571cce4a-2dd0-46db-99d1-14ffd986eb1b	4umIPjkehX1r7uhmGvXiSV	\N	RcSX0hOWcQ0	\N	matched	95	2026-02-12 03:05:11.072065
f8a3b246-fa5d-4367-8b98-ef48c45370cf	571cce4a-2dd0-46db-99d1-14ffd986eb1b	52iLDrSoRtf9lcgFuLVfqE	\N	r1Fx0tqK5Z4	\N	matched	87	2026-02-12 03:05:11.072065
34944d05-6ff5-454d-8488-2ae8eab81718	571cce4a-2dd0-46db-99d1-14ffd986eb1b	0wI7QkCcs8FUQE1OkXUIqd	\N	Em7vc8NWUNY	\N	matched	87	2026-02-12 03:05:11.072065
991696e3-11cc-4421-a2ee-3d12fc686c85	571cce4a-2dd0-46db-99d1-14ffd986eb1b	4l0Mvzj72xxOpRrp6h8nHi	\N	zlJDTxahav0	\N	matched	87	2026-02-12 03:05:11.072065
7fe94a38-dcf0-4aab-9b96-6e3b23d27c62	571cce4a-2dd0-46db-99d1-14ffd986eb1b	1J14CdDAvBTE1AJYUOwl6C	\N	51u5fnyrGj4	\N	matched	87	2026-02-12 03:05:11.072065
d4bacd76-6b28-4bce-a69e-7811bb21e2bb	571cce4a-2dd0-46db-99d1-14ffd986eb1b	0Gl5s8IhMmQE5YQwM8Qx1J	\N	nT0CYXcre0c	\N	matched	100	2026-02-12 03:05:11.072065
f530094e-d571-4d0c-b6c4-4a96c5fc103f	45751671-b1d9-4df3-bff2-ec229a2cb7e0	2J2Z1SkXYghSajLibnQHOa	\N	cfhwSpSDA9c	\N	matched	100	2026-02-12 03:33:37.240606
f7936a37-9124-40e8-a5dc-3b2ed4214c79	45751671-b1d9-4df3-bff2-ec229a2cb7e0	4VrWlk8IQxevMvERoX08iC	\N	uzaYLK3k0DQ	\N	matched	100	2026-02-12 03:33:37.240606
ef08ce1a-da41-4d16-b6b3-7841972c1cbb	45751671-b1d9-4df3-bff2-ec229a2cb7e0	27SdWb2rFzO6GWiYDBTD9j	\N	J1b22l1kFKY	\N	matched	87	2026-02-12 03:33:37.240606
a445aef9-2ab3-4749-8af1-56ea12336f37	45751671-b1d9-4df3-bff2-ec229a2cb7e0	1rMlnD8S0hX7IHZXZDt3gH	\N	oa7JwwdiA98	\N	matched	95	2026-02-12 03:33:37.240606
4e4c1ede-0754-4d0c-865f-c2c966c2baf5	45751671-b1d9-4df3-bff2-ec229a2cb7e0	1v1oIWf2Xgh54kIWuKsDf6	\N	d_HlPboLRL8	\N	matched	87	2026-02-12 03:33:37.240606
5fcb9656-ec12-48ed-9f6d-a05d08c4f4ab	45751671-b1d9-4df3-bff2-ec229a2cb7e0	2MbgQKWhoii8bqsMBWWWgA	\N	mWQACEqf4QY	\N	matched	87	2026-02-12 03:33:37.240606
22a96411-76dd-4dfd-afcf-32ba50270405	45751671-b1d9-4df3-bff2-ec229a2cb7e0	5zHX1aFxP5p10NI4L1nkai	\N	4FrYREQ4XP8	\N	matched	87	2026-02-12 03:33:37.240606
8496c0e9-a9f0-4710-8f60-97435c033949	45751671-b1d9-4df3-bff2-ec229a2cb7e0	2rbDhOo9Fh61Bbu23T2qCk	\N	Voo86mlxZvA	\N	matched	100	2026-02-12 03:33:37.240606
0872dd90-df7b-41b7-97ae-d6568c8b701a	45751671-b1d9-4df3-bff2-ec229a2cb7e0	3Z0oQ8r78OUaHvGPiDBR3W	\N	2qPlX1FMJXI	\N	matched	100	2026-02-12 03:33:37.240606
292ce8dc-f407-4c66-90a8-d98bdd951801	45751671-b1d9-4df3-bff2-ec229a2cb7e0	0Ie5uiv54KgCr7P4sYDTHl	\N	IJpeLGzJBGo	\N	matched	95	2026-02-12 03:33:37.240606
e3d220e4-a9c5-4f2d-970e-6e43f299753d	45751671-b1d9-4df3-bff2-ec229a2cb7e0	50WeOnXhM1H7AZEeIDoWfZ	\N	7noPeO5sKHA	\N	matched	100	2026-02-12 03:33:37.240606
e8dc53af-aad0-4c3a-9faa-1eac1cfc36e4	45751671-b1d9-4df3-bff2-ec229a2cb7e0	0qcr5FMsEO85NAQjrlDRKo	\N	qSU560anReg	\N	matched	95	2026-02-12 03:33:37.240606
fcfe0f9f-ea7f-47be-8636-6c659eddc931	45751671-b1d9-4df3-bff2-ec229a2cb7e0	0EnS2KLYG9ig26yYltoXJf	\N	_373lKEvQ34	\N	matched	100	2026-02-12 03:33:37.240606
eef3ac1a-fe92-4182-b823-01aaaace834f	45751671-b1d9-4df3-bff2-ec229a2cb7e0	4crTd5bkKKISvU2w9eEc0r	\N	HA0vlhJANow	\N	matched	87	2026-02-12 03:33:37.240606
f80cc33e-db46-4539-b810-172f5a513f33	45751671-b1d9-4df3-bff2-ec229a2cb7e0	3ZmEwYX2GbUUM9C2rpEhkm	\N	WhG0-WO0vLE	\N	matched	87	2026-02-12 03:33:37.240606
f0b1c4a6-c3e8-4fca-bb54-58fc40d6ad23	45751671-b1d9-4df3-bff2-ec229a2cb7e0	5Th9jSEXB7XdeQHXPkBvws	\N	eMpLkkhPA70	\N	matched	87	2026-02-12 03:33:37.240606
defe3333-f022-493d-ba96-616fe85071c4	45751671-b1d9-4df3-bff2-ec229a2cb7e0	23KVefoON2iEz1dh940quQ	\N	FX0NxcNPDOc	\N	matched	87	2026-02-12 03:33:37.240606
e6b15597-dbb4-43a1-902f-731b149a288d	45751671-b1d9-4df3-bff2-ec229a2cb7e0	2f2hbFjim051DVx0o8o4rU	\N	b_cFEOHaaDU	\N	matched	84	2026-02-12 03:33:37.240606
2d32517f-eae2-4920-a29c-b8ca63f694a1	45751671-b1d9-4df3-bff2-ec229a2cb7e0	3yMC1KsTwh0ceXdIe4QQAQ	\N	mzwg5Ldkt5Q	\N	matched	87	2026-02-12 03:33:37.240606
33eb10cf-626b-4f37-8599-e55bd7575d80	45751671-b1d9-4df3-bff2-ec229a2cb7e0	6Z165JvPnS8PYvGW8oLGLc	\N	J4UsVfwVIx8	\N	matched	90	2026-02-12 03:33:37.240606
9b791ed0-220d-4acc-8cc4-613fb4b44f29	45751671-b1d9-4df3-bff2-ec229a2cb7e0	23oYG6HAbahMQEtjk0M7GK	\N	pTo_kZQ2rvs	\N	matched	90	2026-02-12 03:33:37.240606
ea1e22cb-9d45-4a0e-a7fb-5ce9fc188d4f	45751671-b1d9-4df3-bff2-ec229a2cb7e0	3hTSUqyG6ntAYXjgYOjS16	\N	wAFfJl6wr-k	\N	matched	87	2026-02-12 03:33:37.240606
818fc8fa-58ab-4492-852b-38a74d77b470	45751671-b1d9-4df3-bff2-ec229a2cb7e0	2Obwg8XE4sWXLpJSDlxn1r	\N	sF0BX5ECsKE	\N	matched	98	2026-02-12 03:33:37.240606
1db67724-8d64-4dc4-9b77-133f27f10377	45751671-b1d9-4df3-bff2-ec229a2cb7e0	2DB4DdfCFMw1iaR6JaR03a	\N	nWr4r-3bHBc	\N	matched	87	2026-02-12 03:33:37.240606
02c7d357-199c-4ac2-b5db-7fd272a43d10	45751671-b1d9-4df3-bff2-ec229a2cb7e0	6v3KW9xbzN5yKLt9YKDYA2	\N	ahiXR3ZmraA	\N	matched	87	2026-02-12 03:33:37.240606
04aa9633-9e71-41d9-9618-8780288631a7	45751671-b1d9-4df3-bff2-ec229a2cb7e0	1rfofaqEpACxVEHIZBJe6W	\N	HCjNJDNzw8Y	\N	matched	87	2026-02-12 03:33:37.240606
7f36629a-2f83-4603-b7e0-6e44446440e8	45751671-b1d9-4df3-bff2-ec229a2cb7e0	2ogKhhoMClkFXek7ZgxAhN	\N	zAw3MLNU56s	\N	matched	100	2026-02-12 03:33:37.240606
6b0d3601-4cd9-46a3-af1c-60b75f5a179f	45751671-b1d9-4df3-bff2-ec229a2cb7e0	3yOlyBJuViE2YSGn3nVE1K	\N	TduHf1tWDvI	\N	matched	87	2026-02-12 03:33:37.240606
de2a5e83-b332-4804-8a2e-c6377c4c0f70	45751671-b1d9-4df3-bff2-ec229a2cb7e0	073ILU1C3APNRGF66VhtyA	\N	6-OvO8ZuW98	\N	matched	87	2026-02-12 03:33:37.240606
19a8b58c-9a58-454a-b517-50cff45e6787	45751671-b1d9-4df3-bff2-ec229a2cb7e0	6VpQdig9pdpTSIFItgkJV5	\N	FITSPSA8gQs	\N	matched	87	2026-02-12 03:33:37.240606
8ed07d78-b64b-467f-8bcd-ffda60feb48f	45751671-b1d9-4df3-bff2-ec229a2cb7e0	705dwKmZCd7ImAUyyD6Mpm	\N	mcr-BYQBqXw	\N	matched	87	2026-02-12 03:33:37.240606
050832e0-9393-4565-9522-23572906de7b	45751671-b1d9-4df3-bff2-ec229a2cb7e0	1mXVgsBdtIVeCLJnSnmtdV	\N	pmc93sA80HI	\N	matched	100	2026-02-12 03:33:37.240606
f2f325d5-8503-4d13-a97e-047ba3d58c57	45751671-b1d9-4df3-bff2-ec229a2cb7e0	76cy1WJvNGJTj78UqeA5zr	\N	j6jA6dCCToQ	\N	matched	90	2026-02-12 03:33:37.240606
b87b6bfe-dcd2-498e-829d-fd2b1c20c0a7	45751671-b1d9-4df3-bff2-ec229a2cb7e0	2qxmye6gAegTMjLKEBoR3d	\N	PKonqhZ5PsA	\N	matched	87	2026-02-12 03:33:37.240606
b13c176f-0a2f-431d-9428-95bf16f9ea0b	45751671-b1d9-4df3-bff2-ec229a2cb7e0	4vUmTMuQqjdnvlZmAH61Qk	\N	q07RanslaGM	\N	matched	87	2026-02-12 03:33:37.240606
34ec5ef1-16b8-4495-b19a-93696aafbb1b	45751671-b1d9-4df3-bff2-ec229a2cb7e0	4umIPjkehX1r7uhmGvXiSV	\N	RcSX0hOWcQ0	\N	matched	95	2026-02-12 03:33:37.240606
66026d4f-183c-45ed-b7d7-b86bffb1cefb	45751671-b1d9-4df3-bff2-ec229a2cb7e0	52iLDrSoRtf9lcgFuLVfqE	\N	r1Fx0tqK5Z4	\N	matched	87	2026-02-12 03:33:37.240606
ed35bcae-4f77-4936-a6c5-abe5a3a8e639	45751671-b1d9-4df3-bff2-ec229a2cb7e0	0wI7QkCcs8FUQE1OkXUIqd	\N	Em7vc8NWUNY	\N	matched	87	2026-02-12 03:33:37.240606
02b7f587-8f73-4910-a540-32894da0cf53	45751671-b1d9-4df3-bff2-ec229a2cb7e0	4l0Mvzj72xxOpRrp6h8nHi	\N	zlJDTxahav0	\N	matched	87	2026-02-12 03:33:37.240606
48d9ed89-00bd-4fba-ae21-5e594c88bacd	45751671-b1d9-4df3-bff2-ec229a2cb7e0	1J14CdDAvBTE1AJYUOwl6C	\N	51u5fnyrGj4	\N	matched	87	2026-02-12 03:33:37.240606
7bd4fcfe-098d-446b-812b-66dabccaa257	45751671-b1d9-4df3-bff2-ec229a2cb7e0	0Gl5s8IhMmQE5YQwM8Qx1J	\N	nT0CYXcre0c	\N	matched	100	2026-02-12 03:33:37.240606
a03218ba-4bd8-458c-bed9-a4232b7c2688	817f6205-2cfe-434e-9576-a3ff2a772abd	3i0J9e00YhlrKO7AqgdO8Z	\N	A2-aUNmYNLM	\N	matched	100	2026-02-12 03:33:37.240606
d14dc5e1-6681-4f48-8450-c9dd9b049e98	817f6205-2cfe-434e-9576-a3ff2a772abd	4aPrZhKlDAh9aoSlXV4jZJ	\N	18Hi_Q7HKyw	\N	matched	90	2026-02-12 03:33:37.240606
acf505e2-421c-4f87-a0ed-7b5084909ca4	399011b8-4308-4292-b251-df4d250ac260	3i0J9e00YhlrKO7AqgdO8Z	\N	A2-aUNmYNLM	\N	matched	100	2026-02-12 03:33:46.605229
eb48a1ac-7d22-4aff-9e70-57529c11a991	399011b8-4308-4292-b251-df4d250ac260	4aPrZhKlDAh9aoSlXV4jZJ	\N	18Hi_Q7HKyw	\N	matched	90	2026-02-12 03:33:46.605229
6af1cbaf-5042-41da-840a-2d8409b7e496	41148f28-33a2-47cc-8313-73b11c9160c2	3i0J9e00YhlrKO7AqgdO8Z	\N	A2-aUNmYNLM	\N	matched	100	2026-02-12 03:52:26.882848
644a7e0e-70bd-41e8-b88d-acf932992f2e	41148f28-33a2-47cc-8313-73b11c9160c2	4aPrZhKlDAh9aoSlXV4jZJ	\N	18Hi_Q7HKyw	\N	matched	90	2026-02-12 03:52:26.882848
bc03005b-acd3-48de-9754-788527b7a473	642d93dc-fc77-4057-bb24-e50770622cbd	UG9ZvLPSWaI	\N	77eZIuMTB9HrQ86L4ljqEX	\N	matched	100	2026-02-12 04:51:25.514338
21fec4f0-24cb-4d0c-81ef-ff615474b8e2	642d93dc-fc77-4057-bb24-e50770622cbd	FzNxEQr62xY	\N	0cnxJjQ8T4GSz87TaIkwDY	\N	matched	100	2026-02-12 04:51:25.514338
aba0f7c7-a065-4dcd-8e05-6a2e6ef46f30	642d93dc-fc77-4057-bb24-e50770622cbd	fUXSk0BoML4	\N	56HnJIjgi41kpjh6L3Y1Ca	\N	matched	100	2026-02-12 04:51:25.514338
e4397cd6-5a91-409d-a445-e4e48219b4b6	642d93dc-fc77-4057-bb24-e50770622cbd	bH4UDgIzQgE	\N	731NDNG7VQOA2KBnS43jqr	\N	matched	100	2026-02-12 04:51:25.514338
fbe626ce-2bb2-4685-a679-e38eb98f80d2	642d93dc-fc77-4057-bb24-e50770622cbd	NKqXl7ddKTQ	\N	16aRxdbEl0smktrm0hK0eX	\N	matched	100	2026-02-12 04:51:25.514338
e8e4efb8-bf5b-48c2-919a-58be96c5d5a1	3164e071-5edf-4f0d-911a-c0f70f410d38	LfwAlJhXeHQ	\N	3UqcIgObI7zZk8e7nvqmfc	\N	matched	100	2026-02-12 04:54:24.54367
a0e4d965-5e0a-4c77-816d-9d2a90954f5f	3164e071-5edf-4f0d-911a-c0f70f410d38	kCCQtMx7V7k	\N	6GkFuU6FVfx866bhM1T1Iz	\N	matched	100	2026-02-12 04:54:24.54367
2f47af01-efba-47d5-963b-74bb8e576a3c	3164e071-5edf-4f0d-911a-c0f70f410d38	hCAt-l4vUuM	\N	731NDNG7VQOA2KBnS43jqr	\N	matched	100	2026-02-12 04:54:24.54367
161b2d2d-4d05-472f-8def-0a31e17efd8c	3164e071-5edf-4f0d-911a-c0f70f410d38	DfLS0A5IAhk	\N	1z567QCaLLuRE7ZxQS5oYS	\N	matched	100	2026-02-12 04:54:24.54367
bab89cd6-b0f3-4ca8-ae70-2d3c9946df10	3164e071-5edf-4f0d-911a-c0f70f410d38	K2XCMnB6984	\N	4jcrO2T6XJ1B7hXsAEJ3ve	\N	matched	100	2026-02-12 04:54:24.54367
ac386ac5-312b-4134-9be1-5e3baa9c0933	3164e071-5edf-4f0d-911a-c0f70f410d38	BaIVxGeRUiY	\N	6D09vfVjCFlRxOGdmja0yp	\N	matched	100	2026-02-12 04:54:24.54367
142c89ae-795a-4bcd-9bc6-22003f620031	f9a8a469-ddf6-428c-8bbe-499162474911	LfwAlJhXeHQ	\N	3UqcIgObI7zZk8e7nvqmfc	\N	matched	100	2026-02-12 05:00:39.320026
a6320690-ed25-4443-a15b-0c13babfad28	f9a8a469-ddf6-428c-8bbe-499162474911	kCCQtMx7V7k	\N	6GkFuU6FVfx866bhM1T1Iz	\N	matched	100	2026-02-12 05:00:39.320026
e0ca2bff-8acd-4c00-801e-d4f6b374c07f	f9a8a469-ddf6-428c-8bbe-499162474911	hCAt-l4vUuM	\N	731NDNG7VQOA2KBnS43jqr	\N	matched	100	2026-02-12 05:00:39.320026
1efa37ff-c2e0-4e7b-819b-f3bb5a46cdfb	f9a8a469-ddf6-428c-8bbe-499162474911	DfLS0A5IAhk	\N	1z567QCaLLuRE7ZxQS5oYS	\N	matched	100	2026-02-12 05:00:39.320026
30ccc8f9-a841-47ac-a96f-0d57eeec4d0f	f9a8a469-ddf6-428c-8bbe-499162474911	K2XCMnB6984	\N	4jcrO2T6XJ1B7hXsAEJ3ve	\N	matched	100	2026-02-12 05:00:39.320026
933e0f8d-8704-48cd-8516-bd6189d8a07a	f9a8a469-ddf6-428c-8bbe-499162474911	BaIVxGeRUiY	\N	6D09vfVjCFlRxOGdmja0yp	\N	matched	100	2026-02-12 05:00:39.320026
295414a5-8cef-409b-a962-a5659c4deb96	babcc50a-7a7e-411b-badc-49907e52d983	zjbMpAhOzLI	\N	1ySzohfiLo3l12pcqW40p3	\N	matched	100	2026-02-16 03:29:00.247787
12ae5d4a-b4ed-4edb-b383-d86126d3e8b1	babcc50a-7a7e-411b-badc-49907e52d983	MD-EM4vApl4	\N	5rTC3tKShDyYK0wk0rq96X	\N	matched	100	2026-02-16 03:29:00.247787
250c5da5-5ef4-4b50-805a-0abf7b3d8b55	babcc50a-7a7e-411b-badc-49907e52d983	aE5nfPCmrjQ	\N	3sgensqwEH4E7icL0TWM1A	\N	matched	100	2026-02-16 03:29:00.247787
246adff4-7c65-45a6-9ce9-03418253f965	babcc50a-7a7e-411b-badc-49907e52d983	NaevY5LCZyc	\N	1Dugqy9EGYV66nkp7yLiA4	\N	matched	100	2026-02-16 03:29:00.247787
9a07af7e-87a9-478a-b87d-a57f81851dd8	babcc50a-7a7e-411b-badc-49907e52d983	OJk_XO0nS9A	\N	5KWV7S3osWbeOMPHCeWVyK	\N	matched	100	2026-02-16 03:29:00.247787
45902355-8046-4bc6-810e-910c4e1a9aad	babcc50a-7a7e-411b-badc-49907e52d983	6rgiz8p_DmQ	\N	0RVH9WtDKwNCQ1GMpPRFzj	\N	matched	100	2026-02-16 03:29:00.247787
a192df1f-5dc4-48e0-ab94-8fe93181a975	babcc50a-7a7e-411b-badc-49907e52d983	b24mOclhalM	\N	3zamtBVBTOhYDizGtduNZQ	\N	matched	73	2026-02-16 03:29:00.247787
57bc8ca0-c1b8-4c14-8260-f1892f9aa631	babcc50a-7a7e-411b-badc-49907e52d983	PwG5YGNvFr4	\N	1PE0jpl3pt4CkmzB34q9ws	\N	matched	100	2026-02-16 03:29:00.247787
e60ac6bc-9fe6-4cca-b87e-1a265f85d350	babcc50a-7a7e-411b-badc-49907e52d983	wo0bpfOSIPc	\N	2TVSN0ed8AFnLUaWPXof2e	\N	matched	100	2026-02-16 03:29:00.247787
25176f5e-85eb-4079-a5a5-88543105cd44	babcc50a-7a7e-411b-badc-49907e52d983	Z-dN-y8_Uzo	\N	6rk87fdFvUTxJqaa5a7Od0	\N	matched	100	2026-02-16 03:29:00.247787
3879d8c5-68f1-4b98-ba00-189252debe4c	babcc50a-7a7e-411b-badc-49907e52d983	Floqvslobvs	\N	3GgYYPUOIwd1uMBvfv5bNW	\N	matched	100	2026-02-16 03:29:00.247787
4aa3b0f0-1ca0-4853-8d36-6eee81e4a19d	babcc50a-7a7e-411b-badc-49907e52d983	YiRV6RjWLck	\N	1Oao2WEZDXfK4TEINsglKg	\N	matched	100	2026-02-16 03:29:00.247787
188bd9e7-5942-48a8-93a7-b59491799f05	babcc50a-7a7e-411b-badc-49907e52d983	t6tqWadIbqU	\N	2oo62jEC2X10AQZFVCRuWI	\N	matched	100	2026-02-16 03:29:00.247787
e26a9613-1f32-48d7-a982-808396a10e2e	babcc50a-7a7e-411b-badc-49907e52d983	RFTuYO0EnJ0	\N	3T3XVQvaJ09MckNdDE8hA8	\N	matched	100	2026-02-16 03:29:00.247787
0e058ba0-03e8-4887-a48e-65ec7eda0864	babcc50a-7a7e-411b-badc-49907e52d983	QynH_vd2_a0	\N	4WS3bhnGgDjxwtAUWaPyNW	\N	matched	100	2026-02-16 03:29:00.247787
cdebce92-84f2-4ec3-827a-502b5f9d64fb	babcc50a-7a7e-411b-badc-49907e52d983	mI5Qv3G1JMI	\N	2t8CVuJb6W5E0FutItjhpd	\N	matched	100	2026-02-16 03:29:00.247787
8261d6f8-669d-4418-9e76-667ba6e1d0af	babcc50a-7a7e-411b-badc-49907e52d983	Efo9US_ZUgU	\N	1rgK0UrJNwkaIptPI0a5kg	\N	matched	100	2026-02-16 03:29:00.247787
821b8e7e-0d9b-4007-879c-1b1a1e868bd4	babcc50a-7a7e-411b-badc-49907e52d983	aJsKPgb6_N4	\N	4kSTjIGLc8KM21Qbv3Y4UN	\N	matched	91	2026-02-16 03:29:00.247787
ec2f5ee0-999f-47f8-8f2e-e3ac7c64737c	9767e769-b740-45e8-8500-4fdad04809f0	m0hIGk6aNxY	\N	5M1qjvTphJznaT4zdtDxNT	\N	matched	100	2026-02-16 03:30:46.914763
cf8de396-b06e-4e1e-939e-f29ea0ba0b7e	58b32531-037f-49ef-b938-b6cadaf0269b	3i0J9e00YhlrKO7AqgdO8Z	\N	A2-aUNmYNLM	\N	matched	100	2026-02-19 04:26:56.778396
413cc63f-144c-41a1-9d79-ecf28ca40b96	58b32531-037f-49ef-b938-b6cadaf0269b	4aPrZhKlDAh9aoSlXV4jZJ	\N	18Hi_Q7HKyw	\N	matched	90	2026-02-19 04:26:56.778396
76269daa-4bbf-4fc2-9ac0-813a22131872	e54dd416-9ce8-44cc-ade9-adb75601de08	3i0J9e00YhlrKO7AqgdO8Z	\N	A2-aUNmYNLM	\N	matched	100	2026-02-19 05:06:21.570533
53a80302-c2f9-4906-8874-b3c95b1e5420	e54dd416-9ce8-44cc-ade9-adb75601de08	4aPrZhKlDAh9aoSlXV4jZJ	\N	18Hi_Q7HKyw	\N	matched	90	2026-02-19 05:06:21.570533
\.


--
-- Data for Name: transfer_jobs; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.transfer_jobs (id, session_id, source_playlist_id, source_playlist_name, target_playlist_id, status, created_at) FROM stdin;
c26dda52-a3dc-4375-bfad-0caaaf2f9941	fe9ed359-3e89-4860-8c12-1bbda9d2f519	2i0zgJtaabtbI1kJUqkidI	Blues	\N	analyzing	2026-02-11 02:35:09.332158
c86aa8df-3bc2-4119-aa14-5b5e47c69ae8	dc9b8355-3970-434c-b63d-842b91205779	2i0zgJtaabtbI1kJUqkidI	Blues	\N	analyzing	2026-02-11 02:39:22.101891
2e6052bd-f046-45d6-8c97-37442649f2b3	a2a8d9f9-9d46-49f0-8508-7b232458c640	2i0zgJtaabtbI1kJUqkidI	Blues	\N	analyzing	2026-02-11 02:43:04.132522
bfc1a6fa-6041-4f32-83ec-04a71f66fd8a	49a463c5-225e-4587-bded-4eb1f19584aa	2i0zgJtaabtbI1kJUqkidI	Blues	\N	analyzing	2026-02-12 02:13:14.642447
b6e1b364-3fd9-4348-bf7e-c99488c36037	b0ae3e92-b096-4e9f-82c1-1433fdff5b56	2i0zgJtaabtbI1kJUqkidI	Blues	\N	analyzing	2026-02-12 02:22:32.629087
2ca9b045-a083-40ef-b933-a070cc7594ce	77873e04-7a3d-45dd-b8df-e622f49f2bd6	2i0zgJtaabtbI1kJUqkidI	Blues	\N	analyzing	2026-02-12 02:37:47.006109
c95bb82e-e96a-446f-a973-f814ea1cfc39	e89c2e9f-352a-4e83-996e-19d8ece297c9	2i0zgJtaabtbI1kJUqkidI	Blues	\N	analyzing	2026-02-12 02:38:24.699954
2b5b4967-35ac-432a-873a-8bec7b56bdff	b279b78d-8b7a-431e-85b8-2d782a77b8a5	2i0zgJtaabtbI1kJUqkidI	Blues	\N	analyzing	2026-02-12 02:38:48.66928
cb69538d-e593-442d-893e-67f68c1c27ab	ef31a103-027b-43db-b4ac-f73e27c757c7	2i0zgJtaabtbI1kJUqkidI	Blues	\N	analyzing	2026-02-12 02:39:16.114838
571cce4a-2dd0-46db-99d1-14ffd986eb1b	d150ef72-5b27-4ae4-8b6c-7a4598bec004	2i0zgJtaabtbI1kJUqkidI	Blues	\N	analyzing	2026-02-12 03:05:09.526265
45751671-b1d9-4df3-bff2-ec229a2cb7e0	d9e2a271-8d1b-4f52-8c37-69ce2708ddaa	2i0zgJtaabtbI1kJUqkidI	Blues	\N	analyzing	2026-02-12 03:33:35.208914
817f6205-2cfe-434e-9576-a3ff2a772abd	d9e2a271-8d1b-4f52-8c37-69ce2708ddaa	3YxJvy4mlC3XqfFg7VKnyP	1	\N	analyzing	2026-02-12 03:33:36.746157
399011b8-4308-4292-b251-df4d250ac260	315a276a-22b0-4341-9a29-feb1919963c3	3YxJvy4mlC3XqfFg7VKnyP	1	PLZpak7yvjd8gYuNtZ8vBUtI8lWxCXJGVY	completed	2026-02-12 03:33:45.54336
942bab84-c7d6-4e3d-989b-c9b9c86cd4e5	eb67029a-a330-449c-aada-57d528f9059d	3YxJvy4mlC3XqfFg7VKnyP	1	\N	analyzing	2026-02-12 03:46:53.336371
41148f28-33a2-47cc-8313-73b11c9160c2	3f2b0360-e889-4a10-b783-f4abe0cf0f2e	3YxJvy4mlC3XqfFg7VKnyP	1	PLZpak7yvjd8hpm7uBciX_d5ti1EL5AArU	completed	2026-02-12 03:52:25.832018
642d93dc-fc77-4057-bb24-e50770622cbd	508c817c-b882-488a-ba25-aaa36563bfaf	PLZpak7yvjd8g4pxWsF3vNa_3ZZ5JO_qpb	9ja best	\N	analyzing	2026-02-12 04:51:21.595736
3164e071-5edf-4f0d-911a-c0f70f410d38	9fc0c2c3-e7f3-4397-8f20-a5f5007e7de7	PLZpak7yvjd8gm_gnnZrKAzKaTr8Z8QO0i	afrobest	\N	analyzing	2026-02-12 04:54:23.166205
f9a8a469-ddf6-428c-8bbe-499162474911	f9b6a77e-740b-44c5-9f2d-8bbbcc7b6871	PLZpak7yvjd8gm_gnnZrKAzKaTr8Z8QO0i	afrobest	\N	completed	2026-02-12 05:00:38.070553
babcc50a-7a7e-411b-badc-49907e52d983	6dffad23-2313-46dc-9837-1679cac98809	PLZpak7yvjd8iUSgJP6HiZliLmZJry7Dvj	Mood	\N	analyzing	2026-02-16 03:28:58.652356
9767e769-b740-45e8-8500-4fdad04809f0	4eb99cce-a5a5-43f3-aef3-88cd29461ae0	PLZpak7yvjd8gy_nnPR96Mr9J69v9bzmQo	highlife	\N	completed	2026-02-16 03:30:45.145253
58b32531-037f-49ef-b938-b6cadaf0269b	df797666-1fd4-4dd3-81fb-60df5ead2097	3YxJvy4mlC3XqfFg7VKnyP	1	PLZpak7yvjd8g66q15LcDwReD9lWs5VtVT	completed	2026-02-19 04:26:55.338324
e54dd416-9ce8-44cc-ade9-adb75601de08	4d9d44ba-e4cc-4762-8c8c-c415f467afe8	3YxJvy4mlC3XqfFg7VKnyP	1	PLZpak7yvjd8hRIe26EkWQdXzbneivwoyw	completed	2026-02-19 05:06:20.240606
\.


--
-- Data for Name: transfer_sessions; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.transfer_sessions (id, created_at, user_id, source_platform, target_platform, status) FROM stdin;
644866be-7674-44f6-bd91-59aba974f26c	2026-02-07 06:14:25.914002	9121df4d-4686-4bba-95bd-efeaff3d9c2b	spotify	youtube	ready
b7618a44-6b7d-4b26-b6db-b35dc59905b4	2026-02-07 06:16:54.432673	9121df4d-4686-4bba-95bd-efeaff3d9c2b	spotify	youtube	analyzing
a7c11c7f-70df-4bc4-bd40-783d9060f34f	2026-02-07 06:17:11.73717	9121df4d-4686-4bba-95bd-efeaff3d9c2b	spotify	youtube	ready
9fc117b5-f1d5-4ae2-8e68-0e8e62d8551d	2026-02-07 06:22:50.225852	9121df4d-4686-4bba-95bd-efeaff3d9c2b	spotify	youtube	ready
f56813d2-afed-44ff-a501-d73ee5545cc8	2026-02-07 06:23:59.36924	9121df4d-4686-4bba-95bd-efeaff3d9c2b	spotify	youtube	ready
a379170e-ecfe-47bb-b4bb-fec59f673993	2026-02-07 07:03:15.117132	9121df4d-4686-4bba-95bd-efeaff3d9c2b	spotify	youtube	ready
aee49280-7339-4654-a95c-99dbe5d4c6d9	2026-02-07 07:05:15.497838	9121df4d-4686-4bba-95bd-efeaff3d9c2b	spotify	youtube	ready
bfcaa83c-a679-4a61-86bc-b527eed9f2ed	2026-02-10 05:09:06.288436	9121df4d-4686-4bba-95bd-efeaff3d9c2b	spotify	youtube	analyzing
c45e90ba-ecd1-4475-8d1c-afbb5e6c9009	2026-02-10 05:14:19.927672	9121df4d-4686-4bba-95bd-efeaff3d9c2b	spotify	youtube	ready
45bf6f4e-8ac0-4afc-b256-b42f3d8a583f	2026-02-10 06:14:08.849747	9121df4d-4686-4bba-95bd-efeaff3d9c2b	spotify	youtube	analyzing
8d7d97a2-73d2-452a-9b37-8f3e7de607b8	2026-02-10 06:21:00.760604	9121df4d-4686-4bba-95bd-efeaff3d9c2b	spotify	youtube	ready
febdfd0c-f1b2-4981-a20a-64854b5abf99	2026-02-10 06:22:24.551153	9121df4d-4686-4bba-95bd-efeaff3d9c2b	youtube	spotify	analyzing
3ebe5514-fd4c-41c4-8d54-01ca566de1a9	2026-02-10 06:24:34.015856	9121df4d-4686-4bba-95bd-efeaff3d9c2b	youtube	spotify	analyzing
b014a2a7-7995-4da2-85ae-1a3830354e5b	2026-02-11 00:17:22.949838	9121df4d-4686-4bba-95bd-efeaff3d9c2b	youtube	spotify	analyzing
3d019c9a-62d3-4d97-980d-4cff2a4c4175	2026-02-11 00:28:35.96739	9121df4d-4686-4bba-95bd-efeaff3d9c2b	youtube	spotify	ready
e88cd266-8b31-4e0d-b4be-cf542cd47370	2026-02-11 00:31:52.960978	9121df4d-4686-4bba-95bd-efeaff3d9c2b	youtube	spotify	ready
4ac500b2-c29a-45fe-95b9-19a76121063c	2026-02-11 00:32:37.689314	9121df4d-4686-4bba-95bd-efeaff3d9c2b	youtube	spotify	ready
87df7878-464c-47f7-a84b-036c97532ca2	2026-02-11 00:33:13.955949	9121df4d-4686-4bba-95bd-efeaff3d9c2b	youtube	spotify	ready
8f42d60c-d984-4da4-a664-5452b236b2e8	2026-02-11 01:43:47.555583	9121df4d-4686-4bba-95bd-efeaff3d9c2b	spotify	youtube	ready
cf3394ed-084d-4d7d-a059-b50adb532da3	2026-02-11 02:16:15.607441	9121df4d-4686-4bba-95bd-efeaff3d9c2b	spotify	youtube	analyzing
fe9ed359-3e89-4860-8c12-1bbda9d2f519	2026-02-11 02:35:09.289113	9121df4d-4686-4bba-95bd-efeaff3d9c2b	spotify	youtube	analyzing
dc9b8355-3970-434c-b63d-842b91205779	2026-02-11 02:39:22.055988	9121df4d-4686-4bba-95bd-efeaff3d9c2b	spotify	youtube	analyzing
a2a8d9f9-9d46-49f0-8508-7b232458c640	2026-02-11 02:43:04.098667	9121df4d-4686-4bba-95bd-efeaff3d9c2b	spotify	youtube	ready
49a463c5-225e-4587-bded-4eb1f19584aa	2026-02-12 02:13:14.529913	9121df4d-4686-4bba-95bd-efeaff3d9c2b	spotify	youtube	ready
b0ae3e92-b096-4e9f-82c1-1433fdff5b56	2026-02-12 02:22:32.543384	9121df4d-4686-4bba-95bd-efeaff3d9c2b	spotify	youtube	ready
77873e04-7a3d-45dd-b8df-e622f49f2bd6	2026-02-12 02:37:46.949906	9121df4d-4686-4bba-95bd-efeaff3d9c2b	spotify	youtube	ready
e89c2e9f-352a-4e83-996e-19d8ece297c9	2026-02-12 02:38:24.697518	9121df4d-4686-4bba-95bd-efeaff3d9c2b	spotify	youtube	ready
b279b78d-8b7a-431e-85b8-2d782a77b8a5	2026-02-12 02:38:48.667028	9121df4d-4686-4bba-95bd-efeaff3d9c2b	spotify	youtube	ready
ef31a103-027b-43db-b4ac-f73e27c757c7	2026-02-12 02:39:16.11203	9121df4d-4686-4bba-95bd-efeaff3d9c2b	spotify	youtube	ready
d150ef72-5b27-4ae4-8b6c-7a4598bec004	2026-02-12 03:05:09.442648	9121df4d-4686-4bba-95bd-efeaff3d9c2b	spotify	youtube	ready
d9e2a271-8d1b-4f52-8c37-69ce2708ddaa	2026-02-12 03:33:35.126171	9121df4d-4686-4bba-95bd-efeaff3d9c2b	spotify	youtube	ready
315a276a-22b0-4341-9a29-feb1919963c3	2026-02-12 03:33:45.54062	9121df4d-4686-4bba-95bd-efeaff3d9c2b	spotify	youtube	ready
eb67029a-a330-449c-aada-57d528f9059d	2026-02-12 03:46:53.203786	9121df4d-4686-4bba-95bd-efeaff3d9c2b	spotify	youtube	analyzing
3f2b0360-e889-4a10-b783-f4abe0cf0f2e	2026-02-12 03:52:25.48772	9121df4d-4686-4bba-95bd-efeaff3d9c2b	spotify	youtube	ready
508c817c-b882-488a-ba25-aaa36563bfaf	2026-02-12 04:51:21.529998	9121df4d-4686-4bba-95bd-efeaff3d9c2b	youtube	spotify	ready
9fc0c2c3-e7f3-4397-8f20-a5f5007e7de7	2026-02-12 04:54:23.095607	9121df4d-4686-4bba-95bd-efeaff3d9c2b	youtube	spotify	ready
f9b6a77e-740b-44c5-9f2d-8bbbcc7b6871	2026-02-12 05:00:38.013856	9121df4d-4686-4bba-95bd-efeaff3d9c2b	youtube	spotify	ready
6dffad23-2313-46dc-9837-1679cac98809	2026-02-16 03:28:58.586058	9121df4d-4686-4bba-95bd-efeaff3d9c2b	youtube	spotify	ready
4eb99cce-a5a5-43f3-aef3-88cd29461ae0	2026-02-16 03:30:45.142248	9121df4d-4686-4bba-95bd-efeaff3d9c2b	youtube	spotify	ready
df797666-1fd4-4dd3-81fb-60df5ead2097	2026-02-19 04:26:55.243271	9121df4d-4686-4bba-95bd-efeaff3d9c2b	spotify	youtube	ready
4d9d44ba-e4cc-4762-8c8c-c415f467afe8	2026-02-19 05:06:20.085837	9121df4d-4686-4bba-95bd-efeaff3d9c2b	spotify	youtube	ready
\.


--
-- Data for Name: transferhistory; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.transferhistory (id, created_at, user_id, source_platform, target_platform, playlist_id, status) FROM stdin;
\.


--
-- Name: song_mappings song_mappings_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.song_mappings
    ADD CONSTRAINT song_mappings_pkey PRIMARY KEY (id);


--
-- Name: transfer_items transfer_items_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.transfer_items
    ADD CONSTRAINT transfer_items_pkey PRIMARY KEY (id);


--
-- Name: transfer_jobs transfer_jobs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.transfer_jobs
    ADD CONSTRAINT transfer_jobs_pkey PRIMARY KEY (id);


--
-- Name: transfer_sessions transfer_sessions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.transfer_sessions
    ADD CONSTRAINT transfer_sessions_pkey PRIMARY KEY (id);


--
-- Name: transferhistory transferhistory_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.transferhistory
    ADD CONSTRAINT transferhistory_pkey PRIMARY KEY (id);


--
-- Name: song_mappings unique_mapping_pair; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.song_mappings
    ADD CONSTRAINT unique_mapping_pair UNIQUE (source_platform, source_track_id, destination_platform);


--
-- Name: idx_jobs_session; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_jobs_session ON public.transfer_jobs USING btree (session_id);


--
-- Name: idx_sessions_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sessions_user ON public.transfer_sessions USING btree (user_id);


--
-- Name: idx_song_mappings_lookup; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_song_mappings_lookup ON public.song_mappings USING btree (source_platform, source_track_id, destination_platform);


--
-- Name: idx_transfer_items_session; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_transfer_items_session ON public.transfer_items USING btree (job_id);


--
-- Name: transfer_items transfer_items_job_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.transfer_items
    ADD CONSTRAINT transfer_items_job_id_fkey FOREIGN KEY (job_id) REFERENCES public.transfer_jobs(id) ON DELETE CASCADE;


--
-- Name: transfer_jobs transfer_jobs_session_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.transfer_jobs
    ADD CONSTRAINT transfer_jobs_session_id_fkey FOREIGN KEY (session_id) REFERENCES public.transfer_sessions(id) ON DELETE CASCADE;


--
-- Name: transfer_sessions transfer_sessions_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.transfer_sessions
    ADD CONSTRAINT transfer_sessions_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: transferhistory transferhistory_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.transferhistory
    ADD CONSTRAINT transferhistory_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

