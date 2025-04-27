$(document).ready(function () {
    const cncAdmin = ["userA"];
    const cncUser = ["userB"];

    // 自动登录检查
    function checkAutoLogin() {
        //过期验证
        const expires = localStorage.getItem('expires') || sessionStorage.getItem('expires');
        if (expires && Date.now() > parseInt(expires)) {
            localStorage.clear();
            sessionStorage.clear();
            return false;
        }
        // 优先检查 localStorage，其次 sessionStorage
        const loggedIn = localStorage.getItem('loggedIn') || sessionStorage.getItem('loggedIn');
        if (loggedIn === 'true') {
            const role = localStorage.getItem('role') || sessionStorage.getItem('role');

            // 根据角色跳转
            if (role === 'superadmin') {
                window.location.href = '/pages/admin.html';
                return true;
            }
            switch (true) {
                case cncAdmin.includes(role):
                    window.location.href = '/pages/cncindex.html';
                    break;
                case cncUser.includes(role):
                    window.location.href = '/pages/cncview.html';
                    break;
                default:
                    window.location.href = '/pages/index.html';
                    break;
            }
            return true; // 已执行自动跳转
        }
        return false; // 未自动登录
    }

    // 执行自动登录检查
    if (checkAutoLogin()) return;
    $('#loginForm').on('submit', function (event) {
        event.preventDefault();
        var username = $('#username').val();
        var password = $('#password').val();
        var rememberMe = $('#rememberMe').is(':checked');
        $.ajax({
            url: 'http://10.10.10.104:8080/user', // 指定的 API 端点
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({username: username, password: password}),
            success: function (response) {
                if (response.success) {
                    if (response.role === null && response.role === "guest") {
                        // 用户不存在，重定向到404页面
                        window.location.href = '/pages/404.html';
                    }
                    if (response.role === 'superadmin') {
                        // 管理员登录成功，重定向到管理员页面
                        window.location.href = '/pages/admin.html';
                    } else {
                        // 登录成功，保存登录状态和角色
                        const storage = rememberMe ? localStorage : sessionStorage;
                        storage.setItem('loggedIn', 'true');
                        storage.setItem('role', response.role);
                        storage.setItem('workcode', response.message);
                        storage.setItem('expires', Date.now() + 7 * 24 * 3600 * 1000);//7天有效期
                        // storage.setItem('token', response.token); // 保存 Token
                        // 重定向到主页
                        switch (true) {
                            case cncAdmin.includes(response.role):
                                window.location.href = '/pages/cncindex.html';
                                break;
                            case cncUser.includes(response.role):
                                window.location.href = '/pages/cncview.html';
                                break;
                            default:
                                window.location.href = '/pages/index.html';
                                break;
                        }
                    }
                } else {
                    alert(response.message);
                }
            },
            error: function () {
                alert('登录请求失败，请稍后再试');
            }
        });
    });
});