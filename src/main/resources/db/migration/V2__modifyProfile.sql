ALTER TABLE user_profile ADD COLUMN gender VARCHAR(20) NULL CHECK (gender IN ('MALE', 'FEMALE'));
ALTER TABLE user_profile ADD COLUMN time_zone VARCHAR(50) NOT NULL DEFAULT 'Europe/Madrid';