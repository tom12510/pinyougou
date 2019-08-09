// 窗口加载完
$(function () {
    var vue = new Vue({
        el : '#app', // 元素绑定
        data : { // 数据模型
            loginName : '', // 登录用户名
            redirectUrl : '', // 重定向回来的URL
            contentList : [], // 广告数据
            keywords : '' // 搜索关键字
        },
        methods : { // 操作方法
            // 根据内容分类id查询广告数据
            findContentByCategoryId : function (categoryId) {
               axios.get("/content/findContentByCategoryId?categoryId="
                   + categoryId).then(function (response) {
                       // 获取响应数据 List<Content> --> [{},{}]
                   vue.contentList = response.data;
               });
            },
            // 搜索方法
            search : function () {
                // 跳转到搜索系统
                location.href = "http://search.pinyougou.com?keywords=" + this.keywords;
            },
            // 获取登录用户名
            loadUsername : function () {
                // 对URL进行了编码
                this.redirectUrl = window.encodeURIComponent(location.href);
                axios.get("/user/showName").then(function(response){
                    // 获取响应数据
                    vue.loginName = response.data.loginName;
                });
            }
        },
        created : function () { // 创建生命周期
            this.loadUsername();
            // 根据内容分类id查询广告数据
            this.findContentByCategoryId(1);
        }
    });
});