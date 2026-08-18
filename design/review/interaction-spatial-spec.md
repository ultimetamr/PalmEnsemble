# 交互 / 空间设计规格 · PalmEnsemble

> 当前修订：r10（live Stage 10 composition synthesis）。

> Live Stage 9 读取 Interaction r8、Visual r5、PM r8 与 UXR r5：Stage/AttachmentPanel 结构、窗口尺寸、屏幕图与稳定退出均保持有效。

## 1. 设计原则

| ID | 原则 | 依据 | 实现检查点 | 冲突优先级 |
|---|---|---|---|---|
| P1 | 任何时刻只有中央八格轨道是主焦点 | PM §1、UXR §5 | scene hierarchy、截图 | 最高 |
| P2 | 用户只做“选声音、选格子、听下一圈”三类决定 | U-01、领域模型 | 状态图、组件数量 | 高于功能丰富度 |
| P3 | 声音变更即时可见但只在下一 bar boundary 可听 | PM 时间合同 | active/pending 双 pattern | 高于即时发声反馈 |
| P4 | 颜色不单独承载意义，所有音色与状态都有图标/文字/试听 | 可访问性风险 | 球、格、状态提示 | 高于视觉简洁 |
| P5 | 手柄完成全路径，手势仅增强 | 用户要求 | 输入映射与设备测试 | 高于新奇手势 |

冲突裁决：节奏连续性 > 立即反馈；可达性 > 空间装饰；清晰 > 信息密度。禁止多窗口、专业术语、参数旋钮墙、镜头运动和网络功能。

## 2. 任务 / 决策模型

| ID | 任务 | 参与者/场景 | 输入 | 决策输出 | 错误后果 | 频率 | 依赖 | 时长目标 |
|---|---|---|---|---|---|---|---|---|
| T1 | 进入演示 | 新用户/冷启动 | “开始演示”“自己试试” | 是否加载示例并播放 | 不知道下一步 | 每会话 1 次 | 无 | ≤60s |
| T2 | 试听并选球 | 用户/编辑 | 球颜色、图标、名称、试听 | soundId | 选错音色 | 多次/小节 | T1 可选 | ≤3s |
| T3 | 放置或替换 | 用户/轨道 | 当前 step、占位、吸附反馈 | family+step→soundId | 误放/覆盖 | 多次 | T2 | 一次抓放 |
| T4 | 播放/暂停 | 用户/transport | 播放状态、100 BPM | isPlaying | 听不到或误停 | 多次 | 可独立 | ≤1s |
| T5 | 播放中编辑 | 用户/循环中 | active 与 pending 指示 | nextPattern | 中途突变 | 多次 | T3,T4 | 下一边界生效 |
| T6 | 清空 | 用户/重做 | 确认文案、pending 规则 | empty pending pattern | 误删 | 低 | 任意 | 2 步确认 |
| T7 | 切换氛围 | 用户/编辑或播放 | Lo-fi/电子/轻摇滚 | presetId | 预期音色不符 | 低 | 无 | 一步；边界生效 |
| T8 | 记录与保存 | 用户/满意后 | 30s、剩余时间、事件类型 | 本地 JSON | 误解为音频/保存失败 | 低 | T4 | ≤30s |
| T9 | 退出 Full Space | 用户/系统返回 | 保存状态 | 退出 | 丢失/困住 | 每会话 1 次 | 任意 | 稳定返回 |

依赖：T2→T3 串行；T4 与 T2/T3 并行；T5 是 T3 在播放态的量化分支；T6/T7 写入同一 pending transaction；T8 只观察事件不改变 pattern。

竞品覆盖裁决：保留预置、量化、试听、循环和保存；有意省略采样/导入、效果、混音、场景编排、分享和 MIDI。

## 3. 空间价值与 2D 反事实

| 任务 | 空间价值 | 空间理由 | 2D 反事实 | 评价 |
|---|---|---|---|---|
| T2 选球 | 左/右方向、距离、身体 | 鼓和旋律从身体两侧形成两个可记忆来源 | 两列 pad 列表同样可选 | 中 |
| T3 放置 | 位置、深度、身体 | “声音物体→时间格”把抽象编程变成抓放 | 触屏 2×8 grid 更高效但物感弱 | 高 |
| T4 播放 | 时间、运动 | 扫描光在真实宽度上逐格穿行，位置对应听觉时刻 | 2D playhead 足够 | 中 |
| T5 量化修改 | 时间、深度 | pending 球在格上方形成“下一圈”层，边界时下沉提交 | 2D ghost note 同样可表达 | 中高 |
| T1 进入演示 | 入口本身无空间价值 | 只有确认后才进入 Stage | 2D 启动面板足够，首版用 Stage 内固定引导但不把它算空间价值 | 低 |
| T6 清空 | 无空间价值 | 仅需防误触确认 | 2D Dialog 足够，作为 AttachmentPanel 内确认 | 低 |
| T7 氛围 | 无需空间 | 只是三选一 | 2D segmented control 更合适，作为附着面板控件 | 低 |
| T8 保存 | 无需空间 | 状态提示而非 3D 内容 | 2D 按钮/状态足够 | 低 |
| T9 退出 | 无空间价值 | 稳定返回是容器安全要求 | 系统返回/2D 退出控件足够 | 低 |

