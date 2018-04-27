# slick plain queries

Example shows how to use slick plain queries along with nesting of and sequencing of DBIO

Sample tables can be created by 

create table foo(
 id bigint not null auto_increment,
 foo varchar(20) not null,
 primary key (id)
);

create table bar(
 id bigint not null auto_increment,
 bar varchar(20) not null,
 primary key (id)
);

create table baz(
 id bigint not null auto_increment,
 baz varchar(20) not null,
 primary key (id)
);

create table input(
foo varchar(20) not null,
bar varchar(20) not null,
baz varchar(20) not null);
