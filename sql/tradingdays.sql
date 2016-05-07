-- Table: tradingdays

-- DROP TABLE tradingdays;

CREATE TABLE tradingdays
(
  date date
)
WITH (
  OIDS=FALSE
);
ALTER TABLE tradingdays
  OWNER TO postgres;

-- Index: date_index

-- DROP INDEX date_index;

CREATE INDEX date_index
  ON tradingdays
  USING btree
  (date);

