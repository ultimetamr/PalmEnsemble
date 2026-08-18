# 视觉系统规格 · PalmEnsemble

> 当前修订：r6（live Stage 11 design system）。

> Live Stage 11 重新读取 Interaction r10、Visual r5、PM r8、UXR r5；SoundOrb/BeatTrack/ControlDeck 八段组件结构与覆盖表保持完整。

## 1. 空间视觉方向候选

| 方向 | 空间论点 | 首屏构图 | 深度/层级 | 交互线索 | 空间价值 | Dashboard 风险 |
|---|---|---|---|---|---|---|
| A 月光节拍温室 | 声音像温暖发光种子落入时间苗床 | 中央低矮八格轨，鼓球左、旋律球右，控制带悬于下方 | 轨道 z=-1.6m；pending 球高 6cm；扫描光前景 | 球轻呼吸、格子磁吸圈 | 强实体隐喻 | 低，但过度柔和可能削弱节奏感 |
| B 夜航信号台 | 八格像跑道灯，声音是不同航标 | 中央横向轨道高对比，左右声库像信标簇 | 扫描束最靠前，轨道为哑光实体 | 硬边吸附、短脉冲 | 时间方向最清楚 | 低，但容易科技仪表化 |
| C 玩具积木桌 | 球与格子像彩色幼教积木 | 明亮实体桌、强图标、大中文 | 全部厚实、低深度 | 弹跳/卡扣 | 上手最直观 | 中，可能显幼稚且高饱和疲劳 |

选定 A“月光节拍温室”，理由：最符合 Lo-fi/轻量创作情绪，同时保留 B 的清晰扫描线；拒绝 B 是因容易像专业控制台，拒绝 C 是因饱和与幼态风险。设计效果结构化评审：单一焦点、中文可读、非 DAW、pending 深度语义、三类氛围适配均 pass；视觉舒适仍需设备验证。本记录作为 visual-direction 的结构化设计效果批准，不替代用户审美确认。

Preview 指令：深蓝黑环境、低饱和雾面轨道、八颗球使用固定颜色+几何图标；中间 8 格必须最大；播放态显示窄扫描光柱与当前格边缘光，不移动镜头。

## 2. 设计 Tokens

| 类别 | 结构化值 |
|---|---|
| tokens | `accent=#78E6D0`, `surface=#182132`, `surfaceRaised=#243047`, `text=#F4F7FB`, `muted=#A9B4C6`, `pending=#F3C969`, `danger=#FF7B7B`, `brandPrimary=#9DD7FF` |
| typography | `display={sans,32sp,40sp,700}`, `title={sans,22sp,28sp,650}`, `metric={mono,18sp,24sp,600}`, `body={sans,16sp,22sp,500}`, `caption={sans,13sp,18sp,500}` |
| scale.spacing | `xs=4dp,s=8dp,m=16dp,l=24dp,xl=32dp` |
| scale.radius | `s=12dp,m=20dp,l=32dp` |
| scale.icon | `s=20dp,m=28dp,l=40dp` |

| semantic key | color | shape | label | desc | aliases |
|---|---|---|---|---|---|
| active | #78E6D0 | circle | 已生效 | 当前循环会发声 | active,已生效 |
| pending | #F3C969 | dashed | 下一圈生效 | 等待 bar boundary | pending,下一圈 |
| playing | #9DD7FF | diamond | 正在播放 | 扫描进行中 | playing,播放 |
| error | #FF7B7B | triangle | 保存失败 | 可重试且事件仍在内存 | error,失败 |
| empty | #68758B | square | 空格 | 无声音 | empty,空 |

- Stage 材质：`trackMat={matte,glassStyle=none,opacity=1}`；`orbMat={matte,none,1}`；`scanMat={emissive matte,none,0.72}`。Stage 禁用/不依赖 Window glass。
- AttachmentPanel：`PicoTheme`；使用系统 `Material.Regular` 根玻璃且不绘制 root 背景；Vibrant=medium 仅用于单色文字/图标，不用于渐变。
- 环境：Mixed Stage 下轨道背板为不透明深色，保证 passthrough 对比；不使用大块高饱和色。正文≥16sp，命中≥56dp，颜色+形状+文字/图标冗余。

## 3. 核心组件结构

### 3.1 SoundOrb（空间音色球）

| 基础字段 | 值 |
|---|---|
| derivedFromTasks | T2,T3,T5 |
| derivedFromData | `SoundDefinition`, `OrbPlacement`, `PreviewEvent` |
| purpose | 选择、试听并把音色放入时间格 |
| layoutRole | supporting source feeding primary track |
| priority | primary |
| runtimeRole | draggableSoundSource |

