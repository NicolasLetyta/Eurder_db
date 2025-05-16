SET search_path TO eurder_db;

ALTER TABLE item
ADD CONSTRAINT ck_item_price_positive
CHECK ( price > 0 );

ALTER TABLE item
    ADD CONSTRAINT ck_item_stock_positive_zero
        CHECK ( stock >= 0 );