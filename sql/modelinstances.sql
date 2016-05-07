-- Table: modelinstances

-- DROP TABLE modelinstances;

CREATE TABLE modelinstances
(
  modelid integer,
  score numeric,
  correct boolean
)
WITH (
  OIDS=FALSE
);
ALTER TABLE modelinstances
  OWNER TO postgres;

-- Index: modelid_index

-- DROP INDEX modelid_index;

CREATE INDEX modelid_index
  ON modelinstances
  USING btree
  (modelid);

-- Index: score_index

-- DROP INDEX score_index;

CREATE INDEX score_index
  ON modelinstances
  USING btree
  (score);

