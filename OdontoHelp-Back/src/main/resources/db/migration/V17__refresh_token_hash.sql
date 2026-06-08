TRUNCATE TABLE refresh_tokens;

ALTER TABLE refresh_tokens RENAME COLUMN token TO token_hash;
ALTER TABLE refresh_tokens ALTER COLUMN token_hash TYPE VARCHAR(64);
