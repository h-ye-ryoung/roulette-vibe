-- Add USED and RECLAIMED types to point_ledger type check constraint
ALTER TABLE point_ledger DROP CONSTRAINT IF EXISTS point_ledger_type_check;
ALTER TABLE point_ledger ADD CONSTRAINT point_ledger_type_check 
  CHECK (type IN ('EARN', 'REFUND', 'USED', 'RECLAIMED'));
