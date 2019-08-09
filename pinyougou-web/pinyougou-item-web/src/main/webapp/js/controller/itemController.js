// 窗口加载完
window.onload = function () {

    var vue = new Vue({
        el : '#app', // 元素绑定
        data : { // 数据模型
            num : 1, // 购买数量
            spec : {}, // 记录用户选择的规格选项
            sku : {} //  SKU对象
        },
        methods : { // 操作方法
            // 购买数量加减操作
            addNum : function (x) {
                this.num = parseInt(this.num);
                this.num += x;
                if (this.num < 1){
                    this.num = 1;
                }
            },
            // 记录用户选择的规格选项
            selectSpec : function (key, value) {
                // 为json对象设置key与value
                Vue.set(this.spec, key, value);
                // 搜索SKU
                this.searchSku();
            },
            // 判断是否选中
            isSelectedSpec : function (key, value) {
                return this.spec[key] == value;
            },
            // 加载默认的SKU
            loadSku : function () {
                // 取默认的SKU
                this.sku = itemList[0];
                this.spec = JSON.parse(this.sku.spec);
            },
            // 搜索SKU
            searchSku : function () {
                for (var i = 0; i < itemList.length; i++){
                    var item = itemList[i];
                    if (item.spec == JSON.stringify(this.spec)){
                        this.sku = item;
                        break;
                    }
                }
            },
            // 加入购物车按钮事件绑定
            addToCart : function () {
                // 发送跨域请求：http://item.pinyougou.com --> http://cart.pinyougou.com
                axios.get("http://cart.pinyougou.com/cart/addCart?itemId="
                    + this.sku.id + "&num=" + this.num, {withCredentials : true})
                    .then(function(response){
                        // 获取响应数据
                        if (response.data){
                            // 跳转到购物车系统
                            location.href = "http://cart.pinyougou.com";
                        }else{
                            alert("加入购物车失败！");
                        }
                });
            }
        },
        created : function () { // 创建生命周期
            this.loadSku();
        }
    });
};