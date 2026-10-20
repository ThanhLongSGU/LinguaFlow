import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { getTouristSiteByQr, resolveAssetUrl, type TouristSiteDetail, type TranslationItem } from "../../services/api";

function VisitorPage() {
  const { qrCodeValue = "" } = useParams();
  const [site, setSite] = useState<TouristSiteDetail | null>(null);
  const [selectedTranslation, setSelectedTranslation] = useState<TranslationItem | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const controller = new AbortController();

    setLoading(true);
    setError(null);
    setSite(null);
    setSelectedTranslation(null);

    getTouristSiteByQr(qrCodeValue, controller.signal)
      .then((result) => {
        setSite(result);
        setSelectedTranslation(
          result.availableTranslations.find((item) => item.isDefault) ?? result.availableTranslations[0] ?? null,
        );
      })
      .catch((requestError: unknown) => {
        if (requestError instanceof DOMException && requestError.name === "AbortError") {
          return;
        }
        setError(requestError instanceof Error ? requestError.message : "Đã xảy ra lỗi khi tải dữ liệu.");
      })
      .finally(() => {
        if (!controller.signal.aborted) setLoading(false);
      });

    return () => controller.abort();
  }, [qrCodeValue]);

  if (loading) {
    return (
      <main className="visitor-shell" aria-busy="true">
        <div className="loading-view">
          <span className="loading-mark" aria-hidden="true" />
          <p>Đang tải thông tin điểm đến...</p>
        </div>
      </main>
    );
  }

  if (error || !site) {
    return (
      <main className="visitor-shell">
        <header className="visitor-topbar">
          <span className="brand-mark" aria-hidden="true">
            L
          </span>
          <span className="brand-name">LinguaFlow</span>
        </header>
        <section className="message-view" role="alert">
          <span className="message-code">QR / NOT FOUND</span>
          <h1>Không tìm thấy điểm đến</h1>
          <p>{error ?? "Mã QR không hợp lệ hoặc địa điểm hiện không khả dụng."}</p>
        </section>
      </main>
    );
  }

  return (
    <main className="visitor-shell">
      <header className="visitor-topbar">
        <span className="brand-mark" aria-hidden="true">
          L
        </span>
        <span className="brand-name">LinguaFlow</span>
        <span className="topbar-note">AUDIO GUIDE</span>
      </header>

      <article className="site-page">
        <section className="site-hero">
          {site.imageUrl ? (
            <img className="site-image" src={resolveAssetUrl(site.imageUrl)} alt={site.name} />
          ) : (
            <div className="site-image-placeholder" aria-hidden="true">
              <span>LF</span>
            </div>
          )}
          <div className="hero-shade" />
          <div className="hero-copy">
            <span className="eyebrow">ĐIỂM ĐẾN · {site.siteId}</span>
            <h1>{site.name}</h1>
            {site.address && <p className="site-address">{site.address}</p>}
          </div>
        </section>

        <section className="guide-content">
          <div className="description-column">
            <span className="section-label">GIỚI THIỆU</span>
            <p className="site-description">{site.description || "Chưa có nội dung giới thiệu cho địa điểm này."}</p>
          </div>

          <aside className="audio-column" aria-label="Thuyết minh âm thanh">
            <span className="section-label">NGHE THUYẾT MINH</span>
            {site.availableTranslations.length > 0 ? (
              <>
                <div className="language-list" role="group" aria-label="Chọn ngôn ngữ">
                  {site.availableTranslations.map((translation) => (
                    <button
                      className={`language-button${selectedTranslation?.languageCode === translation.languageCode ? " is-selected" : ""}`}
                      key={translation.languageCode}
                      type="button"
                      aria-pressed={selectedTranslation?.languageCode === translation.languageCode}
                      onClick={() => setSelectedTranslation(translation)}
                    >
                      <span>{translation.languageName}</span>
                      <span className="language-code">{translation.languageCode.toUpperCase()}</span>
                    </button>
                  ))}
                </div>
                {selectedTranslation && (
                  <div className="audio-player-wrap">
                    <p className="translation-content">{selectedTranslation.content}</p>
                    {selectedTranslation.audioUrl ? (
                      <audio controls preload="none" src={resolveAssetUrl(selectedTranslation.audioUrl)}>
                        Trình duyệt của bạn không hỗ trợ phát audio.
                      </audio>
                    ) : (
                      <p className="audio-unavailable">Audio chưa sẵn sàng.</p>
                    )}
                  </div>
                )}
              </>
            ) : (
              <p className="audio-unavailable">Chưa có bản thuyết minh cho địa điểm này.</p>
            )}
          </aside>
        </section>
      </article>

      <footer className="visitor-footer">
        <span>LINGUAFLOW AUDIO GUIDE</span>
        <span>#{site.siteId.toString().padStart(4, "0")}</span>
      </footer>
    </main>
  );
}

export default VisitorPage;
