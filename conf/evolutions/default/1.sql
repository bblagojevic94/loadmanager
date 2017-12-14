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
  subscription_id   BIGSERIAL NOT NULL,
  group_id          BIGSERIAL NOT NULL,
  created_at        TIMESTAMP NOT NULL,
  CONSTRAINT pk_subscribers_groups                  PRIMARY KEY (subscription_id, group_id),
  CONSTRAINT fk_subscribers_groups_subscribers_01   FOREIGN KEY (subscription_id) REFERENCES subscribers(id),
  CONSTRAINT fk_subscribers_groups_groups_02        FOREIGN KEY (group_id) REFERENCES groups(id)
);

# --- !Downs
DROP TABLE IF EXISTS subscribers_groups;
DROP TABLE IF EXISTS groups_microgrids;
DROP TABLE IF EXISTS subscribers;
DROP TABLE IF EXISTS groups;
DROP TABLE IF EXISTS microgrids;
