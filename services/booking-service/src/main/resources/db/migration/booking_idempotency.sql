ALTER TABLE booking
  ALTER COLUMN idempotency_key SET NOT NULL;

-- Each user can use a given idempotency key only once.
CREATE UNIQUE INDEX uq_booking_user_idem
  ON booking(user_id, idempotency_key);