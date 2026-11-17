/*
 * Repairs Vietnamese seed content that was persisted with an incorrect
 * character encoding before the application switched to UTF-8 consistently.
 */

UPDATE dbo.languages
SET name = N'Tiếng Việt'
WHERE code = 'vi';

UPDATE dbo.categories
SET name = N'Địa danh lịch sử',
    description = N'Các địa danh lịch sử và văn hóa tại TP. Hồ Chí Minh'
WHERE category_id = 2;

UPDATE dbo.tourist_sites
SET name = N'Dinh Độc Lập',
    description = N'Dinh Độc Lập là di tích lịch sử đặc biệt, gắn liền với những dấu mốc quan trọng của Việt Nam hiện đại.',
    address = N'135 Nam Kỳ Khởi Nghĩa, Quận 1, TP. Hồ Chí Minh',
    city = N'TP. Hồ Chí Minh'
WHERE site_id = 2;

UPDATE dbo.tourist_sites
SET name = N'Nhà thờ Đức Bà Sài Gòn',
    description = N'Nhà thờ Đức Bà là công trình kiến trúc nổi bật giữa trung tâm Thành phố Hồ Chí Minh.',
    address = N'Công trường Công xã Paris, Quận 1, TP. Hồ Chí Minh',
    city = N'TP. Hồ Chí Minh'
WHERE site_id = 3;

UPDATE dbo.tourist_sites
SET name = N'Bưu điện Trung tâm Sài Gòn',
    description = N'Bưu điện Trung tâm Sài Gòn nổi bật với kiến trúc cổ điển và là một điểm tham quan quen thuộc.',
    address = N'Số 2 Công trường Công xã Paris, Quận 1, TP. Hồ Chí Minh',
    city = N'TP. Hồ Chí Minh'
WHERE site_id = 4;

UPDATE dbo.tourist_sites
SET name = N'Bảo tàng Chứng tích Chiến tranh',
    description = N'Bảo tàng Chứng tích Chiến tranh lưu giữ nhiều tư liệu về lịch sử và hậu quả của các cuộc chiến tranh.',
    address = N'28 Võ Văn Tần, Quận 3, TP. Hồ Chí Minh',
    city = N'TP. Hồ Chí Minh'
WHERE site_id = 5;

UPDATE translation
SET content = N'Dinh Độc Lập là di tích lịch sử đặc biệt, gắn liền với những dấu mốc quan trọng của Việt Nam hiện đại.'
FROM dbo.translations translation
JOIN dbo.narrations narration ON narration.narration_id = translation.narration_id
JOIN dbo.languages language ON language.language_id = translation.language_id
WHERE narration.site_id = 2
  AND language.code = 'vi';

UPDATE translation
SET content = N'Nhà thờ Đức Bà là công trình kiến trúc nổi bật giữa trung tâm Thành phố Hồ Chí Minh.'
FROM dbo.translations translation
JOIN dbo.narrations narration ON narration.narration_id = translation.narration_id
JOIN dbo.languages language ON language.language_id = translation.language_id
WHERE narration.site_id = 3
  AND language.code = 'vi';

UPDATE translation
SET content = N'Bưu điện Trung tâm Sài Gòn nổi bật với kiến trúc cổ điển và là một điểm tham quan quen thuộc.'
FROM dbo.translations translation
JOIN dbo.narrations narration ON narration.narration_id = translation.narration_id
JOIN dbo.languages language ON language.language_id = translation.language_id
WHERE narration.site_id = 4
  AND language.code = 'vi';

UPDATE translation
SET content = N'Bảo tàng Chứng tích Chiến tranh lưu giữ nhiều tư liệu về lịch sử và hậu quả của các cuộc chiến tranh.'
FROM dbo.translations translation
JOIN dbo.narrations narration ON narration.narration_id = translation.narration_id
JOIN dbo.languages language ON language.language_id = translation.language_id
WHERE narration.site_id = 5
  AND language.code = 'vi';

UPDATE narration
SET title = site.name,
    original_content = site.description
FROM dbo.narrations narration
JOIN dbo.tourist_sites site ON site.site_id = narration.site_id
WHERE site.site_id IN (2, 3, 4, 5);
