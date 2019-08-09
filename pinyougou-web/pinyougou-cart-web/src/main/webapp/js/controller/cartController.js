// 窗口加载完
window.onload = function () {
    var vue = new Vue({
        el : '#app', // 元素绑定
        data : { // 数据模型
            loginName : '', // 登录用户名
            redirectUrl : '', // 重定向回来的URL
            carts : [], // 用户的购物车
            totalEntity : {totalNum : 0, totalMoney  : 0}, // 总计对象
            addressList : [], // 收件地址数组
            address : {}, // 记录用户选中的收件地址
            order : {paymentType : 1} // 订单数据封装
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
            // 查询用户的购物车
            findCart : function () {
                axios.get("/cart/findCart").then(function(response){
                    vue.carts = response.data;
                    // 总计对象
                    vue.totalEntity = {totalNum : 0, totalMoney  : 0};
                    // 统计购物车中商品的总购买数量与购买总金额
                    for (var i = 0; i < vue.carts.length; i++){
                       var cart =  vue.carts[i];
                       for (var j = 0; j < cart.orderItems.length; j++){
                           var orderItem = cart.orderItems[j];
                           // 统计总数量
                           vue.totalEntity.totalNum += orderItem.num;
                           // 统计总金额
                           vue.totalEntity.totalMoney += orderItem.totalFee;
                       }
                    }
                });
            },
            // 增减与删除
            addCart : function (itemId, num) {
                axios.get("/cart/addCart?itemId="
                    + itemId + "&num=" + num).then(function(response){
                    if (response.data){
                        // 重新加载购物车
                        vue.findCart();
                    }else{
                        alert("操作失败！");
                    }
                });
            },
            // 根据用户名获取收件地址
            findAddressByUser : function () {
                axios.get("/order/findAddressByUser").then(function(response){
                    // 获取响应数据
                    vue.addressList = response.data;

                    // 获取默认地址
                    vue.address = vue.addressList[0];
                });
            },
            // 用户选择收件地址
            selectAddress : function (item) {
                this.address = item;
            },
            // 提交订单
            submitOrder : function () {
                // 收件人地址
                this.order.receiverAreaName = this.address.address;
                // 收件人手机号码
                this.order.receiverMobile = this.address.mobile;
                // 收件人
                this.order.receiver = this.address.contact;
                // 订单来源(2:pc端)
                this.order.sourceType = "2";
                // 发送异步请求
                axios.post("/order/submitOrder", this.order).then(function(response){
                    if (response.data){ // 订单提交成功
                        // 判断支付方式
                        if (vue.order.paymentType == 1){ // 在线支付
                            // 跳转到支付页面
                            location.href = "/order/pay.html";
                        }else{
                            // 跳转到成功页面
                            location.href = "/order/paysuccess.html";
                        }
                    }else {
                        alert("提交订单失败！");
                    }
                });
            }
        },
        created : function () { // 创建生命周期
           this.loadUsername();
           this.findCart();
           this.findAddressByUser();
        }
    });
};