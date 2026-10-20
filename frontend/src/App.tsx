import { Navigate, Route, Routes } from "react-router-dom";
import VisitorPage from "./pages/visitor/VisitorPage";
import "./App.css";

function App() {
  return (
    <Routes>
      <Route path="/visitor/:qrCodeValue" element={<VisitorPage />} />
      <Route path="*" element={<Navigate to="/visitor/SAMPLE-QR-123" replace />} />
    </Routes>
  );
}

export default App;
