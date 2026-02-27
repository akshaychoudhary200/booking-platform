CREATE TABLE booking (
  booking_id UUID PRIMARY KEY,
  user_id UUID NOT NULL,
  event_id UUID NOT NULL,
  status TEXT NOT NULL,

  idempotency_key TEXT,

  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_booking_user_id ON booking(user_id);
CREATE INDEX idx_booking_event_id ON booking(event_id);