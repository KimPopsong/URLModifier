-- Table: public.url

-- DROP TABLE IF EXISTS public.url;

CREATE TABLE IF NOT EXISTS public.url
(
    id bigint NOT NULL,
    user_id bigint,
    origin_url text COLLATE pg_catalog."default" NOT NULL,
    shortened_url text COLLATE pg_catalog."default" NOT NULL,
    qr_code text COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT url_pkey PRIMARY KEY (id)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.url
    OWNER to postgres;