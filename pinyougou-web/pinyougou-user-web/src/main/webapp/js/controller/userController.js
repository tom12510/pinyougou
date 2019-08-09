// 窗口加载完
window.onload = function () {
    var vue = new Vue({
        el : '#app', // 元素绑定
        data : { // 数据模型
            user : {}, // 表单数据
            password : '', // 确认密码
            textMsg : '获取短信验证码',// 文本
            disabled : false, // 是否禁用
            code : '' // 验证码

        },
        methods : { // 操作方法
            // 用户注册
            save : function () {
                if (!this.password && this.password != this.user.password){
                    alert("两次密码不一致！");
                }else {
                    // 发送异步请求
                    axios.post("/user/save?code=" + this.code, this.user).then(function (resposne) {
                        // 获取响应数据
                        if (resposne.data) {
                            // 清空数据
                            vue.user = {};
                            vue.password = "";
                            vue.code = "";
                        }else{
                            alert("注册失败！");
                        }
                    });
                }
            },
            // 发送短信验证码
            sendSmsCode : function () {
                // 发送异步请求
                axios.get("/user/sendSmsCode?phone="+ this.user.phone)
                            .then(function (resposne) {
                    // 获取响应数据
                    if (resposne.data) {
                        // 倒计时
                        vue.downcount(90);
                    }else {
                        alert("发送短信验证码失败！");
                    }
                });
            },
            // 倒计时方法
            downcount : function (count) {
                if (count > 0) {
                    count--;
                    this.textMsg = count + "S后，重新获取。";
                    this.disabled = true;
                    setTimeout(function () {
                        vue.downcount(count);
                    }, 1000);
                }else{
                    this.textMsg = "获取短信验证码";
                    this.disabled = false;
                }
            }
        },
        created : function () { // 创建生命周期
           
        }
    });
};