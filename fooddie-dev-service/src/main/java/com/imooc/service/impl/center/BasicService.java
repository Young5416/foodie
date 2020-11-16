package com.imooc.service.impl.center;

import com.github.pagehelper.PageInfo;
import com.imooc.utils.PagedGridResult;
import java.util.List;

/**
 * @description:
 * @author: Young
 * @create: 2020-11-16 21:05
 **/

public class BasicService {

    public PagedGridResult setterPagedGrid(List<?> list, Integer page) {
        PageInfo<?> pageList = new PageInfo<>(list);
        PagedGridResult grid = new PagedGridResult();
        grid.setPage(page);
        grid.setRows(list);
        grid.setTotal(pageList.getPages());
        grid.setRecords(pageList.getTotal());
        return grid;
    }
}
