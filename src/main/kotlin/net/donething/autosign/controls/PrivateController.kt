package net.donething.autosign.controls

import net.donething.autosign.functions.Wo17Sign
import net.donething.autosign.models.User
import net.donething.autosign.models.Wo17
import net.donething.autosign.models.Wo17Repository
import org.apache.shiro.SecurityUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/private")
class PrivateController @Autowired constructor(val repository: Wo17Repository) {
    @RequestMapping("/wo17")
    fun wo17(map: ModelMap): String {
        val wo17s = repository.findByUid((SecurityUtils.getSubject().principal as User).id)
        wo17s ?: return "/private/wo17/wo17"
        val wos = ArrayList<Wo17>()
        wo17s.filter {
            !it.deleted     // 不显示已“删除”的手机号
        }.map {
            val str = it.phone
            it.phone = str.substring(0, 3) + "****" + str.substring(7, 11)  // 不显示完整手机号
            wos.add(it)
        }
        map.addAttribute("wo17s", wos)
        return "/private/wo17/wo17"
    }
}
