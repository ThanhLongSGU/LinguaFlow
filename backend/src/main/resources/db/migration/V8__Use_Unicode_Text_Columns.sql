/*
 * Converts legacy VARCHAR text columns to NVARCHAR before restoring Vietnamese
 * content. VARCHAR cannot represent the required Unicode characters reliably.
 */

ALTER TABLE dbo.languages ALTER COLUMN name NVARCHAR(50) NOT NULL;
ALTER TABLE dbo.categories ALTER COLUMN name NVARCHAR(100) NOT NULL;
ALTER TABLE dbo.tourist_sites ALTER COLUMN name NVARCHAR(200) NOT NULL;
ALTER TABLE dbo.tourist_sites ALTER COLUMN address NVARCHAR(255) NULL;
ALTER TABLE dbo.tourist_sites ALTER COLUMN city NVARCHAR(100) NULL;
ALTER TABLE dbo.narrations ALTER COLUMN title NVARCHAR(200) NOT NULL;

UPDATE dbo.languages
SET name = N'Tiếng Việt'
WHERE code = 'vi';

UPDATE dbo.categories
SET name = N'Địa danh lịch sử'
WHERE category_id = 2;

UPDATE dbo.tourist_sites
SET name = N'Dinh Độc Lập',
    address = N'135 Nam Kỳ Khởi Nghĩa, Quận 1, TP. Hồ Chí Minh',
    city = N'TP. Hồ Chí Minh'
WHERE site_id = 2;

UPDATE dbo.tourist_sites
SET name = N'Nhà thờ Đức Bà Sài Gòn',
    address = N'Công trường Công xã Paris, Quận 1, TP. Hồ Chí Minh',
    city = N'TP. Hồ Chí Minh'
WHERE site_id = 3;

UPDATE dbo.tourist_sites
SET name = N'Bưu điện Trung tâm Sài Gòn',
    address = N'Số 2 Công trường Công xã Paris, Quận 1, TP. Hồ Chí Minh',
    city = N'TP. Hồ Chí Minh'
WHERE site_id = 4;

UPDATE dbo.tourist_sites
SET name = N'Bảo tàng Chứng tích Chiến tranh',
    address = N'28 Võ Văn Tần, Quận 3, TP. Hồ Chí Minh',
    city = N'TP. Hồ Chí Minh'
WHERE site_id = 5;

UPDATE narration
SET title = site.name
FROM dbo.narrations narration
JOIN dbo.tourist_sites site ON site.site_id = narration.site_id
WHERE site.site_id IN (2, 3, 4, 5);
