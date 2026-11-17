import { useEffect, useRef, useState } from "react";
import { Html5Qrcode } from "html5-qrcode";
import { useLanguage } from "../contexts/LanguageContext";

interface QRScannerModalProps {
  isOpen: boolean;
  onClose: () => void;
  onDetected: (qrCodeValue: string) => void;
}

const SCANNER_ID = "linguaflow-qr-reader";

function QRScannerModal({ isOpen, onClose, onDetected }: QRScannerModalProps) {
  const { t } = useLanguage();
  const scannerRef = useRef<Html5Qrcode | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!isOpen) return;

    let disposed = false;
    const scanner = new Html5Qrcode(SCANNER_ID);
    scannerRef.current = scanner;
    setError(null);

    const stopScanner = async () => {
      try {
        if (scanner.isScanning) await scanner.stop();
        scanner.clear();
      } catch {
        // The camera may already be stopped by the browser or a successful scan.
      } finally {
        if (scannerRef.current === scanner) scannerRef.current = null;
      }
    };

    void scanner
      .start(
        { facingMode: "environment" },
        { fps: 10, qrbox: { width: 250, height: 250 } },
        async (decodedText) => {
          if (disposed) return;
          disposed = true;
          await stopScanner();
          onDetected(decodedText);
        },
        () => undefined,
      )
      .catch(() => {
        if (!disposed) setError(t("qr", "cameraError"));
      });

    return () => {
      disposed = true;
      void stopScanner();
    };
  }, [isOpen, onDetected, t]);

  if (!isOpen) return null;

  return (
    <div className="qr-modal-backdrop" role="presentation">
      <section className="qr-modal" role="dialog" aria-modal="true" aria-labelledby="qr-modal-title">
        <div className="qr-modal-header">
          <div>
            <span className="map-kicker">{t("qr", "camera")}</span>
            <h2 id="qr-modal-title">{t("qr", "title")}</h2>
          </div>
          <button type="button" className="qr-close-button" onClick={onClose} aria-label={t("qr", "close")}>
            ×
          </button>
        </div>
        <div id={SCANNER_ID} className="qr-reader" />
        {error && <p className="qr-error" role="alert">{error}</p>}
        <p className="qr-help">{t("qr", "help")}</p>
        <button type="button" className="qr-cancel-button" onClick={onClose}>
          {t("qr", "closeCamera")}
        </button>
      </section>
    </div>
  );
}

export default QRScannerModal;
