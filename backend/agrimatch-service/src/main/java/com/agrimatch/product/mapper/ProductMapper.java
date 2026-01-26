package com.agrimatch.product.mapper;

import com.agrimatch.product.domain.NhtProduct;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface ProductMapper {
    List<NhtProduct> selectAllActive();

    List<NhtProduct> search(@Param("keyword") String keyword);

    int insertCustom(NhtProduct p);

    /**
     * 查询所有产品（包含业态信息）
     */
    @Select("SELECT id, user_id, parent_id, product_name, schema_code, has_params, allow_custom_name, status, del_flag " +
            "FROM nht_product WHERE del_flag = '0' AND status = '0' ORDER BY parent_id ASC, id ASC")
    List<Map<String, Object>> selectAllWithSchema();

    /**
     * 根据业态代码查询产品
     */
    @Select("SELECT id, user_id, parent_id, product_name, schema_code, has_params, allow_custom_name, status, del_flag " +
            "FROM nht_product WHERE del_flag = '0' AND status = '0' AND schema_code = #{schemaCode} ORDER BY parent_id ASC, id ASC")
    List<Map<String, Object>> selectBySchemaCode(@Param("schemaCode") String schemaCode);

    /**
     * 根据ID查询产品
     */
    @Select("SELECT id, user_id, parent_id, product_name, schema_code, has_params, allow_custom_name, status, del_flag " +
            "FROM nht_product WHERE id = #{id} AND del_flag = '0'")
    NhtProduct selectById(@Param("id") Long id);
}


