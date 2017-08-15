package net.donething.autosign.controls

import net.donething.autosign.comm.CommHelper
import net.donething.autosign.models.JSONResult
import net.donething.autosign.models.User
import net.donething.autosign.models.UserRepository
import org.apache.shiro.SecurityUtils
import org.apache.shiro.authc.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("api/user")
class UserController @Autowired constructor(val repository: UserRepository) {
    @RequestMapping("/query/{username}")
    fun findByUsername(@PathVariable username: String) = repository.findByUsernameIgnoreCase(username)

    /**
     * 创建用户
     * 错误代码说明：10：创建成功；20：用户名已被使用；21：邮箱已被使用；22：用户名、邮箱或密码为空；
     * 23：用户名包含非法字符；24：邮箱包含非法字符；30程序运行错误
     */
    @RequestMapping(value = "/create", method = arrayOf(RequestMethod.POST))
    fun create(request: HttpServletRequest, @RequestParam username: String, @RequestParam password: String, @RequestParam email: String): JSONResult {
        val ip = CommHelper.ipAddr(request)
        // 判断请求参数是否为空字符串
        if (!CommHelper.notBlank(username, password, email)) {
            return JSONResult(false, 22, "用户名、邮箱或密码为空")
        }
        // 用户名不能包含非法字符
        if (!username.matches("""^[a-zA-Z0-9_\u4e00-\u9fa5]{1,16}${'$'}""".toRegex())) {
            return JSONResult(false,23, "用户名包含非法字符" )
        }
        // 邮箱必须包含字符'@'且不能包含非法字符
        if (!email.contains("@") || !email.matches("""^[a-zA-Z0-9_@\.]{1,50}${'$'}""".toRegex())) {
            return JSONResult(false, 24, "邮箱包含非法字符")
        }
        // 判断用户名、邮箱是否已经被使用
        if (repository.findByUsernameIgnoreCase(username) != null) {
            return JSONResult(false, 20, "用户名$username 已被使用")
        }
        if (repository.findByEmailIgnoreCase(email) != null) {
            return JSONResult(false, 21, "邮箱$email 已被使用")
        }

        // 添加用户信息
        try {
            val user = User(username = username, password = password, salt = CommHelper.salt(), email = email, regTime = CommHelper.date())
            repository.save(user)
            logger.info("用户创建成功：id:${user.id} @$ip")
            return JSONResult(true, 10, "用户创建成功，id:${user.id}")
        } catch (ex: Exception) {
            logger.info("程序运行错误：${ex.message}")
            return JSONResult(false, 30, "程序运行错误：${ex.message}")
        }
    }

    /**
     * 登录
     * 错误代码说明：10：登录成功；20：用户不存在；21：用户密码错误；22：用户已被锁定；23：登录错误次数过多；30：程序运行错误
     */
    @RequestMapping(value = "/login", method = arrayOf(RequestMethod.POST))
    fun login(request: HttpServletRequest, @RequestParam username: String, @RequestParam password: String,
              @RequestParam rememberMe: Boolean): JSONResult {
        val ip = CommHelper.ipAddr(request)
        val subject = SecurityUtils.getSubject()
        val token = UsernamePasswordToken(username, password, rememberMe)   // “记住我”：一段时间内免登陆
        try {
            subject.login(token)    // 调用MyShiroRealm.AuthorizingRealm()进行密码比对
            // 没有异常就表示比对结果正确
            val user = subject.principal as User
            user.logTime = CommHelper.date()
            user.logIp = ip
            repository.save(user)
            return JSONResult(true, 10, "登录成功")
        } catch (ex: UnknownAccountException) {
            return JSONResult(false, 20, "用户名不存在")
        } catch (ex: IncorrectCredentialsException) {
            return JSONResult(false, 21, "登录密码错误")
        } catch (ex: LockedAccountException) {
            return JSONResult(false, 22, "用户已被锁定")
        } catch (ex: ExcessiveAttemptsException) {
            return JSONResult(false, 23, "登录错误次数过多")
        } catch (ex: Exception) {
            return JSONResult(false, 30, "登录错误：${ex.message}")
        } finally {
            // 记录登录信息
            if (subject.isAuthenticated) {
                logger.info("登录成功：$username @$ip")
            } else {
                logger.info("登录失败：$username:$password @$ip")
            }
        }
    }

    /**
     * 更改密码
     * 错误代码说明：10：更改密码成功；21：用户id不存在；22：用户密码错误；23：id、原密码或新密码为空
     */
    @RequestMapping(value = "/chpwd", method = arrayOf(RequestMethod.POST))
    fun changePwd(request: HttpServletRequest, @RequestParam id: Long, @RequestParam original: String, @RequestParam new: String): JSONResult {
        val ip = CommHelper.ipAddr(request)
        // 判断请求参数是否为空字符串
        if (!CommHelper.notBlank(id.toString(), original, new)) {
            return JSONResult(false, 22, "id、原密码或新密码为空")
        }
        val user = repository.findById(id)
        user ?: return JSONResult(false, 21, "用户id:$id 不存在")
        if (user.password == original) {
            user.password = new
            user.salt = CommHelper.salt()
            repository.save(user)
            logger.info("id:$id 更改密码成功 @$ip")
            return JSONResult(true, 10, "用户id:$id 更改密码成功")
        } else {
            logger.error("id:$id:$original 更改密码失败：原始密码错误 @$ip")
            return JSONResult(false, 22, "用户原始密码错误")
        }
    }

    private val logger = LoggerFactory.getLogger(this.javaClass)
}