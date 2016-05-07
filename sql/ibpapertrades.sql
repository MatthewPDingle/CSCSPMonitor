-- Table: ibpapertrades

-- DROP TABLE ibpapertrades;

CREATE TABLE ibpapertrades
(
  id integer DEFAULT nextval('ibpapertrades_id_seq'::regclass),
  action text,
  amount numeric,
  price numeric,
  tradetime timestamp without time zone,
  awp numeric,
  notes text
)
WITH (
  OIDS=FALSE
);
ALTER TABLE ibpapertrades
  OWNER TO postgres;
