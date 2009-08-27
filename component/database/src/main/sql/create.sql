--table name for mysql on linux is case sensitive
create table  hibernate_unique_key (
  next_hi         BIGINT
  );
INSERT INTO  hibernate_unique_key(next_hi) VALUES ( 100 );
/*
create table RESOURCE_BUNDLE_DATA (
    id            VARCHAR(128) NOT NULL,
    name            VARCHAR(128) NOT NULL,
    language        VARCHAR(25),
    country         VARCHAR(25),
    variant         VARCHAR(25),
    resourceType    VARCHAR(128) NOT NULL,
    data            TEXT NOT NULL, 
    PRIMARY KEY (id));
*/
