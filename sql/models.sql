-- Table: models

-- DROP TABLE models;

CREATE TABLE models
(
  id integer DEFAULT nextval('models2_id_seq'::regclass),
  type text,
  modelfile text,
  algo text,
  params text,
  symbol text,
  duration text,
  interbardata boolean,
  metrics text[],
  trainstart timestamp without time zone,
  trainend timestamp without time zone,
  teststart timestamp without time zone,
  testend timestamp without time zone,
  sellmetric text,
  sellmetricvalue numeric,
  stopmetric text,
  stopmetricvalue numeric,
  numbars integer,
  numclasses integer,
  traindatasetsize integer,
  traintruenegatives integer,
  trainfalsenegatives integer,
  trainfalsepositives integer,
  traintruepositives integer,
  traintruepositiverate real,
  trainfalsepositiverate real,
  traincorrectrate real,
  trainkappa real,
  trainmeanabsoluteerror real,
  trainrootmeansquarederror real,
  trainrelativeabsoluteerror real,
  trainrootrelativesquarederror real,
  trainrocarea real,
  testdatasetsize integer,
  testtruenegatives integer,
  testfalsenegatives integer,
  testfalsepositives integer,
  testtruepositives integer,
  testtruepositiverate real,
  testfalsepositiverate real,
  testcorrectrate real,
  testkappa real,
  testmeanabsoluteerror real,
  testrootmeansquarederror real,
  testrelativeabsoluteerror real,
  testrootrelativesquarederror real,
  testrocarea real,
  testbucketpercentcorrect numeric[],
  testbucketdistribution numeric[],
  testbucketpvalues numeric[],
  notes text,
  favorite boolean,
  tradeoffprimary boolean,
  tradeoffopposite boolean
)
WITH (
  OIDS=FALSE
);
ALTER TABLE models
  OWNER TO postgres;
