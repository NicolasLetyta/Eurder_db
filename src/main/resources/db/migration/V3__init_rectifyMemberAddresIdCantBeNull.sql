SET search_path TO eurder_db;

ALTER TABLE member
    ALTER COLUMN address_id SET NOT NULL;