CREATE TABLE Categories (
    CategoryId INT IDENTITY(1,1) NOT NULL,
    Name NVARCHAR(100) NOT NULL,
    Description NVARCHAR(MAX) NULL,
    CONSTRAINT PK_Categories PRIMARY KEY (CategoryId)
);

CREATE TABLE Languages (
    LanguageId INT IDENTITY(1,1) NOT NULL,
    Code VARCHAR(10) NOT NULL,
    Name NVARCHAR(50) NOT NULL,
    IsActive BIT NOT NULL,
    CONSTRAINT PK_Languages PRIMARY KEY (LanguageId),
    CONSTRAINT UQ_Languages_Code UNIQUE (Code)
);

CREATE TABLE Users (
    UserId INT IDENTITY(1,1) NOT NULL,
    Username NVARCHAR(50) NOT NULL,
    PasswordHash NVARCHAR(255) NOT NULL,
    FullName NVARCHAR(100) NOT NULL,
    Role NVARCHAR(20) NOT NULL,
    IsActive BIT NOT NULL,
    CONSTRAINT PK_Users PRIMARY KEY (UserId),
    CONSTRAINT UQ_Users_Username UNIQUE (Username)
);

CREATE TABLE TouristSites (
    SiteId INT IDENTITY(1,1) NOT NULL,
    CategoryId INT NOT NULL,
    CreatedBy INT NOT NULL,
    Name NVARCHAR(200) NOT NULL,
    Description NVARCHAR(MAX) NULL,
    Address NVARCHAR(255) NULL,
    ImageUrl NVARCHAR(500) NULL,
    ReferenceTicketPrice DECIMAL(12,2) NULL,
    Status BIT NOT NULL,
    CONSTRAINT PK_TouristSites PRIMARY KEY (SiteId),
    CONSTRAINT FK_TouristSites_Categories FOREIGN KEY (CategoryId)
        REFERENCES Categories (CategoryId),
    CONSTRAINT FK_TouristSites_Users FOREIGN KEY (CreatedBy)
        REFERENCES Users (UserId)
);

CREATE TABLE QRCodes (
    QrId INT IDENTITY(1,1) NOT NULL,
    SiteId INT NOT NULL,
    QrCodeValue NVARCHAR(500) NOT NULL,
    IsActive BIT NOT NULL,
    CreatedAt DATETIME2 NOT NULL,
    CONSTRAINT PK_QRCodes PRIMARY KEY (QrId),
    CONSTRAINT UQ_QRCodes_QrCodeValue UNIQUE (QrCodeValue),
    CONSTRAINT FK_QRCodes_TouristSites FOREIGN KEY (SiteId)
        REFERENCES TouristSites (SiteId)
);

CREATE NONCLUSTERED INDEX IX_QRCodes_QrCodeValue
    ON QRCodes (QrCodeValue);

CREATE TABLE QRScanLogs (
    ScanId BIGINT IDENTITY(1,1) NOT NULL,
    QrId INT NOT NULL,
    ScannedAt DATETIME2 NOT NULL,
    CONSTRAINT PK_QRScanLogs PRIMARY KEY (ScanId),
    CONSTRAINT FK_QRScanLogs_QRCodes FOREIGN KEY (QrId)
        REFERENCES QRCodes (QrId)
);

CREATE TABLE Narrations (
    NarrationId INT IDENTITY(1,1) NOT NULL,
    SiteId INT NOT NULL,
    Title NVARCHAR(200) NOT NULL,
    OriginalContent NVARCHAR(MAX) NULL,
    ContentHash VARCHAR(64) NULL,
    CreatedAt DATETIME2 NOT NULL,
    CONSTRAINT PK_Narrations PRIMARY KEY (NarrationId),
    CONSTRAINT FK_Narrations_TouristSites FOREIGN KEY (SiteId)
        REFERENCES TouristSites (SiteId)
);

CREATE TABLE Translations (
    TranslationId INT IDENTITY(1,1) NOT NULL,
    NarrationId INT NOT NULL,
    LanguageId INT NOT NULL,
    Content NVARCHAR(MAX) NULL,
    AudioUrl NVARCHAR(500) NULL,
    CONSTRAINT PK_Translations PRIMARY KEY (TranslationId),
    CONSTRAINT UQ_Translations_Narration_Language UNIQUE (NarrationId, LanguageId),
    CONSTRAINT FK_Translations_Narrations FOREIGN KEY (NarrationId)
        REFERENCES Narrations (NarrationId),
    CONSTRAINT FK_Translations_Languages FOREIGN KEY (LanguageId)
        REFERENCES Languages (LanguageId)
);

CREATE TABLE AiTranslationTasks (
    TaskId BIGINT IDENTITY(1,1) NOT NULL,
    NarrationId INT NOT NULL,
    LanguageId INT NOT NULL,
    Status VARCHAR(20) NOT NULL,
    ErrorMessage NVARCHAR(MAX) NULL,
    RetryCount INT NOT NULL,
    CreatedAt DATETIME2 NOT NULL,
    UpdatedAt DATETIME2 NULL,
    CONSTRAINT PK_AiTranslationTasks PRIMARY KEY (TaskId),
    CONSTRAINT FK_AiTranslationTasks_Narrations FOREIGN KEY (NarrationId)
        REFERENCES Narrations (NarrationId),
    CONSTRAINT FK_AiTranslationTasks_Languages FOREIGN KEY (LanguageId)
        REFERENCES Languages (LanguageId)
);