Stage 合理性来自 T2+T3+T5 的联合物理隐喻，不是所有任务都空间化；低价值任务留在轨道下方 AttachmentPanel。

## 4. 三个设计假设

| 假设 | 信息模型 | 空间程度/容器 | 用户路径与主交互 | 风险/成本 |
|---|---|---|---|---|
| A 时间桌“球入格” | 一维 8 格轨 + 左右声音库 | 单 Stage；轨道/球为 ECS，底部 AttachmentPanel | 抓球→放格→扫描 | 中；最贴合需求 |
| B 环形节拍钟 | 8 格环绕用户，声音球在内外两圈 | Stage Full；身体转向选格 | 指向/投掷到环形节点 | 高；易疲劳、顺序方向不直观 |
| C 共享空间音乐盒 | Volumetric Window 内微缩 3D 轨道 + 2D 控制 | Shared Space；有限体积 | 射线点选、拖放 | 低；更易共存但削弱身体抓放与尺度 |

## 5. 概念选择

5 分制；任务效率/领域深度来自 T1–T9 覆盖，空间价值来自 §3，舒适/安全/可访问性为设计假设而非设备证据（UXR §6、PM 风险登记），可实现性来自 P-01..03；所有舒适、安全分数必须通过坐/站和 10 分钟设备测试验证，任一 P1 失败即降级或改变容器。

| 假设 | 任务效率 | 空间价值 | 舒适 | 领域深度 | 安全 | 可访问 | 可实现 | 独特性 | 总分 | 结论 |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---|
| A | 5(T1–T9最短路径) | 5(§3 T3/T5) | 4假设(中央扇区) | 5(双 pattern) | 4假设(无镜头运动) | 4假设(冗余编码+手柄) | 4(P-01..03) | 5 | 36 | 选中；舒适/安全待设备证 |
| B | 3(转头) | 5(环绕) | 2假设(频繁转向) | 4 | 2假设(周边运动) | 3 | 3 | 5 | 27 | 拒绝：转头与顺序负担 |
| C | 4 | 2(边界削弱抓放) | 5假设(Shared) | 4 | 5假设(有限体积) | 4 | 5 | 3 | 32 | 拒绝：空间收益不足以兑现“抓球放轨” |

选中概念：“时间桌 · 球入格”。市场定位不是专业制作器，而是零术语、单小节、实体时间的空间玩具。依据 UXR M-01..03 与差异化总结：吸收预置、量化、pad 反馈，避免多轨、采样、效果和 8×8 密度；用 PICO 的方向、位置、身体和深度把声音放入时间。

## 6. 体验与容器架构

- Space State：Full Space；打开唯一 Stage 时其他 app 退居后台。
- 容器：`PalmStage`，Stage Mixed（immersion=0），真实环境上叠加乐器；不请求锚点、平面检测或手部权限。
- 进入价值：只有 Full Space 能提供无边界左右声库和身体抓放；入口按钮“进入空间乐器”明确触发。
- 稳定退出：系统 Back 或控制面板“退出”关闭 Stage，返回 launcher window/系统。
- Stage 内容：ECS runtime；轨道、球、扫描束为 ECS 实体；2D 控制通过 `AttachmentPanel` 挂在 `ControlAnchor`。
- 默认可见：1 Stage、0 个额外 WindowContainer；所有关键内容位于前方中央扇区。

## 7. 附着面选择

| 需求 | 放置 | 选择 | 宿主 | 理由 | 拒绝项 |
|---|---|---|---|---|---|
| 播放/清空/示例/录制/氛围 | Stage 内定位 | AttachmentPanel + InlineControl | ControlAnchor | 控件与轨道同生命周期且靠近作用对象 | Toolbar/TabBar 仅用于 WindowContainer；None 会失去必要控制；独立窗口分散焦点 |
| 清空确认 | 临时 | SpatialUI Dialog in AttachmentPanel | ControlAnchor | 防误删 | InlineControl 直接清空风险高；None 不可接受 |
| 新手提示 | 临时锚定 | Coachmark-equivalent inline hint | TrackAnchor | 首次出现后消失 | 持久 Subwindow 过重；None 不能满足一分钟入门 |

## 8. Window sizing

无业务 WindowContainer，因此 Planar default/min/max 不适用。Launcher 仅承载显式 Stage 进入/退出，不属于主体验；若模板要求，采用辅助 Planar 640×360dp、min 480×270、max 800×450，Dynamic worldScale、1.75m、核心在 65°×40°，hit target≥56dp、body≥12dp。Stage 与 AttachmentPanel 以米制布局为准，不伪造为窗口尺寸。

## 9. 状态图

