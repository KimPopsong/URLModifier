CREATE TABLE IF NOT EXISTS public.users
(
    id
    bigint
    NOT
    NULL,
    email
    character
    varying
(
    255
) COLLATE pg_catalog."default" NOT NULL,
    nick_name character varying
(
    255
) COLLATE pg_catalog."default" NOT NULL,
    password character varying
(
    255
) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT users_pkey PRIMARY KEY
(
    id
),
    CONSTRAINT users_email_key UNIQUE
(
    email
)
    )