// 窗口加载完
$(function(){
    // 创建Vue对象
    var vue = new Vue({
        el : '#app', // 元素绑定
        data : { // 数据模型
            goods : {goodsDesc : {itemImages  : [], customAttributeItems : [], specificationItems : []},
                     category1Id : '',
                     category2Id : '',
                     category3Id : '',
                     typeTemplateId : '',
                     brandId : '',
                     items : [],
                     isEnableSpec : 0}, // 数据封装对象(表单)
            picEntity : {color : '', url : ''}, // 上传的商品图片
            itemCatList1 : [], // 商品一级分类数组
            itemCatList2 : [], // 商品二级分类数组
            itemCatList3 : [], // 商品三级分类数组
            brandList : [], // 品牌数组
            specList : []   // 规格选项数组
        },
        methods : { // 定义操作方法
            saveOrUpdate : function () { // 添加或修改
                // 获取富文本编辑器中的内容
                this.goods.goodsDesc.introduction = editor.html();
                // 发送异步请求
                axios.post("/goods/save", this.goods)
                    .then(function(response){
                    // 获取响应数据
                    if (response.data){ // 操作成功
                        // 清空表单数据
                        vue.goods = {goodsDesc : {}};
                        // 清空富文本编辑器中的内容
                        editor.html("");
                    }else {
                        alert('操作失败！');
                    }
                });
            },
            // 异步上传文件
            uploadFile : function () {

                // 创建表单数据对象(封装请求参数) html5
                var formData = new FormData();
                // 追加上传的文件数据
                // 第一个参数：请求参数名称
                // 第二个参数：文件上传的input type=file的dom对象
                formData.append("file", file.files[0]);

                // 异步请求上传文件
                axios({
                    method : "post", // 请求方式
                    url : "/upload", // 请求URL
                    data : formData, // 请求参数
                    headers : {"Content-Type" : "multipart/form-data"} // 请求头
                }).then(function(response){
                    // 获取响应数据 response.data : {status : 200, url : ''}
                    if (response.data.status == 200){
                        vue.picEntity.url = response.data.url;
                    }
                });
            },
            // 添加图片到图片数组
            addPic : function () {
                this.goods.goodsDesc.itemImages.push(this.picEntity);
            },
            // 从图片数组中删除图片
            removePic : function (idx) {
                this.goods.goodsDesc.itemImages.splice(idx,1);
            },
            // 根据父级id查询商品分类
            findItemCatByParentId : function (parentId, name) {
                axios.get("/itemCat/findItemCatByParentId?parentId="
                    + parentId).then(function(response){
                    // [{},{}]
                    vue[name] = response.data;
                });
            },
            // 选择规格选项
            selectSpec : function (e, specName, optionName) {
                /**
                 * goods.goodsDesc.specificationItems:
                 * [{"attributeValue":["联通4G","移动4G","电信4G"],"attributeName":"网络"},
                 * {"attributeValue":["64G","128G"],"attributeName":"机身内存"}]
                 */
                var obj = this.searchJsonByKey(this.goods.goodsDesc.specificationItems,
                    "attributeName", specName);
                if (obj){
                    // obj: {"attributeValue":["联通4G","移动4G","电信4G"],"attributeName":"网络"}
                    // 判断checkbox是否选中
                    if (e.target.checked){ // 选中
                        obj.attributeValue.push(optionName);
                    }else{ // 没选中
                        // 获取optionName元素在obj.attributeValue中的索引号
                        var idx = obj.attributeValue.indexOf(optionName);
                        // 从obj.attributeValue数组中删除一个元素
                        obj.attributeValue.splice(idx,1);

                        // 判断obj.attributeValue数组的长度
                        if (obj.attributeValue.length == 0){
                            // 获取元素在数组中的索引号
                            var idx = this.goods.goodsDesc.specificationItems.indexOf(obj);
                            // 从goods.goodsDesc.specificationItems中删除里面的元素
                            this.goods.goodsDesc.specificationItems.splice(idx,1);
                        }
                    }
                }else {
                    this.goods.goodsDesc.specificationItems
                        .push({attributeValue: [optionName], attributeName: specName});
                }
            },
            // 根据key从json数组中搜索一个json对象
            searchJsonByKey : function (jsonArr, key, specName) {
                for (var i = 0; i < jsonArr.length; i++){
                    // {"attributeValue":["联通4G","移动4G","电信4G"],"attributeName":"网络"}
                    var obj = jsonArr[i];
                    // obj.attributeName
                    if (obj[key] == specName){
                        return obj;
                    }
                }
                return null;
            },
            // 根据选择的规格选项生成SKU数组
            createItems : function () {
                // 1.  定义SKU数组变量，并初始化
                this.goods.items = [{spec:{}, price:0, num:9999,
                    status:'0', isDefault:'0'}];

                // 2. 获取用户选择的规格选项数组
                // [ { "attributeValue": [ "移动4G", "联通3G", "联通4G" ], "attributeName": "网络" } ]
                var specItems = this.goods.goodsDesc.specificationItems;
                // [ { "attributeValue": [ "移动3G", "移动4G", "联通3G" ], "attributeName": "网络" },
                //   { "attributeValue": [ "32G" ], "attributeName": "机身内存" } ]
                // 3. 循环规格选项数组生成SKU数组
                for (var i = 0; i < specItems.length; i++){
                    // 获取一个数组元素
                    // { "attributeValue": [ "移动4G", "联通3G", "联通4G" ], "attributeName": "网络" }
                    var obj = specItems[i];

                    // 调用方法不断扩展原来的SKU数组，返回一个新SKU数组
                    this.goods.items = this.swapItems(this.goods.items,
                        obj.attributeValue, obj.attributeName);
                }

            },
            // 不断扩展原来的SKU数组，返回一个新SKU数组
            swapItems : function (items, attributeValue, attributeName) {
                // items :  [{spec:{}, price:0, num:9999, status:'0', isDefault:'0'}];
                // attributeValue:  [ "移动4G", "联通3G", "联通4G" ]
                // attributeName: "网络"
                // 定义新的SKU数组
                var newItems = [];
                for (var i = 0; i < items.length; i++){ // 1
                    // item : {spec:{}, price:0, num:9999, status:'0', isDefault:'0'}
                    var item = items[i];

                    // attributeValue: [ "移动4G", "联通3G", "联通4G" ]
                    for (var j = 0; j < attributeValue.length; j++){ // 3

                        // 克隆item产生新的item
                        var newItem = JSON.parse(JSON.stringify(item));
                        // newItem.spec = {"网络":"联通4G","机身内存":"64G"}
                        newItem.spec[attributeName] = attributeValue[j];
                        // 添加到新的数组
                        newItems.push(newItem);
                    }
                }
                return newItems;
            }
        },
        watch : { // 监控data中的变量
            // 监控 goods.category1Id变量发生改变，查询二级分类
            "goods.category1Id" : function (newVal, oldVal) {
                // 清空数据
                this.goods.category2Id = "";
                // alert("新值：" + newVal + "，旧值:" + oldVal);
                if (newVal){ // 不是null ""
                   this.findItemCatByParentId(newVal, "itemCatList2");
                }else{
                    this.itemCatList2 = [];
                }
            },
            // 监控 goods.category2Id变量发生改变，查询三级分类
            "goods.category2Id" : function (newVal, oldVal) {
                // 清空数据
                this.goods.category3Id = "";
                if (newVal){ // 不是null ""
                    this.findItemCatByParentId(newVal, "itemCatList3");
                }else{
                    this.itemCatList3 = [];
                }
            },
            // 监控 goods.category3Id变量发生改变，查询类型模板id
            "goods.category3Id" : function (newVal, oldVal) {
                // 清空数据
                this.goods.typeTemplateId = "";
                if (newVal){ // 不是null ""
                    for (var i = 0; i < this.itemCatList3.length; i++){
                       var itemCat = this.itemCatList3[i];
                       if  (itemCat.id == newVal){
                           this.goods.typeTemplateId =itemCat.typeId;
                           break;
                       }
                    }
                }
            },
            // 监控 goods.typeTemplateId变量发生改变，查询类型模板对象
            "goods.typeTemplateId" : function (newVal, oldVal) {
                // 清空数据
                if (newVal){ // 不是null ""
                    axios.get("/typeTemplate/findOne?id=" + newVal).then(function(response){
                        // 获取响应数据 response.data : {}
                        // 获取品牌
                        vue.brandList = JSON.parse(response.data.brandIds);
                        // 获取扩展属性
                        vue.goods.goodsDesc.customAttributeItems = JSON.parse(response.data.customAttributeItems);
                    });

                    // 查询规格选项数据
                    axios.get("/typeTemplate/findSpecOptionByTemplateId?id="
                        + newVal).then(function(response){
                        // 获取响应数据
                        /**
                         * [{"id":27,"text":"网络", "options" : [{},{}]},
                            {"id":32,"text":"机身内存", "options" : [{},{}]}]
                         */
                        vue.specList = response.data;
                    });
                }else {
                    vue.brandList = [];
                    vue.goods.goodsDesc.customAttributeItems = [];
                    vue.specList = [];
                }
            }
        },
        created : function () { // 创建生命周期(初始化方法)
            // 根据父级id查询商品分类
            this.findItemCatByParentId(0, "itemCatList1");
        }
    });
});