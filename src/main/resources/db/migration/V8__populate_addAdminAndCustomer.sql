SET search_path TO eurder_db;

--MEMBER
INSERT INTO member (id, first_name, last_name, email, password, phone, member_role, address_id)
VALUES (
           nextval('member_seq'),
           'customer',
           'klara',
           'klara@gmail.com',
           'pass',
           '+3283289392',
           'CUSTOMER',
           '1'
       );

INSERT INTO member (id, first_name, last_name, email, password, phone, member_role, address_id)
VALUES (
           nextval('member_seq'),
           'admin',
           'roos',
           'roos@gmail.com',
           'pass',
           '+3283289123',
           'ADMIN',
           '2'
       );