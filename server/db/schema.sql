-- ============================================================================
-- BozorGo — PostgreSQL sxemasi (demo onlayn do'kon)
--
-- Qo'llash:
--   psql "$DATABASE_URL" -f server/db/schema.sql
--   yoki:  npm run migrate
--
-- Majburiy tartib: server narx, chegirma, ombor qoldig'i va buyurtma summasini
-- qaytadan hisoblaydi (mijoz yuborgan narxlarga ishonilmaydi).
-- ============================================================================

BEGIN;

CREATE TABLE IF NOT EXISTS categories (
    id          TEXT PRIMARY KEY,
    name        TEXT        NOT NULL,
    icon        TEXT        NOT NULL DEFAULT 'box',
    color       TEXT        NOT NULL DEFAULT '#4CAF50',
    sort_order  INTEGER     NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS subcategories (
    id          TEXT PRIMARY KEY,
    category_id TEXT        NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    name        TEXT        NOT NULL,
    icon        TEXT        NOT NULL DEFAULT 'box',
    sort_order  INTEGER     NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS products (
    id               TEXT PRIMARY KEY,
    name             TEXT        NOT NULL,
    category_id      TEXT        NOT NULL REFERENCES categories(id) ON DELETE RESTRICT,
    subcategory_id   TEXT        NOT NULL REFERENCES subcategories(id) ON DELETE RESTRICT,
    brand            TEXT        NOT NULL DEFAULT '',
    description      TEXT        NOT NULL DEFAULT '',
    price            NUMERIC(12,2) NOT NULL CHECK (price >= 0),
    discount_price   NUMERIC(12,2) CHECK (discount_price IS NULL OR discount_price >= 0),
    discount_percent INTEGER     NOT NULL DEFAULT 0 CHECK (discount_percent BETWEEN 0 AND 95),
    unit             TEXT        NOT NULL DEFAULT 'dona',
    unit_value       NUMERIC(10,3) NOT NULL DEFAULT 1,
    unit_label       TEXT        NOT NULL DEFAULT '1 dona',
    stock            INTEGER     NOT NULL DEFAULT 0 CHECK (stock >= 0),
    in_stock         BOOLEAN     NOT NULL DEFAULT TRUE,
    rating           NUMERIC(3,2) NOT NULL DEFAULT 0 CHECK (rating BETWEEN 0 AND 5),
    review_count     INTEGER     NOT NULL DEFAULT 0 CHECK (review_count >= 0),
    is_popular       BOOLEAN     NOT NULL DEFAULT FALSE,
    is_new           BOOLEAN     NOT NULL DEFAULT FALSE,
    is_deal          BOOLEAN     NOT NULL DEFAULT FALSE,
    image_asset_path TEXT        NOT NULL DEFAULT '',
    image_url        TEXT        NOT NULL DEFAULT '',
    options          JSONB       NOT NULL DEFAULT '[]'::jsonb,
    tags             JSONB       NOT NULL DEFAULT '[]'::jsonb,
    is_demo          BOOLEAN     NOT NULL DEFAULT TRUE,
    is_active        BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT products_discount_lt_price
        CHECK (discount_price IS NULL OR discount_price < price)
);

CREATE INDEX IF NOT EXISTS products_category_idx   ON products (category_id);
CREATE INDEX IF NOT EXISTS products_subcategory_idx ON products (subcategory_id);
CREATE INDEX IF NOT EXISTS products_updated_idx    ON products (updated_at);
CREATE INDEX IF NOT EXISTS products_name_idx       ON products (name);

CREATE TABLE IF NOT EXISTS banners (
    id           TEXT PRIMARY KEY,
    title        TEXT        NOT NULL,
    subtitle     TEXT        NOT NULL DEFAULT '',
    image_url    TEXT        NOT NULL DEFAULT '',
    action_type  TEXT        NOT NULL DEFAULT 'category',
    action_value TEXT        NOT NULL DEFAULT '',
    start_color  TEXT        NOT NULL DEFAULT '#2E7D32',
    end_color    TEXT        NOT NULL DEFAULT '#66BB6A',
    is_active    BOOLEAN     NOT NULL DEFAULT TRUE,
    sort_order   INTEGER     NOT NULL DEFAULT 0,
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS users (
    id         TEXT PRIMARY KEY,
    phone      TEXT UNIQUE,
    name       TEXT        NOT NULL DEFAULT '',
    is_admin   BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Admin panel foydalanuvchilari (parol bcrypt xeshi bilan)
CREATE TABLE IF NOT EXISTS admin_users (
    id            TEXT PRIMARY KEY,
    email         TEXT UNIQUE NOT NULL,
    name          TEXT        NOT NULL,
    password_hash TEXT        NOT NULL,
    role          TEXT        NOT NULL DEFAULT 'admin' CHECK (role IN ('admin', 'manager')),
    is_active     BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Telefon orqali kirish kodlari (haqiqiy SMS provayderi ulanmaganda demo)
CREATE TABLE IF NOT EXISTS otp_codes (
    id          BIGSERIAL PRIMARY KEY,
    phone       TEXT        NOT NULL,
    code        TEXT        NOT NULL,
    attempts    INTEGER     NOT NULL DEFAULT 0,
    consumed    BOOLEAN     NOT NULL DEFAULT FALSE,
    expires_at  TIMESTAMPTZ NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS otp_phone_idx ON otp_codes (phone, consumed);

CREATE TABLE IF NOT EXISTS addresses (
    id         TEXT PRIMARY KEY,
    user_id    TEXT        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title      TEXT        NOT NULL DEFAULT 'Uy',
    city       TEXT        NOT NULL DEFAULT 'Toshkent',
    district   TEXT        NOT NULL DEFAULT '',
    street     TEXT        NOT NULL DEFAULT '',
    house      TEXT        NOT NULL DEFAULT '',
    apartment  TEXT        NOT NULL DEFAULT '',
    comment    TEXT        NOT NULL DEFAULT '',
    is_default BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS addresses_user_idx ON addresses (user_id);

CREATE TABLE IF NOT EXISTS orders (
    id              TEXT PRIMARY KEY,
    user_id         TEXT REFERENCES users(id) ON DELETE SET NULL,
    order_number    TEXT UNIQUE NOT NULL,
    status          TEXT NOT NULL DEFAULT 'created'
                    CHECK (status IN ('created','confirmed','preparing','courier','delivering','delivered','cancelled')),
    delivery_method TEXT NOT NULL DEFAULT 'standard'
                    CHECK (delivery_method IN ('standard','express','pickup')),
    payment_method  TEXT NOT NULL DEFAULT 'cash'
                    CHECK (payment_method IN ('cash','card')),
    recipient_name  TEXT NOT NULL,
    phone           TEXT NOT NULL,
    address_title   TEXT NOT NULL DEFAULT '',
    address_text    TEXT NOT NULL,
    comment         TEXT NOT NULL DEFAULT '',
    items_total     NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (items_total >= 0),
    discount        NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (discount >= 0),
    delivery_fee    NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (delivery_fee >= 0),
    total           NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (total >= 0),
    is_paid         BOOLEAN NOT NULL DEFAULT FALSE,
    payment_ref     TEXT,
    -- Mijoz tomonidan yaratilgan kalit: takroriy yuborishda ikkinchi buyurtma ochilmaydi
    idempotency_key TEXT UNIQUE,
    is_demo         BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS orders_user_idx   ON orders (user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS orders_status_idx ON orders (status);

CREATE TABLE IF NOT EXISTS order_items (
    id              BIGSERIAL PRIMARY KEY,
    order_id        TEXT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id      TEXT NOT NULL,
    product_name    TEXT NOT NULL,
    unit_price      NUMERIC(12,2) NOT NULL,
    discount_price  NUMERIC(12,2),
    unit            TEXT NOT NULL DEFAULT 'dona',
    unit_label      TEXT NOT NULL DEFAULT '',
    image_url       TEXT NOT NULL DEFAULT '',
    quantity        INTEGER NOT NULL CHECK (quantity > 0),
    selected_option TEXT,
    line_total      NUMERIC(12,2) NOT NULL
);
CREATE INDEX IF NOT EXISTS order_items_order_idx ON order_items (order_id);

-- updated_at avtomatik yangilanishi
CREATE OR REPLACE FUNCTION set_updated_at() RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS products_set_updated_at ON products;
CREATE TRIGGER products_set_updated_at BEFORE UPDATE ON products
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS orders_set_updated_at ON orders;
CREATE TRIGGER orders_set_updated_at BEFORE UPDATE ON orders
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

COMMIT;
