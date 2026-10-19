IF COL_LENGTH('dbo.TouristSites', 'City') IS NULL
BEGIN
    ALTER TABLE dbo.TouristSites
    ADD City NVARCHAR(100) NULL;
END;
