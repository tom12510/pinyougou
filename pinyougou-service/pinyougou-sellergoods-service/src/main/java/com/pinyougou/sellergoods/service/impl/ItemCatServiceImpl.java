package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.ItemCatMapper;
import com.pinyougou.pojo.ItemCat;
import com.pinyougou.service.ItemCatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品分类服务接口实现类
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-07-16<p>
 */
@Service
@Transactional
public class ItemCatServiceImpl implements ItemCatService {

    @Autowired
    private ItemCatMapper itemCatMapper;

    @Override
    public void save(ItemCat itemCat) {
        try{
            itemCatMapper.insertSelective(itemCat);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void update(ItemCat itemCat) {
        try{
            itemCatMapper.updateByPrimaryKeySelective(itemCat);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void delete(Serializable id) {

    }

    @Override
    public void deleteAll(Serializable[] ids) {
        try{
            // DELETE FROM `tb_item_cat` WHERE id IN(?,?,?)
            // 1. 定义List集合封装所有的商品分类id
            List<Long> idList = new ArrayList<>();

            // 2. 封装需要删除的id
            for (Serializable id : ids) {
                // 添加id
                idList.add((Long)id);

                // 递归查询商品分类id
                findLeafNode(id, idList);
            }

            // 3. 批量删除
            Example example = new Example(ItemCat.class);
            // 创建条件对象
            Example.Criteria criteria = example.createCriteria();
            // id IN(?,?,?)
            criteria.andIn("id", idList);
            // 条件删除
            itemCatMapper.deleteByExample(example);

        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /** 递归查询商品分类id */
    private void findLeafNode(Serializable id, List<Long> idList) {
        // 根据父级id查询商品分类
        List<ItemCat> itemCatList = findItemCatByParentId((Long)id);
        // 判断商品分类集合，该条件是递归退出的条件
        if (itemCatList != null && itemCatList.size() > 0){
            for (ItemCat itemCat : itemCatList) {
                // 添加需要删除的id
                idList.add(itemCat.getId());
                // 递归查询
                findLeafNode(itemCat.getId(), idList);
            }
        }
    }

    @Override
    public ItemCat findOne(Serializable id) {
        return null;
    }

    @Override
    public List<ItemCat> findAll() {
        return null;
    }

    @Override
    public List<ItemCat> findByPage(ItemCat itemCat, int page, int rows) {
        return null;
    }

    @Override
    public List<ItemCat> findItemCatByParentId(Long parentId) {
        try{
            // SELECT * FROM `tb_item_cat` WHERE parent_id = 0
            ItemCat itemCat = new ItemCat();
            itemCat.setParentId(parentId);
            return itemCatMapper.select(itemCat);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
}
