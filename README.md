# myskill

给开发团队用的 AI 编程技能库。

不绑定特定模型或 IDE——Claude Code、Cursor、Copilot、Windsurf 都能用。核心思路是把团队沉淀的开发规范变成 AI 可执行的流程约束，让 AI 干活的时候守规矩。

## 目前包含的 Skill

### tdd-fix — TDD 驱动的 Bug 修复

修 Bug 时强制走 Red-Green-Verify 三阶段，不让 AI 跳步：

1. **Red**：先写一个失败测试，证明 Bug 确实存在
2. **Green**：最小化修复，只改必要的代码，跑测试确认通过
3. **Verify**：逐项回归检查 + 输出完整修复报告

解决的问题：AI 修 Bug 喜欢直接改代码，改完说"应该没问题了"，但你不知道它有没有引入新问题。这个 Skill 逼它用测试证明。

**目录结构：**

```
tdd-fix/
├── SKILL.md                        # 流程定义和规则约束
├── templates/
│   ├── unit-test-template.java     # JUnit5 测试骨架（单元测试 + 集成测试两个变体）
│   ├── regression-checklist.md     # 回归检查清单（11 大类）
│   └── fix-report-template.md      # 修复报告模板（6 个章节）
└── examples/
    ├── example-bug-fix.md          # 完整修复流程示例
    └── example-test-output.java    # 示例测试代码
```

## 怎么用

**Claude Code：** 把 Skill 目录复制到 `~/.claude/skills/` 下，自动触发。

**Cursor：** 在项目的 `.cursor/rules/` 下放入 SKILL.md 内容，或通过 `@` 引用。

**其他工具：** 把 SKILL.md 的内容喂给你用的 AI 工具，作为系统指令或上下文即可。

## 项目上下文配置

Skill 执行前会自动查找项目上下文文件，按优先级：

1. `PROJECT-CONTEXT.md` — 推荐，通用格式
2. `CLAUDE.md` — Claude Code 项目
3. `.cursorrules` — Cursor 项目
4. `.github/copilot-instructions.md` — Copilot 项目

都找不到就降级扫 `pom.xml` / `build.gradle` 推断技术栈。

建议每个项目维护一份 `PROJECT-CONTEXT.md`，写清楚技术栈版本、测试框架、核心模块、容易踩坑的地方，AI 干活会靠谱很多。

## 后续计划

这个仓库会持续补充新的 Skill，方向包括但不限于：

- 代码审查规范
- 数据库变更检查
- API 设计约束
- 日志规范检查

欢迎提 Issue 或 PR。
