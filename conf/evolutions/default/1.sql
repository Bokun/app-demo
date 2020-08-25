# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table bokun_customer (
  id                            bigint auto_increment not null,
  vendor_id                     varchar(255),
  domain                        varchar(255),
  access_token                  varchar(255),
  permissions                   varchar(255),
  constraint pk_bokun_customer primary key (id)
);

create table oauth_record (
  id                            bigint auto_increment not null,
  state                         varchar(255),
  timestamp                     timestamp,
  constraint pk_oauth_record primary key (id)
);


# --- !Downs

drop table if exists bokun_customer;

drop table if exists oauth_record;

