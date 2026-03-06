ALTER TABLE booking
  ADD COLUMN cancel_idempotency_key TEXT;

CREATE UNIQUE INDEX IF NOT EXISTS uq_booking_cancel_idem
  ON booking(booking_id, cancel_idempotency_key)
  WHERE cancel_idempotency_key IS NOT NULL;