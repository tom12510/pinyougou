// 文档加载完
window.onload = function () {
    // 创建Vue实例
    var vue = new Vue({
        el : '#app', // 元素绑定
        data : {     // 数据模型
            dataList : [], // 品牌数据
            entity : {}, // 表单数据绑定
            pages : 0, // 总页数
            page : 1,   // 当前页码
            searchEntity : {}, // 搜索条件数据封装
            ids : [], // 封装删除的品牌id
            checked : false // 全选checkbox是否选中
        },
        methods : { // 操作方法
            // 查询品牌
            search : function (page) {
                // 发送异步请求
                axios.get("/brand/findByPage?page=" + page,
                    {params : this.searchEntity}).then(function(response){
                    // 获取响应数据 response.data : {pages : 100, rows : [{},{}]}
                    vue.dataList = response.data.rows;
                    // 设置总页数
                    vue.pages = response.data.pages;
                    // 设置当前页码
                    vue.page = page;
                    // 清空数组
                    vue.ids = [];
                });
            },
            // 添加或修改
            saveOrUpdate : function () {
                var url = "save";
                if (this.entity.id){ // 修改
                    url = "update";
                }
                axios.post("/brand/" + url, this.entity).then(function(response){
                    // 获取响应数据 response.data: true|false
                    if (response.data){
                        // 重新加载数据
                        vue.search(vue.page);
                    }else{
                        alert("操作失败！");
                    }
                });
            },
            // 显示修改
            show : function (entity) {
                // 把entity对象转化成json字符串
                var jsonStr = JSON.stringify(entity);
                // 把jsonStr字符串转化成新的json对象
                this.entity = JSON.parse(jsonStr);
            },
            // 全选点击事件
            checkAll : function (e) {
                // 清空ids数组
                this.ids = [];
                // 获取dom元素
                if(e.target.checked){ // 全选checkbox选中
                    for (var i = 0; i < this.dataList.length; i++){
                        this.ids.push(this.dataList[i].id);
                    }
                }
            },
            // 删除
            del : function () {
                if (this.ids.length > 0){
                    axios.get("/brand/delete?ids=" + this.ids).then(function(response){
                        // 获取响应数据
                        if (response.data){
                            // 判断是否全选 并且是最后一页
                            var page = (vue.checked && vue.page == vue.pages) ? vue.page - 1 : vue.page ;
                            if (page <= 0){
                                page = 1;
                            }
                            // 重新加载数据
                            vue.search(page);
                        }else{
                            alert("删除失败！");
                        }
                    });
                }else{
                    alert("请选择要删除的品牌！");
                }
            }
        },
        created : function () { // 创建Vue实例之后，需要调用的方法(生命周期)
            this.search(this.page);
        },
        updated : function () { // 数据更新之后
            // 判断全选checkbox是否选中
            this.checked = this.ids.length == this.dataList.length;
        }
    });
};