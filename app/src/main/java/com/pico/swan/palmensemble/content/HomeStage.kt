package com.pico.swan.palmensemble.content

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.pico.spatial.core.ecs.AnchorComponent
import com.pico.spatial.core.ecs.Entity
import com.pico.spatial.core.ecs.TransformComponent
import com.pico.spatial.core.ecs.anchor.AnchorTarget
import com.pico.spatial.core.math.Vector3
import com.pico.spatial.tracking.controller.ControllerAction
import com.pico.spatial.tracking.controller.ControllerTrackingProvider
import com.pico.spatial.ui.design.Button
import com.pico.spatial.ui.design.PicoTheme
import com.pico.spatial.ui.design.Text
import com.pico.spatial.ui.foundation.content.SpatialView
import com.pico.spatial.ui.foundation.haptic.controllerHapticFeedback
import com.pico.spatial.ui.foundation.hover.spatialHoverEffect
import com.pico.spatial.ui.foundation.layout.zOffset
import com.pico.swan.palmensemble.domain.model.*
import com.pico.swan.palmensemble.ui.ensemble.*
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun HomeStage(state: PalmEnsembleUiState, onEvent: (PalmEnsembleEvent) -> Unit) {
    var launchAnchorReady by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val dragMetrics = remember(density) {
        with(density) {
            DragDropMetrics(
                bankCenterFromTrackCenterPx = 650.dp.toPx(),
                firstStepCenterFromTrackCenterPx = (-406).dp.toPx(),
                stepPitchPx = 116.dp.toPx(),
                snapRadiusPx = 54.dp.toPx(),
            )
        }
    }
    val discardThresholdPx = with(density) { 96.dp.toPx() }
    var drag by remember { mutableStateOf(DragUiState()) }
    val startDrag: (SoundId) -> Unit = { sound ->
        drag = DragUiState(sound = sound)
        onEvent(PalmEnsembleEvent.SelectSound(sound))
    }
    val startDiscardDrag: (Int, SoundId) -> Unit = { step, sound ->
        drag = DragUiState(sound = sound, originStep = step)
        onEvent(PalmEnsembleEvent.SelectSound(sound))
    }
    val dragBy: (Float, Float, Float) -> Unit = { x, y, z ->
        drag.sound?.let { sound ->
            val nextX = drag.offsetX + x
            val nextY = drag.offsetY + y
            val nextZ = drag.offsetZ + z
            drag = drag.copy(
                offsetX = nextX,
                offsetY = nextY,
                offsetZ = nextZ,
                targetStep = if (drag.originStep == null) DragDropMapper.targetStep(sound.family, nextX, dragMetrics) else null,
                discardArmed = drag.originStep != null && DragDropMapper.shouldDiscard(nextX, nextY, nextZ, discardThresholdPx),
            )
        }
    }
    val finishDrag: () -> Unit = {
        val completed = drag
        drag = DragUiState()
        when {
            completed.originStep != null && completed.discardArmed -> onEvent(PalmEnsembleEvent.Remove(completed.originStep, completed.sound!!.family))
            completed.originStep != null -> onEvent(PalmEnsembleEvent.CancelSelection)
            completed.targetStep != null -> onEvent(PalmEnsembleEvent.Place(completed.targetStep))
            else -> onEvent(PalmEnsembleEvent.CancelSelection)
        }
    }
    val cancelDrag: () -> Unit = {
        drag = DragUiState()
        onEvent(PalmEnsembleEvent.CancelSelection)
    }
    ControllerShortcuts(launchAnchorReady, onEvent)
    LaunchedEffect(Unit) {
        delay(StageLaunchPlacement.ANCHOR_SETTLE_MS)
        launchAnchorReady = true
    }

    if (launchAnchorReady) {
        val root = remember { Entity().apply { setName("PalmEnsembleAnchor") } }
        DisposableEffect(root) { onDispose { root.destroy() } }
        SpatialView(
        modifier = Modifier.size(1.dp),
        attachments = {
            AttachmentPanel(id = TITLE) { TitlePanel(state, onEvent) }
            AttachmentPanel(id = DRUMS) { SoundBank("鼓点", SoundFamily.DRUM, state, onEvent, startDrag, dragBy, finishDrag, cancelDrag) }
            AttachmentPanel(id = TRACK) { BeatTrack(state, drag, onEvent, startDiscardDrag, dragBy, finishDrag, cancelDrag) }
            AttachmentPanel(id = MELODY) { SoundBank("和弦 / 旋律", SoundFamily.MELODY, state, onEvent, startDrag, dragBy, finishDrag, cancelDrag) }
            AttachmentPanel(id = DECK) { ControlDeck(state, onEvent) }
            AttachmentPanel(id = CLEAR) { if (state.clearConfirm) ClearDialog(state, onEvent) else Spacer(Modifier.size(1.dp)) }
            AttachmentPanel(id = TUTORIAL) { if (state.tutorialVisible) TutorialPanel(onEvent) else Spacer(Modifier.size(1.dp)) }
            AttachmentPanel(id = DRAG) { DragOverlay(drag) }
        },
        initial = { content, attachments ->
            root.components[AnchorComponent::class.java] = createCameraAnchor()
            content.addEntity(root)
            listOf(TITLE to Vector3(0f,.52f,.06f), DRUMS to Vector3(-.54f,.04f,-.02f), TRACK to Vector3(0f,.02f,StagePanelDepths.TRACK), MELODY to Vector3(.54f,.04f,-.02f), DECK to Vector3(0f,-.43f,.08f), CLEAR to Vector3(0f,0f,StagePanelDepths.MODAL), TUTORIAL to Vector3(0f,0f,StagePanelDepths.MODAL), DRAG to Vector3(0f,.02f,StagePanelDepths.DRAG_OVERLAY)).forEach { (id,pos) ->
                attachments.entity(id)?.apply { components[TransformComponent::class.java]?.setPosition(pos); root.addChild(this) }
            }
        },
        update = { _, attachments ->
            attachments.entity(CLEAR)?.enabled = state.clearConfirm
            attachments.entity(TUTORIAL)?.enabled = state.tutorialVisible
            attachments.entity(DRAG)?.enabled = drag.sound != null && !state.clearConfirm && !state.tutorialVisible
        },
        )
    }
}

