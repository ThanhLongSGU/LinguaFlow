/*
 * Aligns persisted language codes with API_Contracts and SRS locale codes.
 * Existing short codes remain readable through the client fallback logic.
 */

UPDATE dbo.languages
SET code = 'vi-VN'
WHERE code = 'vi';

UPDATE dbo.languages
SET code = 'en-US'
WHERE code = 'en';

IF NOT EXISTS (SELECT 1 FROM dbo.languages WHERE code = 'zh-CN')
    INSERT INTO dbo.languages (code, name, is_active)
    VALUES ('zh-CN', N'中文', 1);

IF NOT EXISTS (SELECT 1 FROM dbo.languages WHERE code = 'ja-JP')
    INSERT INTO dbo.languages (code, name, is_active)
    VALUES ('ja-JP', N'日本語', 1);

IF NOT EXISTS (SELECT 1 FROM dbo.languages WHERE code = 'hi-IN')
    INSERT INTO dbo.languages (code, name, is_active)
    VALUES ('hi-IN', N'हिन्दी', 1);

IF NOT EXISTS (SELECT 1 FROM dbo.languages WHERE code = 'fr-FR')
    INSERT INTO dbo.languages (code, name, is_active)
    VALUES ('fr-FR', N'Français', 1);

IF NOT EXISTS (SELECT 1 FROM dbo.languages WHERE code = 'ar')
    INSERT INTO dbo.languages (code, name, is_active)
    VALUES ('ar', N'العربية', 1);
