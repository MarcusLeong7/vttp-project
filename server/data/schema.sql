drop database if exists project;
create database project;

use project;

create table if not exists users(
	
	id int auto_increment not null,
	email varchar(255) not null unique,
	password varchar(255) not null,
	created_at timestamp default current_timestamp,
	google_access_token varchar(1024),
	google_refresh_token varchar(1024), 
	google_token_expiry timestamp,
	
	constraint pk_id primary key(id)
	
);

create table if not exists meal_plans (
    id varchar(8) not null,
    name varchar(255) not null,
    description text,
    user_id varchar(255) not null,
    day_of_week int,  -- 0 for Sunday, 1 for Monday, etc.
    created_at timestamp default current_timestamp,
    
    constraint pk_mealplan_id primary key (id),
	-- If user is deleted, all their meal plans is deleted
    constraint fk_user_id foreign key (user_id) references users(email) on delete cascade
);

create table if not exists meal_plan_items(
	id int auto_increment not null,
    meal_plan_id varchar(8) not null,
    meal_id varchar(255) not null,
    meal_title varchar(255) not null,
    meal_image text,
    calories varchar(50),
    protein varchar(50),
    carbs varchar(50),
    fats varchar(50),
    meal_type varchar(50),  -- breakfast, lunch, dinner, snack
    constraint pk_mealplan_item_id primary key (id),
    constraint fk_mealplan_id foreign key (meal_plan_id) references meal_plans(id) on delete cascade
);)

grant all privileges on project.* to 'fred'@'%';
flush privileges;