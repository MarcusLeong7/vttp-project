drop database if exists project;
create database project;

use project;

create table users(
	
	id int auto_increment not null,
	email varchar(255) not null unique,
	password varchar(255) not null,
	created_at timestamp default current_timestamp,
	
	constraint pk_id primary key(id)
	
);

grant all privileges on project.* to 'fred'@'%';
flush privileges;