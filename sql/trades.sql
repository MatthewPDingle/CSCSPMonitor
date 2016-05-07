-- Table: trades

-- DROP TABLE trades;

CREATE TABLE trades
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
ALTER TABLE trades
  OWNER TO postgres;

-- Index: exchangeclosetradeid_index

-- DROP INDEX exchangeclosetradeid_index;

CREATE INDEX exchangeclosetradeid_index
  ON trades
  USING btree
  (exchangeclosetradeid);

-- Index: exchangeexpirationtradeid_index

-- DROP INDEX exchangeexpirationtradeid_index;

CREATE INDEX exchangeexpirationtradeid_index
  ON trades
  USING btree
  (exchangeexpirationtradeid);

-- Index: exchangeopentradeid_index

-- DROP INDEX exchangeopentradeid_index;

CREATE INDEX exchangeopentradeid_index
  ON trades
  USING btree
  (exchangeopentradeid);

-- Index: exchangestoptradeid_index

-- DROP INDEX exchangestoptradeid_index;

CREATE INDEX exchangestoptradeid_index
  ON trades
  USING btree
  (exchangestoptradeid);

-- Index: expiration_index

-- DROP INDEX expiration_index;

CREATE INDEX expiration_index
  ON trades
  USING btree
  (expiration);

-- Index: expirationstatus_index

-- DROP INDEX expirationstatus_index;

CREATE INDEX expirationstatus_index
  ON trades
  USING btree
  (expirationstatus COLLATE pg_catalog."default");

-- Index: filledamount_index

-- DROP INDEX filledamount_index;

CREATE INDEX filledamount_index
  ON trades
  USING btree
  (filledamount);

-- Index: status_index

-- DROP INDEX status_index;

CREATE INDEX status_index
  ON trades
  USING btree
  (status COLLATE pg_catalog."default");

-- Index: stopstatus_index

-- DROP INDEX stopstatus_index;

CREATE INDEX stopstatus_index
  ON trades
  USING btree
  (stopstatus COLLATE pg_catalog."default");

-- Index: tempid_index

-- DROP INDEX tempid_index;

CREATE INDEX tempid_index
  ON trades
  USING btree
  (tempid);

