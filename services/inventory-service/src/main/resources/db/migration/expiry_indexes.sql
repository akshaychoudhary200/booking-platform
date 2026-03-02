CREATE INDEX IF NOT EXISTS idx_hold_status_expires
  ON hold(status, expires_at);

CREATE INDEX IF NOT EXISTS idx_seat_hold_expires
  ON seat(hold_expires_at);