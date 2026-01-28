// AxiosUtil.ts
import axios from 'axios';
import type { AxiosInstance, Method, AxiosResponse, InternalAxiosRequestConfig } from 'axios';
import {useAuthStore} from "@/Stores/AuthStore.ts";
import {ElMessage} from "element-plus";
import {AuthApi} from "@/Api/AuthApi.ts";
import {API_SERVER} from "@/Config/ApiConfig.ts";
import {useRouter} from "vue-router";

export class AxiosUtil {
    private static instance: AxiosInstance;
    private static baseURL: string = API_SERVER.getBaseUrl();
    private static timeout: number = 1000 * 60 * 5;
    private static isRefreshing = false; // 防止多个请求同时刷新token
    private static failedQueue: Array<{
        resolve: (value?: any) => void;
        reject: (reason?: any) => void;
        config: InternalAxiosRequestConfig;
    }> = []; // 存储刷新token期间的请求

    /**
     * 初始化配置（可选，如不调用则使用默认配置）
     */
    public static init(): void {
        this.instance = axios.create({
            baseURL: this.baseURL,
            timeout: this.timeout,
        });
        this.setupInterceptors();
    }

    /**
     * 设置拦截器
     */
    private static setupInterceptors(): void {
        if (!this.instance) {
            this.init();
        }

        // 请求拦截器
        this.instance.interceptors.request.use(
            (config) => {
                const authStore = useAuthStore();
                const token = authStore.accessToken;
                if (token) {
                    config.headers.Authorization = `Bearer ${token}`;
                }
                return config;
            },
            (error) => {
                return Promise.reject(error);
            }
        );

        // 响应拦截器 - 处理HTTP错误
        this.instance.interceptors.response.use(
            (response: AxiosResponse) => {
                return response;
            },
            async (error) => {
                const originalRequest = error.config;

                // 如果是401错误且不是刷新token的请求
                if (error.response?.status === 401 && !originalRequest._retry) {

                    // 标记此请求已重试，防止死循环
                    originalRequest._retry = true;

                    // 如果没有在刷新token，则开始刷新
                    if (!this.isRefreshing) {
                        this.isRefreshing = true;

                        try {
                            // 尝试刷新token
                            const success = await this.tryRefreshToken();

                            if (success) {
                                // 刷新成功，重新发送队列中的所有请求
                                this.failedQueue.forEach(pending => {
                                    const authStore = useAuthStore();
                                    if (authStore.accessToken) {
                                        pending.config.headers.Authorization = `Bearer ${authStore.accessToken}`;
                                    }
                                    this.instance.request(pending.config).then(pending.resolve).catch(pending.reject);
                                });

                                // 清空队列
                                this.failedQueue = [];

                                // 重新发送当前请求
                                const authStore = useAuthStore();
                                if (authStore.accessToken) {
                                    originalRequest.headers.Authorization = `Bearer ${authStore.accessToken}`;
                                }
                                return this.instance(originalRequest);
                            } else {
                                // 刷新失败，跳转登录页
                                this.handleAuthError();
                                return Promise.reject(error);
                            }
                        } finally {
                            this.isRefreshing = false;
                        }
                    } else {
                        // 如果已经在刷新token，将请求加入队列
                        return new Promise((resolve, reject) => {
                            this.failedQueue.push({
                                resolve,
                                reject,
                                config: originalRequest
                            });
                        });
                    }
                }

                // 其他错误直接返回
                return Promise.reject(error);
            }
        );
    }

    private static handleAuthError(): void {
        const authStore = useAuthStore();
        authStore.clear()
        ElMessage.error("登录已过期，请重新登录");
        const router = useRouter();
        router.push('/login');
    }

    public static async tryRefreshToken(): Promise<boolean> {
        const authStore = useAuthStore();

        if (!authStore.refreshToken) {
            return false;
        }

        const refreshTokenRequest = {
            refreshToken: authStore.refreshToken
        };

        try {
            // 使用原生axios发送刷新token请求，避免拦截器循环
            const response = await axios.post(
                `${this.baseURL}/auth/refresh`,
                refreshTokenRequest,
                {
                    headers: {
                        'Content-Type': 'application/json',
                    }
                }
            );

            if (response.data.code === 200) {
                const tokenData = response.data.data;
                authStore.saveLoginData(
                    tokenData.accessToken,
                    tokenData.refreshToken || authStore.refreshToken,
                    false
                );
                return true;
            } else {
                return false;
            }
        } catch (error) {
            console.error("刷新token失败:", error);
            return false;
        }
    }

    /**
     * 获取原始实例（用于特殊需求）
     */
    public static getInstance(): AxiosInstance {
        if (!this.instance) {
            this.init();
        }
        return this.instance;
    }

    /**
     * 发送请求
     * @param url
     * @param method
     * @param params
     * @param data
     */
    public static async send(
        url: string,
        method: Method,
        params?: Map<string, any> | Object,
        data?: any
    ): Promise<any> {
        if (!this.instance) {
            this.init();
        }

        try {
            const response = await this.instance.request({
                url: url,
                method: method,
                params: params,
                data: data,
            });

            // 处理业务逻辑错误
            if (response.data.code !== 200 && response.data.code !== 431) {
                ElMessage.error(response.data.message || "请求失败");
            } else if (response.data.message !== "NO_TIP") {
                ElMessage.success(response.data.message || "操作成功");
            }

            // 如果是431错误，表示刷新token失败，跳转登录
            if (response.data.code === 431) {
                this.handleAuthError();
                return {};
            }

            return response.data.data;
        } catch (error) {
            // 这里会捕获到拦截器中未处理的错误
            console.error("请求失败:", error);
            return {};
        }
    }

    public static async get<T>(url: string, params?: Map<string, any> | Object): Promise<T> {
        return await this.send(url, "get", params);
    }

    public static async post<T>(url: string, data?: any): Promise<T> {
        return await this.send(url, "post", undefined, data);
    }

    public static async put<T>(url: string, data?: any): Promise<T> {
        return await this.send(url, "put", undefined, data);
    }

    public static async delete<T>(url: string, params?: Map<string, any> | Object): Promise<T> {
        return await this.send(url, "delete", params);
    }

    /**
     * POST FormData请求（用于文件上传）
     * @param url 请求URL
     * @param formData FormData对象
     */
    public static async postFormData<T>(url: string, formData: FormData): Promise<T> {
        return await this.send(url, "post", undefined, formData);
    }

    /**
     * PUT FormData请求（用于文件上传）
     * @param url 请求URL
     * @param formData FormData对象
     */
    public static async putFormData<T>(url: string, formData: FormData): Promise<T> {
        return await this.send(url, "put", undefined, formData);
    }
}