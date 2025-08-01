import { Route, Routes, Link as RouterLink, useLocation } from "react-router-dom";
import { useAuthStore } from "./api/authStore";
import HomePage from "./pages/HomePage";
import AuthForm from "./pages/auth/AuthForm";
import "./index.css"

function App() {
  const { username, logout } = useAuthStore();
  const location = useLocation();

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <header className="bg-blue-600 shadow-sm">
        <nav className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            <RouterLink
              to="/"
              className="flex items-center text-white text-xl font-bold hover:opacity-90 transition-opacity"
            >
              <span className="hidden sm:inline">Pixel War</span>
              <span className="sm:hidden">PW</span>
            </RouterLink>

            <div className="flex items-center space-x-4">
              {username ? (
                <>
                  <span className="hidden md:inline text-white font-medium">
                    Welcome, {username}
                  </span>
                  <button
                    onClick={logout}
                    className="bg-white text-blue-600 font-medium px-4 py-2 rounded-lg hover:bg-blue-50 transition-colors"
                  >
                    Logout
                  </button>
                </>
              ) : (
                <RouterLink
                  to="/auth"
                  state={{ from: location }}
                  className="bg-white text-blue-600 font-medium px-4 py-2 rounded-lg hover:bg-blue-50 transition-colors"
                >
                  Sign In
                </RouterLink>
              )}
            </div>
          </div>
        </nav>
      </header>

      <main className="flex-1">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/auth" element={<AuthForm />} />
        </Routes>
      </main>

      <footer className="bg-white border-t py-4">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center text-gray-500 text-sm">
          © {new Date().getFullYear()} Pixel War. All rights reserved.
        </div>
      </footer>
    </div>
  );
}

export default App;