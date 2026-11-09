

CREATE SCHEMA IF NOT EXISTS extensions;

CREATE EXTENSION IF NOT EXISTS postgis
WITH SCHEMA extensions;




CREATE TYPE public.user_role AS ENUM (
    'user',
    'admin'
);

CREATE TYPE public.relation_status AS ENUM (
    'pending',
    'accepted',
    'declined',
    'blocked'
);




CREATE TABLE public.users (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    nickname VARCHAR(50) NOT NULL UNIQUE,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    verified BOOLEAN NOT NULL DEFAULT FALSE,

    email VARCHAR(255) NOT NULL UNIQUE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    role public.user_role NOT NULL DEFAULT 'user'
);




CREATE TABLE public.posts (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    title VARCHAR(255) NOT NULL,

    text TEXT NOT NULL,

    posted_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    text_color VARCHAR(20),

    poster_id INTEGER NOT NULL
        REFERENCES public.users(id),

    location extensions.geography(POINT, 4326) NOT NULL,

    image_path TEXT
);




CREATE TABLE public.comments (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    text TEXT NOT NULL,

    post_id INTEGER NOT NULL
        REFERENCES public.posts(id)
        ON DELETE CASCADE,

    user_id INTEGER NOT NULL
        REFERENCES public.users(id),

    posted_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);




CREATE TABLE public.credentials (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    user_id INTEGER NOT NULL UNIQUE
        REFERENCES public.users(id)
        ON DELETE CASCADE,

    login VARCHAR(255) NOT NULL UNIQUE,

    password_hash VARCHAR(255) NOT NULL
);




CREATE TABLE public.user_relations (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    inviter INTEGER NOT NULL
        REFERENCES public.users(id)
        ON DELETE CASCADE,

    invitee INTEGER NOT NULL
        REFERENCES public.users(id)
        ON DELETE CASCADE,

    status public.relation_status
        NOT NULL DEFAULT 'pending',

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT no_self_relation
        CHECK (inviter <> invitee)
);



-- Geographic index for nearby post searches
CREATE INDEX idx_posts_location
ON public.posts
USING GIST (location);

-- Foreign key indexes
CREATE INDEX idx_posts_poster
ON public.posts(poster_id);

CREATE INDEX idx_comments_post
ON public.comments(post_id);

CREATE INDEX idx_comments_user
ON public.comments(user_id);

CREATE INDEX idx_relations_invitee
ON public.user_relations(invitee);

-- Prevent duplicate user relationships
CREATE UNIQUE INDEX idx_unique_user_pair
ON public.user_relations (
    LEAST(inviter, invitee),
    GREATEST(inviter, invitee)
);




CREATE OR REPLACE FUNCTION public.update_relation_timestamp()
RETURNS TRIGGER
LANGUAGE plpgsql
SET search_path = ''
AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_update_relation_timestamp
BEFORE UPDATE ON public.user_relations
FOR EACH ROW
EXECUTE FUNCTION public.update_relation_timestamp();




ALTER TABLE public.users
ENABLE ROW LEVEL SECURITY;

ALTER TABLE public.posts
ENABLE ROW LEVEL SECURITY;

ALTER TABLE public.comments
ENABLE ROW LEVEL SECURITY;

ALTER TABLE public.credentials
ENABLE ROW LEVEL SECURITY;

ALTER TABLE public.user_relations
ENABLE ROW LEVEL SECURITY;
