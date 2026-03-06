ALTER TABLE booking
  ADD COLUMN payment_id UUID,
  ADD COLUMN payment_idempotency_key TEXT;

CREATE UNIQUE INDEX IF NOT EXISTS uq_booking_payment_idem
  ON booking(booking_id, payment_idempotency_key)
  WHERE payment_idempotency_key IS NOT NULL;