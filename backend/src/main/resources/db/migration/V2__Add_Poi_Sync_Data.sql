IF COL_LENGTH('dbo.TouristSites', 'City') IS NULL
    ALTER TABLE dbo.TouristSites ADD City NVARCHAR(100) NULL;

IF COL_LENGTH('dbo.TouristSites', 'Latitude') IS NULL
    ALTER TABLE dbo.TouristSites ADD Latitude DECIMAL(10,7) NULL;

IF COL_LENGTH('dbo.TouristSites', 'Longitude') IS NULL
    ALTER TABLE dbo.TouristSites ADD Longitude DECIMAL(10,7) NULL;

IF COL_LENGTH('dbo.TouristSites', 'TriggerRadius') IS NULL
    ALTER TABLE dbo.TouristSites
        ADD TriggerRadius INT NOT NULL CONSTRAINT DF_TouristSites_TriggerRadius DEFAULT 20;

IF OBJECT_ID('dbo.PoiSyncMetadata', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.PoiSyncMetadata (
        MetadataId INT NOT NULL CONSTRAINT PK_PoiSyncMetadata PRIMARY KEY,
        DataVersion BIGINT NOT NULL,
        CONSTRAINT CK_PoiSyncMetadata_Singleton CHECK (MetadataId = 1)
    );
END;

IF NOT EXISTS (SELECT 1 FROM dbo.PoiSyncMetadata WHERE MetadataId = 1)
    INSERT INTO dbo.PoiSyncMetadata (MetadataId, DataVersion) VALUES (1, 1);

EXEC(N'CREATE TRIGGER dbo.TR_TouristSites_PoiSyncVersion
ON dbo.TouristSites
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE dbo.PoiSyncMetadata SET DataVersion = DataVersion + 1 WHERE MetadataId = 1;
END');

EXEC(N'CREATE TRIGGER dbo.TR_Translations_PoiSyncVersion
ON dbo.Translations
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE dbo.PoiSyncMetadata SET DataVersion = DataVersion + 1 WHERE MetadataId = 1;
END');

EXEC(N'CREATE TRIGGER dbo.TR_Languages_PoiSyncVersion
ON dbo.Languages
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE dbo.PoiSyncMetadata SET DataVersion = DataVersion + 1 WHERE MetadataId = 1;
END');