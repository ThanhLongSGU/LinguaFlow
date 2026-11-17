IF OBJECT_ID('dbo.Regions', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.Regions (
        RegionId INT IDENTITY(1,1) NOT NULL,
        Name NVARCHAR(150) NOT NULL,
        Description NVARCHAR(MAX) NULL,
        IsActive BIT NOT NULL CONSTRAINT DF_Regions_IsActive DEFAULT 1,
        created_at DATETIME2 NOT NULL CONSTRAINT DF_Regions_CreatedAt DEFAULT SYSUTCDATETIME(),
        updated_at DATETIME2 NOT NULL CONSTRAINT DF_Regions_UpdatedAt DEFAULT SYSUTCDATETIME(),
        CONSTRAINT PK_Regions PRIMARY KEY (RegionId)
    );
END;

IF NOT EXISTS (SELECT 1 FROM dbo.Regions)
    INSERT INTO dbo.Regions (Name, Description) VALUES (N'Chưa phân khu', N'Phân khu mặc định cho dữ liệu hiện hữu');

IF COL_LENGTH('dbo.TouristSites', 'RegionId') IS NULL
    ALTER TABLE dbo.TouristSites ADD RegionId INT NULL;

UPDATE site
SET RegionId = (SELECT TOP 1 RegionId FROM dbo.Regions ORDER BY RegionId)
FROM dbo.TouristSites site
WHERE site.RegionId IS NULL;

IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_keys
    WHERE name = 'FK_TouristSites_Regions'
)
    ALTER TABLE dbo.TouristSites ADD CONSTRAINT FK_TouristSites_Regions
        FOREIGN KEY (RegionId) REFERENCES dbo.Regions (RegionId);

IF EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID('dbo.TouristSites') AND name = 'RegionId' AND is_nullable = 1
)
    ALTER TABLE dbo.TouristSites ALTER COLUMN RegionId INT NOT NULL;

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'UQ_Narrations_Site' AND object_id = OBJECT_ID('dbo.Narrations')
)
    CREATE UNIQUE INDEX UQ_Narrations_Site ON dbo.Narrations (SiteId);

IF OBJECT_ID('dbo.SiteLanguages', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.SiteLanguages (
        SiteLanguageId INT IDENTITY(1,1) NOT NULL,
        SiteId INT NOT NULL,
        LanguageId INT NOT NULL,
        IsEnabled BIT NOT NULL CONSTRAINT DF_SiteLanguages_IsEnabled DEFAULT 1,
        created_at DATETIME2 NOT NULL CONSTRAINT DF_SiteLanguages_CreatedAt DEFAULT SYSUTCDATETIME(),
        updated_at DATETIME2 NOT NULL CONSTRAINT DF_SiteLanguages_UpdatedAt DEFAULT SYSUTCDATETIME(),
        CONSTRAINT PK_SiteLanguages PRIMARY KEY (SiteLanguageId),
        CONSTRAINT UQ_SiteLanguages_Site_Language UNIQUE (SiteId, LanguageId),
        CONSTRAINT FK_SiteLanguages_Sites FOREIGN KEY (SiteId) REFERENCES dbo.TouristSites (SiteId),
        CONSTRAINT FK_SiteLanguages_Languages FOREIGN KEY (LanguageId) REFERENCES dbo.Languages (LanguageId)
    );
END;

IF COL_LENGTH('dbo.Translations', 'SourceType') IS NULL
    ALTER TABLE dbo.Translations ADD SourceType VARCHAR(20) NOT NULL CONSTRAINT DF_Translations_SourceType DEFAULT 'MANUAL';
IF COL_LENGTH('dbo.Translations', 'ReviewStatus') IS NULL
    ALTER TABLE dbo.Translations ADD ReviewStatus VARCHAR(20) NOT NULL CONSTRAINT DF_Translations_ReviewStatus DEFAULT 'NOT_REQUIRED';
IF COL_LENGTH('dbo.Translations', 'ContentStatus') IS NULL
    ALTER TABLE dbo.Translations ADD ContentStatus VARCHAR(20) NOT NULL CONSTRAINT DF_Translations_ContentStatus DEFAULT 'AVAILABLE';
IF COL_LENGTH('dbo.Translations', 'ContentHash') IS NULL
    ALTER TABLE dbo.Translations ADD ContentHash VARCHAR(64) NULL;
IF COL_LENGTH('dbo.Translations', 'ContentVersion') IS NULL
    ALTER TABLE dbo.Translations ADD ContentVersion INT NOT NULL CONSTRAINT DF_Translations_ContentVersion DEFAULT 1;
IF COL_LENGTH('dbo.Translations', 'ReviewedBy') IS NULL
    ALTER TABLE dbo.Translations ADD ReviewedBy INT NULL;
IF COL_LENGTH('dbo.Translations', 'ReviewedAt') IS NULL
    ALTER TABLE dbo.Translations ADD ReviewedAt DATETIME2 NULL;

IF COL_LENGTH('dbo.AiTranslationTasks', 'TaskSource') IS NULL
    ALTER TABLE dbo.AiTranslationTasks ADD TaskSource VARCHAR(20) NOT NULL CONSTRAINT DF_AiTasks_TaskSource DEFAULT 'ADMIN';
IF COL_LENGTH('dbo.AiTranslationTasks', 'SourceContentHash') IS NULL
    ALTER TABLE dbo.AiTranslationTasks ADD SourceContentHash VARCHAR(64) NULL;

IF OBJECT_ID('dbo.Audio', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.Audio (
        AudioId BIGINT IDENTITY(1,1) NOT NULL,
        TranslationId INT NOT NULL,
        StorageUrl NVARCHAR(1000) NOT NULL,
        Status VARCHAR(20) NOT NULL,
        ContentHash VARCHAR(64) NOT NULL,
        AudioVersion INT NOT NULL,
        ErrorMessage NVARCHAR(MAX) NULL,
        RetryCount INT NOT NULL CONSTRAINT DF_Audio_RetryCount DEFAULT 0,
        GeneratedAt DATETIME2 NULL,
        created_at DATETIME2 NOT NULL CONSTRAINT DF_Audio_CreatedAt DEFAULT SYSUTCDATETIME(),
        updated_at DATETIME2 NOT NULL CONSTRAINT DF_Audio_UpdatedAt DEFAULT SYSUTCDATETIME(),
        CONSTRAINT PK_Audio PRIMARY KEY (AudioId),
        CONSTRAINT UQ_Audio_Translation UNIQUE (TranslationId),
        CONSTRAINT FK_Audio_Translations FOREIGN KEY (TranslationId)
            REFERENCES dbo.Translations (TranslationId)
    );
END;
