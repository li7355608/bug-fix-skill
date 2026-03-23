# 示例：TDD 修复完整流程

## 用户报告

> "OrderService 的 calculateDiscount 方法在没有配置折扣率的时候直接抛 NPE 了，线上偶发 500 错误。"

---

## 执行前：项目适配

AI 读取项目根目录 `CLAUDE.md`，获取到以下信息：
- 技术栈：Java 17 + Spring Boot 3.1 + MyBatis-Plus
- 测试框架：JUnit5 + Mockito 5.x
- 配置管理：使用 `@Value` 注入，部分配置在 Nacos 动态管理
- 测试运行命令：`mvn test -pl order-service`

---

## 阶段一：Red

### 1.1 根因分析

AI 阅读源码后输出：

> **问题定位**：`OrderService#calculateDiscount`
>
> **根本原因**：方法通过 `@Value("${order.discount.rate}")` 注入折扣率，字段类型为 `Double`（包装类型）。当配置中心未配置 `order.discount.rate` 时，该字段为 `null`。方法内直接使用 `discountRate * originalPrice` 进行运算，触发 `NullPointerException`。
>
> **触发条件**：配置中心中 `order.discount.rate` 未配置或被删除时，调用 `calculateDiscount` 方法。

### 1.2 编写复现测试

AI 选择纯单元测试（Bug 属于逻辑问题，不依赖 Spring 容器）：

```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Test
    @DisplayName("折扣率未配置时应返回原价而非抛出异常")
    void should_return_original_price_when_discount_rate_is_null() {
        // Given —— 模拟折扣率未配置的情况
        ReflectionTestUtils.setField(orderService, "discountRate", null);
        BigDecimal originalPrice = new BigDecimal("100.00");

        // When & Then —— 不应抛异常，应返回原价
        BigDecimal result = orderService.calculateDiscount(originalPrice);
        assertEquals(new BigDecimal("100.00"), result,
                "折扣率为 null 时应返回原价");
    }
}
```

### 1.3 执行测试，确认红灯

```
运行：mvn test -pl order-service -Dtest=OrderServiceTest#should_return_original_price_when_discount_rate_is_null

结果：FAILED
java.lang.NullPointerException: Cannot invoke "java.lang.Double.doubleValue()"
  at com.example.order.service.OrderService.calculateDiscount(OrderService.java:42)
```

**红灯确认**：测试失败，成功复现 Bug。

---

## 阶段二：Green

### 2.1 实施修复

**改动文件**：`order-service/src/main/java/com/example/order/service/OrderService.java`

**改动内容**：在 `calculateDiscount` 方法开头增加 null 检查。

修复前：
```java
public BigDecimal calculateDiscount(BigDecimal originalPrice) {
    return originalPrice.multiply(BigDecimal.valueOf(discountRate));
}
```

修复后：
```java
public BigDecimal calculateDiscount(BigDecimal originalPrice) {
    if (discountRate == null) {
        return originalPrice;
    }
    return originalPrice.multiply(BigDecimal.valueOf(discountRate));
}
```

**改动理由**：当折扣率未配置时，业务语义应为"不打折"，即返回原价。这是最小化修复，只加了一个 null 检查，没有改动其他逻辑。

### 2.2 运行测试，确认绿灯

```
运行：mvn test -pl order-service -Dtest=OrderServiceTest#should_return_original_price_when_discount_rate_is_null

结果：PASSED
Tests run: 1, Failures: 0, Errors: 0
```

**绿灯确认**：测试通过。

---

## 阶段三：Verify

### 3.1 回归检查清单

| 检查项 | 结果 |
|--------|------|
| 配置项读取逻辑是否受影响 | 否，只增加了 null 守卫，不影响正常配置读取 |
| 数据库映射是否改动 | 否 |
| 外部接口调用是否受影响 | 否 |
| 依赖注入是否变化 | 否 |
| 异常处理链路是否完整 | 是，NPE 不再抛出 |
| 多环境配置是否兼容 | 是，无论是否配置折扣率都能正常运行 |

### 3.2 运行模块测试

```
运行：mvn test -pl order-service

结果：
Tests run: 47, Failures: 0, Errors: 0, Skipped: 2
BUILD SUCCESS
```

### 3.3 修复报告

# 修复报告

## 1. Bug 描述

| 项目 | 内容 |
|------|------|
| 报告人 | 用户 |
| 现象描述 | OrderService.calculateDiscount 在折扣率未配置时抛出 NPE，线上偶发 500 |
| 影响范围 | 订单折扣计算接口 |
| 严重程度 | 严重 |

## 2. 根因分析

- **问题定位**：`OrderService#calculateDiscount`
- **根本原因**：`discountRate` 字段为 `Double` 包装类型，未配置时为 `null`，直接参与运算导致 NPE
- **触发条件**：配置中心未配置 `order.discount.rate` 时调用该方法

## 3. 复现测试

| 项目 | 内容 |
|------|------|
| 测试类 | `com.example.order.service.OrderServiceTest` |
| 测试方法 | `should_return_original_price_when_discount_rate_is_null` |
| 修复前执行结果 | **失败** —— NullPointerException |
| 修复后执行结果 | **通过** |

## 4. 修复方案

| 序号 | 文件路径 | 改动说明 |
|------|----------|----------|
| 1 | `OrderService.java` | 在 calculateDiscount 方法开头增加 discountRate 的 null 检查，为 null 时返回原价 |

## 5. 回归测试结果

| 项目 | 内容 |
|------|------|
| 测试范围 | order-service 模块全部测试 |
| 测试总数 | 47 |
| 通过数量 | 45 |
| 失败数量 | 0 |
| 跳过数量 | 2 |

## 6. 风险评估

| 风险项 | 评估 |
|--------|------|
| 是否影响其他模块 | 否 |
| 是否需要修改配置 | 否，建议后续在配置中心补上默认值 |
| 是否需要数据迁移 | 否 |
| 是否需要通知前端 | 否 |
| 多环境兼容性 | 兼容 |
