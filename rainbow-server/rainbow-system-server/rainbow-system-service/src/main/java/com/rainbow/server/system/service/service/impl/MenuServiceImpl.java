package com.rainbow.server.system.service.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rainbow.common.core.entity.system.Menu;
import com.rainbow.common.core.exception.RainbowException;
import com.rainbow.server.system.service.mapper.MenuMapper;
import com.rainbow.server.system.service.service.IMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 *  @Description 菜单业务层实现类
 *  @author liuhu
 *  @Date 2020/5/15 10:23
 */
@Service
public class MenuServiceImpl implements IMenuService {

    @Autowired
    private MenuMapper menuMapper;

    @Override
    public List<Menu> getMenuTree() {
       try {
           List<Menu> rootMenu = menuMapper.selectList(new QueryWrapper<Menu>().orderByDesc("create_time"));
           List<Menu> menuTree=null;
           // 得到顶级菜单  递归子菜单
           if(!CollectionUtils.isEmpty(rootMenu)){
               menuTree = rootMenu.stream().filter(menu -> Menu.TYPE_MENU.equals(menu.getType()) && Menu.TOP_MENU_ID == menu.getParentId())
                       .map(menu -> {
                           menu.setChildMenus(getChildren(menu, rootMenu));
                           return menu;
                       }).collect(Collectors.toList());
           }
           return menuTree;
       }catch (Exception e){
           e.printStackTrace();
           throw new RainbowException("获取树菜单失败");
       }
    }

    @Override
    public Menu save(Menu menu) {
       try {
           if(null != menu.getMenuId()){
               menuMapper.updateById(menu);
           }else{
               menuMapper.insert(menu);
           }
       }catch (Exception e){
           e.printStackTrace();
           throw new RainbowException("保存菜单失败");
       }
        return menu;
    }

    @Override
    public void delete(long menuId) {
        try {
            Menu menu = menuMapper.selectById(menuId);
            QueryWrapper<Menu> queryWrapper = new QueryWrapper<>();
            List<Menu> menus = menuMapper.selectList(queryWrapper.eq("PARENT_ID", menu.getMenuId()));
            if(!CollectionUtils.isEmpty(menus)){
                throw new RainbowException("该菜单有子菜单不允许删除");
            }
            menuMapper.deleteById(menuId);
        }catch (Exception e){
            e.printStackTrace();
            throw new RainbowException("删除菜单失败");
        }
    }

    /**
     * @Description 递归得到子菜单
     * @author liuhu
     * @createTime 2020-05-19 18:38:38
     * @param parentMenu 父菜单
     * @param rootMenu 元数据集合
     * @return java.util.List<com.rainbow.common.core.entity.system.Menu>
     */
    public List<Menu> getChildren(Menu parentMenu, List<Menu> rootMenu) {
     return rootMenu.stream().filter(menu -> Menu.TYPE_MENU.equals(menu.getType()) && parentMenu.getMenuId() == menu.getParentId())
             .map(menu -> {
                        menu.setChildMenus(getChildren(menu,rootMenu));
                        return menu;
                        }).sorted(Comparator.comparing(Menu::getCreateTime)).collect(Collectors.toList());
    }
}
