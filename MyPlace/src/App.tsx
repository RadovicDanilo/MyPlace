import { Route, Routes, Link as RouterLink } from "react-router-dom";
import { useAuthStore } from "./api/authStore";
import "./index.css"
import HomePage from "./pages/HomePage";
import AuthForm from "./pages/auth/AuthForm";

function App() {
  const username = useAuthStore((state) => state.username);
  const logout = useAuthStore((state) => state.logout);

  return (
    <div className="min-h-screen bg-gray-100">
      <header className="bg-blue-600 shadow">
        <nav className="container mx-auto flex items-center justify-between px-4 py-3">
          <div className="flex items-center space-x-4">
            <RouterLink
              to="/"
              className="text-white text-xl font-bold no-underline hover:underline"
            >
              Pixel War
            </RouterLink>
          </div>
          <div className="flex items-center space-x-4">
            {!username ? (
              <RouterLink
                to="/auth"
                className="text-white hover:bg-blue-700 px-4 py-2 rounded transition"
              >
                Login/Register
              </RouterLink>
            ) : (
              <>
                <span className="text-white">{username}</span>
                <button
                  onClick={logout}
                  className="text-white hover:bg-blue-700 px-4 py-2 rounded transition"
                >
                  Logout
                </button>
              </>
            )}
          </div>
        </nav>
      </header>

      <main className="container mx-auto px-4 py-6">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/auth" element={<AuthForm />} />
        </Routes>
      </main>
    </div>
  );
}

export default App;
