-- Table: backtesttrades

-- DROP TABLE backtesttrades;

CREATE TABLE backtesttrades
(
  ibopenorderid integer DEFAULT nextval('backtesttrades_orderid_seq'::regclass),
  ibcloseorderid integer,
  ibstoporderid integer,
  ibocagroup integer,
  ibopenexecid text,
  ibcloseexecid text,
  ibstopexecid text,
  ibordertype text,
  iborderaction text,
  status text,
  statustime timestamp without time zone,
  direction text,
  symbol text,
  duration text,
  requestedamount numeric,
  filledamount numeric,
  suggestedentryprice numeric,
  actualentryprice numeric,
  bestprice numeric,
  suggestedexitprice numeric,
  suggestedstopprice numeric,
  actualexitprice numeric,
  exitreason text,
  closefilledamount numeric,
  commission numeric,
  netprofit numeric,
  grossprofit numeric,
  note text,
  model text,
  awp numeric,
  expiration timestamp without time zone,
  runname text,
  rundate timestamp without time zone
)
WITH (
  OIDS=FALSE
);
ALTER TABLE backtesttrades
  OWNER TO postgres;

-- Index: backtesttrades_direction_index

-- DROP INDEX backtesttrades_direction_index;

CREATE INDEX backtesttrades_direction_index
  ON backtesttrades
  USING btree
  (direction COLLATE pg_catalog."default");

-- Index: backtesttrades_ibcloseexec_index

-- DROP INDEX backtesttrades_ibcloseexec_index;

CREATE INDEX backtesttrades_ibcloseexec_index
  ON backtesttrades
  USING btree
  (ibcloseexecid COLLATE pg_catalog."default");

-- Index: backtesttrades_ibcloseorderid_index

-- DROP INDEX backtesttrades_ibcloseorderid_index;

CREATE INDEX backtesttrades_ibcloseorderid_index
  ON backtesttrades
  USING btree
  (ibcloseorderid);

-- Index: backtesttrades_ibopenexecid_index

-- DROP INDEX backtesttrades_ibopenexecid_index;

CREATE INDEX backtesttrades_ibopenexecid_index
  ON backtesttrades
  USING btree
  (ibopenexecid COLLATE pg_catalog."default");

-- Index: backtesttrades_ibopenorderid_index

-- DROP INDEX backtesttrades_ibopenorderid_index;

CREATE INDEX backtesttrades_ibopenorderid_index
  ON backtesttrades
  USING btree
  (ibopenorderid);

-- Index: backtesttrades_ibstopexec_index

-- DROP INDEX backtesttrades_ibstopexec_index;

CREATE INDEX backtesttrades_ibstopexec_index
  ON backtesttrades
  USING btree
  (ibstopexecid COLLATE pg_catalog."default");

-- Index: backtesttrades_ibstoporderid_index

-- DROP INDEX backtesttrades_ibstoporderid_index;

CREATE INDEX backtesttrades_ibstoporderid_index
  ON backtesttrades
  USING btree
  (ibstoporderid);

-- Index: backtesttrades_ibtrades_expiration_index

-- DROP INDEX backtesttrades_ibtrades_expiration_index;

CREATE INDEX backtesttrades_ibtrades_expiration_index
  ON backtesttrades
  USING btree
  (expiration);

-- Index: backtesttrades_ibtrades_status_index

-- DROP INDEX backtesttrades_ibtrades_status_index;

CREATE INDEX backtesttrades_ibtrades_status_index
  ON backtesttrades
  USING btree
  (status COLLATE pg_catalog."default");

-- Index: backtesttrades_model_index

-- DROP INDEX backtesttrades_model_index;

CREATE INDEX backtesttrades_model_index
  ON backtesttrades
  USING btree
  (model COLLATE pg_catalog."default");

-- Index: backtesttrades_note_index

-- DROP INDEX backtesttrades_note_index;

CREATE INDEX backtesttrades_note_index
  ON backtesttrades
  USING btree
  (note COLLATE pg_catalog."default");

-- Index: backtesttrades_status_direction_model_index

-- DROP INDEX backtesttrades_status_direction_model_index;

CREATE INDEX backtesttrades_status_direction_model_index
  ON backtesttrades
  USING btree
  (model COLLATE pg_catalog."default", status COLLATE pg_catalog."default", direction COLLATE pg_catalog."default");

