SET search_path TO eurder_db;

ALTER TABLE member
ALTER COLUMN phone DROP NOT NULL;