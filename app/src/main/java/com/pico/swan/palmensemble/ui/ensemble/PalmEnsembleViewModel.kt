package com.pico.swan.palmensemble.ui.ensemble

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pico.swan.palmensemble.data.repository.EventSequenceRepository
import com.pico.swan.palmensemble.domain.model.*
import com.pico.swan.palmensemble.domain.usecase.QuantizedAtmosphere
import com.pico.swan.palmensemble.domain.usecase.QuantizedSequencer
import com.pico.swan.palmensemble.domain.usecase.AutoBarGenerator
import com.pico.swan.palmensemble.domain.usecase.selectAtmospherePreservingPattern
import com.pico.swan.palmensemble.platform.AudioEngine
import com.pico.swan.palmensemble.platform.LaunchOptions
import com.pico.swan.palmensemble.platform.SynthAudioEngine
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PalmEnsembleViewModel(app: Application) : AndroidViewModel(app) {
    private val captureMode = LaunchOptions.captureMode
    private val sequencer = QuantizedSequencer(if (captureMode != null) Pattern.EXAMPLE else Pattern.EMPTY)
    private val atmosphere = QuantizedAtmosphere()
    private val autoBarGenerator = AutoBarGenerator()
    private val audio: AudioEngine = SynthAudioEngine()
    private val repository = EventSequenceRepository(app)
    private val _state = MutableStateFlow(PalmEnsembleUiState(active=sequencer.snapshot.active,demo=captureMode==null,status=if(captureMode==null)"按住球拖进格子，或点击球再点格号" else "截图验证态 · 示例节奏"))
    val state: StateFlow<PalmEnsembleUiState> = _state.asStateFlow()
    private var clockJob: Job? = null; private var recordJob: Job? = null
    private var autoMode = false
    private var recordStarted = 0L; private val events = mutableListOf<RecordedEvent>()

    init { if(captureMode=="playing") { sequencer.setPlaying(true); sync("正在播放 · 100 BPM"); startClock() } }

    fun onEvent(event: PalmEnsembleEvent) { when(event) {
        is PalmEnsembleEvent.SelectSound -> { audio.trigger(event.sound); update(selected=event.sound, status="已试听 ${event.sound.label} · 请选择一个格号"); record("select", event.sound.name) }
        is PalmEnsembleEvent.Place -> _state.value.selected?.let { sound -> sequencer.edit { it.place(event.index, sound) }; sync(if (sequencer.snapshot.playing) "已排队 · 下一圈生效" else "已放入第 ${event.index+1} 格"); record("place", "${event.index}:${sound.name}") }
        is PalmEnsembleEvent.Remove -> { sequencer.edit { it.remove(event.index,event.family) }; sync(if(sequencer.snapshot.playing) "移除已排队 · 下一圈生效" else "已移走"); record("remove","${event.index}:${event.family}") }
        is PalmEnsembleEvent.Preset -> {
            selectAtmospherePreservingPattern(sequencer, atmosphere, event.value)
            if (!sequencer.snapshot.playing) audio.setAtmosphere(event.value)
            sync(if (sequencer.snapshot.playing) "${event.value.label} 音色套装已排队 · 轨道不变" else "已切换 ${event.value.label} 音色套装 · 轨道不变")
            record("preset",event.value.name)
        }
        PalmEnsembleEvent.TogglePlay -> togglePlay(); PalmEnsembleEvent.LoadExample -> { sequencer.edit { Pattern.forAtmosphere(atmosphere.snapshot.active) }; if(!sequencer.snapshot.playing) togglePlay(); sync("${atmosphere.snapshot.active.label} 示例整套已播放") }
        PalmEnsembleEvent.Randomize -> { sequencer.edit { Pattern.random() }; sync(if(sequencer.snapshot.playing) "随机轨道已排队 · 下一圈生效" else "已随机生成 · 允许空格"); record("randomize","pattern") }
        PalmEnsembleEvent.ToggleAuto -> toggleAuto()
        PalmEnsembleEvent.OpenTutorial -> update(clearConfirm=false,tutorialVisible=true)
        PalmEnsembleEvent.CloseTutorial -> update(tutorialVisible=false)
        PalmEnsembleEvent.RequestClear -> update(clearConfirm=true,tutorialVisible=false); PalmEnsembleEvent.CancelClear -> update(clearConfirm=false)
        PalmEnsembleEvent.ConfirmClear -> { sequencer.edit { Pattern.EMPTY }; update(clearConfirm=false); sync(if(sequencer.snapshot.playing) "清空已排队 · 下一圈生效" else "轨道已清空"); record("clear","all") }
        PalmEnsembleEvent.ToggleRecord -> if(_state.value.recording) stopRecord() else startRecord()
        PalmEnsembleEvent.StartOwnLoop -> update(demo=false,status="按住球拖进格子，或点击球再点格号")
        PalmEnsembleEvent.CancelSelection -> update(selected=null,status="已取消选择")
    }}
    private fun togglePlay() {
        val start=!sequencer.snapshot.playing; sequencer.setPlaying(start)
        if(start){ sequencer.soundsAtCurrentStep().forEach(audio::trigger); startClock() } else clockJob?.cancel()
        sync(if(autoMode) { if(start) "全自动继续播放 · 当前风格不变" else "全自动已暂停" } else { if(start) "正在播放 · 100 BPM" else "已暂停 · 可以编辑" }); record(if(start)"play" else "pause",sequencer.snapshot.currentStep.toString())
    }
    private fun toggleAuto(){
        autoMode=!autoMode
        if(autoMode && !sequencer.snapshot.playing){ queueAutoBar(); togglePlay() }
        else sync(if(autoMode) "全自动已开启 · 只换轨道，风格不变" else "全自动已关闭 · 保留当前一组")
        record(if(autoMode)"auto_start" else "auto_stop",sequencer.snapshot.currentStep.toString())
    }
    private fun queueAutoBar(){
        sequencer.edit { autoBarGenerator.next() }
        record("auto_bar",atmosphere.snapshot.active.name)
    }
    private fun startClock(){ clockJob?.cancel(); clockJob=viewModelScope.launch { var next=SystemClock.elapsedRealtime()+STEP_MS; while(isActive&&sequencer.snapshot.playing){ delay((next-SystemClock.elapsedRealtime()).coerceAtLeast(1)); if(autoMode&&sequencer.snapshot.currentStep==STEP_COUNT-1)queueAutoBar(); val sounds=sequencer.tick(); atmosphere.onStep(sequencer.snapshot.currentStep)?.let(audio::setAtmosphere); sounds.forEach(audio::trigger); sync(if(autoMode)"全自动播放 · 随机轨道，当前风格不变" else if(sequencer.snapshot.hasPending||atmosphere.snapshot.pending!=null)"下一圈生效" else "正在播放 · 100 BPM"); next+=STEP_MS } } }
    private fun startRecord(){ recordStarted=SystemClock.elapsedRealtime(); events.clear(); update(recording=true,recordRemainingMs=30_000,status="记录事件序列 · 非音频"); recordJob=viewModelScope.launch { while(isActive){ val left=(30_000-(SystemClock.elapsedRealtime()-recordStarted)).coerceAtLeast(0); update(recordRemainingMs=left); if(left==0L){stopRecord();break}; delay(100) } } }
    private fun stopRecord(){ recordJob?.cancel(); val path=repository.save(events).name; update(recording=false,recordRemainingMs=30_000,lastSaved=path,status="已保存事件序列（非音频）") }
    private fun record(type:String,detail:String){ if(_state.value.recording) events += RecordedEvent(SystemClock.elapsedRealtime()-recordStarted,type,detail) }
    private fun sync(status:String){ val s=sequencer.snapshot; val a=atmosphere.snapshot; _state.value=_state.value.copy(active=s.active,pending=s.pending,currentStep=s.currentStep,playing=s.playing,atmosphere=a.active,pendingAtmosphere=a.pending,status=status,autoMode=autoMode) }
    private fun update(selected:SoundId?=_state.value.selected,status:String=_state.value.status,clearConfirm:Boolean=_state.value.clearConfirm,recording:Boolean=_state.value.recording,recordRemainingMs:Long=_state.value.recordRemainingMs,lastSaved:String?=_state.value.lastSaved,demo:Boolean=_state.value.demo,tutorialVisible:Boolean=_state.value.tutorialVisible){ _state.value=_state.value.copy(selected=selected,status=status,clearConfirm=clearConfirm,recording=recording,recordRemainingMs=recordRemainingMs,lastSaved=lastSaved,demo=demo,tutorialVisible=tutorialVisible,autoMode=autoMode) }
    override fun onCleared(){ clockJob?.cancel(); recordJob?.cancel(); audio.close(); super.onCleared() }
}
