package com.kidsroutine.core.model

data class InteractionBlock(
    val blockId: String,
    val type: InteractionBlockType,
    val config: Map<String, Any>,   // data-driven, never hardcoded
    val required: Boolean = true
)
