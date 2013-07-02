#Populate ingredients table  
#'salt' is only used for testing
INSERT INTO ingredients VALUES 	('porre', 'porrer', 0),
																('gulerod', 'gulerødder', 0),
																('inderfilet af kyllingebryst', 'inderfilet af kyllingebryst', 0),
																('vindruekerneolie', 'vindruekerneolie', 1),
																('hvedemel', 'hvedemel', 1),
																('vand', 'vand', 1),
																('pikant flødeost', 'pikant flødeost', 0),
																('tørret basilikum', 'tørret basilikum', 1),
																('groft salt', 'groft salt', 1),
																('salt', 'salt', 1), 
																('citron', 'citroner', 0),
																('peber', 'peber', 1),
																('tørrede æggenuddel', 'tørrede æggenudler', 0);
																
#Add ragout to recipes
INSERT INTO recipes VALUES ( 'Ragout med kylling og grønsager',
														30,
														null,
														100,
														'http://arla.dk/opskrifter/Ragout-med-kylling-og-gronsager/',
													2);	

#Add ragouts sections and link them with recipe and ingredients
#Section 1
INSERT INTO sections (text) VALUES ('Skær roden af porrerne og fjern de yderste, grove blade. Skræl gulerødderne. Skær grønsagerne i tynde strimler og stave. Skær grove sener og hinder af inderfileterne og skær kødet i ca. 2 cm stykker.');

INSERT INTO sections_per_recipe (recipe_name,
																	section_id,
																	placement) SELECT 'Ragout med kylling og grønsager', 
																										LAST_INSERT_ID(),
																										1;

INSERT INTO ingredients_per_section () SELECT LAST_INSERT_ID(),
																									'porre',
																									null,
																									null,
																									null,
																									4;

INSERT INTO ingredients_per_section () SELECT LAST_INSERT_ID(),
																									'gulerod',
																									null,
																									null,
																									null,
																									6;

INSERT INTO ingredients_per_section () SELECT LAST_INSERT_ID(),
																									'inderfilet af kyllingebryst',
																									null,
																									null,
																									'gram',
																									550;

