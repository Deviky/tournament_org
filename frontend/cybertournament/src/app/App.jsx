import { useEffect } from "react";
import { BrowserRouter } from "react-router-dom";
import { AppRouter } from "./router/AppRouter";
import { useAuthStore } from "@/app/store/authStore";
import SiteFooter from "@/shared/ui/SiteFooter";

export default function App() {
  const initAuth = useAuthStore((s) => s.initAuth);

  useEffect(() => {
    initAuth();
    document.title = "CyberTournamentZ";
  }, []);

  return (
    <BrowserRouter>
      <div className="app-shell">
        <div className="app-content">
          <AppRouter />
        </div>
        <SiteFooter />
      </div>
    </BrowserRouter>
  );
}
