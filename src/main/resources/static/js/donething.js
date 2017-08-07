/**
 * Created by zl on 17-7-17.
 */
$(function () {
    formInit()
});

function formInit() {
    $("#username").blur().focus()
}

/**
 * 登录
 * @returns {boolean}
 */
function login() {
    var formData = $("#loginForm").serialize();
    $.post(
        "/api/user/login",
        formData,
        function (data, status) {
            if (data.success) {
                var ref = document.referrer;
                if (ref.lastIndexOf("/public/register") >= 0) {
                    // 如果来自注册页面，则记住注册前的Referer
                    ref = localStorage.getItem("nextUrl");
                    localStorage.removeItem(LOGIN_REGISTER_NEXT_NAME);
                }
                // 来自普通页面，如果不为空就跳转次页面，否则跳转到首页
                window.location.href = ref ? ref : "/";
            } else {
                var selector;
                switch (data.code) {
                    case 20:
                        selector = $("#loginForm #username");
                        break;
                    case 21:
                        selector = $("#loginForm #password");
                        break;
                    default:
                        return;
                }
                // selector.attr("data-match-error", "data-match-error.");
                selector.next(".with-errors").text(data.msg);
            }
        }
    );
    return false;
}

/**
 * 注册
 * @returns {boolean}
 */
function create() {
    var formData = $("#registerForm").serialize();
    $.ajax({
        type: "POST",
        url: "/api/user/create",
        data: formData,
        beforeSend: function () {
            // 注册时记录来源链接，注册成功后跳转到登录页面，需要用到
            localStorage.setItem(LOGIN_REGISTER_NEXT_NAME, document.referrer)
        },
        success: function (data, status) {
            if (data.success) {
                window.location.href = "/public/login"
            } else {
                var selector;
                switch (data.code) {
                    case 20:
                    case 22:
                        selector = $("#registerForm #username");
                        break;
                    case 21:
                    case 23:
                        selector = $("#registerForm #email");
                        break;
                    default:
                        return;
                }
                selector.next(".with-errors").text(data.msg);
            }
        }
    });
    return false;
}

/**
 * 添加一起沃账号
 * @returns {boolean}
 */
function addWo17() {
    var formData = $("#addWo17Form").serialize();
    $.post(
        "/api/wo17/add",
        formData,
        function (data, status) {
            if (data.success) {
                window.location.reload();
                history.replaceState(null, null, location.href)
            } else {
                var selector = $("#addWo17Form #phone");
                selector.next(".with-errors").text(data.msg);
            }
        }
    );
    return false;
}

var LOGIN_REGISTER_NEXT_NAME = "nextUr";    // localStorage.nextUrl: 注册、登录成功后的重定向的链接