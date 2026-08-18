# PalmEnsemble 模拟器测试（2026-08-14）

- 目标：`Pico_Emulator_6_0` / `emulator-5554` / PICO OS 6.0 / x86_64。
- 环境：Node `v24.17.0`，`pico-cli 0.3.0 (public)`，Java 17。
- 主机验证：`testDebugUnitTest assembleDebug` 成功；42 个 Gradle task 无失败。
- 安装：`app-debug.apk` 安装成功，应用版本 `1.0 (1)`。
- 启动：停止冲突的示例 Stage `com.spatialapps.ambientzones` 后，`PalmEnsemble/.platform.LaunchActivity` 成为 `topResumedActivity` 和当前焦点。
- 静态态：中央 8 格轨道、中文引导和控制面板在 Spatial Stage 中成功渲染。
- 播放态：以 `captureMode=playing` 启动内置示例；两次截图分别显示第 8 格和第 7 格扫描高亮，进程持续存活。
- 音频路径：进程日志检测到持续的 `SpatialAudioHelper` 音色播放/释放事件。
- 稳定性：应用进程专属日志中 `FATAL EXCEPTION` / `AndroidRuntime` / 应用 ANR 标记为 0；Android crash buffer 为空。

## 证据

- `emulator-20260814-palmensemble.png`：初始轨道编辑态。
- `emulator-20260814-playing-a.png`：播放扫描态 A。
- `emulator-20260814-playing-b.png`：播放扫描态 B。

## 边界

Spatial Stage 不能用普通 2D 坐标点击可靠自动化，因此手柄射线的抓取、放置、移除、播放键和 30 秒录制按钮仍需模拟器控制器或真机人工操作。量化 `7→0` 提交由主机单元测试覆盖；真实音频延迟、舒适度和手柄命中率不由模拟器结果证明。

## 左右音色库裁切修复

- 根因：左右 AttachmentPanel 中心位于 `x=±0.72m, z=+0.12m`，在模拟器视野中只剩卡片边缘，球、标题与标签被裁到画外。
- 补丁：调整为 `x=±0.54m, z=-0.02m`，不改变容器、面板宽度、音色或交互逻辑。
- 同一颜色检测：修复前 `left=0/right=0`；最终编辑态 `left=14480/right=13167`，PASS（每侧门槛 1000）。
- 新证据：`emulator-20260814-banks-fix-final2.png` 与 `emulator-20260814-banks-fix-playing.png`。
- 环境说明：共享模拟器随后被其他正在运行的 Spatial Stage 包抢占；空房间截图来自 PalmEnsemble 已失去前台，不是本应用渲染回退。有效截图获取时 PalmEnsemble 为 `topResumedActivity`，应用 crash buffer 与 fatal marker 均为 0。

## 空间拖放增量

- 音色卡接入 `detectSpatialDragGesture`；按住扳机开始试听并抓取，移动时显示跨面板浮动球。
- 中央 8 格按拖动横向位置吸附并高亮，松手调用原有 `Place` 事件；播放中仍只在 `7→0` 小节边界提交。
- 保留“点击球 → 点击格子”的回退路径，避免新手或输入设备不支持拖动时失去编辑能力。
- 左右映射与越界拒绝新增 3 项测试；当前全量主机测试 8/8 通过。
- 最新 APK 安装到 `emulator-5554`，PalmEnsemble 为 `topResumedActivity`，PID `24027`；新截图为 `emulator-20260814-drag-enabled.png`，crash buffer 与 AndroidRuntime fatal 均为空。
- CLI 无法注入空间控制器射线/扳机，最终抓取、跨窗移动和松手命中仍需模拟器控制器或真机人工验证。

## 拖出轨道丢弃

- 已占用的鼓点/旋律球同样接入空间拖拽；移动距离超过 96dp 后，原格、浮动球和提示切换为警示丢弃态。
- 仅在警示态松手才发送 `Remove(step, family)`；未达到阈值或手势取消不会误删。点击已占用球仍可直接移除。
- 播放中丢弃继续进入 pending pattern，并由新增测试确认只在 `7→0` 边界提交，不会造成断拍。
- 当前全量主机测试 10/10 通过；最终模拟器截图为 `emulator-20260814-drag-discard.png`，安装后 PID `26422`，crash buffer 与 AndroidRuntime fatal 均为空。
