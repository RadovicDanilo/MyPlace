import { useState } from "react";
import { useAuthStore } from "../../api/authStore";
import { useNavigate } from "react-router-dom";

const AuthForm = () => {
  const [isLogin, setIsLogin] = useState(true);

  const [form, setForm] = useState({ username: "", password: "" });

  const login = useAuthStore((state) => state.login);
  const register = useAuthStore((state) => state.register);
  const username = useAuthStore((state) => state.username);

  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (isLogin) {
      await login(form);
    } else {
      await register(form);
    }
    navigate("/")
  };

  if (username) {
    navigate("/");
    return;
  }

  return (
    <div className="max-w-sm mx-auto p-6 bg-white rounded-lg shadow-md">
      <div className="max-w-md mx-auto bg-white p-6 rounded-lg shadow-md">
        <h2 className="text-2xl font-semibold text-center mb-6">Register</h2>
        <form onSubmit={handleSubmit} className="space-y-4">
          <input
            type="text"
            placeholder="Username"
            value={form.username}
            onChange={(e) => setForm({ ...form, username: e.target.value })}
            className="w-full p-3 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          <input
            type="password"
            placeholder="Password"
            value={form.password}
            onChange={(e) => setForm({ ...form, password: e.target.value })}
            className="w-full p-3 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          <button
            type="submit"
            className="w-full bg-blue-500 text-white p-3 rounded-md hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            {isLogin ? "Login" : "Register"}
          </button>
        </form>
      </div>

      <p
        className="text-blue-600 underline text-center mt-4 cursor-pointer"
        onClick={() => setIsLogin(!isLogin)}
      >
        {isLogin
          ? "Don't have an account? Register"
          : "Already have an account? Login"}
      </p>
    </div>
  );
};

export default AuthForm;
