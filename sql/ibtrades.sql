-- Table: ibtrades

-- DROP TABLE ibtrades;

CREATE TABLE ibtrades
(
  ibopenorderid integer DEFAULT nextval('ibtrades_orderid_seq'::regclass),
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
  expiration timestamp without time zone
)
WITH (
  OIDS=FALSE
);
ALTER TABLE ibtrades
  OWNER TO postgres;
