// 窗口加载完
window.onload = function () {

    var vue = new Vue({
        el : '#app', // 元素绑定
        data : { // 数据模型
            searchParam : {keywords : "", category : '', brand : '',
                          spec : {}, price : '', page : 1,
                          sortField : '', sortValue : ''}, // 搜索参数封装对象
            resultMap : {}, // 后台响应的数据
            pageNums : [], // 页码数组
            keywords : '', // 搜索关键字
            jumpPage : 1,  // 页码
            firstDot : true, // 前面显示省略号
            lastDot : true // 后面显示省略号
        },
        methods : { // 操作方法
            // 商品搜索方法
            search : function () {
                axios.post("/search", this.searchParam).then(function(response){
                    // 获取响应数据 response.data: {rows : [{},{}], total : 2000}
                    vue.resultMap = response.data;
                    // 设置搜索关键字
                    vue.keywords = vue.searchParam.keywords;
                    // 初始化页码数组
                    vue.initPageNum();
                });
            },
            // 初始化页码数组
            initPageNum : function () {
                // 清空页码数组
                this.pageNums = [];
                // 开始页码
                var firstPage = 1;
                // 结束页面
                var lastPage = this.resultMap.totalPages;

                this.firstDot = true; // 前面显示省略号
                this.lastDot = true; // 后面显示省略号

                // 判断总页数是否大于5
                if (this.resultMap.totalPages > 5){
                    // 当前页码离首页近些
                    if (this.searchParam.page <= 3){
                        lastPage = 5;
                        this.firstDot = false; // 前面不显示省略号
                    }else if (this.resultMap.totalPages - 3 <= this.searchParam.page){
                        // 当前页码离尾页近些 100- 4 = 96
                        firstPage = this.resultMap.totalPages - 4;
                        this.lastDot = false; // 后面不显示省略号
                    }else {
                        // 当前页码在中间
                        firstPage = this.searchParam.page - 2;
                        lastPage = this.searchParam.page + 2;
                    }
                }else{
                    this.firstDot = false; // 前面不显示省略号
                    this.lastDot = false; // 后面不显示省略号
                }

                for (var i = firstPage; i <= lastPage; i++){
                    this.pageNums.push(i);
                }

            },
            // 搜索分页
            pageSearch : function (page) {
                // v-model 绑定数据是字符串类型
                page = parseInt(page);
                // 验证页码有效性
                if (page >= 1 && page <= this.resultMap.totalPages
                    && page != this.searchParam.page){
                    this.searchParam.page = page;
                    this.jumpPage = page;
                    // 执行搜索
                    this.search();
                }
            },
            // 添加过滤条件
            addSearchItem : function (key, value) {
                // 判断是否为：分类、品牌、价格
                if (key == 'category' || key == 'brand' || key == 'price'){
                    this.searchParam[key] = value;
                }else{
                    // 规格选项
                    this.searchParam.spec[key] = value;
                }
                this.searchParam.page = 1;
                // 执行搜索
                this.search();
            },
            // 删除过滤条件
            removeSearchItem : function (key) {
                // 判断是否为：分类、品牌、价格
                if (key == 'category' || key == 'brand' || key == 'price'){
                    this.searchParam[key] = "";
                }else{
                    // 规格选项
                    delete this.searchParam.spec[key];
                }
                this.searchParam.page = 1;
                // 执行搜索
                this.search();
            },
            // 搜索排序
            sortSearch : function (sortField, sortValue) {
                this.searchParam.sortField = sortField;
                this.searchParam.sortValue = sortValue;
                // 执行搜索
                this.search();
            },
            // 初始化搜索方法
            initSearch : function () {
                // http://search.pinyougou.com/?keywords=%E5%B0%8F%E7%B1%B3
                var keywords = this.getUrlParam("keywords");
                this.searchParam.keywords = keywords ? keywords : '';
                this.search();
            }
        },
        created : function () { // 创建生命周期
           this.initSearch();
        }
    });
};