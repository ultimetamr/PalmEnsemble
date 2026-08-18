# PalmEnsemble PICO 真机测试

- 目标设备：`PB314XHGKC160016G`
- 型号：PICO B3110（product/device `swan`）
- 系统：Android 16 / API 36
- ABI：arm64-v8a
- 电量：100%，USB 供电
- APK：`app/build/outputs/apk/debug/app-debug.apk`
- 安装：`pico-cli app install ... --device PB314XHGKC160016G` → `Success`
- 启动：`com.pico.swan.palmensemble/.platform.LaunchActivity`
- 运行：PID `8614`，`topResumedActivity` 与 `mFocusedApp` 均为 PalmEnsemble
- 稳定性：清空 crash buffer 后启动，crash buffer 为空；未发现 PalmEnsemble `FATAL EXCEPTION` / `AndroidRuntime`。
- 截图边界：`pico-cli capture screenshot` 返回 `ADB screencap returned non-PNG output: Failed to take screenshot`，未生成图片，未把失败输出当作视觉证据。

## 世界固定摆放

- 已移除 `AnchorTarget.createCameraTarget()`，根实体只在 `SpatialView.initial` 设置一次世界坐标 `(0, 1.35, -0.90)m`。
- Stage 坐标为右手系：轨道中心约位于 1.35m 高、用户初始正前方 0.90m；转头或移动视线不会再更新根实体。
- 结构回归确认 `camera_anchor=false`、`world_position=true`；全量测试 10/10、Debug 构建及 SpatialUI 样式校验 0 error/0 warning。
- 最终包覆盖安装成功。首次启动被真机前台 `Inkward` Stage 抢占；停止竞争 Stage 后，PalmEnsemble PID `11039`，`topResumedActivity`/`mFocusedApp` 正确，crash buffer 与 AndroidRuntime fatal 均为空。
- 是否处于用户期望的具体房间落点、站姿/坐姿高度仍需戴头显转头和移动后人工确认。

## 人工验收步骤

1. 戴上头显，选择“开始演示”得到已填充轨道。
2. 按住左右音色球，移动到中央轨道；格子高亮后松手，应放入目标格。
3. 播放中再次拖入音色，确认显示“下一圈生效”，且当前循环不断拍。
4. 按住轨道内已占用的球向外拖；出现“松手丢弃”警示后释放，应移除该球。
5. 播放中丢弃时，确认只在第 8 格回到第 1 格的边界生效。

命令行无法注入空间控制器射线和扳机，因此步骤 2–5 需要头显控制器人工确认。

## Compose 拖动与氛围修复复测

- 真机反馈确认旧版拖动无回调：旧实现把 ECS 目标用的 `detectSpatialDragGesture(TargetEntity.any())` 挂在普通 `AttachmentPanel` Compose 控件上，而球没有 ECS `CollisionComponent + InteractableComponent`。
- 新版改为面板自带的 Compose-style `detectDragGestures`；手柄扳机在球上按住并移动后，直接累计二维面板像素，左/右球吸附算法与轨道内 96dp 拖出丢弃阈值保持不变。
- Lo-fi / 电子 / 轻摇滚现在分别生成暗暖削频、明亮短促、厚实驱动三套 PCM；播放中切换显示 `◌` 并在 7→0 边界生效，暂停时立即切换。
- 新增量化氛围与三套 PCM 回归测试；全量 `testDebugUnitTest assembleDebug` 通过，SpatialUI verifier 为 0 error / 0 warning。
- 新版已覆盖安装至当前在线模拟器 `emulator-5554`，PID `2475`；清空后 crash buffer 为空，未发现相关 `FATAL` / `AndroidRuntime`。
- 当前设备列表只有 `emulator-5554`，原真机 `PB314XHGKC160016G` 不在线。恢复 USB/无线 ADB 后需重新覆盖安装，并人工执行上面的步骤 2–5。

## 黑色对称视觉

