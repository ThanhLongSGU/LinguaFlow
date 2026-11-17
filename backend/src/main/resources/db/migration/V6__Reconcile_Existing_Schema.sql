/*
 * Reconciles the existing SQL Server database that predates Flyway adoption.
 * The local profile baselines that database at version 5, so only this
 * forward repair migration is applied there.
 */

IF OBJECT_ID('dbo.regions', 'U') IS NULL
    THROW 51000, 'Cannot reconcile schema: dbo.regions does not exist.', 1;

IF OBJECT_ID('dbo.tourist_sites', 'U') IS NULL
    THROW 51000, 'Cannot reconcile schema: dbo.tourist_sites does not exist.', 1;

IF OBJECT_ID('dbo.users', 'U') IS NULL
    THROW 51000, 'Cannot reconcile schema: dbo.users does not exist.', 1;

IF COL_LENGTH('dbo.tourist_sites', 'region_id') IS NULL
    ALTER TABLE dbo.tourist_sites ADD region_id INT NULL;

IF NOT EXISTS (SELECT 1 FROM dbo.regions)
    INSERT INTO dbo.regions (name, description, is_active, created_at, updated_at)
    VALUES (N'Chưa phân khu', N'Phân khu mặc định cho dữ liệu hiện hữu', 1, SYSUTCDATETIME(), SYSUTCDATETIME());

EXEC sp_executesql N'
    UPDATE site
    SET region_id = (SELECT TOP 1 region_id FROM dbo.regions ORDER BY region_id)
    FROM dbo.tourist_sites site
    WHERE site.region_id IS NULL;';

EXEC sp_executesql N'
    IF EXISTS (SELECT 1 FROM dbo.tourist_sites WHERE region_id IS NULL)
        THROW 51000, ''Cannot reconcile schema: tourist_sites.region_id still contains NULL values.'', 1;';

IF NOT EXISTS (
    SELECT 1
    FROM sys.foreign_keys
    WHERE name = 'FK_tourist_sites_regions'
      AND parent_object_id = OBJECT_ID('dbo.tourist_sites')
)
    EXEC sp_executesql N'
        ALTER TABLE dbo.tourist_sites
            ADD CONSTRAINT FK_tourist_sites_regions
            FOREIGN KEY (region_id) REFERENCES dbo.regions (region_id);';

IF EXISTS (
    SELECT 1
    FROM sys.columns
    WHERE object_id = OBJECT_ID('dbo.tourist_sites')
      AND name = 'region_id'
      AND is_nullable = 1
)
    EXEC sp_executesql N'
        ALTER TABLE dbo.tourist_sites ALTER COLUMN region_id INT NOT NULL;';

IF COL_LENGTH('dbo.translations', 'source_type') IS NULL
    ALTER TABLE dbo.translations ADD source_type VARCHAR(20) NOT NULL
        CONSTRAINT DF_translations_source_type DEFAULT 'MANUAL';
IF COL_LENGTH('dbo.translations', 'review_status') IS NULL
    ALTER TABLE dbo.translations ADD review_status VARCHAR(20) NOT NULL
        CONSTRAINT DF_translations_review_status DEFAULT 'NOT_REQUIRED';
IF COL_LENGTH('dbo.translations', 'content_status') IS NULL
    ALTER TABLE dbo.translations ADD content_status VARCHAR(20) NOT NULL
        CONSTRAINT DF_translations_content_status DEFAULT 'AVAILABLE';
IF COL_LENGTH('dbo.translations', 'content_hash') IS NULL
    ALTER TABLE dbo.translations ADD content_hash VARCHAR(64) NULL;
IF COL_LENGTH('dbo.translations', 'content_version') IS NULL
    ALTER TABLE dbo.translations ADD content_version INT NOT NULL
        CONSTRAINT DF_translations_content_version DEFAULT 1;
IF COL_LENGTH('dbo.translations', 'reviewed_by') IS NULL
    ALTER TABLE dbo.translations ADD reviewed_by INT NULL;
IF COL_LENGTH('dbo.translations', 'reviewed_at') IS NULL
    ALTER TABLE dbo.translations ADD reviewed_at DATETIME2 NULL;

IF COL_LENGTH('dbo.ai_translation_tasks', 'task_source') IS NULL
    ALTER TABLE dbo.ai_translation_tasks ADD task_source VARCHAR(20) NOT NULL
        CONSTRAINT DF_ai_translation_tasks_task_source DEFAULT 'ADMIN';
IF COL_LENGTH('dbo.ai_translation_tasks', 'source_content_hash') IS NULL
    ALTER TABLE dbo.ai_translation_tasks ADD source_content_hash VARCHAR(64) NULL;

IF COL_LENGTH('dbo.users', 'failed_login_attempts') IS NULL
    ALTER TABLE dbo.users ADD failed_login_attempts INT NOT NULL
        CONSTRAINT DF_users_failed_login_attempts DEFAULT 0;

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'UQ_narrations_site'
      AND object_id = OBJECT_ID('dbo.narrations')
)
    CREATE UNIQUE INDEX UQ_narrations_site
        ON dbo.narrations (site_id);