**Anatomy · Layout**

```text
world anchor: DrumBank(-1.05,1.25,-1.45) / MelodyBank(+1.05,1.25,-1.45)
   ┌ sphere Ø0.13m ┐
   │ icon plane    │ front +Z, 0.07m
   │ label below   │ Attachment label, 56×24dp
   └ hit sphere Ø0.18m ┘
vertical gap 0.19m; faces user; metric Stage space
```

**Anatomy · Sizing**

| Tier | Size | Window/Stage fit |
|---|---|---|
| Regular | visual Ø0.13m / hit Ø0.18m | Stage default reach |
| Compact | same size, anchor x ±0.82m | Compact reach; no scaling |
| Constrained | same hit size; banks become 2×2 near each side | Seated/narrow reach |

**Anatomy · Internal Metrics**

| Metric | Value | Source |
|---|---|---|
| background | customColor per sound | no glass in Stage |
| radius | sphere Ø0.13m | world metric |
| padding | N/A, 3D sphere | Stage component |
| gap | label 0.03m below | geometry |
| stroke | focused 0.006m #F4F7FB | active semantic |
| icon | 0.07m monochrome symbol | sound catalog |
| text | caption 13/18/500 | typography |
| hitTarget | collision sphere Ø0.18m | controller tolerance |

**Render Elements**

| id | label | type | bind | role |
|---|---|---|---|---|
| `orb.body` | — | 3D sphere | `sound.color` | identity |
| `orb.icon` | 鼓/旋律图标 | icon plane | `sound.icon` | non-color identity |
| `orb.label` | 底鼓等 | text | `sound.nameZh` | readable identity |
| `orb.ring` | 下一圈 | dashed ring | `placement.status` | pending semantic |

**Data Bindings**

| Source | Target | fallback | type |
|---|---|---|---|
| `sound.nameZh` | label.text | “未知音色” | display |
| `sound.icon` | icon.asset | family default | display |
| `sound.color` | body.material | brandPrimary | display |
| `placement.status` | ring visibility/style | available | semantic |

**Variants**：`kick/square`, `snare/triangle`, `hat/diamond`, `clap/circle`, `chord/square`, `bass/triangle`, `bell/diamond`, `lead/circle`；鼓放左、旋律放右，结构不变。

**States**

| State | Trigger | Visual | Size/motion | Accessibility | stacking |
|---|---|---|---|---|---|
| available | 未放置 | full color | static | 名称+试听 | base |
| focused | ray/gaze | white stroke | 1.05×/120ms | haptic tick | over available |
| grabbed | trigger down | trail + ghost slot | follows controller | continuous tone preview once | over focused |
| pending | drop while playing | dashed gold ring, +0.06m | snap 180ms | “下一圈生效” | over placed |
| placed | committed | dim bank copy + active slot copy | no pulse | icon retained | active |
| disabled | recording save only state | 45% opacity | none | label “暂不可用” | overrides focused |
| error | audio unavailable | red triangle badge | none | text error | highest |

Precedence: error > disabled > grabbed > focused > pending > placed > available。

### 3.2 BeatTrack（八格时间轨）

| 基础字段 | 值 |
|---|---|
| derivedFromTasks | T3,T4,T5,T6 |
| derivedFromData | `activePattern`, `pendingPattern`, `currentStep`, `isPlaying` |
| purpose | 显示、接收放置并扫描 8-step 单小节 |
| layoutRole | primary hero |
| priority | primary |
| runtimeRole | sequencerGrid |

**Anatomy · Layout**

```text
TrackAnchor(0,1.05,-1.55), faces +Z
┌─S0─┬─S1─┬─S2─┬─S3─┬─S4─┬─S5─┬─S6─┬─S7─┐  width 1.60m
│ drum(front slot z+0.025) / melody(back slot z-0.025) │
└────┴────┴────┴────┴────┴────┴────┴────┘
scanner thin plane travels center-to-center; step label below
```

**Anatomy · Sizing**

| Tier | Size | Fit |
|---|---|---|
| Regular | 1.60×0.24×0.06m; cell 0.18m gap 0.02m | Stage default |
| Compact | 1.44×0.22×0.06m; hit remains 0.18m via overlap tolerance | reach compact |
| Constrained | 1.28×0.22×0.06m; visual narrower, collision remains 0.17m | seated narrow |

**Anatomy · Internal Metrics**

