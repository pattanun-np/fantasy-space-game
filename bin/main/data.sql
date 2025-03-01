-- Accounts
INSERT INTO account (name, username, password)
VALUES ('Game', 'game', 'game'),
       ('Motyka', 'motyka', 'heslo'),
       ('John Doe', 'admin', 'admin');

-- User Character
INSERT INTO character (account_id, name, health, attack, mana, healing, experience, class, level)
VALUES ((SELECT id FROM account WHERE username = 'motyka'),
        'Moni The Teacher', 100, 40, 30, 30, 6000, 'SORCERER', 1);

-- Add a character for the admin user
INSERT INTO character (account_id, name, health, attack, stamina, defense, experience, class, level)
VALUES ((SELECT id FROM account WHERE username = 'admin'),
        'Admin Warrior',120, 50, 30, 30,1, 'WARRIOR', 1);

-- Harry Potter Characters
INSERT INTO character (account_id, name, health, attack, mana, healing, experience, class, level)
VALUES ((SELECT id FROM account WHERE username = 'game'), 'Harry Potter', 100, 40, 30, 30, 1, 'SORCERER', 10),
       ((SELECT id FROM account WHERE username = 'game'), 'Hermione Granger', 90, 40, 40, 30, 1, 'SORCERER', 10),
       ((SELECT id FROM account WHERE username = 'game'), 'Ron Weasley', 120, 50, 20, 10,10, 'SORCERER', 10),
       ((SELECT id FROM account WHERE username = 'game'), 'Severus Snape', 80, 60, 30, 30, 2, 'SORCERER', 10),
       ((SELECT id FROM account WHERE username = 'game'), 'Albus Dumbledore', 90, 40, 40, 30, 1, 'SORCERER', 1),
       ((SELECT id FROM account WHERE username = 'game'), 'Lord Voldemort', 80, 80, 10, 30, 1, 'SORCERER', 10),
       ((SELECT id FROM account WHERE username = 'game'), 'Minerva McGonagall', 100, 40, 30, 30,1, 'SORCERER', 10),
       ((SELECT id FROM account WHERE username = 'game'), 'Bellatrix Lestrange', 80, 70, 20, 30,1, 'SORCERER', 20),
       ((SELECT id FROM account WHERE username = 'game'), 'Draco Malfoy', 100, 40, 30, 30,1, 'SORCERER', 10),
       ((SELECT id FROM account WHERE username = 'game'), 'Neville Longbottom', 130, 30, 10, 30,1, 'SORCERER', 10);

-- Star Wars Characters
INSERT INTO character (account_id, name, health, attack, stamina, defense, experience, class, level)
VALUES ((SELECT id FROM account WHERE username = 'game'), 'Luke Skywalker', 110, 40, 20, 30,1, 'WARRIOR', 10),
       ((SELECT id FROM account WHERE username = 'game'), 'Yoda', 80, 30, 50, 40,1, 'WARRIOR', 10),
       ((SELECT id FROM account WHERE username = 'game'), 'Han Solo', 120, 40, 10, 30,1, 'WARRIOR', 10),
       ((SELECT id FROM account WHERE username = 'game'), 'Darth Vader', 100, 60, 10, 30,1, 'WARRIOR', 10),
       ((SELECT id FROM account WHERE username = 'game'), 'Obi-Wan Kenobi', 100, 40, 30, 30,1, 'WARRIOR', 10),
       ((SELECT id FROM account WHERE username = 'game'), 'Emperor Palpatine', 80, 80, 10, 30,1, 'WARRIOR', 10),
       ((SELECT id FROM account WHERE username = 'game'), 'Mace Windu', 110, 40, 20, 30,1, 'WARRIOR', 10),
       ((SELECT id FROM account WHERE username = 'game'), 'Darth Maul', 90, 60, 20, 30,1, 'WARRIOR', 10),
       ((SELECT id FROM account WHERE username = 'game'), 'Kylo Ren', 100, 50, 20, 30,1, 'WARRIOR', 10),
       ((SELECT id FROM account WHERE username = 'game'), 'Finn', 130, 20, 10, 40,1, 'WARRIOR', 10);


-- Leaderboard
INSERT INTO leaderboard (character_id, wins, losses, draws)
VALUES ((SELECT id FROM character WHERE name = 'Luke Skywalker'),1,1,1),
       ((SELECT id FROM character WHERE name = 'Yoda'),1,1,1),
       ((SELECT id FROM character WHERE name = 'Han Solo'),1,1,1),
       ((SELECT id FROM character WHERE name = 'Darth Vader'),1,1,1),
       ((SELECT id FROM character WHERE name = 'Obi-Wan Kenobi'),1,1,1),
       ((SELECT id FROM character WHERE name = 'Emperor Palpatine'),1,1,1),
       ((SELECT id FROM character WHERE name = 'Mace Windu'),1,1,1),
       ((SELECT id FROM character WHERE name = 'Darth Maul'),1,1,1),
       ((SELECT id FROM character WHERE name = 'Finn'),1,1,1);




