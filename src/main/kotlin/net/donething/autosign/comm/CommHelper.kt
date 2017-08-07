package net.donething.autosign.comm

import net.donething.dtjavatool.StringTool
import org.springframework.util.DigestUtils
import java.util.*
import javax.servlet.http.HttpServletRequest

class CommHelper {
    companion object {
        /**
         * 获取当前日期，格式：2017-01-02 03:04:05
         */
        fun date(): String {
            return StringTool.date2Str(Date())
        }

        /**
         * 获取用户真实IP
         */
        fun ipAddr(request: HttpServletRequest): String {
            var ip = request.getHeader("X-Real-IP")
            if (!ip.isNullOrBlank() && !"unknown".equals(ip, ignoreCase = true)) {
                return ip
            }
            ip = request.getHeader("X-Forwarded-For")
            if (!ip.isNullOrBlank() && !"unknown".equals(ip, ignoreCase = true)) {
                // 多次反向代理后会有多个IP值，第一个为真实IP。
                val index = ip.indexOf(',')
                if (index != -1) {
                    return ip.substring(0, index)
                } else {
                    return ip
                }
            }
            return request.remoteAddr
        }

        /**
         * 判断指定的字符串是否都非空（null、""、"  "）
         */
        fun notBlank(vararg strs: String?): Boolean {
            strs.forEach {
                if (it.isNullOrBlank()) return false
            }
            return true
        }

        fun salt(): String {
            val salt = DigestUtils.md5DigestAsHex(("Welcome to Donething.net~"
                    + Random(System.currentTimeMillis()).nextLong()
                    + System.currentTimeMillis())
                    .toByteArray()).toUpperCase()
            return salt
        }
    }
}