| Metric | Value | Source |
|---|---|---|
| background | customColor #182132 | opaque Stage backing |
| radius | visual corner 0.025m | spatial geometry |
| padding | 0.02m ends | track |
| gap | 0.02m cells | track |
| stroke | 0.004m muted; active 0.008m | semantics |
| icon | 0.045m per occupied slot | sound icon |
| text | metric 18/24/600 step number | typography |
| hitTarget | each cell 0.18×0.24×0.10m collision | controller |

**Render Elements**

| id | label | type | bind | role |
|---|---|---|---|---|
| `track.cell.0..7` | 1..8 | 3D cell | patterns | drop target |
| `track.activeOrb` | sound icon | mini sphere | activePattern | committed sound |
| `track.pendingOrb` | 下一圈 | ghost sphere+ring | pendingPattern | pending change |
| `track.scanner` | — | emissive plane | currentStep | playhead |
| `track.status` | 下一圈生效 | text badge | hasPending | comprehension |

**Data Bindings**

| Source | Target | fallback | type |
|---|---|---|---|
| `activePattern[step][family]` | activeOrb | empty square | semantic |
| `pendingPattern[step][family]` | pendingOrb | active value | semantic |
| `currentStep` | scanner transform/cell stroke | hidden when paused | semantic |
| `hasPending` | status visibility | false | semantic |

**Variants**：`empty`, `exampleLoaded`, `userPattern`; geometry identical，only occupancy differs。

**States**

| State | Trigger | Visual | Motion | Accessibility | stacking |
|---|---|---|---|---|---|
| empty | no sounds | square outlines + “把球放这里” | none | clear text | base |
| editing | paused | active orbs | hover cell outline | step numbers | over empty |
| dragTarget | grabbed orb near | nearest cell bright | 120ms | haptic on target change | over editing |
| playing | clock active | scanner + current diamond | 300ms/step | sound+number | over editing |
| pending | active≠pending | ghost + dashed gold | commit 160ms | “下一圈生效” | over playing |
| error | clock/audio error | red triangle status | scanner stops if clock fails | error text | highest |
| boundaryDisabled | save/transition | targets disabled | none | “请稍候” | overrides hover |

Precedence: error > boundaryDisabled > dragTarget > pending > playing > editing > empty。

### 3.3 ControlDeck（附着控制带）

| 基础字段 | 值 |
|---|---|
| derivedFromTasks | T1,T4,T6,T7,T8,T9 |
| derivedFromData | playback,preset,recording,saveStatus |
| purpose | 承载非空间化的低复杂度控制和可信状态 |
| layoutRole | supporting control |
| priority | secondary |
| runtimeRole | transportControl |

**Anatomy · Layout**

```text
AttachmentPanel @ ControlAnchor(0,0.72,-1.48)
┌ 氛围: [Lo-fi][电子][轻摇滚] │ [示例] [▶/Ⅱ] [清空] [●记录] │ 100 BPM / 状态 ┐
1 row Regular; Compact wraps status to row2; all controls ≥56dp
```

**Anatomy · Sizing**

| Tier | Size | Fit |
|---|---|---|
| Regular | 1040×104dp | Stage attachment, default |
| Compact | 760×176dp, 2 rows | narrow reach |
| Constrained | 560×240dp, 3 rows; labels retained | seated/narrow |

**Anatomy · Internal Metrics**

| Metric | Value | Source |
|---|---|---|
| background | system glass Material.Regular | AttachmentPanel Window UI |
| radius | l=32dp | scale |
| padding | m=16dp | scale |
| gap | s=8dp / m=16dp groups | scale |
| stroke | 1dp active semantic | tokens |
| icon | m=28dp | scale |
| text | body 16/22/500; metric 18/24/600 | typography |
| hitTarget | min 56×56dp | PICO floor |

**Render Elements**

| id | label | type | bind | role |
|---|---|---|---|---|
| `control.preset` | Lo-fi/电子/轻摇滚 | Segmented control | presetId | atmosphere |
| `control.example` | 加载示例 | SpatialUI Button | — | onboarding |
| `control.play` | 播放/暂停 | SpatialUI Button | isPlaying | transport |
| `control.clear` | 清空 | SpatialUI Button | hasContent | destructive |
| `control.record` | 记录 30 秒 | SpatialUI Button | recording | event capture |
| `control.status` | 100 BPM · 下一圈生效 | text/status | status | trust |

**Data Bindings**

| Source | Target | fallback | type |
|---|---|---|---|
| `presetId` | preset selected+label | Lo-fi | semantic |
| `isPlaying` | play icon+label | 暂停 | semantic |
| `hasContent` | clear enabled | false | semantic |
| `recording.remainingMs` | record label | 30 秒 | display |
| `saveStatus` | status label | “事件序列（非音频）” | semantic |

