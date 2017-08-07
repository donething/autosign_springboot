package net.donething.autosign.controls

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class MainControl {
    @RequestMapping("/")
    fun index(): String {
        return "index"
    }
}