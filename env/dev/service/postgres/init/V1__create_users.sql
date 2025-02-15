CREATE USER telegram_user WITH PASSWORD 'telegram_password';
GRANT ALL PRIVILEGES ON DATABASE telegram TO telegram_user;

\connect telegram

GRANT USAGE ON SCHEMA public TO telegram_user;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO telegram_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO telegram_user;
ALTER ROLE telegram_user SET search_path TO telegram;

CREATE SCHEMA telegram;
GRANT ALL PRIVILEGES ON SCHEMA telegram TO telegram_user;
ALTER ROLE telegram_user SET search_path TO telegram;