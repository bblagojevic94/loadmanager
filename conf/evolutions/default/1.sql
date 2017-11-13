# --- !Ups

CREATE TABLE microgrids (
  id                BIGSERIAL PRIMARY KEY,
  url               TEXT NOT NULL,
  platform          VARCHAR(10) NOT NULL,
  organisation_id   VARCHAR(50) NOT NULL,
  created_at        TIMESTAMP NOT NULL
);

CREATE TABLE groups (
  id           BIGSERIAL PRIMARY KEY,
  name         VARCHAR(60) NOT NULL,
  created_at   TIMESTAMP NOT NULL
);

CREATE TABLE groups_microgrids (
  group_id       BIGSERIAL NOT NULL,
  microgrid_id   BIGSERIAL NOT NULL,
  created_at        TIMESTAMP NOT NULL,
  CONSTRAINT pk_groups_microgrids                 PRIMARY KEY (group_id, microgrid_id),
  CONSTRAINT fk_groups_microgrids_groups_01       FOREIGN KEY (group_id) REFERENCES groups(id),
  CONSTRAINT fk_groups_microgrids_microgrids_02   FOREIGN KEY (microgrid_id) REFERENCES microgrids(id)
);

# --- !Downs

DROP TABLE IF EXISTS groups_microgrids;
DROP TABLE IF EXISTS groups;
DROP TABLE IF EXISTS microgrids;
