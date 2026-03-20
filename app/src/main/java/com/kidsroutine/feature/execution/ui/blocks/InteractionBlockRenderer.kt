package com.kidsroutine.feature.execution.ui.blocks

import androidx.compose.runtime.Composable
import com.kidsroutine.core.model.InteractionBlock
import com.kidsroutine.core.model.InteractionBlockType
import com.kidsroutine.feature.execution.ui.ExecutionEvent

/**
 * Central dispatcher — UI MUST call this, never render blocks directly.
 * Adding a new block type = add one 'when' branch here + a new Composable.
 */
@Composable
fun InteractionBlockRenderer(
    block: InteractionBlock,
    onEvent: (ExecutionEvent) -> Unit
) {
    when (block.type) {
        InteractionBlockType.CHECKBOX      -> CheckboxBlock(block, onEvent)
        InteractionBlockType.TIMER         -> TimerBlock(block, onEvent)
        InteractionBlockType.TAP_SELECT    -> TapSelectBlock(block, onEvent)
        InteractionBlockType.MULTI_SELECT  -> MultiSelectBlock(block, onEvent)
        InteractionBlockType.PHOTO_CAPTURE -> PhotoCaptureBlock(block, onEvent)
        InteractionBlockType.TEXT_INPUT    -> TextInputBlock(block, onEvent)
        InteractionBlockType.PARENT_CONFIRM -> ParentConfirmBlock(block, onEvent)
        InteractionBlockType.DUAL_CONFIRM  -> DualConfirmBlock(block, onEvent)
        InteractionBlockType.DRAW_INPUT    -> DrawInputBlock(block, onEvent)
    }
}
