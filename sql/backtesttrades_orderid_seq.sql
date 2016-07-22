-- Sequence: backtesttrades_orderid_seq

-- DROP SEQUENCE backtesttrades_orderid_seq;

CREATE SEQUENCE backtesttrades_orderid_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE backtesttrades_orderid_seq
  OWNER TO postgres;