@Composable private fun TitlePanel(state: PalmEnsembleUiState,onEvent:(PalmEnsembleEvent)->Unit) = Panel(560.dp) {
    Text("PALM ENSEMBLE", style=PicoTheme.typography.titleMedium, color=PicoTheme.colorScheme.interaction)
    Text("把声音放进时间里", style=PicoTheme.typography.titleLarge, color=PicoTheme.colorScheme.labelPrimaryLight)
    Row(horizontalArrangement=Arrangement.spacedBy(10.dp),verticalAlignment=Alignment.CenterVertically){
        Text("100 BPM · 4/4 · 每圈 8 格" + if(state.pending!=null||state.pendingAtmosphere!=null) " · 下一圈生效" else "", color=PicoTheme.colorScheme.labelPrimaryLight)
        Button(onClick={onEvent(PalmEnsembleEvent.OpenTutorial)}){Text("教程")}
    }
    Text("稳定定位：启动后保持固定，不随视线移动",color=PicoTheme.colorScheme.interaction)
}

@Composable private fun SoundBank(
    title:String,
    family:SoundFamily,
    state:PalmEnsembleUiState,
    onEvent:(PalmEnsembleEvent)->Unit,
    onDragStart:(SoundId)->Unit,
    onDragBy:(Float,Float,Float)->Unit,
    onDragEnd:()->Unit,
    onDragCancel:()->Unit,
)=Panel(260.dp){
    Text(title,style=PicoTheme.typography.titleMedium,color=PicoTheme.colorScheme.labelPrimaryLight)
    SoundId.entries.filter{it.family==family}.forEach { sound ->
        val selected=state.selected==sound
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).palmPanelDrag(sound,{onDragStart(sound)},onDragBy,onDragEnd,onDragCancel).palmClickable{onEvent(PalmEnsembleEvent.SelectSound(sound))}.background(if(selected) PicoTheme.colorScheme.interaction else OPAQUE_BLACK).padding(12.dp), horizontalAlignment=Alignment.CenterHorizontally){
            Box(Modifier.size(58.dp).clip(CircleShape).background(Color(sound.color)),contentAlignment=Alignment.Center){Text(sound.icon,color=PicoTheme.colorScheme.labelPrimary)}
            Text((if(selected)"● " else "")+sound.label,color=PicoTheme.colorScheme.labelPrimaryLight,textAlign=TextAlign.Center)
        }
    }
}

