-- Sequence: trades_temp_id_seq

-- DROP SEQUENCE trades_temp_id_seq;

CREATE SEQUENCE trades_temp_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 82259
  CACHE 1;
ALTER TABLE trades_temp_id_seq
  OWNER TO postgres;
