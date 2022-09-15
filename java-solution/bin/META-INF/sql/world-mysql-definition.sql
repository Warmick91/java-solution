DROP DATABASE IF EXISTS world;
CREATE DATABASE world;
USE world;


CREATE TABLE Country (
  longCode CHAR(3) NOT NULL,
  globalName CHAR(52) NOT NULL,
  continent ENUM('Asia','Europe','North America','Africa','Oceania','Antarctica','South America') NOT NULL,
  region CHAR(26) NOT NULL,
  surfaceArea FLOAT NOT NULL,
  foundingYear SMALLINT NULL,
  totalPopulation BIGINT NOT NULL,
  lifeExpectancy FLOAT NULL,
  grossNationalProduct FLOAT NULL,
  localName CHAR(45) NOT NULL,
  governmentForm CHAR(45) NOT NULL,
  headOfState CHAR(60) NULL,
  capitalReference BIGINT NULL,
  shortCode CHAR(2) NOT NULL,
  PRIMARY KEY  (longCode),
  UNIQUE KEY (shortCode),
  UNIQUE KEY (globalName)
);

CREATE TABLE CountryLanguage (
  countryCode CHAR(3) NOT NULL,
  language CHAR(30) NOT NULL,
  isOfficial BOOL NOT NULL,
  relevance FLOAT NOT NULL,
  PRIMARY KEY  (countryCode,language),
  FOREIGN KEY (countryCode) REFERENCES Country (longCode) ON DELETE CASCADE ON UPDATE CASCADE
);


CREATE TABLE City (
  cityIdentity BIGINT NOT NULL AUTO_INCREMENT,
  name CHAR(35) NOT NULL,
  countryCode CHAR(3) NOT NULL,
  district CHAR(30) NOT NULL,
  population BIGINT NOT NULL,
  PRIMARY KEY  (cityIdentity),
  FOREIGN KEY (countryCode) REFERENCES Country (longCode) ON DELETE CASCADE ON UPDATE CASCADE
);