INSERT INTO sections (text) VALUES ('Varm fedtstoffet i en stor, tykbundet gryde ved kraftig varme, men uden at det bruner. Rør melet i. Bag det sammen under omrøring i ca. ½ min. Rør vandet i og derefter osten. Bring blandingen i kog, stadig under omrøring. Tilsæt kyllingekød, basilikum, salt og peber og kog det i ca. 2 min. Kom nudler og grønsager i og kog videre ved kraftig varme og under omrøring i ca. 2 min. - til nudlerne er "løst op" og møre, men ikke bløde. Smag til.
	Ved servering, kom ragouten i varme skåle og spis med ske og gaffel.');

INSERT INTO sections_per_recipe (recipe_name,
																	section_id,
																	placement) SELECT 'Ragout med kylling og grønsager', 
																										LAST_INSERT_ID(),
																										2;

INSERT INTO ingredients_per_section () SELECT LAST_INSERT_ID(),
																									'vindruekerneolie',
																									null,
																									null,
																									'spsk',
																									1.5;

INSERT INTO ingredients_per_section () SELECT LAST_INSERT_ID(),
																									'hvedemel',
																									null,
																									null,
																									'spsk',
																									2;

INSERT INTO ingredients_per_section () SELECT LAST_INSERT_ID(),
																									'vand',
																									null,
																									null,
																									'liter',
																									1.5;

INSERT INTO ingredients_per_section () SELECT LAST_INSERT_ID(),
																									'Pikant flødeost',
																									null,
																									null,
																									'g',
																									200;

INSERT INTO ingredients_per_section () SELECT LAST_INSERT_ID(),
																									'tørret basilikum',
																									null,
																									null,
																									'tsk',
																									2;

INSERT INTO ingredients_per_section () SELECT LAST_INSERT_ID(),
																									'groft salt',
																									null,
																									null,
																									'spsk',
																									1;

INSERT INTO ingredients_per_section () SELECT LAST_INSERT_ID(),
																									'peber',
																								  null,
																									'friskkværnet',	
																									null,
																									null;

INSERT INTO ingredients_per_section () SELECT LAST_INSERT_ID(),
																									'tørrede æggenuddel',
																									null,
																									null,
																									'g',
																									200;

#Make "Frisk Pasta med tun og citron" recipe
INSERT IGNORE INTO ingredients VALUES 	('smør', 'smør', 1),
																('bladselleri', 'bladselleri', 0),
																('citronskal', 'citronskal', 0),
																('flødeost med grønneurter', 'flødeost med grønneurter', 0),
																('vand', 'vand', 1),
																('peber', 'peber', 1),
																('tun i vand', 'tun i vand', 0),
																('frisk pasta', 'frisk pasta', 0),
																('frosne fine ærter', 'frosne fine ærter', 0),
																('groft salt', 'groft salt', 1);

INSERT INTO recipes VALUES ("Frisk pasta med tun og citron",
														null,
														null,
														null,
														'http://www.arla.dk/da/opskrifter/frisk-pasta-med-tun-og-citron/',
													2);

INSERT INTO sections (text) VALUES ('Smelt smørret i en lille tykbundet gryde ved kraftig varme, men uden at det bruner. Svits bladselleri og citronskal i ca. 1 min. Tilsæt flødeost, vand, salt og peber. Bring saucen i kog og kog den ved svag varme og under omrøring i ca. 1 min. Tag gryden af varmen. Hæld vandet fra tunen og vend den i saucen.');

INSERT INTO ingredients_per_section () SELECT LAST_INSERT_ID(),
																							'smør',
																							null,
																							null,
																							'g',
																							25;

INSERT INTO ingredients_per_section () SELECT LAST_INSERT_ID(),
																							'bladselleri',
																							'i tynde skiver',
																							null,
																							'stilke',
																							4;

INSERT INTO ingredients_per_section () SELECT LAST_INSERT_ID(),
																							'citronskal',
																							null,
																							'fintrevet',
																							'tsk',
																							4;

INSERT INTO ingredients_per_section () SELECT LAST_INSERT_ID(),
																							'flødeost med grønneurter',
																							null,
																							null,
																							'g',
																							150;

INSERT INTO ingredients_per_section () SELECT LAST_INSERT_ID(),
																							'vand',
																							null,
																							null,
																							'dl',
																							2;

INSERT INTO ingredients_per_section () SELECT LAST_INSERT_ID(),
																							'groft salt',
																							null,
																							null,
																							'tsk',
																							1;

INSERT INTO ingredients_per_section () SELECT LAST_INSERT_ID(),
																							'tun i vand',
																							null,
																							null,
																							'dåser',
																							2;

INSERT INTO sections_per_recipe () SELECT 'Frisk pasta med tun og citron',
																					LAST_INSERT_ID(),
																					1;

INSERT INTO sections (text) VALUES ('Kom imens pasta og ærter i en gryde med kogende vand tilsat salt. Bring pasta og ærter i kog og kog det uden låg ca. ½ min. - pastaen skal være mør, men ikke blød. Lad pasta og ærter dryppe af i en sigte og læg det lagvis med tunsaucen i en varm skål. Pasta med tunsauce skal spises med det samme.
Tip: Skal du lave denne ret til mange, er det nemmere at bruge tørret spaghetti. Beregn ca. 100 g tørret spaghetti pr. person. Og tilsæt først ærterne de sidste 2 min. af kogetiden.');

INSERT INTO ingredients_per_section () SELECT LAST_INSERT_ID(),
																							'frisk pasta',
																							'f.eks. tagliolini',
																							null,
																							'g',
																							500;

INSERT INTO ingredients_per_section () SELECT LAST_INSERT_ID(),
																							'frosne fine ærter',
																							null,
																							null,
																							'g',
																							300;

INSERT INTO ingredients_per_section () SELECT LAST_INSERT_ID(),
																							'vand',
																							null,
																							null,
																							'liter',
																							4;

INSERT INTO ingredients_per_section () SELECT LAST_INSERT_ID(),
																							'groft salt',
																							null,
																							null,
																							'tsk',
																							4;

INSERT INTO sections_per_recipe () SELECT 'Frisk pasta med tun og citron',
																					LAST_INSERT_ID(),
																					2;

#This fails due to foreign key constraints, as it should
#INSERT INTO sections_per_recipe VALUES (9,9,9);

#Convert from old data model to the new one
UPDATE ingredients_per_section SET prefix=(TRIM(CONCAT_WS(' ', TRIM(CONCAT_WS(' ', amount, unit)), prefix)));
ALTER TABLE ingredients_per_section DROP COLUMN unit, DROP COLUMN amount;