@Composable private fun BeatTrack(
    state:PalmEnsembleUiState,
    drag:DragUiState,
    onEvent:(PalmEnsembleEvent)->Unit,
    onDiscardStart:(Int,SoundId)->Unit,
    onDragBy:(Float,Float,Float)->Unit,
    onDragEnd:()->Unit,
    onDragCancel:()->Unit,
)=Panel(980.dp){
    val dragTarget=drag.targetStep
    Text(when {
        drag.originStep != null && drag.discardArmed -> "松手丢弃 ${drag.sound!!.label}"
        drag.originStep != null -> "把 ${drag.sound!!.label} 拖出轨道"
        drag.sound != null && dragTarget != null -> "松手放入第 ${dragTarget+1} 格"
        drag.sound != null -> "正在拖动 ${drag.sound.label} · 对准格子"
        state.selected == null -> "轨道编辑 · 抓球拖入，或先选球再点格号"
        else -> "已选择 ${state.selected.label} · 点击格号放入"
    },style=PicoTheme.typography.titleMedium,color=PicoTheme.colorScheme.labelPrimaryLight)
    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
        repeat(STEP_COUNT){i->
            val active=state.active.steps[i]; val pending=state.pending?.steps?.get(i); val current=state.playing&&state.currentStep==i
            val discardOrigin=drag.originStep==i
            val stepShape=RoundedCornerShape(18.dp)
            Column(Modifier.width(108.dp).height(150.dp).clip(stepShape).border(1.dp,PicoTheme.colorScheme.fillSecondary,stepShape).background(when { discardOrigin&&drag.discardArmed -> PicoTheme.colorScheme.alert; dragTarget==i||discardOrigin -> PicoTheme.colorScheme.interaction; current -> PicoTheme.colorScheme.interaction; else -> OPAQUE_BLACK }).padding(8.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.SpaceBetween){
                Box(Modifier.fillMaxWidth().height(32.dp).clip(RoundedCornerShape(10.dp)).palmClickable{onEvent(PalmEnsembleEvent.Place(i))},contentAlignment=Alignment.Center){
                    Text((if(dragTarget==i)"⌄ " else "")+"${i+1}",color=if(current||dragTarget==i)PicoTheme.colorScheme.labelPrimary else PicoTheme.colorScheme.labelPrimaryLight)
                }
                StepSound("$i:drum",active.drum,pending?.drum!=active.drum,discardOrigin&&drag.sound?.family==SoundFamily.DRUM,drag.discardArmed,{onEvent(PalmEnsembleEvent.Remove(i,SoundFamily.DRUM))},{active.drum?.let{onDiscardStart(i,it)}},onDragBy,onDragEnd,onDragCancel)
                StepSound("$i:melody",active.melody,pending?.melody!=active.melody,discardOrigin&&drag.sound?.family==SoundFamily.MELODY,drag.discardArmed,{onEvent(PalmEnsembleEvent.Remove(i,SoundFamily.MELODY))},{active.melody?.let{onDiscardStart(i,it)}},onDragBy,onDragEnd,onDragCancel)
            }
        }
    }
    Text(when { drag.originStep!=null&&drag.discardArmed -> "已移出轨道 · 松手丢弃"; drag.originStep!=null -> "继续向外拖，出现丢弃提示后松手"; drag.sound!=null&&dragTarget==null -> "按住扳机移动 · 格子亮起后松手"; drag.sound!=null -> "已吸附第 ${dragTarget!!+1} 格 · 松手放入"; state.pending!=null -> "◌ 修改已排队 · 到第 8 格后一起生效"; else -> state.status },color=if((state.pending!=null&&drag.sound==null)||drag.discardArmed)PicoTheme.colorScheme.alert else PicoTheme.colorScheme.labelPrimaryLight)
}

@Composable private fun DragOverlay(drag:DragUiState){
    val sound=drag.sound
    if(sound==null){Spacer(Modifier.size(1.dp));return}
    val density=LocalDensity.current
    val familySounds=SoundId.entries.filter{it.family==sound.family}
    val bankX=with(density){if(drag.originStep!=null)(-406+drag.originStep*116).dp.toPx() else (if(sound.family==SoundFamily.DRUM)(-650).dp else 650.dp).toPx()}
    val bankY=with(density){if(drag.originStep!=null)(if(sound.family==SoundFamily.DRUM)(-18).dp else 42.dp).toPx() else (-174+familySounds.indexOf(sound)*116).dp.toPx()}
    Box(Modifier.width(1600.dp).height(620.dp),contentAlignment=Alignment.Center){
        Column(
            Modifier.offset{IntOffset((bankX+drag.offsetX).roundToInt(),(bankY+drag.offsetY).roundToInt())}.zOffset{drag.offsetZ}.clip(RoundedCornerShape(22.dp)).background(if(drag.discardArmed)PicoTheme.colorScheme.alert else OPAQUE_BLACK).padding(10.dp),
            horizontalAlignment=Alignment.CenterHorizontally,
        ){
            Box(Modifier.size(62.dp).clip(CircleShape).background(Color(sound.color)),contentAlignment=Alignment.Center){Text(sound.icon,color=PicoTheme.colorScheme.labelPrimary)}
            Text(when { drag.discardArmed -> "松手丢弃"; drag.originStep!=null -> "拖出轨道"; drag.targetStep!=null -> "第 ${drag.targetStep+1} 格"; else -> sound.label },color=PicoTheme.colorScheme.labelPrimaryLight)
        }
    }
}

