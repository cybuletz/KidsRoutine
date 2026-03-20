package com.kidsroutine.core.model

data class InteractionBlock(
    val blockId: String = "",
    val type: InteractionBlockType = InteractionBlockType.TAP_SELECT,
    val config: Map<String, Any> = emptyMap(),
    val required: Boolean = true
)