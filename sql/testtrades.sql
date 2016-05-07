-- Table: testtrades

-- DROP TABLE testtrades;

CREATE TABLE testtrades
(
  modelfile text,
  "time" timestamp without time zone,
  entry real,
  close real,
  stop real,
  numbars text
)
WITH (
  OIDS=FALSE
);
ALTER TABLE testtrades
  OWNER TO postgres;
