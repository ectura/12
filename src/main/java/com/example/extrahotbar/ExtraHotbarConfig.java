package com.example.extrahotbar;

/**
 * 硬编码配置类
 */
public class ExtraHotbarConfig {

    // 热键栏位置枚举 - 移动到外部，作为ExtraHotbarConfig的静态内部类
    public enum HotbarPosition {
        BOTTOM,
        LEFT,
        RIGHT
    }

    public static final ClientConfig CLIENT = new ClientConfig();

    // 空对象，仅用于兼容性
    public static final Object CLIENT_SPEC = new Object();

    public static class ClientConfig {
        // 使用public字段

        // 热键栏透明度 (0.0 - 1.0)
        public double hotbarOpacity = 1.0;

        // 原版热键栏大小
        public int vanillaHotbarSize = 9;

        // 最小槽位数
        public int minSlots = 1;

        // 最大槽位数
        public int maxSlots = 9;

        // 是否启用热键栏缩放
        public boolean scalingEnabled = true;
    }
}