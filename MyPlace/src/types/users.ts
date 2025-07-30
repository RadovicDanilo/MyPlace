import { z } from "zod";

export const authSchema = z.object({
    username: z.string()
        .min(3, "Username must be at least 3 characters")
        .max(32, "Password cannot exceed 32 characters"),
    password: z
        .string()
        .min(8, "Password must be at least 8 characters")
        .max(128, "Password cannot exceed 128 characters"),
});

export type AuthModel = z.infer<typeof authSchema>;
