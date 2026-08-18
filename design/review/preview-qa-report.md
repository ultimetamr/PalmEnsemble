# Preview / QA 报告 · PalmEnsemble

> 当前修订：r11（live Stage 14 final reviewer-owned record）。范围：`web_design_validation_only`。

> Live 输入为 Interaction r10 + Visual r6 + Stage12 pass；Preview r4 已刷新来源注释，DOM 实现事实未变，所有 mapping 已按当前源重新核对。

## 1. Input Readiness

| Input | Evidence | Verdict |
|---|---|---|
| design-system review | `PE-DCR-LIVE-I10-V6-20260813-01`, Visual r6 | pass |
| states/transitions | Interaction r10 §9 | pass |
| core 8-part components | Visual r6 §3 + checklist | pass |
| stable elements/bindings | Visual r6 §3 | pass |
| variants/states/precedence | Visual r6 §3 | pass |
| responsive + Reduce Motion | Interaction r10 §10/§12, Visual r6 sizing | pass |
| visual grammar | Visual r6 §2 | pass |

## 2. Preview Coverage Manifest

### 2.1 States and transitions

| Item | Source | Trigger | Expected |
|---|---|---|---|
| state.ready_demo | Interaction §9 | mode button / start | demo choice visible |
| state.editing | Interaction §9 | pause / self try | scanner hidden, drop enabled |
| state.playing | Interaction §9 | play | scanner advances |
| state.playing_pending | Interaction §9 | edit while playing | gold pending + next-loop copy |
| state.recording_events | Interaction §9 | record | countdown, non-audio copy |
| state.confirm_clear | Interaction §9 | clear | blocking confirm dialog |
| transition.user.enterStage | Interaction §9 | enter button | confirm then ready_demo |
| transition.user.loadExample | Interaction §9 | example button | populated pattern + playing |
| transition.user.dropOrb | Interaction §9 | click orb then cell | pattern changes/pending |
| transition.clock.barBoundary | Interaction §9 | simulated wrap | pending becomes active |
| transition.user.togglePlayback | Interaction §9 | play button | editing↔playing |
| transition.user.clear | Interaction §9 | clear button | confirm_clear dialog |
| transition.user.record | Interaction §9 | record button | recording_events |
| transition.user.exitStage | Interaction §9 | exit button | blocking confirm dialog |

### 2.2 renderSpec elements

| Component.element | Source | Label/bind | Expected |
|---|---|---|---|
| SoundOrb.body | Visual §3.1 | sound.color | colored sphere |
| SoundOrb.icon | Visual §3.1 | sound.icon | shape icon |
| SoundOrb.label | Visual §3.1 | sound.nameZh | Chinese label |
| SoundOrb.ring | Visual §3.1 | placement.status | pending dashed ring |
| BeatTrack.cell | Visual §3.2 | pattern | 8 drop targets |
| BeatTrack.activeOrb | Visual §3.2 | activePattern | solid placed sound |
| BeatTrack.pendingOrb | Visual §3.2 | pendingPattern | ghost pending sound |
| BeatTrack.scanner | Visual §3.2 | currentStep | current playhead |
| BeatTrack.status | Visual §3.2 | hasPending | next-loop status |
| ControlDeck.preset | Visual §3.3 | presetId | 3 atmosphere choices |
| ControlDeck.example | Visual §3.3 | action | example button |
| ControlDeck.play | Visual §3.3 | isPlaying | play/pause |
| ControlDeck.clear | Visual §3.3 | hasContent | clear |
| ControlDeck.record | Visual §3.3 | recording | 30s event record |
| ControlDeck.status | Visual §3.3 | status | BPM/pending/save type |

### 2.3 dataBindings

| Binding | Source | Normal | Fallback/error |
|---|---|---|---|
| sound.nameZh | Visual §3.1 | 底鼓 | 未知音色 |
| sound.icon | Visual §3.1 | shape | family default |
| sound.color | Visual §3.1 | sound color | brandPrimary |
| placement.status | Visual §3.1 | available/pending | available |
| activePattern | Visual §3.2 | placed sound | empty square |
| pendingPattern | Visual §3.2 | ghost change | active value |
| currentStep | Visual §3.2 | 0..7 | scanner hidden paused |
| hasPending | Visual §3.2 | true copy | false/hidden |
| presetId | Visual §3.3 | Lo-fi | Lo-fi fallback |
| isPlaying | Visual §3.3 | play/pause | paused |
| hasContent | Visual §3.3 | enabled | disabled |
| recording.remainingMs | Visual §3.3 | countdown | 30 seconds |
| saveStatus | Visual §3.3 | event sequence copy | save error/retry |

### 2.4 variants, states and stacking

