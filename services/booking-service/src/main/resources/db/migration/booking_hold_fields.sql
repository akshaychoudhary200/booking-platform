ALTER TABLE booking
  ADD COLUMN hold_id UUID,
  ADD COLUMN hold_expires_at TIMESTAMPTZ,
  ADD COLUMN seats_held INT;