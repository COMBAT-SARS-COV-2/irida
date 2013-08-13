INSERT INTO system_role (`name`,`description`) VALUES ('ROLE_USER','A basic user in the system.');INSERT INTO system_role (`name`,`description`) VALUES ('ROLE_ADMIN','An administrative user in the system.');INSERT INTO system_role (`name`,`description`) VALUES ('ROLE_CLIENT','A client tool in the system.');-- user account required for integration testsINSERT INTO user (`createdDate`, `modifiedDate`, `email`, `firstName`, `lastName`, `locale`, `password`, `phoneNumber`, `username`, `enabled`, `system_role`) VALUES (now(), now() , 'franklin.bristow@phac-aspc.gc.ca', 'Franklin', 'Bristow', 'en', '$2a$10$yvzFLxWA9m2wNQmHpJtWT.MRZv8qV8Mo3EMB6HTkDnUbi9aBrbWWW', '7029', 'fbristow', 1, 1);INSERT INTO user (`createdDate`, `modifiedDate`, `email`, `firstName`, `lastName`, `locale`, `password`, `phoneNumber`, `username`, `enabled`, `system_role`) VALUES (now(), now() , 'josh.adam@phac-aspc.gc.ca', 'Josh', 'Adam', 'en', '$2a$10$yvzFLxWA9m2wNQmHpJtWT.MRZv8qV8Mo3EMB6HTkDnUbi9aBrbWWW', '7418', 'josh', 1, 1);INSERT INTO user (`createdDate`, `modifiedDate`, `email`, `firstName`, `lastName`, `locale`, `password`, `phoneNumber`, `username`, `enabled`, `system_role`) VALUES (now(), now() , 'thomas.matthews@phac-aspc.gc.ca', 'Tom', 'Matthews', 'en', '$2a$10$yvzFLxWA9m2wNQmHpJtWT.MRZv8qV8Mo3EMB6HTkDnUbi9aBrbWWW', '7418', 'tom', 1, 1);INSERT INTO user (`createdDate`, `modifiedDate`, `email`, `firstName`, `lastName`, `locale`, `password`, `phoneNumber`, `username`, `enabled`, `system_role`) VALUES (now(), now() , 'admin@nowhere.ca', 'Admin', 'Admin', 'en', '$2a$10$yvzFLxWA9m2wNQmHpJtWT.MRZv8qV8Mo3EMB6HTkDnUbi9aBrbWWW', '0000', 'admin', 1, 2);INSERT INTO user (`createdDate`, `modifiedDate`, `email`, `firstName`, `lastName`, `locale`, `password`, `phoneNumber`, `username`, `enabled`, `system_role`) VALUES (now(), now() , 'test@nowhere.ca', 'Test', 'User', 'en', '$2a$10$yvzFLxWA9m2wNQmHpJtWT.MRZv8qV8Mo3EMB6HTkDnUbi9aBrbWWW', '1234', 'test', 1, 1);-- projects required for integration testsINSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 1', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 2', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 3', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 4', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 5', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 6', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 7', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 8', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 9', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 10', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 11', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 12', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 13', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 14', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 15', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 16', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 17', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 18', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 19', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 20', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 21', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 22', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 23', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 24', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 25', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 26', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 27', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 28', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 29', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 30', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 31', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 32', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 33', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 34', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 35', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 36', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 37', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 38', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 39', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 40', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 41', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 42', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 43', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 44', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 45', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 46', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 47', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 48', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 49', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 50', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 51', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 52', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 53', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 54', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 55', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 56', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 57', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 58', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 59', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 60', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 61', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 62', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 63', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 64', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 65', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 66', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 67', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 68', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 69', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 70', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 71', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 72', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 73', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 74', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 75', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 76', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 77', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 78', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 79', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 80', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 81', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 82', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 83', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 84', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 85', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 86', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 87', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 88', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 89', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 90', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 91', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 92', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 93', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 94', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 95', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 96', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 97', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 98', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 99', 1);INSERT INTO project (`createdDate`, `modifiedDate`, `name`, `enabled`) VALUES (now(), now() , 'Project 100', 1);-- relationship between projects and usersINSERT INTO project_user (`createdDate`, `project_id`, `user_id`) VALUES (now(), 1, 2);INSERT INTO project_user (`createdDate`, `project_id`, `user_id`) VALUES (now(), 2, 2);INSERT INTO project_user (`createdDate`, `project_id`, `user_id`) VALUES (now(), 3, 2);INSERT INTO project_user (`createdDate`, `project_id`, `user_id`) VALUES (now(), 4, 2);INSERT INTO project_user (`createdDate`, `project_id`, `user_id`) VALUES (now(), 5, 2);INSERT INTO project_user (`createdDate`, `project_id`, `user_id`) VALUES (now(), 6, 2);INSERT INTO project_user (`createdDate`, `project_id`, `user_id`) VALUES (now(), 7, 2);INSERT INTO project_user (`createdDate`, `project_id`, `user_id`) VALUES (now(), 8, 2);INSERT INTO project_user (`createdDate`, `project_id`, `user_id`) VALUES (now(), 9, 2);INSERT INTO project_user (`createdDate`, `project_id`, `user_id`) VALUES (now(), 10, 2);INSERT INTO project_user (`createdDate`, `project_id`, `user_id`) VALUES (now(), 11, 2);INSERT INTO project_user (`createdDate`, `project_id`, `user_id`) VALUES (now(), 12, 2);INSERT INTO project_user (`createdDate`, `project_id`, `user_id`) VALUES (now(), 13, 2);INSERT INTO project_user (`createdDate`, `project_id`, `user_id`) VALUES (now(), 14, 2);INSERT INTO project_user (`createdDate`, `project_id`, `user_id`) VALUES (now(), 1, 5);INSERT INTO project_user (`createdDate`, `project_id`, `user_id`) VALUES (now(), 2, 5);INSERT INTO project_user (`createdDate`, `project_id`, `user_id`) VALUES (now(), 3, 5);INSERT INTO project_user (`createdDate`, `project_id`, `user_id`) VALUES (now(), 4, 5);INSERT INTO project_user (`createdDate`, `project_id`, `user_id`) VALUES (now(), 5, 5);INSERT INTO project_user (`createdDate`, `project_id`, `user_id`) VALUES (now(), 6, 5);INSERT INTO project_user (`createdDate`, `project_id`, `user_id`) VALUES (now(), 7, 5);INSERT INTO project_user (`createdDate`, `project_id`, `user_id`) VALUES (now(), 8, 5);INSERT INTO project_user (`createdDate`, `project_id`, `user_id`) VALUES (now(), 9, 5);INSERT INTO project_user (`createdDate`, `project_id`, `user_id`) VALUES (now(), 10, 5);INSERT INTO project_user (`createdDate`, `project_id`, `user_id`) VALUES (now(), 11, 5);INSERT INTO project_user (`createdDate`, `project_id`, `user_id`) VALUES (now(), 12, 5);INSERT INTO project_user (`createdDate`, `project_id`, `user_id`) VALUES (now(), 13, 5);INSERT INTO project_user (`createdDate`, `project_id`, `user_id`) VALUES (now(), 14, 5);INSERT INTO project_user (`createdDate`, `project_id`, `user_id`) VALUES (now(), 1, 1);INSERT INTO project_user (`createdDate`, `project_id`, `user_id`) VALUES (now(), 2, 1);INSERT INTO project_user (`createdDate`, `project_id`, `user_id`) VALUES (now(), 3, 1);INSERT INTO project_user (`createdDate`, `project_id`, `user_id`) VALUES (now(), 4, 1);INSERT INTO project_user (`createdDate`, `project_id`, `user_id`) VALUES (now(), 5, 1);INSERT INTO project_user (`createdDate`, `project_id`, `user_id`) VALUES (now(), 100, 1);-- samplesINSERT INTO sample (`createdDate`, `modifiedDate`, `sampleName`, `enabled`) VALUES (now(), now() , 'Sample 1', 1);INSERT INTO sample (`createdDate`, `modifiedDate`, `sampleName`, `enabled`) VALUES (now(), now() , 'Sample 2', 1);INSERT INTO sample (`createdDate`, `modifiedDate`, `sampleName`, `enabled`) VALUES (now(), now() , 'Sample 3', 1);INSERT INTO sample (`createdDate`, `modifiedDate`, `sampleName`, `enabled`) VALUES (now(), now() , 'Sample 4', 1);INSERT INTO sample (`createdDate`, `modifiedDate`, `sampleName`, `enabled`) VALUES (now(), now() , 'Sample 5', 1);-- sample relationshipINSERT INTO project_sample (`createdDate`, `project_id`, `sample_id`) VALUES (now(), 5, 1);INSERT INTO project_sample (`createdDate`, `project_id`, `sample_id`) VALUES (now(), 4, 2);INSERT INTO project_sample (`createdDate`, `project_id`, `sample_id`) VALUES (now(), 4, 3);INSERT INTO project_sample (`createdDate`, `project_id`, `sample_id`) VALUES (now(), 4, 4);INSERT INTO project_sample (`createdDate`, `project_id`, `sample_id`) VALUES (now(), 5, 5);