| Component | Item | Source | Trigger/expected |
|---|---|---|---|
| SoundOrb | 8 sound variants | Visual §3.1 | sound selector changes color/icon/name |
| SoundOrb | available/focused/grabbed/pending/placed/disabled/error | Visual §3.1 | component-state selector changes ring/stroke/opacity/badge |
| SoundOrb | error>disabled>grabbed>focused>pending>placed>available | Visual §3.1 | stack test chooses highest |
| BeatTrack | empty/exampleLoaded/userPattern | Visual §3.2 | pattern selector changes occupancy |
| BeatTrack | empty/editing/dragTarget/playing/pending/error/boundaryDisabled | Visual §3.2 | scene/state triggers visible scanner/ghost/error |
| BeatTrack | error>boundaryDisabled>dragTarget>pending>playing>editing>empty | Visual §3.2 | stack test chooses highest |
| ControlDeck | Regular/Compact/Constrained/demo/experienced | Visual §3.3 | tier/demo selector reflows or hides coachmark only |
| ControlDeck | default/focused/playing/pending/recording/disabled/error | Visual §3.3 | state/sample controls change label/badge |
| ControlDeck | error>disabled>recording>pending>playing>focused>default | Visual §3.3 | stack test chooses highest |

### 2.5 responsive / motion

| Scenario | Source | Mapping | Expected |
|---|---|---|---|
| Large | Visual sizing | Regular/default | one-row deck, wide banks |
| Compact | Visual sizing | Compact/min | two-row deck, banks nearer |
| Constrained | Visual sizing | Constrained | three-row deck, 2x2 bank layout |
| Reduce Motion | Interaction §10 | accessibility | scanner becomes cell outline, no translate/scale |

## 3. Generation Mapping

### 3.1 States / transitions → scene

| Item | Selector / trigger | Actual visible result | Verdict |
|---|---|---|---|
| ready_demo | `#sceneState=ready_demo` | coachmark + demo deck | pass |
| editing | state select / pause | scanner hidden, “可以编辑” | pass |
| playing | state select / play | current cell + scanner | pass |
| playing_pending | select orb then cell | dashed ghost + “下一圈生效” | pass |
| recording_events | `#record` | 30s countdown + event wording | pass |
| confirm_clear | `#clear` | `#confirm.open` blocks scene | pass |
| user.enterStage | `#showLauncher`→`#enterStage`→blocking `#confirm(kind=enter)` | 确认后 Launcher 关闭、Stage 显现，进入 ready_demo 且播放保持暂停 | pass |
| user.loadExample | `#example` | example occupancy + play | pass |
| user.dropOrb | `.orb` then `.cell` | pending or active slot | pass |
| clock.barBoundary | wait until step wrap | pending count 1→0, active slot persists | pass |
| user.togglePlayback | `#play` | label/scanner changes | pass |
| user.clear | `#clear` | confirm/cancel; confirm writes pending empty | pass |
| user.record | `#record` | recording_events/countdown | pass |
| user.exitStage | `#exit` | exit confirmation/cancel | pass |

### 3.2 renderSpec → DOM

| Element | Selector | Actual | Verdict |
|---|---|---|---|
| orb.body/icon/label/ring | `[data-preview-id^=orb.body/icon/label/ring]` | 8 distinct spheres, shapes, Chinese names, conditional ring | pass |
| track.cell | `[data-preview-id^=track.cell]` | 8 unique cells | pass |
| track.activeOrb | `[data-preview-id^=track.activeOrb]` | solid occupied slot | pass |
| track.pendingOrb | `[data-preview-id^=track.pendingOrb]` | dashed ghost when pending | pass |
| track.scanner | `[data-preview-id=track.scanner]` | moves per currentStep / hidden paused | pass |
| track.status | `[data-preview-id=track.status]` | human semantic label | pass |
| control.preset | `[data-preview-id=control.preset]` | 3 atmosphere buttons | pass |
| control.example | `[data-preview-id=control.example]` | load example | pass |
| control.play | `[data-preview-id=control.play]` | play/pause | pass |
| control.clear | `[data-preview-id=control.clear]` | clear + dialog | pass |
| control.record | `[data-preview-id=control.record]` | event recording countdown | pass |
| control.status | `[data-preview-id=control.status]` | BPM/pending/error/non-audio | pass |

### 3.3 bindings → normal/fallback