@Composable private fun StepSound(
    dragKey:Any,
    sound:SoundId?,
    pending:Boolean,
    dragging:Boolean,
    discardArmed:Boolean,
    onRemove:()->Unit,
    onDragStart:()->Unit,
    onDragBy:(Float,Float,Float)->Unit,
    onDragEnd:()->Unit,
    onDragCancel:()->Unit,
){
    var modifier=Modifier.size(48.dp).clip(CircleShape)
    if(sound!=null)modifier=modifier.palmPanelDrag(dragKey,onDragStart,onDragBy,onDragEnd,onDragCancel)
    Box(modifier.palmClickable(enabled=sound!=null,onClick=onRemove).background(when { dragging&&discardArmed -> PicoTheme.colorScheme.alert; dragging -> PicoTheme.colorScheme.interaction; sound!=null -> Color(sound.color); else -> OPAQUE_BLACK }),contentAlignment=Alignment.Center){Text((if(dragging)"↗" else if(pending)"◌" else "")+(sound?.icon?:"·"),color=if(sound==null)PicoTheme.colorScheme.labelTertiary else PicoTheme.colorScheme.labelPrimary)}
}

@Composable private fun ControlDeck(state:PalmEnsembleUiState,onEvent:(PalmEnsembleEvent)->Unit)=Panel(980.dp){
    if(state.demo) Row(horizontalArrangement=Arrangement.spacedBy(10.dp),verticalAlignment=Alignment.CenterVertically){Text("第一次？一键听示例，再自己换球。",color=PicoTheme.colorScheme.labelPrimaryLight);Button(onClick={onEvent(PalmEnsembleEvent.LoadExample)}){Text("开始演示")};Button(onClick={onEvent(PalmEnsembleEvent.StartOwnLoop)}){Text("我自己来")}}
    Row(horizontalArrangement=Arrangement.spacedBy(8.dp),verticalAlignment=Alignment.CenterVertically){
        Atmosphere.entries.forEach{p->Button(onClick={onEvent(PalmEnsembleEvent.Preset(p))}){Text((if(state.atmosphere==p)"● " else if(state.pendingAtmosphere==p)"◌ " else "")+p.label+" 套装")}}
        Button(onClick={onEvent(PalmEnsembleEvent.LoadExample)}){Text("示例")}
        Button(onClick={onEvent(PalmEnsembleEvent.Randomize)}){Text("随机生成")}
        Button(onClick={onEvent(PalmEnsembleEvent.ToggleAuto)}){Text(if(state.autoMode)"关闭全自动" else "全自动")}
        Button(onClick={onEvent(PalmEnsembleEvent.TogglePlay)}){Text(if(state.playing)"Ⅱ 暂停" else "▶ 播放")}
        Button(onClick={onEvent(PalmEnsembleEvent.RequestClear)},enabled=state.active.hasContent||state.pending?.hasContent==true){Text("清空")}
        Button(onClick={onEvent(PalmEnsembleEvent.ToggleRecord)}){Text(if(state.recording)"■ ${kotlin.math.ceil(state.recordRemainingMs/1000.0).toInt()}s" else "● 记录 30 秒")}
    }
    Text(state.lastSaved?.let{"已保存 $it · 事件序列（非音频）"}?:"${state.status} · 本地事件序列（非音频）",color=PicoTheme.colorScheme.labelPrimaryLight)
}

@Composable private fun ClearDialog(state:PalmEnsembleUiState,onEvent:(PalmEnsembleEvent)->Unit)=Panel(460.dp){
    Text("确认清空？",style=PicoTheme.typography.titleLarge,color=PicoTheme.colorScheme.labelPrimaryLight)
    Text(if(state.playing)"不会打断当前一圈；下一圈开始时清空。" else "将清空八格轨道。",color=PicoTheme.colorScheme.labelPrimaryLight)
    Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){Button(onClick={onEvent(PalmEnsembleEvent.CancelClear)}){Text("取消")};Button(onClick={onEvent(PalmEnsembleEvent.ConfirmClear)}){Text("确认清空")}}
}

