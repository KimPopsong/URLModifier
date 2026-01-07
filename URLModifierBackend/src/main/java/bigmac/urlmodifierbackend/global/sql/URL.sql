CREATE TABLE IF NOT EXISTS public.url
(
    created_at
    timestamp
(
    6
) without time zone NOT NULL,
    id bigint NOT NULL,
    users bigint,
    shortened_url character varying
(
    30
) COLLATE pg_catalog."default" NOT NULL,
    origin_url text COLLATE pg_catalog."default" NOT NULL,
    qr_code text COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT url_pkey PRIMARY KEY
(
    id
),
    CONSTRAINT url_shortened_url_key UNIQUE
(
    shortened_url
),
    CONSTRAINT fkqtwl2ssjmwy4iflyryi4suuor FOREIGN KEY
(
    users
)
    REFERENCES public.users
(
    id
) MATCH SIMPLE
  ON UPDATE NO ACTION
  ON DELETE NO ACTION
    );

-- 성능 최적화를 위한 인덱스 추가
CREATE INDEX IF NOT EXISTS idx_url_origin_url ON public.url(origin_url);
CREATE INDEX IF NOT EXISTS idx_url_user_origin ON public.url(users, origin_url);
CREATE INDEX IF NOT EXISTS idx_url_user ON public.url(users);