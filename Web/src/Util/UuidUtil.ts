// utils/uuid.ts
import { v4 as uuidv4, v1 as uuidv1, validate, version } from 'uuid';
export class UuidUtil {
    // 生成 32 位 UUID v4（随机生成），符合 Mybatis-plus 的 UUID 生成规则
    static randomUUID32 = (): string => {
        return uuidv4().replace(/-/g, '');
    };

    // 生成 32 位 UUID v4（随机生成）
    static randomUUID36 = (): string => {
        return uuidv4();
    };

    // 生成 UUID v1（基于时间戳）
    static TimestampUUID = (): string => {
        return uuidv1();
    };

    // 验证 UUID
    static isValidUUID = (uuid: string): boolean => {
        return validate(uuid);
    };

    // 获取 UUID 版本
    static getUUIDVersion = (uuid: string): number => {
        return version(uuid);
    };
}


