-- Table: tradingaccount

-- DROP TABLE tradingaccount;

CREATE TABLE tradingaccount
(
  cash real,
  bitcoin real
)
WITH (
  OIDS=FALSE
);
ALTER TABLE tradingaccount
  OWNER TO postgres;
