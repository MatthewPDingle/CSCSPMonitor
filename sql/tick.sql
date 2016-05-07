-- Table: tick

-- DROP TABLE tick;

CREATE TABLE tick
(
  symbol text,
  price real,
  volume real,
  "timestamp" timestamp without time zone
)
WITH (
  OIDS=FALSE
);
ALTER TABLE tick
  OWNER TO postgres;

-- Index: bitcointick_symbol_index

-- DROP INDEX bitcointick_symbol_index;

CREATE INDEX bitcointick_symbol_index
  ON tick
  USING btree
  (symbol COLLATE pg_catalog."default");

-- Index: bitcointick_symbol_timestamp_index

-- DROP INDEX bitcointick_symbol_timestamp_index;

CREATE INDEX bitcointick_symbol_timestamp_index
  ON tick
  USING btree
  (symbol COLLATE pg_catalog."default", "timestamp");

-- Index: bitcointick_timestamp_index

-- DROP INDEX bitcointick_timestamp_index;

CREATE INDEX bitcointick_timestamp_index
  ON tick
  USING btree
  ("timestamp");

