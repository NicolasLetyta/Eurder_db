CREATE SCHEMA IF NOT EXISTS eurder_db;

ALTER SCHEMA eurder_db owner TO student;

SET search_path TO eurder_db;

CREATE SEQUENCE address_seq start with 1 increment by 1;
CREATE TABLE address
(
    id int DEFAULT nextval('address_seq'),
    street varchar(25) NOT NULL,
    street_number int,
    postal_code varchar(25) NOT NULL,
    city varchar(25) NOT NULL,
    country varchar(25) NOT NULL,

    CONSTRAINT pk_address PRIMARY KEY (id)
);

CREATE SEQUENCE item_seq start with 1 increment by 1;
CREATE TABLE item
(
    id int DEFAULT nextval('item_seq'),
    CONSTRAINT pk_item PRIMARY KEY (id),
    name varchar(25) NOT NULL UNIQUE,
    description varchar(25),
    price decimal NOT NULL,
    stock integer NOT NULL
);

CREATE SEQUENCE member_seq start with 1 increment by 1;
CREATE TABLE member
(
    id int DEFAULT nextval('member_seq'),
    CONSTRAINT pk_member PRIMARY KEY (id),

    first_name varchar(25) NOT NULL,
    last_name varchar(25) NOT NULL,
    email varchar(25) NOT NULL UNIQUE ,
    password varchar(25) NOT NULL,
    phone varchar(25) NOT NULL UNIQUE ,
    member_role varchar(25) NOT NULL,

    address_id integer,
    CONSTRAINT fk_member_address FOREIGN KEY (address_id) REFERENCES address(id)
);

CREATE SEQUENCE eurder_seq start with 1 increment by 1;
CREATE TABLE eurder
(
    id int DEFAULT nextval('eurder_seq'),
    CONSTRAINT pk_eurder PRIMARY KEY (id),

    member_id integer NOT NULL,
    CONSTRAINT fk_eurder_member FOREIGN KEY (member_id) REFERENCES member(id)
);

CREATE SEQUENCE item_group_seq start with 1 increment by 1;
CREATE TABLE item_group
(
    id int DEFAULT nextval('item_group_seq'),
    CONSTRAINT pk_item_group PRIMARY KEY (id),

    quantity integer NOT NULL,
    shipping_date date NOT NULL,

    item_id integer NOT NULL,
    CONSTRAINT fk_item_group_item FOREIGN KEY (item_id) REFERENCES item(id),
    eurder_id integer NOT NULL,
    CONSTRAINT fk_item_group_eurder FOREIGN KEY (eurder_id) REFERENCES eurder(id)
);
