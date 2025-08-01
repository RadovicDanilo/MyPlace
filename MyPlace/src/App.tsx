import { Route, Routes, Link as RouterLink } from "react-router-dom";
import { useAuthStore } from "./api/authStore";
import "./index.css"
import HomePage from "./pages/HomePage";
import AuthForm from "./pages/auth/AuthForm";

function App() {
  const username = useAuthStore((state) => state.username);
  const logout = useAuthStore((state) => state.logout);

  return (
    <div className="min-h-screen bg-gray-100 text-gray-900">
      <header className="bg-blue-600 shadow-md">
        <nav className="max-w-7xl mx-auto flex items-center justify-between px-6 py-4">
          <RouterLink
            to="/"
            className="text-white text-2xl font-semibold tracking-wide hover:underline"
          >
            Pixel War
          </RouterLink>

          <div className="flex items-center space-x-6">
            {!username ? (
              <RouterLink
                to="/auth"
                className="bg-white text-blue-600 font-medium px-4 py-2 rounded hover:bg-blue-50 transition"
              >
                Login / Register
              </RouterLink>
            ) : (
              <>
                <span className="text-white font-medium">{username}</span>
                <button
                  onClick={logout}
                  className="bg-white text-blue-600 font-medium px-4 py-2 rounded hover:bg-blue-50 transition"
                >
                  Logout
                </button>
              </>
            )}
          </div>
        </nav>
      </header>

      <main className="max-w-7xl w-full p-6">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/auth" element={<AuthForm />} />
        </Routes>
      </main>
    </div>
  );

}

export default App;
