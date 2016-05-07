-- Table: metrics

-- DROP TABLE metrics;

CREATE TABLE metrics
(
  name character varying(32) NOT NULL,
  symbol character varying(16) NOT NULL,
  start timestamp without time zone NOT NULL,
  "end" timestamp without time zone NOT NULL,
  duration character varying(16) NOT NULL,
  value real,
  CONSTRAINT metrics_pk PRIMARY KEY (name, symbol, start, "end")
)
WITH (
  OIDS=FALSE
);
ALTER TABLE metrics
  OWNER TO postgres;

-- Index: metrics_duration_index

-- DROP INDEX metrics_duration_index;

CREATE INDEX metrics_duration_index
  ON metrics
  USING btree
  (duration COLLATE pg_catalog."default");

-- Index: metrics_end_index

-- DROP INDEX metrics_end_index;

CREATE INDEX metrics_end_index
  ON metrics
  USING btree
  ("end");

-- Index: metrics_name_index

-- DROP INDEX metrics_name_index;

CREATE INDEX metrics_name_index
  ON metrics
  USING btree
  (name COLLATE pg_catalog."default");

-- Index: metrics_name_symbol_duration_index

-- DROP INDEX metrics_name_symbol_duration_index;

CREATE INDEX metrics_name_symbol_duration_index
  ON metrics
  USING btree
  (name COLLATE pg_catalog."default", symbol COLLATE pg_catalog."default", duration COLLATE pg_catalog."default");

-- Index: metrics_name_symbol_start_duration_index

-- DROP INDEX metrics_name_symbol_start_duration_index;

CREATE INDEX metrics_name_symbol_start_duration_index
  ON metrics
  USING btree
  (name COLLATE pg_catalog."default", symbol COLLATE pg_catalog."default", start, duration COLLATE pg_catalog."default");

-- Index: metrics_start_index

-- DROP INDEX metrics_start_index;

CREATE INDEX metrics_start_index
  ON metrics
  USING btree
  (start);

-- Index: metrics_symbol_duration_index

-- DROP INDEX metrics_symbol_duration_index;

CREATE INDEX metrics_symbol_duration_index
  ON metrics
  USING btree
  (symbol COLLATE pg_catalog."default", duration COLLATE pg_catalog."default");

-- Index: metrics_symbol_index

-- DROP INDEX metrics_symbol_index;

CREATE INDEX metrics_symbol_index
  ON metrics
  USING btree
  (symbol COLLATE pg_catalog."default");

-- Index: metrics_value_index

-- DROP INDEX metrics_value_index;

CREATE INDEX metrics_value_index
  ON metrics
  USING btree
  (value);

