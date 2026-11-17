import { Navigate, Route, Routes } from "react-router-dom";
import MapPage from "./pages/MapPage";
import VisitorPage from "./pages/visitor/VisitorPage";
import { LanguageProvider } from "./contexts/LanguageContext";
import "./App.css";

function App() {
  return (
    <LanguageProvider>
      <Routes>
        <Route path="/visitor/:qrCodeValue" element={<VisitorPage />} />
        <Route path="/map" element={<MapPage />} />
        <Route path="*" element={<Navigate to="/visitor/SAMPLE-QR-123" replace />} />
      </Routes>
    </LanguageProvider>
  );
}

export default App;
