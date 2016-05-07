-- Sequence: ibpapertrades_id_seq

-- DROP SEQUENCE ibpapertrades_id_seq;

CREATE SEQUENCE ibpapertrades_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 894
  CACHE 1;
ALTER TABLE ibpapertrades_id_seq
  OWNER TO postgres;
