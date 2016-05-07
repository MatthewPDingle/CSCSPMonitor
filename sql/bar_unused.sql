-- Table: bar_unused

-- DROP TABLE bar_unused;

CREATE TABLE bar_unused
(
  symbol text NOT NULL,
  open real,
  close real,
  high real,
  low real,
  vwap real,
  volume real,
  numtrades integer,
  change real,
  gap real,
  start timestamp without time zone NOT NULL,
  "end" timestamp without time zone,
  duration text NOT NULL,
  partial boolean,
  CONSTRAINT bar_unused_pk PRIMARY KEY (symbol, start, duration)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE bar_unused
  OWNER TO postgres;
