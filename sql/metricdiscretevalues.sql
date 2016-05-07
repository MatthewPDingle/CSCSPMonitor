-- Table: metricdiscretevalues

-- DROP TABLE metricdiscretevalues;

CREATE TABLE metricdiscretevalues
(
  name text,
  symbol text,
  start timestamp without time zone,
  "end" timestamp without time zone,
  duration text,
  percentiles real[],
  "values" real[]
)
WITH (
  OIDS=FALSE
);
ALTER TABLE metricdiscretevalues
  OWNER TO postgres;
COMMENT ON TABLE metricdiscretevalues
  IS 'Used for the MetricDiscreteValueHash object seen in the code.  It defines cutoff points in the range of values for a metric so they can be grouped into buckets.';
