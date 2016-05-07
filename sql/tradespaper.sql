-- Table: tradespaper

-- DROP TABLE tradespaper;

CREATE TABLE tradespaper
(
  tempid integer DEFAULT nextval('trades_temp_id_seq'::regclass),
  exchangeopentradeid integer,
  exchangeclosetradeid integer,
  exchangestoptradeid integer,
  exchangeexpirationtradeid integer,
  status text,
  statustime timestamp without time zone,
  stopstatus text,
  stopstatustime timestamp without time zone,
  expirationstatus text,
  expirationstatustime timestamp without time zone,
  opentradetime timestamp without time zone,
  closetradetime timestamp without time zone,
  stoptradetime timestamp without time zone,
  expirationtradetime timestamp without time zone,
  type text,
  symbol text,
  duration text,
  requestedamount real,
  filledamount real,
  suggestedentryprice real,
  actualentryprice real,
  suggestedexitprice real,
  suggestedstopprice real,
  actualexitprice real,
  exitreason text,
  closefilledamount real,
  commission real,
  netprofit real,
  grossprofit real,
  model text,
  expiration timestamp without time zone
)
WITH (
  OIDS=FALSE
);
ALTER TABLE tradespaper
  OWNER TO postgres;
