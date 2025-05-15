SET search_path TO eurder_db;

ALTER TABLE eurder
    ADD COLUMN status varchar(25) NOT NULL default 'CART';