- 已移除所有内层面板的 `Material.Regular` 透明灰玻璃；标题、左右音色库、轨道、控制区和确认层统一使用不透明纯黑底。
- 色彩仅承担音色识别、选中/扫描和警示。左右第 1–4 行分别镜像为桃色、黄色、蓝色、紫色，图标与文字保持冗余识别。
- 最终 APK 已再次覆盖安装到 `emulator-5554`，运行 PID `5760`；清空后 crash buffer 和 PalmEnsemble `FATAL`/`AndroidRuntime` 筛选均为空。

## 弹窗层级与上下球命中修复

- 清空弹窗原先位于 `z=-0.12m`，落在轨道 `0m` 和拖动浮层 `+0.10m` 后方；现由 `StagePanelDepths.MODAL=+0.24m` 固定在最前，并在弹窗出现时隐藏拖动浮层。
- 轨道格原先同时存在“整格点击”和上下球各自点击，空间控制器可能把下球命中路由给父格并沿用上球选择。现取消整格点击：32dp 格号区域专用于放入，上鼓球/下旋律球各自拥有独立点击、拖动和 `step:family` key。
- 新增 `StagePanelDepthsTest`，全量测试、`assembleDebug` 和 SpatialUI verifier 均通过。
- 本轮 `pico-env-doctor` 为 `FAILED`（缺 Android 35 Sources、`PICO_HOME`、完整 Editor 与项目上下文），按设备流程未继续覆盖安装；磁盘 APK 已更新。

## 启动视野居中修复

- 固定世界坐标 `(0, 1.35, -0.90)m` 无法适配用户启动时的头部朝向，曾导致界面整体偏离当前视野。
- Stage 根实体现使用 `AnchorTarget.createCameraTarget()` + `AnchorComponent.TrackingMode.ONCE`，首帧以当前视线为基准放到 `(0, -0.04, -1.35)m`；首帧后保持世界锁定，不持续跟随头部。
- `testDebugUnitTest assembleDebug` 通过；新版覆盖安装到 `emulator-5554`，运行 PID `6032`，fresh crash buffer 为空。
- 居中截图：`artifacts/emulator-20260814-centered-launch.png`。截图中标题、左右 8 颗音色球、中央轨道与底部控制栏均完整可见并围绕画面中心对称。

## 稳定锚定与完整音乐套装

- 根因：`TrackingMode.ONCE` 原先在 Stage 首帧立即采样；Full Space 切换时若头部仍转动或追踪刚恢复，瞬时 yaw 会被永久锁住，表现为首次进入随机左偏或右偏。
- 修复：先等待 650ms，再创建 `SpatialView`，并在它的 `initial` 生命周期内创建一次性相机锚点；之后保持世界锁定，不随视线移动。曾尝试“先隐藏 ECS 根实体、协程中再启用”，模拟器证明该可见性变更未可靠进入空间渲染，因此已删除该路径。
- Lo-fi / 电子 / 轻摇滚现各自拥有不同的完整 8 格编排；点击套装同时替换 pattern 与 PCM 音色。播放中两者在同一个 7→0 边界提交，暂停时立即生效。
- 合成差异扩大为暗暖低通/磁带抖动、硬门限锯齿/方波/FM、失真 power-chord/噪声鼓组三类；回归测试要求任意同音色跨风格 PCM cosine similarity 绝对值 `<0.72`。
- 最终 `testDebugUnitTest assembleDebug` 通过；新版覆盖安装到模拟器 `emulator-5554`，PID `12294`，fresh crash buffer 为空。截图 `artifacts/emulator-20260814-stable-anchor-presets-fixed2.png` 显示标题、双侧音色库、8 格轨道和控制区围绕中心轴完整出现。
- 同一 APK 已覆盖安装到真机 `PB314XHGKC160016G`，PID `23632`，fresh crash buffer 为空；启动前停止了占用 Full Space 的 `com.pico.swan.airribbon`。
- 真机 ADB 截图失败：`ADB screencap returned non-PNG output: Failed to take screenshot. Capturing failed.`；实际左右居中与三套听感仍需头显内人工确认。

## 风格保留轨道与手动校准

