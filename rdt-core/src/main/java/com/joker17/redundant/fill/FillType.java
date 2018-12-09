package com.joker17.redundant.fill;

/**
 * 填充类型
 */
public enum FillType {
    /**
     * 只填充为transient的列
     */
    TRANSIENT,
    /**
     * 只填充持久化的列
     */
    PERSISTENT,
    /**
     * 填充所有的列
     */
    ALL
}
