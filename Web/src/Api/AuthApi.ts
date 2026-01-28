import {AxiosUtil} from "@/Util/AxiosUtil.ts";
import type {
    CaptchaRequest,
    LoginRequest,
    LoginResponse,
    RefreshTokenRequest, RefreshTokenResponse,
    RegisterRequest
} from "@/Entity/AuthEntity.ts";


const PREFIX = "/auth"
export class AuthApi{

    // 登录
    public static async login(loginRequest: LoginRequest): Promise<LoginResponse>{
        return await AxiosUtil.post(PREFIX+"/login", loginRequest);
    }

    // 注册
    public static async register(registerRequest: RegisterRequest){
        return await AxiosUtil.post(PREFIX+"/register", registerRequest);
    }

    // 发送验证码
    public static async sendCaptcha(captchaRequest: CaptchaRequest){
        return AxiosUtil.post(PREFIX+"/captcha", captchaRequest);
    }
}