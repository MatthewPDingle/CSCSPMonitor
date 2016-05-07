-- Table: paperloose

-- DROP TABLE paperloose;

CREATE TABLE paperloose
(
  valuecny real,
  valuebtc real,
  btcprice real,
  "time" timestamp without time zone
)
WITH (
  OIDS=FALSE
);
ALTER TABLE paperloose
  OWNER TO postgres;
