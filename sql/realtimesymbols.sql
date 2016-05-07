-- Table: realtimesymbols

-- DROP TABLE realtimesymbols;

CREATE TABLE realtimesymbols
(
  symbol character varying(16)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE realtimesymbols
  OWNER TO postgres;

-- Index: symbol_index

-- DROP INDEX symbol_index;

CREATE INDEX symbol_index
  ON realtimesymbols
  USING btree
  (symbol COLLATE pg_catalog."default");

