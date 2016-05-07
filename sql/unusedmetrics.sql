-- Table: unusedmetrics

-- DROP TABLE unusedmetrics;

CREATE TABLE unusedmetrics
(
  name character varying(32) NOT NULL,
  symbol character varying(16) NOT NULL,
  start timestamp without time zone NOT NULL,
  "end" timestamp without time zone NOT NULL,
  duration character varying(16) NOT NULL,
  value real,
  CONSTRAINT unusedmetrics_pk PRIMARY KEY (name, symbol, start, duration)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE unusedmetrics
  OWNER TO postgres;
