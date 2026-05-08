import { useNavigate } from "react-router-dom";

export default function SiteFooter() {
  const navigate = useNavigate();

  return (
    <footer className="site-footer">
      <div className="site-footer__inner">
        <div className="site-footer__brand">
          <div className="site-footer__logo">CyberTournamentZ</div>
          <p>
            Платформа для создания команд, управления турнирами и проведения матчей
            в одном месте.
          </p>
        </div>

        <div className="site-footer__links">
          <button className="site-footer__link" onClick={() => navigate("/")}>
            Главная
          </button>
          <button className="site-footer__link" onClick={() => navigate("/teams")}>
            Команды
          </button>
          <button className="site-footer__link" onClick={() => navigate("/faq")}>
            FAQ
          </button>
        </div>
      </div>
    </footer>
  );
}
