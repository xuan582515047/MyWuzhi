export interface CaptchaRequest {
    captcha: string;
}

export interface LoginRequest {
    phone: string;
    password: string;
}

export interface LoginResponse {
    accessToken: string;
    refreshToken: string;
    name: string;
    avatar: string;
}

export interface RefreshTokenRequest {
    refreshToken: string;
}

export interface RefreshTokenResponse {
    accessToken: string;
}

export interface RegisterRequest {
    phone: string;
    password: string;
    captcha: string;
}