import { create } from "zustand";
import axios from "./axiosConfig";
import type { AuthModel } from "../types/users";

interface AuthStore {
    username: string;
    token: string;
    register: (register: AuthModel) => Promise<void>;
    login: (login: AuthModel) => Promise<void>;
    logout: () => Promise<void>;
}

export const useAuthStore = create<AuthStore>()((set) => ({
    username: localStorage.getItem("username") || "",
    token: localStorage.getItem("token") || "",

    register: async (register) => {
        await axios.post(`/user/register`, register);
    },

    login: async (login) => {
        const res = await axios.post(`/user/login`, login);

        localStorage.setItem("username", login.username);
        localStorage.setItem("token", res.data.token);

        set({ username: login.username, token: res.data.token })
    },

    logout: async () => {
        set({ username: "", token: "" })
        localStorage.removeItem("username")
        localStorage.removeItem("token")
    },
}));
