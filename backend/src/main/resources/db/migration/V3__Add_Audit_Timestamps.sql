/*
 * Runtime databases may already contain the legacy CreatedAt/UpdatedAt columns.
 * The application now uses the canonical snake_case audit columns consistently.
 */

IF COL_LENGTH('dbo.Users', 'created_at') IS NULL
    ALTER TABLE Users ADD created_at DATETIME2 NULL;
IF COL_LENGTH('dbo.Users', 'updated_at') IS NULL
    ALTER TABLE Users ADD updated_at DATETIME2 NULL;

IF COL_LENGTH('dbo.TouristSites', 'created_at') IS NULL
    ALTER TABLE TouristSites ADD created_at DATETIME2 NULL;
IF COL_LENGTH('dbo.TouristSites', 'updated_at') IS NULL
    ALTER TABLE TouristSites ADD updated_at DATETIME2 NULL;

IF COL_LENGTH('dbo.Narrations', 'created_at') IS NULL
    ALTER TABLE Narrations ADD created_at DATETIME2 NULL;
IF COL_LENGTH('dbo.Narrations', 'updated_at') IS NULL
    ALTER TABLE Narrations ADD updated_at DATETIME2 NULL;

IF COL_LENGTH('dbo.QRCodes', 'created_at') IS NULL
    ALTER TABLE QRCodes ADD created_at DATETIME2 NULL;
IF COL_LENGTH('dbo.QRCodes', 'updated_at') IS NULL
    ALTER TABLE QRCodes ADD updated_at DATETIME2 NULL;

IF COL_LENGTH('dbo.Translations', 'created_at') IS NULL
    ALTER TABLE Translations ADD created_at DATETIME2 NULL;
IF COL_LENGTH('dbo.Translations', 'updated_at') IS NULL
    ALTER TABLE Translations ADD updated_at DATETIME2 NULL;

IF COL_LENGTH('dbo.AiTranslationTasks', 'created_at') IS NULL
    ALTER TABLE AiTranslationTasks ADD created_at DATETIME2 NULL;
IF COL_LENGTH('dbo.AiTranslationTasks', 'updated_at') IS NULL
    ALTER TABLE AiTranslationTasks ADD updated_at DATETIME2 NULL;

UPDATE Users SET created_at = COALESCE(created_at, SYSUTCDATETIME()),
                 updated_at = COALESCE(updated_at, created_at, SYSUTCDATETIME());
UPDATE TouristSites SET created_at = COALESCE(created_at, SYSUTCDATETIME()),
                        updated_at = COALESCE(updated_at, created_at, SYSUTCDATETIME());
UPDATE Narrations SET created_at = COALESCE(created_at, SYSUTCDATETIME()),
                      updated_at = COALESCE(updated_at, created_at, SYSUTCDATETIME());
UPDATE QRCodes SET created_at = COALESCE(created_at, SYSUTCDATETIME()),
                   updated_at = COALESCE(updated_at, created_at, SYSUTCDATETIME());
UPDATE Translations SET created_at = COALESCE(created_at, SYSUTCDATETIME()),
                        updated_at = COALESCE(updated_at, created_at, SYSUTCDATETIME());
UPDATE AiTranslationTasks SET created_at = COALESCE(created_at, SYSUTCDATETIME()),
                              updated_at = COALESCE(updated_at, created_at, SYSUTCDATETIME());

ALTER TABLE Users ALTER COLUMN created_at DATETIME2 NOT NULL;
ALTER TABLE Users ALTER COLUMN updated_at DATETIME2 NOT NULL;
ALTER TABLE TouristSites ALTER COLUMN created_at DATETIME2 NOT NULL;
ALTER TABLE TouristSites ALTER COLUMN updated_at DATETIME2 NOT NULL;
ALTER TABLE Narrations ALTER COLUMN created_at DATETIME2 NOT NULL;
ALTER TABLE Narrations ALTER COLUMN updated_at DATETIME2 NOT NULL;
ALTER TABLE QRCodes ALTER COLUMN created_at DATETIME2 NOT NULL;
ALTER TABLE QRCodes ALTER COLUMN updated_at DATETIME2 NOT NULL;
ALTER TABLE Translations ALTER COLUMN created_at DATETIME2 NOT NULL;
ALTER TABLE Translations ALTER COLUMN updated_at DATETIME2 NOT NULL;
ALTER TABLE AiTranslationTasks ALTER COLUMN created_at DATETIME2 NOT NULL;
ALTER TABLE AiTranslationTasks ALTER COLUMN updated_at DATETIME2 NOT NULL;