@Composable private fun TutorialPanel(onEvent:(PalmEnsembleEvent)->Unit)=Panel(680.dp){
    Text("一分钟上手",style=PicoTheme.typography.titleLarge,color=PicoTheme.colorScheme.labelPrimaryLight)
    Text("1. 面向舒服的正前方再打开应用；启动后界面保持固定，不随视线或手势移动。",color=PicoTheme.colorScheme.labelPrimaryLight)
    Text("2. 点击“随机生成”快速得到一组节奏；有些格子会故意留空。",color=PicoTheme.colorScheme.labelPrimaryLight)
    Text("3. 按住左右音色球拖向中央，格子亮起后松手即可放入。",color=PicoTheme.colorScheme.labelPrimaryLight)
    Text("4. 按住轨道里的球向外拖，出现“松手丢弃”后释放即可移除。",color=PicoTheme.colorScheme.labelPrimaryLight)
    Text("5. 切换 Lo-fi、电子、轻摇滚只换整套音色，不会删除轨道。",color=PicoTheme.colorScheme.labelPrimaryLight)
    Text("6. 点击“全自动”会每圈随机生成新轨道，但保持当前风格；用播放按钮暂停或继续。",color=PicoTheme.colorScheme.labelPrimaryLight)
    Text("播放中的修改都会等到下一圈生效，所以不会突然断拍。",color=PicoTheme.colorScheme.interaction)
    Button(onClick={onEvent(PalmEnsembleEvent.CloseTutorial)}){Text("我知道了")}
}

@Composable private fun Panel(width:androidx.compose.ui.unit.Dp,content:@Composable ColumnScope.()->Unit){Column(Modifier.width(width).clip(RoundedCornerShape(26.dp)).background(OPAQUE_BLACK).padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp),horizontalAlignment=Alignment.CenterHorizontally,content=content)}

@Composable
private fun Modifier.palmClickable(
    enabled: Boolean = true,
    onClick: () -> Unit,
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    return spatialHoverEffect(enabled = enabled)
        .clickable(
            enabled = enabled,
            interactionSource = interactionSource,
            indication = LocalIndication.current,
            onClick = onClick,
        )
        .controllerHapticFeedback(interactionSource = interactionSource)
}

@Composable
private fun Modifier.palmPanelDrag(
    dragKey: Any,
    onStart: () -> Unit,
    onDragBy: (Float, Float, Float) -> Unit,
    onEnd: () -> Unit,
    onCancel: () -> Unit,
): Modifier {
    val latestStart by rememberUpdatedState(onStart)
    val latestDragBy by rememberUpdatedState(onDragBy)
    val latestEnd by rememberUpdatedState(onEnd)
    val latestCancel by rememberUpdatedState(onCancel)
    return pointerInput(dragKey) {
        detectDragGestures(
            onDragStart = { latestStart() },
            onDragEnd = { latestEnd() },
            onDragCancel = { latestCancel() },
        ) { change, dragAmount ->
            change.consume()
            latestDragBy(dragAmount.x, dragAmount.y, 0f)
        }
    }
}

@Composable private fun ControllerShortcuts(enabled:Boolean,onEvent:(PalmEnsembleEvent)->Unit){
    val provider=remember{ControllerTrackingProvider()}; val latest by rememberUpdatedState(onEvent)
    LaunchedEffect(provider,enabled){if(enabled){delay(StageLaunchPlacement.TRACKING_START_DELAY_MS);provider.start()}}; DisposableEffect(provider){onDispose{provider.stop()}}
    val listener=remember(provider){var a=false;var b=false;ControllerTrackingProvider.ControllerActionListener{actions->val right:ControllerAction=actions.right;if(right.aButtonPressed&&!a)latest(PalmEnsembleEvent.TogglePlay);if(right.bButtonPressed&&!b)latest(PalmEnsembleEvent.CancelSelection);a=right.aButtonPressed;b=right.bButtonPressed}}
    DisposableEffect(provider,listener){provider.addControllerActionListener(listener);onDispose{provider.removeControllerActionListener(listener)}}
}

private fun createCameraAnchor() = AnchorComponent(
    AnchorTarget.createCameraTarget(),
    AnchorComponent.TrackingMode.ONCE,
).apply {
    positionOffset = Vector3(
        StageLaunchPlacement.CENTER_X,
        StageLaunchPlacement.CENTER_Y,
        StageLaunchPlacement.DISTANCE_Z,
    )
}

// Explicit product requirement: inner AttachmentPanel surfaces are opaque black, never transparent gray.
private val OPAQUE_BLACK=Color.Black
private const val TITLE="title"; private const val DRUMS="drums"; private const val TRACK="track"; private const val MELODY="melody"; private const val DECK="deck"; private const val CLEAR="clear"; private const val TUTORIAL="tutorial"; private const val DRAG="drag"
