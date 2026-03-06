CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE TABLE payment (
  payment_id UUID PRIMARY KEY,
  booking_id UUID NOT NULL UNIQUE,                 -- one payment per booking
  user_id UUID NOT NULL,
  amount_cents BIGINT NOT NULL,
  status TEXT NOT NULL,                            -- AUTHORIZED | FAILED
  payment_idempotency_key TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_payment_idem ON payment(payment_idempotency_key);

CREATE TABLE outbox (
  outbox_id UUID PRIMARY KEY,
  aggregate_type TEXT NOT NULL,
  aggregate_id UUID NOT NULL,
  event_type TEXT NOT NULL,
  payload_json TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  published_at TIMESTAMPTZ
);

CREATE INDEX idx_outbox_unpublished ON outbox(published_at) WHERE published_at IS NULL;