#Drop old tables to get a clean db

DROP TABLE IF EXISTS sections_per_recipe;
DROP TABLE IF EXISTS ingredients_per_section;
DROP TABLE IF EXISTS recipes;
DROP TABLE IF EXISTS ingredients;
DROP TABLE IF EXISTS sections;


#Create statements

#recipe
CREATE TABLE recipes (
	name VARCHAR(255) NOT NULL PRIMARY KEY, 
	time_in_minutes SMALLINT UNSIGNED,
	picture LONGBLOB,
	calories INT UNSIGNED,
	source VARCHAR(255),
	portions INT UNSIGNED NOT NULL
) ENGINE=INNODB;

#ingredients
CREATE TABLE ingredients (
	name_singular VARCHAR(100) PRIMARY KEY,
	name_plural VARCHAR(100) UNIQUE,
	common BOOLEAN DEFAULT 0
) ENGINE=INNODB;

#section
CREATE TABLE sections (
	id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
	headline VARCHAR(255),
	text TEXT
) ENGINE=INNODB;

#sections_per_recipe
CREATE TABLE sections_per_recipe (
	recipe_name VARCHAR(225),
	section_id INT UNSIGNED,
	placement TINYINT UNSIGNED,
	FOREIGN KEY (recipe_name) REFERENCES recipes(name) 
	ON DELETE CASCADE ON UPDATE CASCADE,
	FOREIGN KEY (section_id) REFERENCES sections(id)
	ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=INNODB;

#ingredients_per_section
CREATE TABLE ingredients_per_section (
	section_id INT UNSIGNED,
	name_singular VARCHAR(100),
	suffix VARCHAR(255),
	prefix VARCHAR(255),
	unit VARCHAR(25),
	amount DECIMAL(10,3),
	FOREIGN KEY (section_id) REFERENCES sections(id)
	ON DELETE CASCADE ON UPDATE CASCADE,
	FOREIGN KEY (name_singular) REFERENCES ingredients(name_singular)
	ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=INNODB;
