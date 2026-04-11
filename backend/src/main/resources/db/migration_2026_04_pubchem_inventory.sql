ALTER TABLE IF EXISTS inventory_items
    ADD COLUMN IF NOT EXISTS pubchem_cid INTEGER,
    ADD COLUMN IF NOT EXISTS molecular_formula TEXT,
    ADD COLUMN IF NOT EXISTS molecular_weight TEXT,
    ADD COLUMN IF NOT EXISTS iupac_name TEXT,
    ADD COLUMN IF NOT EXISTS description TEXT,
    ADD COLUMN IF NOT EXISTS safety_notes TEXT;

CREATE INDEX IF NOT EXISTS idx_inventory_items_pubchem_cid ON inventory_items(pubchem_cid);
