// 窗口加载完
window.onload = function () {
    var vue = new Vue({
        el : '#app', // 元素绑定
        data : { // 数据模型
            loginName : "" // 登录用户名
        },
        methods : { // 操作方法
            // 获取登录用户名
            showName : function () {
                axios.get("/user/showName").then(function(response){
                    vue.loginName = response.data.loginName;
                });
            }
        },
        created : function () { // 创建生命周期
            this.showName();
        }
    });
};