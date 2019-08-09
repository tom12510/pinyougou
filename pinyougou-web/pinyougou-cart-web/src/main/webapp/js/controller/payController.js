// 窗口加载完
window.onload = function () {
    var vue = new Vue({
        el : '#app', // 元素绑定
        data : { // 数据模型
            loginName : '', // 登录用户名
            redirectUrl : '', // 重定向回来的URL
            outTradeNo : '', // 交易订单号
            money : 0 // 支付总金额
        },
        methods : { // 操作方法
            // 获取登录用户名
            loadUsername : function () {
                // 对URL进行了编码
                this.redirectUrl = window.encodeURIComponent(location.href);
                axios.get("/user/showName").then(function(response){
                    // 获取响应数据
                    vue.loginName = response.data.loginName;
                });
            },
            // 生成支付二维码
            genPayCode : function(){
                // 发送异步请求
                axios.get("/order/genPayCode").then(function(response){
                    // 获取响应数据  response.data : {outTradeNo : '', totalFee : 0, codeUrl : ''}
                    // 设置交易订单号
                    vue.outTradeNo = response.data.outTradeNo;
                    // 设置交易总金额
                    vue.money = (response.data.totalFee / 100).toFixed(2);
                    // 支付URL
                    var codeUrl = response.data.codeUrl;
                    // 生成二维码
                    document.getElementById("qrious").src = "/barcode?url=" + codeUrl;

                    /**
                     * 开启定时器
                     */
                    var timer = setInterval(function(){
                        // 发送异步请求，查询支付状态
                        axios.get("/order/queryPayStatus?outTradeNo="
                            + vue.outTradeNo).then(function(response){
                            // 获取响应数据: response.data: {status : 1|2|3}
                            if (response.data.status == 1){ // 支付成功
                                // 关闭定时器
                                clearInterval(timer);
                                // 支付成功，跳转到支付成功页面
                                location.href = "/order/paysuccess.html?money=" + vue.money;
                            }
                            if (response.data.status == 3){ // 支付失败
                                // 关闭定时器
                                clearInterval(timer);
                                // 支付失败，跳转到支付失败页面
                                location.href = "/order/payfail.html";
                            }
                        });
                    }, 3000);
                });
            }
        },
        created : function () { // 创建生命周期
            this.loadUsername();
            this.genPayCode();
        }
    });
};