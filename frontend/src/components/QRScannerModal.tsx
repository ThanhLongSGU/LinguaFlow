import { useEffect, useRef, useState } from "react";
import { Html5Qrcode } from "html5-qrcode";

interface QRScannerModalProps {
  isOpen: boolean;
  onClose: () => void;
  onDetected: (qrCodeValue: string) => void;
}

const SCANNER_ID = "linguaflow-qr-reader";

function QRScannerModal({ isOpen, onClose, onDetected }: QRScannerModalProps) {
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
        if (!disposed) setError("Không thể truy cập camera. Hãy cấp quyền camera hoặc dùng thiết bị có camera.");
      });

    return () => {
      disposed = true;
      void stopScanner();
    };
  }, [isOpen, onDetected]);

  if (!isOpen) return null;

  return (
    <div className="qr-modal-backdrop" role="presentation">
      <section className="qr-modal" role="dialog" aria-modal="true" aria-labelledby="qr-modal-title">
        <div className="qr-modal-header">
          <div>
            <span className="map-kicker">CAMERA</span>
            <h2 id="qr-modal-title">Quét mã QR</h2>
          </div>
          <button type="button" className="qr-close-button" onClick={onClose} aria-label="Đóng">
            ×
          </button>
        </div>
        <div id={SCANNER_ID} className="qr-reader" />
        {error && <p className="qr-error" role="alert">{error}</p>}
        <p className="qr-help">Đưa mã QR vào khung hình để mở thuyết minh.</p>
        <button type="button" className="qr-cancel-button" onClick={onClose}>
          Đóng camera
        </button>
      </section>
    </div>
  );
}

export default QRScannerModal;
