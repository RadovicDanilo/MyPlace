import axios, { AxiosError } from "axios";
import { toast } from "react-toastify";
import { useAuthStore } from "./authStore";

const API_BASE_URL = "http://localhost:8081/api";

const api = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true, 
});

api.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token;
  if (token) {
    config.headers?.set('Authorization', `Bearer ${token}`);
  }
  return config;
});

const getErrorMessage = (code: string | undefined): string => {
  switch (code) {
    // TODO: add err codes
    default:
      return "An error occurred.";
  }
};

api.interceptors.response.use(
  (response) => response,
  (error: AxiosError<any>) => {
    const code = error.response?.data?.code;
    toast.error(getErrorMessage(code));
    return Promise.reject(error);
  }
);

export default api;
