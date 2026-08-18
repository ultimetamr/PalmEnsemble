package com.pico.swan.palmensemble.domain.usecase

import com.pico.swan.palmensemble.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class QuantizedSequencerTest {
    @Test fun paused_edits_apply_immediately() {
        val s=QuantizedSequencer(); s.edit{it.place(2,SoundId.KICK)}
        assertEquals(SoundId.KICK,s.snapshot.active.steps[2].drum); assertNull(s.snapshot.pending)
    }
    @Test fun playing_edits_commit_only_at_seven_to_zero_boundary() {
        val s=QuantizedSequencer(); s.setPlaying(true); s.edit{it.place(3,SoundId.BELL)}
        repeat(7){s.tick(); assertNull(s.snapshot.active.steps[3].melody)}
        s.tick(); assertEquals(0,s.snapshot.currentStep); assertEquals(SoundId.BELL,s.snapshot.active.steps[3].melody); assertNull(s.snapshot.pending)
    }
    @Test fun multiple_playing_edits_merge_into_single_pending_pattern() {
        val s=QuantizedSequencer(); s.setPlaying(true); s.edit{it.place(0,SoundId.KICK)}; s.edit{it.place(4,SoundId.SNARE)}
        assertEquals(SoundId.KICK,s.snapshot.pending!!.steps[0].drum); assertEquals(SoundId.SNARE,s.snapshot.pending!!.steps[4].drum)
    }
    @Test fun playing_discard_commits_only_at_seven_to_zero_boundary() {
        val s=QuantizedSequencer(Pattern.EXAMPLE); s.setPlaying(true); s.edit{it.remove(0,SoundFamily.DRUM)}
        repeat(7){s.tick(); assertEquals(SoundId.KICK,s.snapshot.active.steps[0].drum)}
        s.tick(); assertEquals(0,s.snapshot.currentStep); assertNull(s.snapshot.active.steps[0].drum); assertNull(s.snapshot.pending)
    }
    @Test fun example_has_eight_steps_and_both_families() {
        assertEquals(8,Pattern.EXAMPLE.steps.size); assertTrue(Pattern.EXAMPLE.steps.any{it.drum!=null}); assertTrue(Pattern.EXAMPLE.steps.any{it.melody!=null})
    }
}
