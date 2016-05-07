-- Sequence: ibtrades_orderid_seq

-- DROP SEQUENCE ibtrades_orderid_seq;

CREATE SEQUENCE ibtrades_orderid_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 4512
  CACHE 1;
ALTER TABLE ibtrades_orderid_seq
  OWNER TO postgres;