| Binding group | DOM/JS evidence | Normal | fallback/error | Verdict |
|---|---|---|---|---|
| sound.nameZh/icon/color | `orbHtml()`, orb selectors | 8 catalog values | checkbox→未知音色/?/brand | pass |
| placement.status | orb class/ring | available | pending/default fallback | pass |
| activePattern/pendingPattern | `slotsFor()`, track slots | solid sound | empty/active fallback, dashed pending | pass |
| currentStep/hasPending | `renderTrack()` | moving current, status | hidden paused / false hidden | pass |
| presetId | `[data-preset]` | selected label | Lo-fi initial | pass |
| isPlaying/hasContent | `renderDeck()` + controls | pause/clear enabled | play/empty semantics | pass |
| recording.remainingMs | `startRecord()` | countdown | reset 30s | pass |
| saveStatus | save error checkbox | event sequence copy | error + retained memory/retry | pass |

### 3.4 variants / states / precedence

| Component | Trigger | Actual | Verdict |
|---|---|---|---|
| SoundOrb 8 variants | rendered banks / choose each | distinct name/color/shape/family | pass |
| SoundOrb 7 states | `#orbState` | class-driven stroke/move/ring/opacity/error | pass |
| SoundOrb precedence | `#stackTest` | error visible as highest | pass |
| BeatTrack 3 variants | empty/example/user via state/actions | occupancy changes | pass |
| BeatTrack 7 states | `#trackState` + scene | 每态写入 `data-render-state`；empty 清内容、dragTarget 高亮、pending 金边、boundaryDisabled 禁用、error 危险态，playing 扫描 | pass |
| BeatTrack precedence | `#stackTest` | 输入组合写入 `data-stack-inputs`，按表决出 dragTarget 或 error | pass |
| ControlDeck 5 variants | tier + ready_demo | 1/2/3 rows; coachmark only demo | pass |
| ControlDeck 7 states | `#deckState`, play/pending/record/error | 每态写入 `data-render-state`；focused/playing/pending/recording/disabled/error 均有独立视觉或行为 | pass |
| ControlDeck precedence | `#stackTest` | 输入组合写入 `data-stack-inputs`，按表决出 recording 或 error | pass |

### 3.5 responsive / Reduce Motion

| Scenario | Trigger | Structural result | Actual | Verdict |
|---|---|---|---|---|
| Large | `data-tier=large` | wide banks, one-row deck | observed | pass |
| Compact | `data-tier=compact` | nearer banks, deck wraps ≤760px | observed | pass |
| Constrained | `data-tier=constrained` | bank 2×2, deck ≤560px/3 rows | `body.constrained` observed | pass |
| Reduce Motion | `#reduce` | no transitions; scanner hidden; current outline | `reduce-motion` observed | pass |

## 4. Declarative checks and generation totals

| Check | Evidence | Generation verdict |
|---|---|---|
| Manifest complete | §2: 6 states, 8 transitions, 15 render elements, 13 bindings, 9 grouped variant/state/precedence denominators, 4 responsive/motion | pass |
| state machine | `app.state`, `sceneState`, actions, `render()` | pass |
| stable DOM lookup | §3.2 selectors | pass |
| data modes | normal/fallback/save-error triggers | pass |
| high-risk confirmation | `#confirm`, clear/exit, cancel/confirm | pass |
| responsive/motion | §3.5 | pass |

Independent QA must recount from Visual r3/Interaction r5 and inspect current Preview r3; generation assertions are not QA evidence.

## 5. Independent QA（preview r1，已被 r2 取代）

- reviewerRole: `prototype_qa_reviewer`
- invocationId: `PE-PQA-FAST-01`
- contextPolicy: `isolated_subagent`
- reviewedRevision: Interaction r5 + Visual r3 + PreviewQA r2 + Preview r1
- evidenceRebuilt: yes
- deviceValidation.status: `not_performed`

| Denominator | Design fact | Manifest | QA rebuilt | Diff | Verdict |
|---|---:|---:|---:|---:|---|
| states | 6 | 6 | 6 | 0 | pass |
| transitions | 8 | 8 | 8 | 0 | pass |
| render elements | 15 | 15 | 15 | 0 | pass |
| bindings | 13 | 13 | 13 | 0 | pass |
| variant/state/precedence groups | 9 | 9 | 9 | 0 | pass |
| responsive/Reduce Motion | 4 | 4 | 4 | 0 | pass |

QA 反向确认所有 15 类 render selector、状态/渲染/action 函数、fallback/save-error、clear/exit blocking dialog 及 confirm/cancel 存在。原型实际路径为 `design/preview.html`（不是 `design/review/preview.html`），与设计师输出约定一致，不影响覆盖。

该结论仅对 Preview r1；Stage 15 复审发现状态兑现不足后，已被 Preview r2 取代，不作为当前放行证据。

## 6. Device boundary

`deviceValidation.status=not_performed`。Physical distance, comfort, fatigue, controller hit precision, audio latency and PICO runtime performance remain device-owned.

## 7. Preview r2 作者侧验证（非独立审查）

