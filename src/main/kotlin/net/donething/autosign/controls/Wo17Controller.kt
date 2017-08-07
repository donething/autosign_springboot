package net.donething.autosign.controls

import net.donething.autosign.comm.CommHelper
import net.donething.autosign.models.JSONResult
import net.donething.autosign.models.User
import net.donething.autosign.models.Wo17
import net.donething.autosign.models.Wo17Repository
import org.apache.shiro.SecurityUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/api/wo17")
class Wo17Controller @Autowired constructor(val repository: Wo17Repository) {
    /**
     * 添加一起沃手机号
     * 错误代码：10：手机号添加成功；20：手机号或密码为空；21：手机号须以'1'开头；22：手机号位数须为11位；23：手机号必须为全数字
     * 24：手机号码已存在；25：手机号或密码错误，登录一起沃失败；
     */
    @RequestMapping("/add", method = arrayOf(RequestMethod.POST))
    fun add(request: HttpServletRequest, @RequestParam phone: String, @RequestParam password: String): JSONResult {
        val ip = CommHelper.ipAddr(request)
        val p = phone.trim()
        if (!CommHelper.notBlank(p, password)) {
            return JSONResult(false, 20, "手机号或密码为空")
        }
        if (!p.startsWith("1")) {
            return JSONResult(false, 21, "手机号须以'1'开头")
        }
        if (p.length != 11) {
            return JSONResult(false, 22, "手机号位数须为11位")
        }
        if (Regex(""".*\D.*""").matches(p)) {
            return JSONResult(false, 23, "手机号必须为全数字")
        }
        if (repository.findByPhone(p) != null) {
            return JSONResult(false, 24, "手机号码已存在")
        }
        try {
            val wo17 = Wo17(uid = (SecurityUtils.getSubject().principal as User).id, phone = p, password = password)
            repository.save(wo17)
            logger.info("手机号添加成功：$p @$ip")
            return JSONResult(true, 10, "手机号添加成功")
        } catch (ex: Exception) {
            logger.error("添加一起沃手机号错误：${ex.message}")
            return JSONResult(false, 30, "手机号添加错误：${ex.message}")
        }
    }

    /**
     * 删除一起沃信息
     * 错误代码：10：删除成功；20：一起沃手机号不存在；30：系统错误
     */
    @RequestMapping("/delete", method = arrayOf(RequestMethod.POST))
    fun delete(request: HttpServletRequest, @RequestParam phone: String): JSONResult {
        val ip = CommHelper.ipAddr(request)
        try {
            val wo17 = repository.findByPhone(phone.trim())
            wo17 ?: return JSONResult(false, 20, "一起沃手机号不存在")
            wo17.deleted = true
            repository.save(wo17)
            logger.info("一起沃信息删除成功：${phone.trim()} $ip")
            return JSONResult(true, 10, "一起沃信息删除成功")
        } catch (ex: Exception) {
            logger.info("一起沃信息删除失败：${phone.trim()} $ip")
            return JSONResult(false, 30, "一起沃信息删除失败：${ex.message}")
        }
    }

    /**
     * 查询一起沃信息
     * 错误代码：10：正确查询，且包含数据；11：正确查询，但数据为空；20：查询类型错误；30：系统错误
     */
    @RequestMapping("/query", method = arrayOf(RequestMethod.POST))
    fun query(@RequestParam type: String, @RequestParam value: String): JSONResult {
        try {
            when (type) {
                "uid" -> {
                    val wo17s = repository.findByUid(value.toLongOrNull() ?: -1)
                    val wos = arrayListOf<Wo17>()
                    if (wo17s == null || wo17s.isEmpty()) {
                        return JSONResult(true, 11, "用户id", "[]")
                    }
                    wo17s.filter {
                        !it.deleted     // 不返回已“删除”的手机号
                    }.map {
                        val str = it.phone
                        it.phone = str.substring(0, 3) + "****" + str.substring(7, 11)  // 不返回完整手机号
                        wos.add(it)
                    }
                    return JSONResult(true, 11, "用户id", wos)
                }
                "phone" -> {
                    val wo17 = repository.findByPhone(value)
                    if (wo17 != null && !wo17.deleted) {
                        return JSONResult(true, 10, "一起沃手机号", wo17)
                    } else {
                        return JSONResult(true, 11, "一起沃手机号", "")
                    }
                }
                else -> return JSONResult(false, 20, "查询类型错误")
            }
        } catch (ex: Exception) {
            return JSONResult(false, 30, "一起沃信息查询错误：${ex.message}")
        }
    }


    private val logger = LoggerFactory.getLogger(this.javaClass)
}
