-- Table: forexfundamentals

-- DROP TABLE forexfundamentals;

CREATE TABLE forexfundamentals
(
  date timestamp without time zone,
  currency text,
  event text,
  actualpercent numeric,
  actualchange numeric,
  actualvalue numeric,
  forecastvalue numeric,
  previousvalue numeric
)
WITH (
  OIDS=FALSE
);
ALTER TABLE forexfundamentals
  OWNER TO postgres;
