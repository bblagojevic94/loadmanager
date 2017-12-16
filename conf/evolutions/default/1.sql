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

CREATE TABLE subscribers (
  id           BIGSERIAL PRIMARY KEY,
  callback     TEXT NOT NULL,
  created_at   TIMESTAMP NOT NULL
);

CREATE TABLE groups_microgrids (
  group_id       BIGSERIAL NOT NULL,
  microgrid_id   BIGSERIAL NOT NULL,

  CONSTRAINT pk_gm                                PRIMARY KEY (group_id, microgrid_id),
  CONSTRAINT fk_gm_groups                         FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,
  CONSTRAINT fk_gm_grids                          FOREIGN KEY (microgrid_id) REFERENCES microgrids(id) ON DELETE CASCADE
);

CREATE TABLE subscribers_groups (
  subscriber_id     BIGSERIAL NOT NULL,
  group_id          BIGSERIAL NOT NULL,

  CONSTRAINT pk_sg                                  PRIMARY KEY (subscriber_id, group_id),
  CONSTRAINT fk_sg_subs                             FOREIGN KEY (subscriber_id) REFERENCES subscribers(id) ON DELETE CASCADE,
  CONSTRAINT fk_sg_groups                           FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE
);

# --- !Downs
DROP TABLE IF EXISTS subscribers_groups;
DROP TABLE IF EXISTS groups_microgrids;
DROP TABLE IF EXISTS subscribers;
DROP TABLE IF EXISTS groups;
DROP TABLE IF EXISTS microgrids;
