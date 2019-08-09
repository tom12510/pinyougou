package com.pinyougou.common.pojo;

import java.io.Serializable;
import java.util.List;

/**
 * 分页实体
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-07-12<p>
 */
public class PageResult implements Serializable {

    //  {pages : 100, rows : [{},{}]}
    // 总页数
    private long pages;
    // 分页数据
    private List<?> rows;
    public PageResult(){
    }
    public PageResult(long pages, List<?> rows) {
        this.pages = pages;
        this.rows = rows;
    }

    public long getPages() {
        return pages;
    }

    public void setPages(long pages) {
        this.pages = pages;
    }

    public List<?> getRows() {
        return rows;
    }

    public void setRows(List<?> rows) {
        this.rows = rows;
    }
}
