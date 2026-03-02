CREATE TABLE seat (
  event_id UUID NOT NULL,
  seat_id  UUID NOT NULL,
  status   TEXT NOT NULL,          -- AVAILABLE | HELD | CONFIRMED
  hold_id  UUID,
  hold_expires_at TIMESTAMPTZ,
  version  BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (event_id, seat_id)
);

CREATE TABLE hold (
  hold_id UUID PRIMARY KEY,
  booking_id UUID NOT NULL,
  user_id UUID NOT NULL,
  event_id UUID NOT NULL,
  status TEXT NOT NULL,            -- ACTIVE | EXPIRED | CANCELLED | CONFIRMED
  expires_at TIMESTAMPTZ NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_hold_booking ON hold(booking_id);