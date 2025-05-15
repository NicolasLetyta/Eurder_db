SET search_path TO eurder_db;

ALTER TABLE item_group
    ADD COLUMN total_price_eurder_date decimal;