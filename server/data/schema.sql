drop database if exists project;
create database project;

use project;

create table if not exists users(
	
	id int auto_increment not null,
	email varchar(255) not null unique,
	password varchar(255) not null,
	created_at timestamp default current_timestamp,
	is_premium boolean default false,
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
);

CREATE TABLE user_health_data (
    id INT AUTO_INCREMENT NOT NULL,
    user_id INT NOT NULL,
    height DECIMAL(5,2) COMMENT 'Height in cm',
    weight DECIMAL(5,2) COMMENT 'Weight in kg',
    age INT,
    gender VARCHAR(20),
    activity_level VARCHAR(50) COMMENT 'Sedentary, Lightly Active, Moderately Active, Very Active, Extra Active',
    fitness_goal VARCHAR(50) COMMENT 'Weight Loss, Maintenance, Muscle Gain, etc.',
    bmi DECIMAL(4,2),
    bmr INT COMMENT 'Basal Metabolic Rate in calories',
    tdee INT COMMENT 'Total Daily Energy Expenditure in calories',
    
    PRIMARY KEY (id),
    CONSTRAINT fk_health_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT unique_user_health UNIQUE (user_id)
);

CREATE TABLE IF NOT EXISTS weight_logs (
    id INT AUTO_INCREMENT NOT NULL,
    user_id INT NOT NULL,
    weight DECIMAL(5,2) NOT NULL, 
    date DATE NOT NULL,
    notes TEXT,
    
    CONSTRAINT pk_weightlog_id PRIMARY KEY(id),
    CONSTRAINT fk_user_id_int FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT unique_user_date UNIQUE (user_id, date)
);

