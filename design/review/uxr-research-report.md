# 用户研究报告 · PalmEnsemble

> 复核运行 `palmensemble-design-revalidation-20260813-1913`：Stage 2 重新读取 PM、三项官方竞品资料与本地 PICO 约束，事实/推断/假设及证据缺口分类保持有效。

> 角色：`research_analyst`。修订：r5（live Stage 2 research）。观察时间：2026-08-13。

## 1. 研究问题与方法

- 问题：零乐理用户怎样在一分钟内理解并做出循环？怎样避免播放中修改造成断拍？空间化是否比 2D 网格更有价值？
- 方法：分析用户规格；查阅 Ableton Note、Koala Sampler 与 Novation Launchpad 的官方功能说明；对照本机 PICO Spatial SDK 6.0 的 Stage、ECS、交互约束。
- 样本缺口：尚未进行真实零基础用户访谈与 PICO 实机可用性测试；所有舒适度和命中率结论均不得视为设备证据。

## 2. 五类证据

| 类别 | 证据或缺口 | 来源 / 类型 | 范围与置信度 | 验证计划 |
|---|---|---|---|---|
| market | 成熟移动音乐工具通过鼓垫、循环、预置乐器快速起步，但也常扩展到采样、效果、混音和导出；PalmEnsemble 的机会是只保留“一小节、八音色、抓放时间格” | [Ableton Note](https://www.ableton.com/en/note/)、[Koala](https://www.koalasampler.com/)、[Launchpad iOS](https://novationmusic.com/software/launchpad-ios)；官方 | 公开功能，2026-08-13，高 | 不复制其布局；仅核对需求覆盖与反模式 |
| user | 用户明确要求中文、零乐理、一分钟出节奏、一键示例和多通道反馈 | 原始需求；用户提供 | 本项目，高 | 5 名零基础用户完成首节奏任务 |
| domain | 8-step 序列的关键实体是小节、step、voice、触发事件、pending edit 与 committed pattern；播放中编辑必须在小节边界提交 | 用户约束 + 音序器领域推导；用户提供/推导 | 本项目，中高 | 单元测试边界提交、20 圈无缝循环 |
| platform | Stage 仅在 Full Space，坐标为右手米制；可交互实体需 Collision + Interactable；手柄必须有等价路径 | 本机 PICO Spatial SDK 6.0 指南；官方本地资料 | SDK 6.0，高 | 编译、设备安装、控制器测试 |
| safety | 中央视野单一焦点、避免世界移动与高频全屏闪烁可降低负担；具体舒适时长、球体距离和闪光强度尚无实机证据 | PICO 设计工作流 + 明确证据缺口；官方/假设 | 设计阶段，中低 | 设备上验证坐姿/站姿、Reduce Motion 与 10 分钟疲劳 |

### 可复现证据账本

| ID | 结论 | 分类 | 不可变来源锚点 | 范围/版本 | 置信度 |
|---|---|---|---|---|---|
| U-01 | 中文、零乐理、1 分钟、8-step、100 BPM、3 氛围、8 音色、量化、手柄、30 秒保存和排除项 | fact | 本次用户原始消息；PM §1–§6 逐项转录 | PalmEnsemble brief，2026-08-13 | 高 |
| P-01 | Stage 仅 Full Space、单 app 单 Stage、右手米制 | fact | `spatial-sdk-guideline/SKILL.md` → Containers and Space States | PICO OS 6 / plugin 0.4.1 | 高 |
| P-02 | 用户交互需要 CollisionComponent + InteractableComponent | fact | 同上 → Pattern: Make an entity hittable and interactive | PICO OS 6 / plugin 0.4.1 | 高 |
| P-03 | Compose UI 必须 PicoTheme、SpatialUI、系统默认玻璃根背景 | fact | `spatial-ui-design-style/SKILL.md` → Highest-Priority Decision Table | plugin 0.4.1 | 高 |
| M-01 | Ableton Note 提供鼓/旋律、量化、8 tracks/scenes 与导出 | fact | https://www.ableton.com/en/note/ | 访问 2026-08-13 | 高 |
| M-02 | Koala 提供采样、sequencer、FX、导入/导出 | fact | https://www.koalasampler.com/ | 访问 2026-08-13 | 高 |
| M-03 | Launchpad 提供同步 loops、soundpacks、FX、录制导出 | fact | https://novationmusic.com/software/launchpad-ios | 访问 2026-08-13 | 高 |
| H-01 | 多层制作概念可能提高零基础用户认知负担 | inference | M-01/M-02 + U-01；无直接用户研究 | 本项目假设 | 低 |
| H-02 | RGB pad 的颜色+位置反馈可能降低学习成本 | inference | M-03；无本项目用户研究 | 本项目假设 | 低 |

## 3. 竞品基线（需求层吸收，不复用界面）

| 产品 / 平台 | 功能需求 | 交互体验 | 视觉体验 | 空间能力与迁移风险 |
|---|---|---|---|---|
| Ableton Note / iOS | 事实 M-01：鼓、旋律、量化、场景和导出 | 事实：触屏 pad 与 MIDI 编辑；推断 H-01：轨道/clip 对零基础可能增加负担，待比较测试 | 观察：信息层级多；不据此复用视觉 | 2D；本项目不照搬多轨 Session View |
| Koala Sampler / 多平台 | 事实 M-02：录音/导入、sequencer、效果和重采样 | 事实：pad、sequence、effects；推断 H-01：深层编辑可能增加认知负担 | 观察：pad 为主要触发面；不据此复用视觉 | 2D；采样/混音超出本项目边界 |
| Novation Launchpad / iOS+硬件 | 事实 M-03：同步 loops、声音包、FX、录制导出 | 事实：pad 触发；推断 H-02：颜色+位置可能降低学习成本，待测试 | 观察：8×8 RGB grid；不据此复用视觉 | 物理/2D；本项目验证空间抓放是否更直观 |

逐项吸收与规避：

- Ableton Note：吸收“预置音色即可开始、量化纠错”；规避轨道、clip、参数页面蔓延。
- Koala：吸收“即时试听与拖放直觉”；规避导入、采样、效果器、混音器和专业导出。
- Launchpad：吸收颜色+位置双通道反馈与同步循环；规避 64 格密度和音乐人术语。

差异化机会：把音色实体化为可抓取球，把单小节实体化为 8 个可达格，把量化等待实体化为格内光环；用户不需要理解 MIDI、音符长度或和声理论。

## 4. 领域模型

- 工作流：进入 → 开始演示或直接编辑 → 试听球 → 放置 → 播放 → 播放中修改 → 下一小节听到结果 → 可选记录 30 秒 → 本地保存 → 退出。
- 决策变量：氛围预设、声音家族、step index、当前/待提交状态、播放状态、录制剩余时间。
- 数据实体及时性：`Pattern` 每小节边界提交；`PreviewNote` 立即试听但不入序列；`RecordingEvent` 毫秒时间戳保存；`Preset` 本地静态。
- 专业风险：时钟漂移、音频线程阻塞、播放中直接改数组导致半小节突变、同音重复触发截断、视觉扫描与声音不同步。
- 用户心智：不是“写歌”，而是“把声音玩具放进八个时间盒子”。
- 成熟模式：step 量化、预听、颜色/图标冗余编码、bar-boundary commit、单一 transport；反模式是钢琴卷帘、参数旋钮墙、隐藏播放状态和任意 BPM。

## 5. Persona 与旅程

Persona“第一次做节奏的小羽”：18–35 岁，XR 基础操作熟悉但没有 DAW 经验；希望 60 秒内听到像样结果；不理解拍号、音阶、MIDI；可能有色觉差异，因此需要图标与声音冗余；实际人口学和引语为证据缺口，待招募验证。

| 阶段 | 进入 | 第一次操作 | 核心循环 | 修改 | 退出 |
|---|---|---|---|---|---|
| 目标 | 知道做什么 | 放下第一个声音 | 听懂 8 格循环 | 不断拍地替换 | 留下成果 |
| 行为 | 点击开始演示 | 抓球放格 | 播放并看扫描 | 移球/换球 | 记录并保存 |
| 情绪 | 好奇 | 若没反馈则困惑 | 首次合拍时愉悦 | 等待量化时可能疑惑 | 看见保存路径后安心 |
| 设计机会 | 一句引导 | 即时试听+吸附 | 光束与声音同步 | 明示“下一圈生效” | 明示“事件序列，不是音频” |

最低谷是“已经放了球，却不知道何时生效”；必须用待提交光环和“下一圈生效”文案消除不确定性。

## 6. 时长、姿态与安全边界

- 一瞥判断目标：播放/暂停、当前 step、pending 状态在 1 秒内辨认（项目目标，需实测）。
- 竞品推断验证：同一批新手分别完成“识别当前播放位置”和“播放中替换一个声音”，记录首次正确率、耗时、术语提问和误触；不把官方功能页当成可用性证据。
- 首个有效循环：60 秒内（用户验收要求）。
- 姿态：坐/站均可，主要交互保持在正前方舒适扇区；具体米制距离待设备验证。
- 眼手交互：球与格都提供 controller ray 目标；命中尺寸、抖动和深度需设备测试。
- 动效：不移动世界、不做镜头强制运动；Reduce Motion 下扫描改为格子描边/亮度变化。
- 疲劳：首版按 5–15 分钟轻量体验设计；实际耐受上限是证据缺口。

## 7. 最低完整性门

| 检查 | 证据 | 结论 |
|---|---|---|
| 五类证据或显式缺口 | §2 | pass |
| 至少三项竞品的四维比较 | §3 | pass |
| 领域流程、变量、实体、风险、心智与反模式 | §4 | pass |
| Persona/旅程不伪造用户事实 | §5 明示缺口 | pass |
| 时长、姿态、输入和安全均有目标或验证计划 | §6 | pass |

`minimumCompletenessGate=pass`
