import { Routes, Route } from "react-router-dom";
import { PrivateRoute } from "./PrivateRoute";
import TournamentPage from "@/pages/tournament/TournamentPage";
import TournamentPageMock from "@/pages/tournament/TournamentPageMock";
import LoginPage from "@/pages/auth/LoginPage";
import RegisterPage from "@/pages/auth/RegisterPage";
import ConfirmPage from "@/pages/auth/ConfirmPage";
import ForgotPage from "@/pages/auth/ForgotPage";
import ResetPage from "@/pages/auth/ResetPage";
import PlayerPage from "@/pages/participant/PlayerPage";
import OrganizationPage from "@/pages/participant/OrganizationPage";
import ProfileEditPage from "@/pages/profile/ProfileEditPage";
import SelfProfilePage from "@/pages/profile/SelfProfilePage";
import HomePage from "@/pages/HomePage";
import TournamentManagePage from "@/pages/tournament/TournamentManagePage";
import CreateTournamentPage from "@/pages/tournament/CreateTournamentPage";
import CreateTeamPage from "@/pages/team/CreateTeamPage";
import TeamsPage from "@/pages/team/TeamsPage";
import TeamPage from "@/pages/team/TeamPage";
import MatchPage from "@/pages/match/MatchPage";
import MatchManagePage from "@/pages/match/MatchManagePage";
import FaqPage from "@/pages/FaqPage";

export const AppRouter = () => (
  <Routes>
    <Route path="/login" element={<LoginPage/>}/>
    <Route path="/register" element={<RegisterPage/>}/>
    <Route path="/confirm" element={<ConfirmPage/>}/>
    <Route path="/forgot" element={<ForgotPage/>}/>
    <Route path="/reset" element={<ResetPage/>}/>
    <Route path="/" element={<HomePage />} />
    <Route path="/teams" element={<TeamsPage />} />
    <Route path="/teams/:id" element={<TeamPage />} />
    <Route path="/create-team" element={<PrivateRoute><CreateTeamPage /></PrivateRoute>} />
    <Route path="/tournaments/:id" element={<TournamentPage />} />
    <Route path="/tournaments/create" element={<PrivateRoute><CreateTournamentPage /></PrivateRoute>} />
    <Route path="/tournaments/:id/manage" element={<PrivateRoute><TournamentManagePage /></PrivateRoute>} />
    <Route path="/matches/:id" element={<MatchPage />} />
    <Route path="/matches/:id/manage" element={<PrivateRoute><MatchManagePage /></PrivateRoute>} />
    <Route path="/faq" element={<FaqPage />} />
    <Route path="/tournament-mock" element={<TournamentPageMock />} />
    <Route path="/players/:id" element={<PlayerPage />} />
    <Route path="/organizations/:id" element={<OrganizationPage />} />
    <Route path="/profile" element={<PrivateRoute><SelfProfilePage /></PrivateRoute>} />
    <Route path="/profile/edit" element={<PrivateRoute><ProfileEditPage /></PrivateRoute>} />
    <Route path="/player-mock" element={<PlayerPage />} />
  </Routes>
);
