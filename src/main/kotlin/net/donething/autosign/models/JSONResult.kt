package net.donething.autosign.models

data class JSONResult(
        var success: Boolean = false,
        var code: Int = 0,
        var msg: Any? = null,
        var result: Any? = null
)