**Variants**：Regular/Compact/Constrained reflow；`demo` 将示例按钮置于主按钮前，`experienced` 位置不变但无 coachmark。

**States**

| State | Trigger | Visual | Size/motion | Accessibility | stacking |
|---|---|---|---|---|---|
| default | idle | system glass + text | none | controller focus order | base |
| focused | ray/gaze | `spatialHoverEffect` | built-in | haptic | over base |
| playing | isPlaying | pause icon + playing diamond | 150ms crossfade | label “暂停” | over default |
| pending | hasPending | gold dashed badge | none | text | over playing |
| recording | recording | red circle + countdown | 1s text update, no flash | label “事件记录” | over pending |
| disabled | unavailable | LocalDisableAlpha | none | readable reason | overrides focused |
| error | save failure | triangle + 重试 | 200ms crossfade | live announcement | highest |

Precedence: error > disabled > recording > pending > playing > focused > default。

## 4. 完整性与覆盖核对

| Core Component | base | layout | sizing | metrics | render | bindings | variants | states | verdict |
|---|---|---|---|---|---|---|---|---|---|
| SoundOrb | yes | yes | yes | yes | yes | yes | yes | yes | pass |
| BeatTrack | yes | yes | yes | yes | yes | yes | yes | yes | pass |
| ControlDeck | yes | yes | yes | yes | yes | yes | yes | yes | pass |

| 数据实体/变量 | 及时性 | binding | 表达 | 缺口 |
|---|---|---|---|---|
| Pattern active/pending | bar boundary | BeatTrack patterns | solid/dashed+depth | none |
| step/isPlaying | 300ms | BeatTrack scanner, ControlDeck | light+diamond+text | none |
| SoundDefinition/placement | static/immediate | SoundOrb | color+icon+name+audio | none |
| preset | boundary | ControlDeck | segmented label | none |
| RecordingEvent/remaining | event/immediate | ControlDeck | countdown + non-audio label | none |
| saveStatus | on save | ControlDeck | human label/error | none |

| 决策输出 | 类型 | 组件交互 | 缺口 |
|---|---|---|---|
| T1 demo choice | actionable | ControlDeck.example click | none |
| T2 soundId | actionable | SoundOrb focus/preview/grab | none |
| T3 placement | actionable | BeatTrack drop target | none |
| T4 play | actionable | ControlDeck.play/A | none |
| T5 nextPattern | actionable | SoundOrb+BeatTrack pending | none |
| T6 empty pending | actionable | clear dialog | none |
| T7 presetId | actionable | ControlDeck.preset | none |
| T8 saved event file | actionable | ControlDeck.record/status | none |
| T9 exit | actionable | system Back/exit | intentionally system-owned |

| Primary subcomponent | substates | primitive | binding |
|---|---|---|---|
| SoundOrb body/ring | available/focused/grabbed/pending/placed/disabled/error | sphere/stroke/ring/badge | SoundDefinition/placement |
| BeatTrack cell/orb/scanner | empty/editing/dragTarget/playing/pending/error/boundaryDisabled | cell/sphere/plane/text | patterns/currentStep/clock |
| ControlDeck preset/transport/status | default/focused/playing/pending/recording/disabled/error | SpatialUI controls/icon/text/badge | preset/playback/recording/saveStatus |

## 5. 数据信任、素材与最低门

- displayOnlyPaths：`sound.nameZh`, `recording.remainingMs`, 固定 `100 BPM`；null fallback 分别为“未知音色”“30 秒”“100 BPM”。
- semanticEnumPaths：`placement.status`, `saveStatus`, `isPlaying` 映射 §2 semantic label，不显示 raw enum。
- 数据状态：本地静态无需 network freshness；save 为 `idle/saving/saved/error`，错误不冒充成功且保留内存事件可重试。
- 八音色：底鼓(方)、军鼓(三角)、踩镲(菱形)、拍手(圆)、暖和弦(方)、圆润贝斯(三角)、微光铃(菱形)、柔和主音(圆)。由应用内程序合成 WAV 生成，工程记录生成参数，不使用版权曲库。
- 3D 素材：运行时 primitive sphere/box/plane，无外部模型和贴图；总实体预算 <80；无 LOD 需求。
- 音频：mono PCM WAV 48kHz/16-bit，短 one-shot；SoundPool 预载；试听与序列复用同源文件。
- Visual direction pass；tokens pass；Stage/AttachmentPanel 结构 pass；3 个核心组件八段结构 pass；覆盖表无缺口；数据状态/错误/非音频文案 pass。

`minimumCompletenessGate=pass`
