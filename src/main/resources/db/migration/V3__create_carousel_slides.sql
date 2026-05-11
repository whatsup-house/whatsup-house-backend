CREATE TABLE IF NOT EXISTS carousel_slides (
     id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
     type         VARCHAR(20)  NOT NULL,
     title        VARCHAR(200) NOT NULL,
     content      VARCHAR(500),
     image_url    VARCHAR(500) NOT NULL,
     gathering_id UUID         REFERENCES gatherings(id),
     sort_order   INTEGER      NOT NULL DEFAULT 0,
     is_active    BOOLEAN      NOT NULL DEFAULT FALSE,
     created_at   TIMESTAMP    NOT NULL,
     updated_at   TIMESTAMP    NOT NULL,
     deleted_at   TIMESTAMP
);