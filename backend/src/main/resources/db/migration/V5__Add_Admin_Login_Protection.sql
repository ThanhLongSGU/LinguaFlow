IF COL_LENGTH('dbo.Users', 'FailedLoginAttempts') IS NULL
    ALTER TABLE dbo.Users ADD FailedLoginAttempts INT NOT NULL
        CONSTRAINT DF_Users_FailedLoginAttempts DEFAULT 0;

IF COL_LENGTH('dbo.Users', 'LockedUntil') IS NULL
    ALTER TABLE dbo.Users ADD LockedUntil DATETIME2 NULL;
