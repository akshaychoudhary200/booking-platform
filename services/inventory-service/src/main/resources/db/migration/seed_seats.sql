-- pick a constant UUID and use it everywhere during dev
-- example event: 11111111-1111-1111-1111-111111111111
CREATE EXTENSION IF NOT EXISTS pgcrypto; -- for gen_random_uuid()

INSERT INTO seat (event_id, seat_id, status)
SELECT
  '11111111-1111-1111-1111-111111111111'::uuid,
  gen_random_uuid(),
  'AVAILABLE'
FROM generate_series(1, 50)
ON CONFLICT DO NOTHING;