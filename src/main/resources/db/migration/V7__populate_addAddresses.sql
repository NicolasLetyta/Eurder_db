SET search_path TO eurder_db;

--ADDRESS
INSERT INTO address (id, street, street_number, postal_code, city, country)
VALUES (
           nextval('address_seq'),
           'langevlierstraat',
           '15',
           '2000 Antwerpen',
           'Antwerpen',
           'Belgie'
       );

INSERT INTO address (id, street, street_number, postal_code, city, country)
VALUES (
           nextval('address_seq'),
           'lamonierestraat',
           '88',
           '2018 Antwerpen',
           'Antwerpen',
           'Belgie'
       );

INSERT INTO address (id, street, street_number, postal_code, city, country)
VALUES (
           nextval('address_seq'),
           'Generaal de Gaullelaan',
           '18',
           '1050 Elsene',
           'Brussel',
           'Belgie'
       );