| 状态 | 主任务/焦点 | 容器/布局 | 数据 | 进入 | 继续/退出 | 异常与返回 |
|---|---|---|---|---|---|---|
| `ready_demo` | 选择示例或自己试 | Launcher/Stage 引导 | preset、empty pattern | 启动 | 进入 `editing`/`playing` | 加载失败→空 pattern |
| `editing` | 中央轨道抓放 | PalmStage | active=pending、selected orb | 放置/暂停 | 播放→`playing`；退出 | 非法放置→回弹 |
| `playing` | 扫描和听循环 | PalmStage | active pattern、step、clock | play | 编辑→`playing_pending`；pause→editing | audio failure→视觉继续+错误提示 |
| `playing_pending` | 下一圈变更 | PalmStage | active≠pending | 播放中修改 | boundary commit→playing | pending 可撤回/清空 |
| `recording_events` | 30 秒事件记录 | PalmStage | event list、remaining | record | 30s/save→原状态 | 写入失败→保留内存+重试 |
| `confirm_clear` | 防误删 | Attachment Dialog | pending pattern | clear | confirm→pending clear；cancel→原状态 | 无 |

| Transition ID | From→To | Trigger | Action | 确认 |
|---|---|---|---|---|
| `user.enterStage` | launcher→ready_demo | 点击进入 | openStage(Mixed) | 是 |
| `user.loadExample` | ready_demo→playing | 示例 | set pending example; commit; start | 否 |
| `user.dropOrb` | editing→editing / playing→playing_pending | 松开扳机 | snap; preview; update pending | 否 |
| `clock.barBoundary` | playing_pending→playing | step 7→0 | atomic swap active=pending | 否 |
| `user.togglePlayback` | editing↔playing | 按钮/A 键 | start/pause monotonic clock | 否 |
| `user.clear` | any→confirm_clear | 清空 | open dialog | 是 |
| `user.record` | playing→recording_events | 录制 | start event capture max30s | 否 |
| `user.exitStage` | any→launcher/system | Back/退出 | closeStage | 是 |

## 10. 输入、动效与几何

- 手柄：ray hover；扳机按下抓球、移动、松开吸附；A 播放/暂停；B 取消抓取/返回；摇杆不用于移动世界。
- 直接手势：gaze+pinch 等价选择/拖放；所有目标 Collision + Interactable，球 HoverEffect。
- 反馈：抓取 120ms 放大至 1.05；吸附 180ms ease-out、最大位移 0.12m；pending 仅以 6cm 高度+虚线环表达；commit 160ms 下沉；扫描每 step 300ms 线性移动。
- Reduce Motion：取消位移/缩放，改为描边与亮度切换；扫描以当前格描边替代移动光束。
- 性能降级：扫描束改不透明细板；球体取消粒子；音频视觉仍由同一 step tick 驱动。
- 几何（Stage 右手米制，用户原点前方 -Z）：TrackAnchor `(0,1.05,-1.55)`，8 格总宽 1.60m、单格 0.18m、间距 0.02m；鼓球中心 `(-1.05,1.25,-1.45)` 纵向四颗；旋律球中心 `(1.05,1.25,-1.45)`；ControlAnchor `(0,0.72,-1.48)`；扫描束 z=-1.50，朝用户更近 0.05m。
- 密度：同时仅 8 格、8 球、1 控制带；主焦点计数=1（轨道）。拒绝上下两层 2×8 网格，因为会把玩具变成 DAW；每格内部用鼓/旋律前后双槽且图标区分。

## 11. 最低完整性门

| 检查 | 证据 | 结论 |
|---|---|---|
| 原则与 T1–T9 决策完整 | §1–§2 | pass |
| 每任务反事实、三假设、选择和拒绝理由 | §3–§5 | pass |
| Full Space/Stage 入口退出、附着面 None/Inline 比较 | §6–§8 | pass |
| 状态、transition、异常、稳定退出 | §9 | pass |
| 输入、动效、Reduce Motion、米制几何可实现 | §10 | pass |

`minimumCompletenessGate=pass`

## 12. 构图综合

```text
            左声库                    右声库
       ○  ○  ○  ○                ○  ○  ○  ○
                 ┌─1─2─3─4─5─6─7─8─┐
                 │   中央八格轨道    │  ← 唯一主焦点
                 └──────────────────┘
                    [ 附着控制带 ]
```

- 任务关系：T2 的两类声音从左右汇入 T3 轨道；T4/T6/T7/T8 在轨道下方，避免抢占主焦点。
- 数据关系：active/pending 同格共址；当前 step 光束与音频 tick 同源；录制只读取事件流。
- 交互频率：球与轨道最常用且最大；氛围/清空/录制次要且更低更远。
- 空间约束：全部目标落在正前舒适扇区；左右球距离轨道最近边约 0.16m；不要求转身。
- 响应策略：Stage 根据头高整体校准 y；Compact reach 模式将左右球移至 `x=±0.82m` 并纵向间距缩至 0.16m，不缩小球/命中体；Seated 模式整体下移 0.12m。
- 拒绝：双排 2×8 轨道（像 DAW）；环形包围（转头）；多个漂浮面板（注意分裂）。
