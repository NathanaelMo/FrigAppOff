CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE users (
                       id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                       first_name    VARCHAR(50)  NOT NULL,
                       email         VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE TABLE fridges (
                         id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                         owner_id   UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                         name       VARCHAR(100) NOT NULL,
                         created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
                         updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE TABLE fridge_members (
                                id        UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                                fridge_id UUID        NOT NULL REFERENCES fridges(id) ON DELETE CASCADE,
                                user_id   UUID        NOT NULL REFERENCES users(id)   ON DELETE CASCADE,
                                role      VARCHAR(20) NOT NULL DEFAULT 'collaborator'
                                    CHECK (role IN ('owner', 'collaborator')),
                                joined_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
                                UNIQUE (fridge_id, user_id)
);

CREATE TABLE products (
                          id        UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                          barcode   VARCHAR(50)  NOT NULL UNIQUE,
                          name      VARCHAR(255) NOT NULL,
                          brand     VARCHAR(100),
                          category  VARCHAR(100),
                          image_url TEXT,
                          source    VARCHAR(50)  NOT NULL DEFAULT 'open_food_facts'
                              CHECK (source IN ('open_food_facts', 'manual'))
);

CREATE TABLE fridge_items (
                              id          UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
                              fridge_id   UUID    NOT NULL REFERENCES fridges(id)  ON DELETE CASCADE,
                              product_id  UUID    NOT NULL REFERENCES products(id) ON DELETE CASCADE,
                              added_by    UUID    NOT NULL REFERENCES users(id)    ON DELETE RESTRICT,
                              quantity    INTEGER NOT NULL DEFAULT 1 CHECK (quantity > 0),
                              expiry_date DATE    NOT NULL,
                              created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
                              updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_products_barcode       ON products(barcode);
CREATE INDEX idx_fridge_items_fridge_id ON fridge_items(fridge_id);
CREATE INDEX idx_fridge_items_expiry    ON fridge_items(expiry_date);
CREATE INDEX idx_fridge_members_user_id ON fridge_members(user_id);