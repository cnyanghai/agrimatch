package com.agrimatch.product_schema.mapper;

import com.agrimatch.product_schema.domain.NhtProductSchema;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProductSchemaMapper {

    /**
     * 查询所有有效业态
     */
    @Select("SELECT id, schema_code, schema_name, description, form_config_json, icon, sort, status, create_time, update_time " +
            "FROM nht_product_schema WHERE status = '0' ORDER BY sort ASC")
    List<NhtProductSchema> selectAllActive();

    /**
     * 根据业态代码查询
     */
    @Select("SELECT id, schema_code, schema_name, description, form_config_json, icon, sort, status, create_time, update_time " +
            "FROM nht_product_schema WHERE schema_code = #{schemaCode} AND status = '0'")
    NhtProductSchema selectByCode(@Param("schemaCode") String schemaCode);

    /**
     * 根据ID查询
     */
    @Select("SELECT id, schema_code, schema_name, description, form_config_json, icon, sort, status, create_time, update_time " +
            "FROM nht_product_schema WHERE id = #{id}")
    NhtProductSchema selectById(@Param("id") Long id);
}
