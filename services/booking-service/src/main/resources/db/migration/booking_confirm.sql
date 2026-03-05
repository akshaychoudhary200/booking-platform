ALTER TABLE booking
  ADD COLUMN confirm_idempotency_key TEXT;

CREATE UNIQUE INDEX IF NOT EXISTS uq_booking_confirm_idem
  ON booking(booking_id, confirm_idempotency_key)
  WHERE confirm_idempotency_key IS NOT NULL;