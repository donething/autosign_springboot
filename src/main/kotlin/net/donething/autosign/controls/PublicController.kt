package net.donething.autosign.controls

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.util.DigestUtils
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/public")
class PublicController {
    @RequestMapping("/")
    fun index(): String {
        return "index"
    }

    @RequestMapping("/register")
    fun register(): String {
        return "/public/register"
    }

    @RequestMapping("/login")
    fun login(): String {
        return "/public/login"
    }

    @RequestMapping("/403")
    fun forbidden(): String {
        return "/public/403"
    }

    @RequestMapping("test")
    fun test(@RequestParam string: String, model: Model): String {
        val md5 = DigestUtils.md5DigestAsHex(string.toByteArray()).toUpperCase()
        model.addAttribute("string", string)
        model.addAttribute("md5", md5)
        return "/public/test"
    }
}