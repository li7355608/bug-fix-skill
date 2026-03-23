// ============================================================
// TDD Fix 测试骨架模板
// 使用说明：根据 Bug 性质选择下方两个变体之一，替换占位符即可
// ============================================================

// ============================================================
// 变体一：纯单元测试（适用于纯逻辑 Bug，不依赖 Spring 容器）
// 使用 Mockito 隔离外部依赖，执行速度快
// ============================================================

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ${TargetClass}Test {

    @Mock
    private ${DependencyClass} ${dependencyField};
    // 如有多个依赖，继续添加 @Mock 字段

    @InjectMocks
    private ${TargetClass} ${targetField};

    @Test
    @DisplayName("${测试描述：用中文简述期望行为}")
    void should_${期望行为}_when_${触发条件}() {
        // Given —— 构造触发 Bug 的前置条件
        ${GivenType} givenData = ${构造测试数据};
        when(${dependencyField}.${mockedMethod}(${参数}))
                .thenReturn(${模拟返回值});

        // When —— 执行被测方法
        ${ResultType} result = ${targetField}.${testedMethod}(${参数});

        // Then —— 断言修复后的正确行为
        assertNotNull(result, "结果不应为 null");
        assertEquals(${期望值}, result, "${断言失败时的说明}");
        // 根据需要添加更多断言
        // verify(${dependencyField}, times(1)).${mockedMethod}(${参数});
    }
}


// ============================================================
// 变体二：集成测试（适用于涉及 Spring 容器、配置注入、Bean 装配的 Bug）
// 启动 Spring 上下文，使用 @MockBean 替换外部依赖
// ============================================================

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.bean.MockBean;
// 注意：Spring Boot 3.4+ 已将 @MockBean 迁移至
// org.springframework.test.context.bean.override.mockito.MockitoBean
// 如果项目使用 Spring Boot 3.4+，请替换为 @MockitoBean
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test") // 使用测试环境配置
class ${TargetClass}IntegrationTest {

    @MockBean
    private ${ExternalDependencyClass} ${externalDependencyField};
    // 只 Mock 外部依赖（如第三方 API 客户端、消息队列等）
    // 内部 Bean 让 Spring 正常注入，以测试真实装配

    @Autowired
    private ${TargetClass} ${targetField};

    @Test
    @DisplayName("${测试描述：用中文简述期望行为}")
    void should_${期望行为}_when_${触发条件}() {
        // Given —— 构造触发 Bug 的前置条件
        ${GivenType} givenData = ${构造测试数据};
        when(${externalDependencyField}.${mockedMethod}(${参数}))
                .thenReturn(${模拟返回值});

        // When —— 执行被测方法
        ${ResultType} result = ${targetField}.${testedMethod}(${参数});

        // Then —— 断言修复后的正确行为
        assertNotNull(result, "结果不应为 null");
        assertEquals(${期望值}, result, "${断言失败时的说明}");
        // 根据需要添加更多断言
    }
}
