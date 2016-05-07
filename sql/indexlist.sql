-- Table: indexlist

-- DROP TABLE indexlist;

CREATE TABLE indexlist
(
  symbol character varying(16) NOT NULL,
  index character varying(32) NOT NULL
)
WITH (
  OIDS=FALSE
);
ALTER TABLE indexlist
  OWNER TO postgres;

-- Index: index_indexlist_index

-- DROP INDEX index_indexlist_index;

CREATE INDEX index_indexlist_index
  ON indexlist
  USING btree
  (index COLLATE pg_catalog."default");

-- Index: index_indexlist_symbol

-- DROP INDEX index_indexlist_symbol;

CREATE INDEX index_indexlist_symbol
  ON indexlist
  USING btree
  (symbol COLLATE pg_catalog."default");

