package com.flux.export.mysql.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flux.export.mysql.pojo.entity.ExportTaskEntity;

/**
 * ExportTaskEntity Mapper 接口
 * 继承 MyBatis-Plus 的 BaseMapper, 自动获得 CRUD 方法
 */
public interface ExportTaskMapper extends BaseMapper<ExportTaskEntity> {
    
    // MyBatis-Plus 已经提供了常用的 CRUD 方法
    // 如果需要自定义 SQL, 可以在这里添加方法
 }
