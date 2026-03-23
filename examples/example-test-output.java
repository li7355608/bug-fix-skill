package com.example.order.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OrderService Bug 修复测试
 *
 * Bug 描述：折扣率未配置时 calculateDiscount 方法抛出 NullPointerException
 * 根因：discountRate 字段为 Double 包装类型，未配置时为 null，直接参与乘法运算触发 NPE
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    // ==================== Bug 复现测试（阶段一：Red） ====================

    @Test
    @DisplayName("折扣率未配置时应返回原价而非抛出异常")
    void should_return_original_price_when_discount_rate_is_null() {
        // Given —— 模拟折扣率未配置的情况
        ReflectionTestUtils.setField(orderService, "discountRate", null);
        BigDecimal originalPrice = new BigDecimal("100.00");

        // When —— 调用折扣计算方法
        BigDecimal result = orderService.calculateDiscount(originalPrice);

        // Then —— 应返回原价，不应抛异常
        assertNotNull(result, "返回值不应为 null");
        assertEquals(new BigDecimal("100.00"), result,
                "折扣率为 null 时应返回原价");
    }

    // ==================== 补充测试（确保正常场景不受影响） ====================

    @Test
    @DisplayName("折扣率正常配置时应正确计算折扣价")
    void should_return_discounted_price_when_discount_rate_is_configured() {
        // Given —— 折扣率配置为 0.8（八折）
        ReflectionTestUtils.setField(orderService, "discountRate", 0.8);
        BigDecimal originalPrice = new BigDecimal("100.00");

        // When —— 调用折扣计算方法
        BigDecimal result = orderService.calculateDiscount(originalPrice);

        // Then —— 应返回 80.00
        assertNotNull(result, "返回值不应为 null");
        // 使用 compareTo 避免 scale 不同导致 equals 失败
        assertEquals(0,
                new BigDecimal("80.000").compareTo(result),
                "100 * 0.8 应等于 80");
    }

    @Test
    @DisplayName("折扣率为 1.0 时应返回原价")
    void should_return_original_price_when_discount_rate_is_one() {
        // Given —— 折扣率配置为 1.0（不打折）
        ReflectionTestUtils.setField(orderService, "discountRate", 1.0);
        BigDecimal originalPrice = new BigDecimal("250.00");

        // When
        BigDecimal result = orderService.calculateDiscount(originalPrice);

        // Then
        assertEquals(0,
                new BigDecimal("250.00").compareTo(result),
                "折扣率为 1.0 时应返回原价");
    }
}
