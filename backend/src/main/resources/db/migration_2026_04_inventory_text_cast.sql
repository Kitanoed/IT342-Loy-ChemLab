DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'inventory_items'
          AND column_name = 'item_name'
          AND data_type = 'bytea'
    ) THEN
        ALTER TABLE inventory_items
            ALTER COLUMN item_name TYPE TEXT
            USING convert_from(item_name, 'UTF8');
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'inventory_items'
          AND column_name = 'item_code'
          AND data_type = 'bytea'
    ) THEN
        ALTER TABLE inventory_items
            ALTER COLUMN item_code TYPE TEXT
            USING convert_from(item_code, 'UTF8');
    END IF;
END $$;