- browser target: `http://127.0.0.1:8877/preview.html`
- `ready_demo` 中可见 `开始演示`；点击后 `stage.state=editing`、播放按钮为 `▶ 播放`、轨道提示“已暂停 · 可以编辑”。
- `trackState=dragTarget` 后 `data-render-state=dragTarget` 且提示“松开手柄扳机以吸附”。
- `deckState=disabled` 后 `data-render-state=disabled`，除入场外的控制按钮均具原生 `disabled=true`。
- 第一组 precedence 输入 `orb=[available,pending,focused]`、`track=[playing,pending,dragTarget]`、`deck=[playing,pending,recording]`，输出 `focused/dragTarget/recording`，输入组合保存在 `data-stack-inputs`。
- independentReviewer: pending；本节不可替代 Stage 14 独立重建。

## 8. Preview r3 安全路径补丁（非独立审查）

- `user.enterStage`：`#showLauncher` 打开独立 Launcher 终端层；`#enterStage` 打开 blocking confirmation；确认后进入 `ready_demo` 且播放暂停。
- `user.exitStage`：确认后停止播放与记录、清零 currentStep/selected，Stage 进入 `exited`，Launcher 终端层重新可见。
- `hasContent`：由 active/pending pattern 计算；empty 或清空完成后 `control.clear.disabled=true` 并暴露 `data-has-content=false`。
- `confirm_clear cancel`：Dialog 保存打开前的 state/trackState/deckState/playing；取消恢复全部字段与 selector。浏览器复验由 playing/playing/default 恢复为同一组状态。
- independentReviewer: pending；Preview r2 的 PE-PQA-R2-RERUN-20260813-01 block 已使 §7 作者验证失效，需按 r3 重跑。

## 9. Preview r3 独立 QA

- reviewerRole: `prototype_qa_reviewer`
- invocationId: `PE-PQA-R3-RERUN-20260813-01`
- contextPolicy: `isolated_subagent`
- reviewedRevision: Interaction r5 + Visual r3 + Preview r3 + PreviewQA r6
- evidenceRebuilt: yes
- deviceValidation.status: `not_performed`

独立重建分母 6/8/15/13/9/4，全部 diff=0。Preview r3 代码 fidelity 全部通过：四个上轮阻断项、BeatTrack/ControlDeck 全状态、两轮 precedence、SoundOrb、fallback/save-error、blocking dialog、三档响应布局与 Reduce Motion 均有真实消费。该次 review 唯一 `PQA-R3-01` 是本报告 §3.1 仍写 r2 的旧 enterStage 映射；现已校正为 launcher→blocking confirm→ready_demo，且 §4 的检查版本已改为 Preview r3。该补丁只修 QA 载体，不改变设计或 Preview implementation facts。

载体复核 `PE-PQA-R3-CARRIER-R7-CHECK-20260813-01` 发生在 r7 写入并传播之前，实际读取 r6，因此以 race-condition block 结束；它确认 PQA-R3-01 内容已修正且 Preview r3 SHA256 未变化，但不能作为 r7 放行。当前文件头与 Trace ledger 已正式建立 r7，需重新复核。

最终复核 `PE-PQA-R3-R7-FINAL-20260813-01`（isolated_subagent，reviewedRevision=Interaction r5 + Visual r3 + Preview r3 + PreviewQA r7，evidenceRebuilt=yes）确认 r7 身份、映射和 6/8/15/13/9/4 全覆盖，`PQA-R3-01=closed`，recommendation=`pass`。`previewImplementationFidelity=pass`，`minimumCompletenessGate=pass`；deviceValidation 仍为 `not_performed`。

## 10. Live Stage 14

`PE-PQA-LIVE-I10-V6-P4-QA9-20260813-01`（isolated_subagent，reviewedRevision=Interaction r10 + Visual r6 + Preview r4 + PreviewQA r9，evidenceRebuilt=yes）重建 6/8/15/13/9/4 与全部实现覆盖，代码 fidelity pass；唯一 finding `PQA-LIVE-01` 是 §1 仍引用旧 I5/V3/DCR，现已逐行修正为 I10/V6 与 `PE-DCR-LIVE-I10-V6-20260813-01`。该补丁不改变 Preview r4 DOM；需对 r10 进行最终 carrier 复核。

最终 reviewer-owned 记录：`PE-PQA-LIVE-I10-V6-P4-QA10-FINAL-20260813-02`，contextPolicy=`isolated_subagent`，reviewedRevision=Interaction r10 + Visual r6 + Preview r4 + PreviewQA r10，evidenceRebuilt=yes，deviceValidation.status=`not_performed`，recommendation=`pass`。`PQA-LIVE-01=closed`；6/8/15/13/9/4 与全部 implementation fidelity 无新增阻断。