- 用户反馈风格切换会删除已拖入内容；根因是 `Preset` 事件直接调用 `sequencer.edit { Pattern.forAtmosphere(...) }`，播放中还会把整套编排放入 pending。
- 现改为风格按钮只切换八种 PCM 音色，active/pending pattern 均保持不变；播放中音色仍在 7→0 边界生效。只有明确点击“示例”才替换编排。
- 新增顶部“校准位置”按钮。每次点击递增 calibration request，销毁旧 Stage 根实体，并在新的 `SpatialView.initial` 中按当前相机姿态创建 `TrackingMode.ONCE` 锚点；完成后不随视线移动。
- 回归测试覆盖暂停/播放两种风格切换均保持用户 pattern，以及重复校准请求编号；最终 `testDebugUnitTest assembleDebug` 通过。
- 同一 APK 已覆盖安装到真机 `PB314XHGKC160016G`，最终复查 PID `29900`，Activity 为 resumed/focused，fresh crash buffer 为空。
- 模拟器 `emulator-5554` 冷重启后 Spatial compositor 持续输出黑色大块，连恢复上一已通过的 `HomeStage` 基线也仍复现；因此本轮模拟器截图不作为视觉通过证据。空间点击和校准后的实际方向需在头显中人工确认。

## 随机生成轨道

- 控制区新增“随机生成”。每个 step 独立生成鼓点槽与旋律槽，鼓点填充概率 55%、旋律填充概率 40%，其余保持为空；允许出现空格，也不强制整条轨道非空。
- 暂停时随机结果立即生效；播放中复用 `QuantizedSequencer.edit` 进入 pending，只在下一次 7→0 边界整体提交。随机生成不会切换 Lo-fi / 电子 / 轻摇滚音色。
- `RandomPatternTest` 使用固定随机种子覆盖 8-step 长度、family 合法性、空槽存在性、结果变化及播放中量化提交；`testDebugUnitTest assembleDebug` 通过。
- APK 已安装并启动于模拟器 `emulator-5554`（PID `6706`，crash buffer 为空）和真机 `PB314XHGKC160016G`（PID `6608`，Activity resumed/focused，crash buffer 为空）。模拟器 Spatial compositor 黑块问题仍在，截图 `artifacts/emulator-20260814-randomize.png` 仅记录该环境边界，不作为按钮视觉通过证据。

## 手柄按钮校准与全自动风格锁定

- 顶部恢复“校准位置”按钮；空间点击按 `InteractionKind.Pointer` 过滤，仅接受手柄射线，明确拒绝 DirectPinch、Poke、GazePinch、RayBasedPinch 与 Unknown。没有恢复独立物理按键校准。
- 校准不再重建 `SpatialView`、根实体或 Attachment；只在现有 `AnchorComponent` 上更新相机目标、偏移和 `TrackingMode.ONCE`。真机临时探针连续执行 3 次，三次 request 均生效，进程保持前台，未出现 `onSpaceViewDestroyed`、`destroyShm`、FATAL 或 crash buffer 记录。探针已从最终包删除。
- `AutoBarGenerator` 现只生成随机 `Pattern`。全自动每次 7→0 边界只替换八格内容，不再写入 pending/active atmosphere；教程、状态文案与 README 已同步为“当前风格不变”。
- 最终 `testDebugUnitTest assembleDebug` 通过，SpatialUI 样式校验 0 error / 0 warning。最终 APK 已覆盖安装并启动于真机 `PB314XHGKC160016G`（PID `9515`）和模拟器 `emulator-5554`（PID `22228`），两端 crash buffer 均为空。
- 模拟器截图 `artifacts/emulator-20260814-controller-calibration-auto-style.png` 可见顶部“校准位置”按钮；下方仍受模拟器空间合成黑块影响。手柄射线实际点击和校准方向仍需头显内人工确认。

> 2026-08-14 最终回退：用户确认运行中校准无效后，已删除校准按钮、输入过滤、校准状态和锚点更新。当前版本只在应用启动时创建一次性相机锚点；全自动保持当前风格的修改继续保留。
