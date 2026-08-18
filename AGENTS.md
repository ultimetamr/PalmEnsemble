# PalmEnsemble agent notes

- This is a PICO Spatial SDK 0.13.3 Android/Kotlin Stage app.
- Package: `com.pico.swan.palmensemble`.
- Keep the musical contract fixed at 100 BPM, 300 ms per eighth-note step, 8 steps / 2400 ms per bar, commit pending edits only at 7→0.
- UI must use SpatialUI + `PicoTheme`; do not add Material/Material3.
- Recording is local event-sequence JSON, explicitly not audio.
- Do not add song import, auto-composition, complex mixing, or network sharing.
- Run unit tests and `assembleDebug` before handoff.
- Side-bank visibility regression: keep DRUMS/MELODY centers near `x=±0.54m, z=-0.02m`; emulator evidence is in `artifacts/emulator-20260814-banks-fix-*.png`.
- Sound balls are regular Compose controls inside `AttachmentPanel`, so their controller drag path uses Compose-style `detectDragGestures` (not the ECS-targeted `detectSpatialDragGesture`). Hold the controller trigger, move toward the center track, wait for a numbered step to highlight, then release. A placed ball can be grabbed and moved beyond the 96dp discard threshold; release while the alert state is visible to remove it. Click-select/click-step and click-to-remove remain fallbacks. Mapping lives in `DragDropMapper.kt`; keep its left/right/discard tests green.
- Lo-fi / electronic / light-rock buttons replace the complete eight-sound timbre palette but must never edit the user's current or pending 8-step pattern. While playing, timbre alone queues and commits at the 7→0 bar boundary; paused changes apply immediately. `Pattern.forAtmosphere` remains only for the explicit “load example” action, where replacing the arrangement is expected.
- `Pattern.random(Random)` generates all eight steps with independent optional drum (55% fill) and melody (40% fill) slots, so empty slots and even an all-empty result are valid. The “随机生成” action replaces the pattern immediately while paused or queues it for the next 7→0 boundary while playing; it never changes atmosphere.
- Auto mode uses `AutoBarGenerator`: at every 7→0 boundary it queues a new random pattern while preserving the currently selected atmosphere. The ordinary play action pauses/resumes without disarming auto; disabling auto preserves the current group.
- Visual baseline is opaque near-black `PicoTheme.colorScheme.fillPrimary` panels, not transparent gray glass. Accent colors are limited to sound balls, scanner/selection, and alerts; the four left/right sound rows mirror the same peach/yellow/blue/purple palette.
- Modal depth is centralized in `StagePanelDepths.kt`: clear confirmation stays at `+0.24m`, in front of the track (`0m`) and drag overlay (`+0.10m`). Positive Stage Z points toward the user; keep the depth regression test green.
- Beat cells do not use a parent-wide click target: the numbered 32dp header is the placement target, while the upper drum and lower melody balls own separate click/drag targets and unique `step:family` drag keys. This prevents lower-ball controller hits from bubbling to the cell and reusing an upper-ball selection.
- Delay creation of `SpatialView` by `StageLaunchPlacement.ANCHOR_SETTLE_MS=650ms`; create exactly one camera `TrackingMode.ONCE` anchor at `(0, -0.04, -1.35)m` and keep it for the session. Dynamic calibration is rolled back: both keyed SpatialView recreation and runtime Anchor→Transform switching made the real-device surface disappear. Do not add HMD calibration, gaze following, `TrackingMode.CONTINUOUS`, or runtime anchor replacement without a device-verified lifecycle design.
