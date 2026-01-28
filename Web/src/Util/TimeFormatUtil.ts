export class TimeFormatUtil {
    /**
     * 将Date对象格式化为ISO字符串（适合存储）
     * @param date 日期对象
     * @returns ISO 8601格式的字符串
     */
    static getTimeString(date: Date): string {
        return date.toISOString();
    }

    /**
     * 将存储的时间字符串转换为微信风格的展示格式
     * @param timeString getTimeString生成的字符串
     * @returns 人性化的时间展示字符串
     */
    static getTimeView(timeString: string): string {
        const now = new Date();
        const target = new Date(timeString);
        const nowTimestamp = now.getTime();
        const targetTimestamp = target.getTime();

        // 计算时间差（毫秒）
        const diff = nowTimestamp - targetTimestamp;
        const diffSeconds = Math.floor(diff / 1000);
        const diffMinutes = Math.floor(diffSeconds / 60);
        const diffHours = Math.floor(diffMinutes / 60);
        const diffDays = Math.floor(diffHours / 24);

        // 当天00:00的时间戳
        const todayStart = new Date(now);
        todayStart.setHours(0, 0, 0, 0);
        const todayStartTimestamp = todayStart.getTime();

        // 昨天00:00的时间戳
        const yesterdayStartTimestamp = todayStartTimestamp - 86400000;

        // 本周一00:00的时间戳
        const weekStart = new Date(todayStart);
        weekStart.setDate(weekStart.getDate() - weekStart.getDay() + (weekStart.getDay() === 0 ? -6 : 1));
        const weekStartTimestamp = weekStart.getTime();

        // 今年1月1日00:00的时间戳
        const yearStart = new Date(now.getFullYear(), 0, 1);
        const yearStartTimestamp = yearStart.getTime();

        // 1分钟内：刚刚
        if (diffSeconds < 60) {
            return "刚刚";
        }

        // 1小时内：X分钟前
        if (diffMinutes < 60) {
            return `${diffMinutes}分钟前`;
        }

        // 今天内：今天 HH:mm
        if (targetTimestamp >= todayStartTimestamp) {
            return `今天 ${this.formatTime(target)}`;
        }

        // 昨天：昨天 HH:mm
        if (targetTimestamp >= yesterdayStartTimestamp) {
            return `昨天 ${this.formatTime(target)}`;
        }

        // 本周内：星期X HH:mm
        if (targetTimestamp >= weekStartTimestamp) {
            const weekDays = ['日', '一', '二', '三', '四', '五', '六'];
            return `星期${weekDays[target.getDay()]} ${this.formatTime(target)}`;
        }

        // 本年内：MM月DD日 HH:mm
        if (targetTimestamp >= yearStartTimestamp) {
            return `${target.getMonth() + 1}月${target.getDate()}日 ${this.formatTime(target)}`;
        }

        // 跨年：YYYY年MM月DD日 HH:mm
        return `${target.getFullYear()}年${target.getMonth() + 1}月${target.getDate()}日 ${this.formatTime(target)}`;
    }

    /**
     * 格式化时间部分（HH:mm）
     * @param date 日期对象
     * @returns 格式化的时间字符串
     */
    private static formatTime(date: Date): string {
        return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
    }

}
