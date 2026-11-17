import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { getTouristSiteByQr, resolveAssetUrl, type TouristSiteDetail, type TranslationItem } from "../../services/api";
import { useLanguage } from "../../contexts/LanguageContext";
import LanguageSelector from "../../components/LanguageSelector";
import { languageMatches } from "../../i18n/translations";

function VisitorPage() {
  const { narrationLanguage, t } = useLanguage();
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
          result.translations.find((item) => languageMatches(item.languageCode, narrationLanguage)) ??
            result.translations.find((item) => languageMatches(item.languageCode, "vi-VN")) ??
            result.translations[0] ??
            null,
        );
      })
      .catch((requestError: unknown) => {
        if (requestError instanceof DOMException && requestError.name === "AbortError") {
          return;
        }
        setError(requestError instanceof Error ? requestError.message : t("visitor", "loadError"));
      })
      .finally(() => {
        if (!controller.signal.aborted) setLoading(false);
      });

    return () => controller.abort();
  }, [qrCodeValue, narrationLanguage]);

  useEffect(() => {
    if (!site) return;
    setSelectedTranslation(
        site.translations.find((item) => languageMatches(item.languageCode, narrationLanguage)) ??
        site.translations.find((item) => languageMatches(item.languageCode, "vi-VN")) ??
        site.translations[0] ??
        null,
    );
  }, [narrationLanguage, site]);

  if (loading) {
    return (
      <main className="visitor-shell" aria-busy="true">
        <div className="loading-view">
          <span className="loading-mark" aria-hidden="true" />
          <p>{t("visitor", "loading")}</p>
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
          <h1>{t("visitor", "notFound")}</h1>
          <p>{error ?? t("visitor", "invalidQr")}</p>
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
        <Link className="map-nav-link" to="/map">
          {t("visitor", "mapLink")}
        </Link>
        <LanguageSelector />
        <span className="topbar-note">{t("visitor", "audioGuide")}</span>
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
            <span className="eyebrow">{t("visitor", "destination")} · {site.siteId}</span>
            <h1>{site.name}</h1>
            {site.address && <p className="site-address">{site.address}</p>}
          </div>
        </section>

        <section className="guide-content">
          <div className="description-column">
            <span className="section-label">{t("visitor", "introduction")}</span>
            <p className="site-description">{site.description || t("visitor", "noDescription")}</p>
          </div>

          <aside className="audio-column" aria-label={t("visitor", "listenNarration")}>
            <span className="section-label">{t("visitor", "listenNarration")}</span>
            {site.translations.length > 0 ? (
              <>
                <div className="language-list" role="group" aria-label={t("visitor", "chooseLanguage")}>
                  {site.translations.map((translation) => (
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
                        {t("visitor", "browserAudioUnsupported")}
                      </audio>
                    ) : (
                      <p className="audio-unavailable">{t("visitor", "audioUnavailable")}</p>
                    )}
                  </div>
                )}
              </>
            ) : (
              <p className="audio-unavailable">{t("visitor", "noNarration")}</p>
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
