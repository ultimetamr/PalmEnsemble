# PalmEnsemble MVP 验收记录

## 已实现

- PICO Spatial SDK `Stage` 项目，应用名 `PalmEnsemble`，包名 `com.pico.swan.palmensemble`。
- 中央 8 格轨道、左侧 4 个鼓点、右侧 4 个和弦/旋律音色，均有中文标签、颜色、图标和点选试听。
- 固定 100 BPM：每拍 600 ms、每格 300 ms、每圈 2.4 s；扫描按 `0…7→0` 循环。
- 播放时的放置、替换、移除和清空写入 `pending Pattern`，只在下一次 `7→0` 边界原子提交。
- Lo-fi、电子、轻摇滚三种氛围；一键示例；播放/暂停；二次确认清空；30 秒记录与保存。
- 手柄射线可点击所有主要操作，右手 A 播放/暂停，B 取消声音选择。
- 8 种本地实时合成音色，无版权曲库、无联网依赖。

## 录制边界

当前“记录 30 秒”保存的是带相对时间戳的本地 JSON 事件序列，不是系统音频文件。PICO 系统混音/录屏 API 未在本机资料中得到可靠确认，因此保留为明确标注的录制骨架。

## 自动验证

- `testDebugUnitTest`：通过，覆盖暂停即时编辑、播放中量化提交、多次 pending 合并、示例节奏。
- `assembleDebug`：通过。
- PICO 模拟器：安装成功，`LaunchActivity` 为 `topResumedActivity`；Spatial Runtime 报告 `type=stage`，无应用级 `FATAL EXCEPTION`。
- 工作流检查：artifact、layout、implementation、architecture 全部通过。

## 尚需 PICO 设备验证

真实物理距离与十分钟舒适度、手柄命中率、音频延迟/视听同步、PICO 设备性能尚未执行。模拟器的 Spatial compositor 无法生成可信的普通 `adb screencap`，交付截图因此为与实现状态一一对应的设计验收预览图，不冒充设备实拍。
