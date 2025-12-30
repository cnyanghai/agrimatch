/*
 Navicat Premium Data Transfer

 Source Server         : @localhost
 Source Server Type    : MySQL
 Source Server Version : 80042
 Source Host           : localhost:3306
 Source Schema         : nonghuitong

 Target Server Type    : MySQL
 Target Server Version : 80042
 File Encoding         : 65001

 Date: 19/12/2025 21:04:29
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for gen_table
-- ----------------------------
DROP TABLE IF EXISTS `gen_table`;
CREATE TABLE `gen_table` (
  `table_id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `table_name` varchar(200) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '表名称',
  `table_comment` varchar(500) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '表描述',
  `sub_table_name` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '关联子表的表名',
  `sub_table_fk_name` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '子表关联的外键名',
  `class_name` varchar(100) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '实体类名称',
  `tpl_category` varchar(200) COLLATE utf8mb4_general_ci DEFAULT 'crud' COMMENT '使用的模板（crud单表操作 tree树表操作）',
  `tpl_web_type` varchar(30) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '前端模板类型（element-ui模版 element-plus模版）',
  `package_name` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '生成包路径',
  `module_name` varchar(30) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '生成模块名',
  `business_name` varchar(30) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '生成业务名',
  `function_name` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '生成功能名',
  `function_author` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '生成功能作者',
  `gen_type` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '生成代码方式（0zip压缩包 1自定义路径）',
  `gen_path` varchar(200) COLLATE utf8mb4_general_ci DEFAULT '/' COMMENT '生成路径（不填默认项目路径）',
  `options` varchar(1000) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '其它生成选项',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`table_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='代码生成业务表';

-- ----------------------------
-- Records of gen_table
-- ----------------------------
BEGIN;
INSERT INTO `gen_table` (`table_id`, `table_name`, `table_comment`, `sub_table_name`, `sub_table_fk_name`, `class_name`, `tpl_category`, `tpl_web_type`, `package_name`, `module_name`, `business_name`, `function_name`, `function_author`, `gen_type`, `gen_path`, `options`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1, 'nht_address', '地址表', NULL, NULL, 'NhtAddress', 'crud', 'element-ui', 'com.ruoyi.nht', 'nht', 'address', '地址', 'ruoyi', '0', '/', '{\"parentMenuId\":2000}', 'admin', '2025-12-13 17:20:16', '', '2025-12-13 17:37:46', NULL);
INSERT INTO `gen_table` (`table_id`, `table_name`, `table_comment`, `sub_table_name`, `sub_table_fk_name`, `class_name`, `tpl_category`, `tpl_web_type`, `package_name`, `module_name`, `business_name`, `function_name`, `function_author`, `gen_type`, `gen_path`, `options`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2, 'nht_product', '产品表', '', '', 'NhtProduct', 'tree', 'element-ui', 'com.ruoyi.nht', 'nht', 'product', '产品', 'ruoyi', '0', '/', '{\"treeCode\":\"id\",\"treeName\":\"product_name\",\"treeParentCode\":\"parent_id\",\"parentMenuId\":2000}', 'admin', '2025-12-14 17:20:16', '', '2025-12-14 17:21:55', NULL);
INSERT INTO `gen_table` (`table_id`, `table_name`, `table_comment`, `sub_table_name`, `sub_table_fk_name`, `class_name`, `tpl_category`, `tpl_web_type`, `package_name`, `module_name`, `business_name`, `function_name`, `function_author`, `gen_type`, `gen_path`, `options`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3, 'nht_product_parameters', '产品属性表', NULL, NULL, 'NhtProductParameters', 'crud', 'element-ui', 'com.ruoyi.nht', 'nht', 'parameters', '产品属性', 'ruoyi', '0', '/', '{}', 'admin', '2025-12-14 17:20:16', '', '2025-12-14 17:22:25', NULL);
INSERT INTO `gen_table` (`table_id`, `table_name`, `table_comment`, `sub_table_name`, `sub_table_fk_name`, `class_name`, `tpl_category`, `tpl_web_type`, `package_name`, `module_name`, `business_name`, `function_name`, `function_author`, `gen_type`, `gen_path`, `options`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4, 'nht_product_base_offer', '产品基础报价', NULL, NULL, 'NhtProductBaseOffer', 'crud', 'element-ui', 'com.ruoyi.nht', 'nht', 'bascOffer', '产品基础报价', 'ruoyi', '0', '/', '{\"parentMenuId\":2016}', 'admin', '2025-12-19 15:38:38', '', '2025-12-19 15:39:18', NULL);
COMMIT;

-- ----------------------------
-- Table structure for gen_table_column
-- ----------------------------
DROP TABLE IF EXISTS `gen_table_column`;
CREATE TABLE `gen_table_column` (
  `column_id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `table_id` bigint DEFAULT NULL COMMENT '归属表编号',
  `column_name` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '列名称',
  `column_comment` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '列描述',
  `column_type` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '列类型',
  `java_type` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'JAVA类型',
  `java_field` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'JAVA字段名',
  `is_pk` char(1) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '是否主键（1是）',
  `is_increment` char(1) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '是否自增（1是）',
  `is_required` char(1) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '是否必填（1是）',
  `is_insert` char(1) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '是否为插入字段（1是）',
  `is_edit` char(1) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '是否编辑字段（1是）',
  `is_list` char(1) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '是否列表字段（1是）',
  `is_query` char(1) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '是否查询字段（1是）',
  `query_type` varchar(200) COLLATE utf8mb4_general_ci DEFAULT 'EQ' COMMENT '查询方式（等于、不等于、大于、小于、范围）',
  `html_type` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '显示类型（文本框、文本域、下拉框、复选框、单选框、日期控件）',
  `dict_type` varchar(200) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '字典类型',
  `sort` int DEFAULT NULL COMMENT '排序',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`column_id`)
) ENGINE=InnoDB AUTO_INCREMENT=49 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='代码生成业务表字段';

-- ----------------------------
-- Records of gen_table_column
-- ----------------------------
BEGIN;
INSERT INTO `gen_table_column` (`column_id`, `table_id`, `column_name`, `column_comment`, `column_type`, `java_type`, `java_field`, `is_pk`, `is_increment`, `is_required`, `is_insert`, `is_edit`, `is_list`, `is_query`, `query_type`, `html_type`, `dict_type`, `sort`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (1, 1, 'id', 'id', 'bigint', 'Long', 'id', '1', '1', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2025-12-13 17:20:16', '', '2025-12-13 17:37:46');
INSERT INTO `gen_table_column` (`column_id`, `table_id`, `column_name`, `column_comment`, `column_type`, `java_type`, `java_field`, `is_pk`, `is_increment`, `is_required`, `is_insert`, `is_edit`, `is_list`, `is_query`, `query_type`, `html_type`, `dict_type`, `sort`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (2, 1, 'user_id', '用户ID', 'bigint', 'Long', 'userId', '0', '0', '1', '1', '0', '0', '0', 'EQ', 'input', '', 2, 'admin', '2025-12-13 17:20:16', '', '2025-12-13 17:37:46');
INSERT INTO `gen_table_column` (`column_id`, `table_id`, `column_name`, `column_comment`, `column_type`, `java_type`, `java_field`, `is_pk`, `is_increment`, `is_required`, `is_insert`, `is_edit`, `is_list`, `is_query`, `query_type`, `html_type`, `dict_type`, `sort`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (3, 1, 'name', '地址别名', 'varchar(30)', 'String', 'name', '0', '0', '1', '1', '1', '1', '1', 'LIKE', 'input', '', 3, 'admin', '2025-12-13 17:20:16', '', '2025-12-13 17:37:46');
INSERT INTO `gen_table_column` (`column_id`, `table_id`, `column_name`, `column_comment`, `column_type`, `java_type`, `java_field`, `is_pk`, `is_increment`, `is_required`, `is_insert`, `is_edit`, `is_list`, `is_query`, `query_type`, `html_type`, `dict_type`, `sort`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (4, 1, 'address', '详细地址', 'varchar(255)', 'String', 'address', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'input', '', 4, 'admin', '2025-12-13 17:20:16', '', '2025-12-13 17:37:46');
INSERT INTO `gen_table_column` (`column_id`, `table_id`, `column_name`, `column_comment`, `column_type`, `java_type`, `java_field`, `is_pk`, `is_increment`, `is_required`, `is_insert`, `is_edit`, `is_list`, `is_query`, `query_type`, `html_type`, `dict_type`, `sort`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (5, 1, 'contacts', '联系人', 'varchar(20)', 'String', 'contacts', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'input', '', 5, 'admin', '2025-12-13 17:20:16', '', '2025-12-13 17:37:46');
INSERT INTO `gen_table_column` (`column_id`, `table_id`, `column_name`, `column_comment`, `column_type`, `java_type`, `java_field`, `is_pk`, `is_increment`, `is_required`, `is_insert`, `is_edit`, `is_list`, `is_query`, `query_type`, `html_type`, `dict_type`, `sort`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (6, 1, 'contacts_post', '职务', 'varchar(20)', 'String', 'contactsPost', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 6, 'admin', '2025-12-13 17:20:16', '', '2025-12-13 17:37:46');
INSERT INTO `gen_table_column` (`column_id`, `table_id`, `column_name`, `column_comment`, `column_type`, `java_type`, `java_field`, `is_pk`, `is_increment`, `is_required`, `is_insert`, `is_edit`, `is_list`, `is_query`, `query_type`, `html_type`, `dict_type`, `sort`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (8, 1, 'status', '状态（0正常 1停用）', 'char(1)', 'String', 'status', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'select', 'sys_normal_disable', 8, 'admin', '2025-12-13 17:20:16', '', '2025-12-13 17:37:46');
INSERT INTO `gen_table_column` (`column_id`, `table_id`, `column_name`, `column_comment`, `column_type`, `java_type`, `java_field`, `is_pk`, `is_increment`, `is_required`, `is_insert`, `is_edit`, `is_list`, `is_query`, `query_type`, `html_type`, `dict_type`, `sort`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (9, 1, 'del_flag', '删除标志（0代表存在 2代表删除）', 'char(1)', 'String', 'delFlag', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 9, 'admin', '2025-12-13 17:20:16', '', '2025-12-13 17:37:46');
INSERT INTO `gen_table_column` (`column_id`, `table_id`, `column_name`, `column_comment`, `column_type`, `java_type`, `java_field`, `is_pk`, `is_increment`, `is_required`, `is_insert`, `is_edit`, `is_list`, `is_query`, `query_type`, `html_type`, `dict_type`, `sort`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (10, 1, 'create_by', '创建者', 'varchar(64)', 'String', 'createBy', '0', '0', '0', '1', NULL, '1', NULL, 'EQ', 'input', '', 10, 'admin', '2025-12-13 17:20:16', '', '2025-12-13 17:37:46');
INSERT INTO `gen_table_column` (`column_id`, `table_id`, `column_name`, `column_comment`, `column_type`, `java_type`, `java_field`, `is_pk`, `is_increment`, `is_required`, `is_insert`, `is_edit`, `is_list`, `is_query`, `query_type`, `html_type`, `dict_type`, `sort`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (11, 1, 'create_time', '创建时间', 'datetime', 'Date', 'createTime', '0', '0', '0', '1', NULL, '1', NULL, 'EQ', 'datetime', '', 11, 'admin', '2025-12-13 17:20:16', '', '2025-12-13 17:37:46');
INSERT INTO `gen_table_column` (`column_id`, `table_id`, `column_name`, `column_comment`, `column_type`, `java_type`, `java_field`, `is_pk`, `is_increment`, `is_required`, `is_insert`, `is_edit`, `is_list`, `is_query`, `query_type`, `html_type`, `dict_type`, `sort`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (12, 1, 'update_by', '更新者', 'varchar(64)', 'String', 'updateBy', '0', '0', '0', '1', '0', '1', NULL, 'EQ', 'input', '', 12, 'admin', '2025-12-13 17:20:16', '', '2025-12-13 17:37:46');
INSERT INTO `gen_table_column` (`column_id`, `table_id`, `column_name`, `column_comment`, `column_type`, `java_type`, `java_field`, `is_pk`, `is_increment`, `is_required`, `is_insert`, `is_edit`, `is_list`, `is_query`, `query_type`, `html_type`, `dict_type`, `sort`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (13, 1, 'update_time', '更新时间', 'datetime', 'Date', 'updateTime', '0', '0', '0', '1', '0', '1', NULL, 'EQ', 'datetime', '', 13, 'admin', '2025-12-13 17:20:16', '', '2025-12-13 17:37:46');
INSERT INTO `gen_table_column` (`column_id`, `table_id`, `column_name`, `column_comment`, `column_type`, `java_type`, `java_field`, `is_pk`, `is_increment`, `is_required`, `is_insert`, `is_edit`, `is_list`, `is_query`, `query_type`, `html_type`, `dict_type`, `sort`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (14, 1, 'remark', '备注', 'varchar(500)', 'String', 'remark', '0', '0', '0', '1', '1', '1', NULL, 'EQ', 'textarea', '', 14, 'admin', '2025-12-13 17:20:16', '', '2025-12-13 17:37:46');
INSERT INTO `gen_table_column` (`column_id`, `table_id`, `column_name`, `column_comment`, `column_type`, `java_type`, `java_field`, `is_pk`, `is_increment`, `is_required`, `is_insert`, `is_edit`, `is_list`, `is_query`, `query_type`, `html_type`, `dict_type`, `sort`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (15, 1, 'is_default', '是否默认 Y是  N否', 'char(2)', 'String', 'isDefault', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'select', 'sys_yes_no', 7, '', '2025-12-13 17:36:54', '', '2025-12-13 17:37:46');
INSERT INTO `gen_table_column` (`column_id`, `table_id`, `column_name`, `column_comment`, `column_type`, `java_type`, `java_field`, `is_pk`, `is_increment`, `is_required`, `is_insert`, `is_edit`, `is_list`, `is_query`, `query_type`, `html_type`, `dict_type`, `sort`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (16, 2, 'id', 'id', 'bigint', 'Long', 'id', '1', '1', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2025-12-14 17:20:16', '', '2025-12-14 17:21:55');
INSERT INTO `gen_table_column` (`column_id`, `table_id`, `column_name`, `column_comment`, `column_type`, `java_type`, `java_field`, `is_pk`, `is_increment`, `is_required`, `is_insert`, `is_edit`, `is_list`, `is_query`, `query_type`, `html_type`, `dict_type`, `sort`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (17, 2, 'user_id', '用户id', 'bigint', 'Long', 'userId', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 2, 'admin', '2025-12-14 17:20:16', '', '2025-12-14 17:21:55');
INSERT INTO `gen_table_column` (`column_id`, `table_id`, `column_name`, `column_comment`, `column_type`, `java_type`, `java_field`, `is_pk`, `is_increment`, `is_required`, `is_insert`, `is_edit`, `is_list`, `is_query`, `query_type`, `html_type`, `dict_type`, `sort`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (18, 2, 'parent_id', '父id', 'bigint', 'Long', 'parentId', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 3, 'admin', '2025-12-14 17:20:16', '', '2025-12-14 17:21:55');
INSERT INTO `gen_table_column` (`column_id`, `table_id`, `column_name`, `column_comment`, `column_type`, `java_type`, `java_field`, `is_pk`, `is_increment`, `is_required`, `is_insert`, `is_edit`, `is_list`, `is_query`, `query_type`, `html_type`, `dict_type`, `sort`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (19, 2, 'ancestors', '祖级列表', 'varchar(50)', 'String', 'ancestors', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 4, 'admin', '2025-12-14 17:20:16', '', '2025-12-14 17:21:55');
INSERT INTO `gen_table_column` (`column_id`, `table_id`, `column_name`, `column_comment`, `column_type`, `java_type`, `java_field`, `is_pk`, `is_increment`, `is_required`, `is_insert`, `is_edit`, `is_list`, `is_query`, `query_type`, `html_type`, `dict_type`, `sort`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (20, 2, 'product_name', '产品名称', 'varchar(30)', 'String', 'productName', '0', '0', '0', '1', '1', '1', '1', 'LIKE', 'input', '', 5, 'admin', '2025-12-14 17:20:16', '', '2025-12-14 17:21:55');
INSERT INTO `gen_table_column` (`column_id`, `table_id`, `column_name`, `column_comment`, `column_type`, `java_type`, `java_field`, `is_pk`, `is_increment`, `is_required`, `is_insert`, `is_edit`, `is_list`, `is_query`, `query_type`, `html_type`, `dict_type`, `sort`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (21, 2, 'status', '状态（0正常 1停用）', 'char(1)', 'String', 'status', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'radio', 'sys_normal_disable', 6, 'admin', '2025-12-14 17:20:16', '', '2025-12-14 17:21:55');
INSERT INTO `gen_table_column` (`column_id`, `table_id`, `column_name`, `column_comment`, `column_type`, `java_type`, `java_field`, `is_pk`, `is_increment`, `is_required`, `is_insert`, `is_edit`, `is_list`, `is_query`, `query_type`, `html_type`, `dict_type`, `sort`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (22, 2, 'del_flag', '删除标志（0代表存在 2代表删除）', 'char(1)', 'String', 'delFlag', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 7, 'admin', '2025-12-14 17:20:16', '', '2025-12-14 17:21:55');
INSERT INTO `gen_table_column` (`column_id`, `table_id`, `column_name`, `column_comment`, `column_type`, `java_type`, `java_field`, `is_pk`, `is_increment`, `is_required`, `is_insert`, `is_edit`, `is_list`, `is_query`, `query_type`, `html_type`, `dict_type`, `sort`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (23, 2, 'create_by', '创建者', 'varchar(64)', 'String', 'createBy', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 8, 'admin', '2025-12-14 17:20:16', '', '2025-12-14 17:21:55');
INSERT INTO `gen_table_column` (`column_id`, `table_id`, `column_name`, `column_comment`, `column_type`, `java_type`, `java_field`, `is_pk`, `is_increment`, `is_required`, `is_insert`, `is_edit`, `is_list`, `is_query`, `query_type`, `html_type`, `dict_type`, `sort`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (24, 2, 'create_time', '创建时间', 'datetime', 'Date', 'createTime', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'datetime', '', 9, 'admin', '2025-12-14 17:20:16', '', '2025-12-14 17:21:55');
INSERT INTO `gen_table_column` (`column_id`, `table_id`, `column_name`, `column_comment`, `column_type`, `java_type`, `java_field`, `is_pk`, `is_increment`, `is_required`, `is_insert`, `is_edit`, `is_list`, `is_query`, `query_type`, `html_type`, `dict_type`, `sort`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (25, 2, 'update_by', '更新者', 'varchar(64)', 'String', 'updateBy', '0', '0', '0', '1', '1', NULL, NULL, 'EQ', 'input', '', 10, 'admin', '2025-12-14 17:20:16', '', '2025-12-14 17:21:55');
INSERT INTO `gen_table_column` (`column_id`, `table_id`, `column_name`, `column_comment`, `column_type`, `java_type`, `java_field`, `is_pk`, `is_increment`, `is_required`, `is_insert`, `is_edit`, `is_list`, `is_query`, `query_type`, `html_type`, `dict_type`, `sort`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (26, 2, 'update_time', '更新时间', 'datetime', 'Date', 'updateTime', '0', '0', '0', '1', '1', NULL, NULL, 'EQ', 'datetime', '', 11, 'admin', '2025-12-14 17:20:16', '', '2025-12-14 17:21:55');
INSERT INTO `gen_table_column` (`column_id`, `table_id`, `column_name`, `column_comment`, `column_type`, `java_type`, `java_field`, `is_pk`, `is_increment`, `is_required`, `is_insert`, `is_edit`, `is_list`, `is_query`, `query_type`, `html_type`, `dict_type`, `sort`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (27, 3, 'id', 'id', 'bigint', 'Long', 'id', '1', '1', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2025-12-14 17:20:16', '', '2025-12-14 17:22:25');
INSERT INTO `gen_table_column` (`column_id`, `table_id`, `column_name`, `column_comment`, `column_type`, `java_type`, `java_field`, `is_pk`, `is_increment`, `is_required`, `is_insert`, `is_edit`, `is_list`, `is_query`, `query_type`, `html_type`, `dict_type`, `sort`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (28, 3, 'product_id', '产品id', 'bigint', 'Long', 'productId', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'input', '', 2, 'admin', '2025-12-14 17:20:16', '', '2025-12-14 17:22:25');
INSERT INTO `gen_table_column` (`column_id`, `table_id`, `column_name`, `column_comment`, `column_type`, `java_type`, `java_field`, `is_pk`, `is_increment`, `is_required`, `is_insert`, `is_edit`, `is_list`, `is_query`, `query_type`, `html_type`, `dict_type`, `sort`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (29, 3, 'options', '选择值', 'varchar(255)', 'String', 'options', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 3, 'admin', '2025-12-14 17:20:16', '', '2025-12-14 17:22:25');
INSERT INTO `gen_table_column` (`column_id`, `table_id`, `column_name`, `column_comment`, `column_type`, `java_type`, `java_field`, `is_pk`, `is_increment`, `is_required`, `is_insert`, `is_edit`, `is_list`, `is_query`, `query_type`, `html_type`, `dict_type`, `sort`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (30, 3, 'param_name', '参数名称', 'varchar(50)', 'String', 'paramName', '0', '0', '0', '1', '1', '1', '1', 'LIKE', 'input', '', 4, 'admin', '2025-12-14 17:20:16', '', '2025-12-14 17:22:25');
INSERT INTO `gen_table_column` (`column_id`, `table_id`, `column_name`, `column_comment`, `column_type`, `java_type`, `java_field`, `is_pk`, `is_increment`, `is_required`, `is_insert`, `is_edit`, `is_list`, `is_query`, `query_type`, `html_type`, `dict_type`, `sort`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (31, 3, 'param_type', '参数类型（ 0输入框 ，1下拉框 ）', 'int', 'Long', 'paramType', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'select', '', 5, 'admin', '2025-12-14 17:20:16', '', '2025-12-14 17:22:25');
INSERT INTO `gen_table_column` (`column_id`, `table_id`, `column_name`, `column_comment`, `column_type`, `java_type`, `java_field`, `is_pk`, `is_increment`, `is_required`, `is_insert`, `is_edit`, `is_list`, `is_query`, `query_type`, `html_type`, `dict_type`, `sort`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (32, 3, 'required', '是否必填（Y是 N否）', 'char(2)', 'String', 'required', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 6, 'admin', '2025-12-14 17:20:16', '', '2025-12-14 17:22:25');
INSERT INTO `gen_table_column` (`column_id`, `table_id`, `column_name`, `column_comment`, `column_type`, `java_type`, `java_field`, `is_pk`, `is_increment`, `is_required`, `is_insert`, `is_edit`, `is_list`, `is_query`, `query_type`, `html_type`, `dict_type`, `sort`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (33, 3, 'sort', '排序', 'int', 'Long', 'sort', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 7, 'admin', '2025-12-14 17:20:16', '', '2025-12-14 17:22:25');
INSERT INTO `gen_table_column` (`column_id`, `table_id`, `column_name`, `column_comment`, `column_type`, `java_type`, `java_field`, `is_pk`, `is_increment`, `is_required`, `is_insert`, `is_edit`, `is_list`, `is_query`, `query_type`, `html_type`, `dict_type`, `sort`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (34, 3, 'del_flag', '删除标志（0代表存在 2代表删除）', 'char(1)', 'String', 'delFlag', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 8, 'admin', '2025-12-14 17:20:16', '', '2025-12-14 17:22:25');
INSERT INTO `gen_table_column` (`column_id`, `table_id`, `column_name`, `column_comment`, `column_type`, `java_type`, `java_field`, `is_pk`, `is_increment`, `is_required`, `is_insert`, `is_edit`, `is_list`, `is_query`, `query_type`, `html_type`, `dict_type`, `sort`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (35, 3, 'create_by', '创建者', 'varchar(64)', 'String', 'createBy', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 9, 'admin', '2025-12-14 17:20:16', '', '2025-12-14 17:22:25');
INSERT INTO `gen_table_column` (`column_id`, `table_id`, `column_name`, `column_comment`, `column_type`, `java_type`, `java_field`, `is_pk`, `is_increment`, `is_required`, `is_insert`, `is_edit`, `is_list`, `is_query`, `query_type`, `html_type`, `dict_type`, `sort`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (36, 3, 'create_time', '创建时间', 'datetime', 'Date', 'createTime', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'datetime', '', 10, 'admin', '2025-12-14 17:20:16', '', '2025-12-14 17:22:25');
INSERT INTO `gen_table_column` (`column_id`, `table_id`, `column_name`, `column_comment`, `column_type`, `java_type`, `java_field`, `is_pk`, `is_increment`, `is_required`, `is_insert`, `is_edit`, `is_list`, `is_query`, `query_type`, `html_type`, `dict_type`, `sort`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (37, 3, 'update_by', '更新者', 'varchar(64)', 'String', 'updateBy', '0', '0', '0', '1', '1', NULL, NULL, 'EQ', 'input', '', 11, 'admin', '2025-12-14 17:20:16', '', '2025-12-14 17:22:25');
INSERT INTO `gen_table_column` (`column_id`, `table_id`, `column_name`, `column_comment`, `column_type`, `java_type`, `java_field`, `is_pk`, `is_increment`, `is_required`, `is_insert`, `is_edit`, `is_list`, `is_query`, `query_type`, `html_type`, `dict_type`, `sort`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (38, 3, 'update_time', '更新时间', 'datetime', 'Date', 'updateTime', '0', '0', '0', '1', '1', NULL, NULL, 'EQ', 'datetime', '', 12, 'admin', '2025-12-14 17:20:16', '', '2025-12-14 17:22:25');
INSERT INTO `gen_table_column` (`column_id`, `table_id`, `column_name`, `column_comment`, `column_type`, `java_type`, `java_field`, `is_pk`, `is_increment`, `is_required`, `is_insert`, `is_edit`, `is_list`, `is_query`, `query_type`, `html_type`, `dict_type`, `sort`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (39, 4, 'id', 'id', 'bigint', 'Long', 'id', '1', '1', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2025-12-19 15:38:38', '', '2025-12-19 15:39:18');
INSERT INTO `gen_table_column` (`column_id`, `table_id`, `column_name`, `column_comment`, `column_type`, `java_type`, `java_field`, `is_pk`, `is_increment`, `is_required`, `is_insert`, `is_edit`, `is_list`, `is_query`, `query_type`, `html_type`, `dict_type`, `sort`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (40, 4, 'product_id', '产品id', 'bigint', 'Long', 'productId', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'input', '', 2, 'admin', '2025-12-19 15:38:38', '', '2025-12-19 15:39:18');
INSERT INTO `gen_table_column` (`column_id`, `table_id`, `column_name`, `column_comment`, `column_type`, `java_type`, `java_field`, `is_pk`, `is_increment`, `is_required`, `is_insert`, `is_edit`, `is_list`, `is_query`, `query_type`, `html_type`, `dict_type`, `sort`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (41, 4, 'year', '年份', 'varchar(20)', 'String', 'year', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 3, 'admin', '2025-12-19 15:38:38', '', '2025-12-19 15:39:18');
INSERT INTO `gen_table_column` (`column_id`, `table_id`, `column_name`, `column_comment`, `column_type`, `java_type`, `java_field`, `is_pk`, `is_increment`, `is_required`, `is_insert`, `is_edit`, `is_list`, `is_query`, `query_type`, `html_type`, `dict_type`, `sort`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (42, 4, 'contract_name', '合约名称', 'varchar(255)', 'String', 'contractName', '0', '0', '0', '1', '1', '1', '1', 'LIKE', 'input', '', 4, 'admin', '2025-12-19 15:38:38', '', '2025-12-19 15:39:18');
INSERT INTO `gen_table_column` (`column_id`, `table_id`, `column_name`, `column_comment`, `column_type`, `java_type`, `java_field`, `is_pk`, `is_increment`, `is_required`, `is_insert`, `is_edit`, `is_list`, `is_query`, `query_type`, `html_type`, `dict_type`, `sort`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (43, 4, 'base_price', '基价', 'decimal(10,2)', 'BigDecimal', 'basePrice', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 5, 'admin', '2025-12-19 15:38:38', '', '2025-12-19 15:39:18');
INSERT INTO `gen_table_column` (`column_id`, `table_id`, `column_name`, `column_comment`, `column_type`, `java_type`, `java_field`, `is_pk`, `is_increment`, `is_required`, `is_insert`, `is_edit`, `is_list`, `is_query`, `query_type`, `html_type`, `dict_type`, `sort`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (44, 4, 'del_flag', '删除标志（0代表存在 2代表删除）', 'char(1)', 'String', 'delFlag', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 6, 'admin', '2025-12-19 15:38:38', '', '2025-12-19 15:39:18');
INSERT INTO `gen_table_column` (`column_id`, `table_id`, `column_name`, `column_comment`, `column_type`, `java_type`, `java_field`, `is_pk`, `is_increment`, `is_required`, `is_insert`, `is_edit`, `is_list`, `is_query`, `query_type`, `html_type`, `dict_type`, `sort`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (45, 4, 'create_by', '创建者', 'varchar(64)', 'String', 'createBy', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 7, 'admin', '2025-12-19 15:38:38', '', '2025-12-19 15:39:18');
INSERT INTO `gen_table_column` (`column_id`, `table_id`, `column_name`, `column_comment`, `column_type`, `java_type`, `java_field`, `is_pk`, `is_increment`, `is_required`, `is_insert`, `is_edit`, `is_list`, `is_query`, `query_type`, `html_type`, `dict_type`, `sort`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (46, 4, 'create_time', '创建时间', 'datetime', 'Date', 'createTime', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'datetime', '', 8, 'admin', '2025-12-19 15:38:38', '', '2025-12-19 15:39:18');
INSERT INTO `gen_table_column` (`column_id`, `table_id`, `column_name`, `column_comment`, `column_type`, `java_type`, `java_field`, `is_pk`, `is_increment`, `is_required`, `is_insert`, `is_edit`, `is_list`, `is_query`, `query_type`, `html_type`, `dict_type`, `sort`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (47, 4, 'update_by', '更新者', 'varchar(64)', 'String', 'updateBy', '0', '0', '0', '1', '1', NULL, NULL, 'EQ', 'input', '', 9, 'admin', '2025-12-19 15:38:38', '', '2025-12-19 15:39:18');
INSERT INTO `gen_table_column` (`column_id`, `table_id`, `column_name`, `column_comment`, `column_type`, `java_type`, `java_field`, `is_pk`, `is_increment`, `is_required`, `is_insert`, `is_edit`, `is_list`, `is_query`, `query_type`, `html_type`, `dict_type`, `sort`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (48, 4, 'update_time', '更新时间', 'datetime', 'Date', 'updateTime', '0', '0', '0', '1', '1', NULL, NULL, 'EQ', 'datetime', '', 10, 'admin', '2025-12-19 15:38:38', '', '2025-12-19 15:39:18');
COMMIT;

-- ----------------------------
-- Table structure for nht_address
-- ----------------------------
DROP TABLE IF EXISTS `nht_address`;
CREATE TABLE `nht_address` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '地址别名',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '详细地址',
  `contacts` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '联系人',
  `contacts_post` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '职务',
  `is_default` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT 'Y' COMMENT '是否默认 Y是  N否',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '状态（0正常 1停用）',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=103 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='地址表';

-- ----------------------------
-- Records of nht_address
-- ----------------------------
BEGIN;
INSERT INTO `nht_address` (`id`, `user_id`, `name`, `address`, `contacts`, `contacts_post`, `is_default`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (100, 100, '默认地址', '测试地址', 'pan', '测试', 'Y', '0', '0', '', '2025-12-13 17:55:02', '', NULL, NULL);
INSERT INTO `nht_address` (`id`, `user_id`, `name`, `address`, `contacts`, `contacts_post`, `is_default`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (101, 101, '默认地址', '测试地址', 'pan', '测试', 'Y', '0', '0', '', '2025-12-13 18:15:24', '', NULL, NULL);
INSERT INTO `nht_address` (`id`, `user_id`, `name`, `address`, `contacts`, `contacts_post`, `is_default`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (102, 102, '默认地址', '测试地址', 'pan', '测试', 'Y', '0', '0', '', '2025-12-13 18:20:47', '', NULL, NULL);
COMMIT;

-- ----------------------------
-- Table structure for nht_product
-- ----------------------------
DROP TABLE IF EXISTS `nht_product`;
CREATE TABLE `nht_product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` bigint DEFAULT '0' COMMENT '用户id',
  `parent_id` bigint DEFAULT '0' COMMENT '父id',
  `ancestors` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '祖级列表',
  `product_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '产品名称',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=57 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='产品表';

-- ----------------------------
-- Records of nht_product
-- ----------------------------
BEGIN;
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (1, 1, 0, '', '谷物及其加工产品', '0', '0', '', '2025-12-19 16:34:52', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (2, 1, 1, '', '玉米', '0', '0', '', '2025-12-19 16:34:53', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (3, 1, 1, '', '小麦', '0', '0', '', '2025-12-19 16:34:55', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (4, 1, 1, '', '皮大麦', '0', '0', '', '2025-12-19 16:34:57', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (5, 1, 1, '', '裸大麦', '0', '0', '', '2025-12-19 16:34:59', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (6, 1, 1, '', '碎米', '0', '0', '', '2025-12-19 16:35:02', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (7, 1, 1, '', '糙米（掺混）', '0', '0', '', '2025-12-19 16:35:04', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (8, 1, 1, '', '小麦粉（面粉）', '0', '0', '', '2025-12-19 16:35:06', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (9, 1, 1, '', '麦麸', '0', '0', '', '2025-12-19 16:35:08', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (10, 1, 1, '', '小麦次粉', '0', '0', '', '2025-12-19 16:35:11', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (11, 1, 1, '', '全脂米糠/小米糠', '0', '0', '', '2025-12-19 16:35:13', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (12, 1, 1, '', '米糠粕（脱脂）', '0', '0', '', '2025-12-19 16:35:16', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (13, 1, 1, '', '大米抛光次粉', '0', '0', '', '2025-12-19 16:35:19', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (14, 1, 1, '', '木薯颗粒（粉）', '0', '0', '', '2025-12-19 16:35:22', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (15, 1, 1, '', '玉米蛋白粉', '0', '0', '', '2025-12-19 16:35:25', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (16, 1, 1, '', 'DDGS', '0', '0', '', '2025-12-19 16:35:28', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (17, 1, 1, '', '玉米胚芽粕', '0', '0', '', '2025-12-19 16:35:32', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (18, 1, 0, '', '油料籽实及其加工产品', '0', '0', '', '2025-12-19 16:38:47', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (19, 1, 18, '', '豆粕', '0', '0', '', '2025-12-19 16:38:49', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (20, 1, 18, '', '棉粕', '0', '0', '', '2025-12-19 16:38:53', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (21, 1, 18, '', '棉籽蛋白', '0', '0', '', '2025-12-19 16:38:58', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (22, 1, 18, '', '双底菜粕', '0', '0', '', '2025-12-19 16:39:02', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (23, 1, 18, '', '葵花籽粕', '0', '0', '', '2025-12-19 16:39:07', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (24, 1, 18, '', '花生粕', '0', '0', '', '2025-12-19 16:39:11', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (25, 1, 18, '', '芝麻粕', '0', '0', '', '2025-12-19 16:39:15', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (26, 1, 18, '', '棕榈仁粕', '0', '0', '', '2025-12-19 16:39:18', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (27, 1, 0, '', '油脂类', '0', '0', '', '2025-12-19 16:40:39', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (28, 1, 27, '', '大豆油', '0', '0', '', '2025-12-19 16:40:40', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (29, 1, 27, '', '玉米油', '0', '0', '', '2025-12-19 16:40:44', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (30, 1, 27, '', '四级稻米油', '0', '0', '', '2025-12-19 16:40:47', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (31, 1, 27, '', '米糠油', '0', '0', '', '2025-12-19 16:40:50', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (32, 1, 27, '', '大豆磷脂油', '0', '0', '', '2025-12-19 16:40:53', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (33, 1, 27, '', '猪油', '0', '0', '', '2025-12-19 16:40:57', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (34, 1, 27, '', '鸡/鸭油', '0', '0', '', '2025-12-19 16:40:59', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (35, 1, 0, '', '微生物发酵产品及副产品', '0', '0', '', '2025-12-19 16:42:23', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (36, 1, 35, '', '发酵豆粕', '0', '0', '', '2025-12-19 16:42:25', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (37, 1, 35, '', '啤酒酵母', '0', '0', '', '2025-12-19 16:42:29', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (38, 1, 35, '', '赖氨酸渣', '0', '0', '', '2025-12-19 16:42:32', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (39, 1, 35, '', '核苷酸渣', '0', '0', '', '2025-12-19 16:42:35', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (40, 1, 35, '', '谷氨酸渣', '0', '0', '', '2025-12-19 16:42:39', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (41, 1, 0, '', '动物产品及其副产品', '0', '0', '', '2025-12-19 16:42:46', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (42, 1, 41, '', '鱼粉', '0', '0', '', '2025-12-19 16:42:47', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (43, 1, 41, '', '鸡肠粉', '0', '0', '', '2025-12-19 16:42:49', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (44, 1, 41, '', '进口肉骨粉', '0', '0', '', '2025-12-19 16:42:51', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (45, 1, 41, '', '羽毛粉', '0', '0', '', '2025-12-19 16:42:54', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (46, 1, 41, '', '骨质氢钙', '0', '0', '', '2025-12-19 16:42:57', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (47, 1, 41, '', '喷雾血浆蛋白', '0', '0', '', '2025-12-19 16:42:58', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (48, 1, 41, '', '雪球蛋白粉', '0', '0', '', '2025-12-19 16:43:01', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (49, 1, 0, '', '矿物质', '0', '0', '', '2025-12-19 16:43:32', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (50, 1, 49, '', '植物磷酸氢钙', '0', '0', '', '2025-12-19 16:43:32', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (51, 1, 49, '', '石粉（细/粗）', '0', '0', '', '2025-12-19 16:43:34', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (52, 1, 49, '', '沸石粉', '0', '0', '', '2025-12-19 16:43:35', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (53, 1, 49, '', '腐植酸钠', '0', '0', '', '2025-12-19 16:43:37', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (54, 1, 0, '', '添加剂', '0', '0', '', '2025-12-19 16:44:10', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (55, 1, 54, '', '磷酸氢钙', '0', '0', '', '2025-12-19 16:44:11', '', NULL);
INSERT INTO `nht_product` (`id`, `user_id`, `parent_id`, `ancestors`, `product_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (56, 1, 54, '', '磷酸二氢钙', '0', '0', '', '2025-12-19 16:44:13', '', NULL);
COMMIT;

-- ----------------------------
-- Table structure for nht_product_base_offer
-- ----------------------------
DROP TABLE IF EXISTS `nht_product_base_offer`;
CREATE TABLE `nht_product_base_offer` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `product_id` bigint NOT NULL DEFAULT '0' COMMENT '产品id',
  `year` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '年份',
  `contract_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '合约名称',
  `base_price` decimal(10,2) DEFAULT NULL COMMENT '基价',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='产品基础报价';

-- ----------------------------
-- Records of nht_product_base_offer
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for nht_product_parameters
-- ----------------------------
DROP TABLE IF EXISTS `nht_product_parameters`;
CREATE TABLE `nht_product_parameters` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `product_id` bigint NOT NULL DEFAULT '0' COMMENT '产品id',
  `options` varchar(255) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '选择值',
  `param_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '参数名称',
  `param_type` int DEFAULT NULL COMMENT '参数类型（ 0输入框 ，1下拉框 ）',
  `required` char(2) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '是否必填（Y是 N否）',
  `sort` int DEFAULT NULL COMMENT '排序',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=215 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='产品属性表';

-- ----------------------------
-- Records of nht_product_parameters
-- ----------------------------
BEGIN;
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (1, 2, '≥720,≥700,≥690', '容重(g/L)', 1, 'Y', 1, '0', '', '2025-12-19 16:34:54', '', '2025-12-19 20:25:54');
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (2, 2, '≤14.0,≤14.5', '水分', 1, 'Y', 2, '0', '', '2025-12-19 16:34:54', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (3, 2, '≤5.0,≤6.5', '不完善粒', 1, 'Y', 3, '0', '', '2025-12-19 16:34:54', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (4, 2, '≤1.0,≤2.0', '生霉粒', 1, 'Y', 4, '0', '', '2025-12-19 16:34:54', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (5, 2, '≥7.5,≥7.0', '粗蛋白', 1, 'Y', 5, '0', '', '2025-12-19 16:34:54', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (6, 2, '≤0.5', '杂质', 1, 'Y', 6, '0', '', '2025-12-19 16:34:54', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (7, 3, '≥760', '容重', 1, 'Y', 1, '0', '', '2025-12-19 16:34:56', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (8, 3, '≤14.0', '水分', 1, 'Y', 2, '0', '', '2025-12-19 16:34:56', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (9, 3, '≥12.0', '粗蛋白', 1, 'Y', 3, '0', '', '2025-12-19 16:34:56', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (10, 3, '≤8.0', '不完善粒', 1, 'Y', 4, '0', '', '2025-12-19 16:34:56', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (11, 3, '≤1.0', '生霉粒', 1, 'Y', 5, '0', '', '2025-12-19 16:34:56', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (12, 3, '≤1.0', '杂质', 1, 'Y', 6, '0', '', '2025-12-19 16:34:56', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (13, 4, '<14,<14.5,<15', '水分', 1, 'Y', 1, '0', '', '2025-12-19 16:34:58', '', '2025-12-19 20:22:37');
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (14, 4, '≥10.0,≥9.0', '粗蛋白', 1, 'Y', 2, '0', '', '2025-12-19 16:34:58', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (15, 4, '≥35,≥30', '千粒重', 1, 'Y', 3, '0', '', '2025-12-19 16:34:58', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (16, 4, '≤1.0,≤2.0', '霉变粒', 1, 'Y', 4, '0', '', '2025-12-19 16:34:58', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (17, 4, '≤5.0,≤5.5', '粗纤维', 1, 'Y', 5, '0', '', '2025-12-19 16:34:58', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (18, 4, '≤3.0', '粗灰分', 1, 'Y', 6, '0', '', '2025-12-19 16:34:58', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (19, 4, '≤80', '脂肪酸值', 1, 'Y', 7, '0', '', '2025-12-19 16:34:58', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (20, 5, '(5.1-9.1)≤14.0,≤14.5,其他≤14.5,≤15.0', '水分', 1, 'Y', 1, '0', '', '2025-12-19 16:35:00', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (21, 5, '≥13.0,≥11.0', '粗蛋白', 1, 'Y', 2, '0', '', '2025-12-19 16:35:00', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (22, 5, '≥750,≥730', '容重', 1, 'Y', 3, '0', '', '2025-12-19 16:35:00', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (23, 5, '≤1.0,≤2.0', '霉变粒', 1, 'Y', 4, '0', '', '2025-12-19 16:35:00', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (24, 5, '≤2.0,≤2.5', '粗纤维', 1, 'Y', 5, '0', '', '2025-12-19 16:35:00', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (25, 5, '≤2.0,≤2.5', '粗灰分', 1, 'Y', 6, '0', '', '2025-12-19 16:35:00', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (26, 5, '≤80', '脂肪酸值', 1, 'Y', 7, '0', '', '2025-12-19 16:35:00', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (27, 6, '夏≤14.0,冬≤15.0', '水分', 1, 'Y', 1, '0', '', '2025-12-19 16:35:03', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (28, 6, '≥6.0', '粗蛋白', 1, 'Y', 2, '0', '', '2025-12-19 16:35:03', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (29, 6, '≤2.0', '粗纤维', 1, 'Y', 3, '0', '', '2025-12-19 16:35:03', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (30, 6, '≤2.0', '粗灰分', 1, 'Y', 4, '0', '', '2025-12-19 16:35:03', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (31, 7, '≤15.0', '水分', 1, 'Y', 1, '0', '', '2025-12-19 16:35:05', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (32, 7, '≥7.5', '粗蛋白', 1, 'Y', 2, '0', '', '2025-12-19 16:35:05', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (33, 7, '≤1.0', '杂质', 1, 'Y', 3, '0', '', '2025-12-19 16:35:05', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (34, 8, '≤13.0,≤14.0', '水分', 1, 'Y', 1, '0', '', '2025-12-19 16:35:07', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (35, 8, '≥35.0,≥30.0', '面筋', 1, 'Y', 2, '0', '', '2025-12-19 16:35:07', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (36, 8, '≥14.0,≥12.5', '粗蛋白', 1, 'Y', 3, '0', '', '2025-12-19 16:35:07', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (37, 8, '≤3.0', '粗纤维', 1, 'Y', 4, '0', '', '2025-12-19 16:35:07', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (38, 9, '(2.1-10.31)≤14.0,其他≤14.5', '水分', 1, 'Y', 1, '0', '', '2025-12-19 16:35:09', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (39, 9, '≥15.0', '粗蛋白', 1, 'Y', 2, '0', '', '2025-12-19 16:35:09', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (40, 9, '≤6.0', '粗灰分', 1, 'Y', 3, '0', '', '2025-12-19 16:35:09', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (41, 9, '<12.0', '粗纤维', 1, 'Y', 4, '0', '', '2025-12-19 16:35:09', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (42, 10, '≤13.0', '水分', 1, 'Y', 1, '0', '', '2025-12-19 16:35:12', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (43, 10, '≥14.0', '粗蛋白', 1, 'Y', 2, '0', '', '2025-12-19 16:35:12', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (44, 10, '≤3.5', '粗纤维', 1, 'Y', 3, '0', '', '2025-12-19 16:35:12', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (45, 11, '(10.1-5.1)≤14.0,其他≤13.0', '水分', 1, 'Y', 1, '0', '', '2025-12-19 16:35:14', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (46, 11, '≥12.0,≥11.0', '粗蛋白', 1, 'Y', 2, '0', '', '2025-12-19 16:35:14', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (47, 11, '≥15.0,≥14.0', '粗脂肪', 1, 'Y', 3, '0', '', '2025-12-19 16:35:14', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (48, 11, '≤9.0', '粗灰分', 1, 'Y', 4, '0', '', '2025-12-19 16:35:14', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (49, 11, '≤9.0,≤10.0', '粗纤维', 1, 'Y', 5, '0', '', '2025-12-19 16:35:14', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (50, 12, '≤12.0,≤13.0', '水分', 1, 'Y', 1, '0', '', '2025-12-19 16:35:17', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (51, 12, '≥15.0,≥14.0', '粗蛋白', 1, 'Y', 2, '0', '', '2025-12-19 16:35:17', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (52, 12, '≤10.0,≤12.0', '粗灰分', 1, 'Y', 3, '0', '', '2025-12-19 16:35:17', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (53, 12, '≤8.0,≤10.0', '粗纤维', 1, 'Y', 4, '0', '', '2025-12-19 16:35:17', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (54, 13, '5-9月≤13.0,其他≤16.5', '水分', 1, 'Y', 1, '0', '', '2025-12-19 16:35:20', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (55, 13, '≥8.0', '粗蛋白', 1, 'Y', 2, '0', '', '2025-12-19 16:35:20', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (56, 13, '≤7.0', '粗灰分', 1, 'Y', 3, '0', '', '2025-12-19 16:35:20', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (57, 13, '≤10.0', '粗脂肪', 1, 'Y', 4, '0', '', '2025-12-19 16:35:20', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (58, 14, '≤13.0', '水分', 1, 'Y', 1, '0', '', '2025-12-19 16:35:23', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (59, 14, '≤8.0', '粗灰分', 1, 'Y', 2, '0', '', '2025-12-19 16:35:23', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (60, 14, '≥45.0', '淀粉', 1, 'Y', 3, '0', '', '2025-12-19 16:35:23', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (61, 14, '≤3.0', '含沙量', 1, 'Y', 4, '0', '', '2025-12-19 16:35:23', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (62, 15, '≥60.0,≥58.0,≥55.0', '粗蛋白', 1, 'Y', 1, '0', '', '2025-12-19 16:35:26', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (63, 15, '≤5.0,≤8.0', '水分', 1, 'Y', 2, '0', '', '2025-12-19 16:35:26', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (64, 15, '≤2.0', '粗灰分', 1, 'Y', 3, '0', '', '2025-12-19 16:35:26', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (65, 15, '≤10.0', '粗脂肪', 1, 'Y', 4, '0', '', '2025-12-19 16:35:26', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (66, 16, '≥24.0(高脂),≥27.0(低脂)', '粗蛋白', 1, 'Y', 1, '0', '', '2025-12-19 16:35:30', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (67, 16, '≤9.0', '水分', 1, 'Y', 2, '0', '', '2025-12-19 16:35:30', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (68, 16, '<58.0', '粗灰分', 1, 'Y', 3, '0', '', '2025-12-19 16:35:30', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (69, 16, '≥8.0(高脂),≥4.0(低脂)', '粗脂肪', 1, 'Y', 4, '0', '', '2025-12-19 16:35:30', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (70, 16, '≤12.0', '粗纤维', 1, 'Y', 5, '0', '', '2025-12-19 16:35:30', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (71, 17, '≥16.0,≥22.0', '粗蛋白', 1, 'Y', 1, '0', '', '2025-12-19 16:35:33', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (72, 17, '≤12.0', '水分', 1, 'Y', 2, '0', '', '2025-12-19 16:35:33', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (73, 17, '≥2.5', '粗灰分', 1, 'Y', 3, '0', '', '2025-12-19 16:35:34', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (74, 17, '≤6.0,≤8.0', '粗脂肪', 1, 'Y', 4, '0', '', '2025-12-19 16:35:34', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (75, 17, '≤8.0', '粗纤维', 1, 'Y', 5, '0', '', '2025-12-19 16:35:34', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (76, 19, '≤13.0', '水分', 1, 'Y', 1, '0', '', '2025-12-19 16:38:50', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (77, 19, '≥43.0(普),≥45.0(一),≥46.0(二)', '粗蛋白', 1, 'Y', 2, '0', '', '2025-12-19 16:38:50', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (78, 19, '≤7.0', '粗灰分', 1, 'Y', 3, '0', '', '2025-12-19 16:38:50', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (79, 19, '≥73.0(脱皮)', 'KOH溶解度', 1, 'Y', 4, '0', '', '2025-12-19 16:38:50', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (80, 19, '≤7.0', '粗纤维', 1, 'Y', 5, '0', '', '2025-12-19 16:38:50', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (81, 19, '≤0.30', '尿素酶', 1, 'Y', 6, '0', '', '2025-12-19 16:38:50', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (82, 20, '≤13.0', '水分', 1, 'Y', 1, '0', '', '2025-12-19 16:38:55', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (83, 20, '≥50.0,≥46.0,≥42.0', '粗蛋白', 1, 'Y', 2, '0', '', '2025-12-19 16:38:55', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (84, 20, '≤6.5', '粗灰分', 1, 'Y', 3, '0', '', '2025-12-19 16:38:55', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (85, 20, '60-70', 'KOH溶解度', 1, 'Y', 4, '0', '', '2025-12-19 16:38:55', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (86, 20, '≤10.0,≤11.0,≤14.0', '粗纤维', 1, 'Y', 5, '0', '', '2025-12-19 16:38:55', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (87, 20, '≤300,≤750,≤1200', '游离棉酚', 1, 'Y', 6, '0', '', '2025-12-19 16:38:55', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (88, 21, '≤10.0', '水分', 1, 'Y', 1, '0', '', '2025-12-19 16:38:59', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (89, 21, '≥55.0,≥50.0', '粗蛋白', 1, 'Y', 2, '0', '', '2025-12-19 16:38:59', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (90, 21, '≤6.5', '粗灰分', 1, 'Y', 3, '0', '', '2025-12-19 16:38:59', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (91, 21, '≤300.0', '游离棉酚', 1, 'Y', 4, '0', '', '2025-12-19 16:38:59', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (92, 21, '≤7.5', '粗纤维', 1, 'Y', 5, '0', '', '2025-12-19 16:38:59', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (93, 22, '≤13.0', '水分', 1, 'Y', 1, '0', '', '2025-12-19 16:39:03', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (94, 22, '≥36.0', '粗蛋白', 1, 'Y', 2, '0', '', '2025-12-19 16:39:03', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (95, 22, '≤8.0', '粗灰分', 1, 'Y', 3, '0', '', '2025-12-19 16:39:03', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (96, 22, '≤1.5', '粗脂肪', 1, 'Y', 4, '0', '', '2025-12-19 16:39:03', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (97, 22, '≤14.0', '粗纤维', 1, 'Y', 5, '0', '', '2025-12-19 16:39:04', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (98, 22, '≥36.0', 'KOH溶解度', 1, 'Y', 6, '0', '', '2025-12-19 16:39:04', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (99, 23, '≤12.0', '水分', 1, 'Y', 1, '0', '', '2025-12-19 16:39:08', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (100, 23, '≥37.0', '粗蛋白', 1, 'Y', 2, '0', '', '2025-12-19 16:39:08', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (101, 23, '≤8.0', '粗灰分', 1, 'Y', 3, '0', '', '2025-12-19 16:39:08', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (102, 23, '<28.0', '粗纤维', 1, 'Y', 4, '0', '', '2025-12-19 16:39:08', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (103, 24, '≤12.0', '水分', 1, 'Y', 1, '0', '', '2025-12-19 16:39:12', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (104, 24, '≥50.0,≥46.0', '粗蛋白', 1, 'Y', 2, '0', '', '2025-12-19 16:39:12', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (105, 24, '<7.0', '粗灰分', 1, 'Y', 3, '0', '', '2025-12-19 16:39:12', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (106, 24, '<9.0', '粗纤维', 1, 'Y', 4, '0', '', '2025-12-19 16:39:12', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (107, 25, '≤12.0', '水分', 1, 'Y', 1, '0', '', '2025-12-19 16:39:16', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (108, 25, '≥42.0', '粗蛋白', 1, 'Y', 2, '0', '', '2025-12-19 16:39:16', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (109, 25, '≤15.0', '粗灰分', 1, 'Y', 3, '0', '', '2025-12-19 16:39:16', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (110, 25, '≥48.0', 'CP+EE', 1, 'Y', 4, '0', '', '2025-12-19 16:39:16', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (111, 26, '≤12.0', '水分', 1, 'Y', 1, '0', '', '2025-12-19 16:39:20', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (112, 26, '≥14.0', '粗蛋白', 1, 'Y', 2, '0', '', '2025-12-19 16:39:20', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (113, 26, '<6.0', '粗灰分', 1, 'Y', 3, '0', '', '2025-12-19 16:39:20', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (114, 26, '<13.0', '粗纤维', 1, 'Y', 4, '0', '', '2025-12-19 16:39:20', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (115, 28, '≤0.2', '水分', 1, 'Y', 1, '0', '', '2025-12-19 16:40:42', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (116, 28, '≤4.0', '酸价', 1, 'Y', 2, '0', '', '2025-12-19 16:40:42', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (117, 28, '≤1.5', '不皂化', 1, 'Y', 3, '0', '', '2025-12-19 16:40:42', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (118, 29, '≤0.2(夏),0.5(冬)', '水分', 1, 'Y', 1, '0', '', '2025-12-19 16:40:45', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (119, 29, '≤8.0', '酸价', 1, 'Y', 2, '0', '', '2025-12-19 16:40:45', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (120, 29, '≤2.8', '不皂化', 1, 'Y', 3, '0', '', '2025-12-19 16:40:45', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (121, 30, '0.2(夏),0.5(冬)', '水分', 1, 'Y', 1, '0', '', '2025-12-19 16:40:49', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (122, 30, '≤3.0', '酸价', 1, 'Y', 2, '0', '', '2025-12-19 16:40:49', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (123, 30, '≤2.5', '不皂化', 1, 'Y', 3, '0', '', '2025-12-19 16:40:49', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (124, 31, '≤1.0', '水分', 1, 'Y', 1, '0', '', '2025-12-19 16:40:52', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (125, 31, '≤50.0', '酸价', 1, 'Y', 2, '0', '', '2025-12-19 16:40:52', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (126, 31, '≤5.0', '不皂化', 1, 'Y', 3, '0', '', '2025-12-19 16:40:52', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (127, 32, '≤1.0', '水分', 1, 'Y', 1, '0', '', '2025-12-19 16:40:55', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (128, 32, '≤45.0', '酸价', 1, 'Y', 2, '0', '', '2025-12-19 16:40:55', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (129, 32, '≤2.5', '不皂化', 1, 'Y', 3, '0', '', '2025-12-19 16:40:55', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (130, 33, '≤0.25', '水分', 1, 'Y', 1, '0', '', '2025-12-19 16:40:58', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (131, 33, '≤4.0(夏),3.0(冬)', '酸价', 1, 'Y', 2, '0', '', '2025-12-19 16:40:58', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (132, 33, '≤2.5', '不皂化', 1, 'Y', 3, '0', '', '2025-12-19 16:40:58', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (133, 34, '≤0.25', '水分', 1, 'Y', 1, '0', '', '2025-12-19 16:41:01', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (134, 34, '≤3.0', '酸价', 1, 'Y', 2, '0', '', '2025-12-19 16:41:01', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (135, 34, '≤2.5', '不皂化', 1, 'Y', 3, '0', '', '2025-12-19 16:41:01', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (136, 36, '≤12.0', '水分', 1, 'Y', 1, '0', '', '2025-12-19 16:42:26', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (137, 36, '≥45.0', '粗蛋白', 1, 'Y', 2, '0', '', '2025-12-19 16:42:26', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (138, 36, '≤8.0', '粗灰分', 1, 'Y', 3, '0', '', '2025-12-19 16:42:26', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (139, 36, '≤5.0', '粗纤维', 1, 'Y', 4, '0', '', '2025-12-19 16:42:26', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (140, 36, '≤11', '非蛋白氮', 1, 'Y', 5, '0', '', '2025-12-19 16:42:26', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (141, 37, '≤10.0', '水分', 1, 'Y', 1, '0', '', '2025-12-19 16:42:30', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (142, 37, '≥40.0', '粗蛋白', 1, 'Y', 2, '0', '', '2025-12-19 16:42:30', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (143, 37, '≤14.0', '粗灰分', 1, 'Y', 3, '0', '', '2025-12-19 16:42:30', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (144, 37, '64-87', '胃蛋白酶消化率', 1, 'Y', 4, '0', '', '2025-12-19 16:42:30', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (145, 38, '≤8.0', '水分', 1, 'Y', 1, '0', '', '2025-12-19 16:42:33', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (146, 38, '≥60.0', '粗蛋白', 1, 'Y', 2, '0', '', '2025-12-19 16:42:33', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (147, 38, '≤10.0', '粗灰分', 1, 'Y', 3, '0', '', '2025-12-19 16:42:33', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (148, 38, '≥65', 'TP/CP', 1, 'Y', 4, '0', '', '2025-12-19 16:42:33', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (149, 39, '≤10.0', '水分', 1, 'Y', 1, '0', '', '2025-12-19 16:42:37', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (150, 39, '≥65.0', '粗蛋白', 1, 'Y', 2, '0', '', '2025-12-19 16:42:37', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (151, 39, '≤5.0,6.0', '粗灰分', 1, 'Y', 3, '0', '', '2025-12-19 16:42:37', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (152, 39, '≥10.0,8.0', '苏氨酸', 1, 'Y', 4, '0', '', '2025-12-19 16:42:37', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (153, 40, '≤13.0', '水分', 1, 'Y', 1, '0', '', '2025-12-19 16:42:40', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (154, 40, '≥65.0', '粗蛋白', 1, 'Y', 2, '0', '', '2025-12-19 16:42:40', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (155, 40, '≤4.0', '粗灰分', 1, 'Y', 3, '0', '', '2025-12-19 16:42:40', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (156, 40, '≥50.0', 'TP/CP', 1, 'Y', 4, '0', '', '2025-12-19 16:42:40', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (157, 42, '≤10.0', '水分', 1, 'Y', 1, '0', '', '2025-12-19 16:42:47', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (158, 42, '≥68.0(S),≥67.0(P),≥64.0(1),≥60.0(2)', '粗蛋白', 1, 'Y', 2, '0', '', '2025-12-19 16:42:47', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (159, 42, '≤16.5-20.0', '粗灰分', 1, 'Y', 3, '0', '', '2025-12-19 16:42:47', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (160, 42, '≤9.0-14.0', 'EE', 1, 'Y', 4, '0', '', '2025-12-19 16:42:47', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (161, 42, '≤4.0,≤5.0', '盐', 1, 'Y', 5, '0', '', '2025-12-19 16:42:47', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (162, 42, '2.5-6.0', '钙', 1, 'Y', 6, '0', '', '2025-12-19 16:42:47', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (163, 42, '≥1.8-2.0', '总磷', 1, 'Y', 7, '0', '', '2025-12-19 16:42:47', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (164, 43, '≥65.0', '粗蛋白', 1, 'Y', 1, '0', '', '2025-12-19 16:42:50', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (165, 43, '≤16.0', '粗脂肪', 1, 'Y', 2, '0', '', '2025-12-19 16:42:50', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (166, 43, '≤10.0', '粗灰分', 1, 'Y', 3, '0', '', '2025-12-19 16:42:50', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (167, 43, '≤7.0', '酸价', 1, 'Y', 4, '0', '', '2025-12-19 16:42:50', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (168, 43, '≤2.0', '总磷', 1, 'Y', 5, '0', '', '2025-12-19 16:42:50', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (169, 44, '≤10.0', '水分', 1, 'Y', 1, '0', '', '2025-12-19 16:42:52', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (170, 44, '≤12.0-15.0', '粗脂肪', 1, 'Y', 2, '0', '', '2025-12-19 16:42:52', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (171, 44, '≤30.0-43.0', '粗灰分', 1, 'Y', 3, '0', '', '2025-12-19 16:42:52', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (172, 44, '.0-15.0', '钙', 1, 'Y', 4, '0', '', '2025-12-19 16:42:52', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (173, 44, '≤3.5-4.0', '总磷', 1, 'Y', 5, '0', '', '2025-12-19 16:42:52', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (174, 44, '≤5.0-9.0', '酸价', 1, 'Y', 6, '0', '', '2025-12-19 16:42:52', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (175, 44, '≤50.0', 'VBN', 1, 'Y', 7, '0', '', '2025-12-19 16:42:52', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (176, 45, '≤8.0', '水分', 1, 'Y', 1, '0', '', '2025-12-19 16:42:55', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (177, 45, '≥85.0(1),≥80.0(2)', '粗蛋白', 1, 'Y', 2, '0', '', '2025-12-19 16:42:55', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (178, 45, '≤7.0', '粗脂肪', 1, 'Y', 3, '0', '', '2025-12-19 16:42:55', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (179, 45, '≤3.0,≤6.0', '粗灰分', 1, 'Y', 4, '0', '', '2025-12-19 16:42:55', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (180, 45, '≥75.0', '胃消化率', 1, 'Y', 5, '0', '', '2025-12-19 16:42:55', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (181, 45, '≤200.0', 'VBN', 1, 'Y', 6, '0', '', '2025-12-19 16:42:55', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (182, 46, '≥16.0', '总磷', 1, 'Y', 1, '0', '', '2025-12-19 16:42:57', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (183, 46, '≥21.0', '钙', 1, 'Y', 2, '0', '', '2025-12-19 16:42:57', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (184, 46, '≤75', '灰分', 1, 'Y', 3, '0', '', '2025-12-19 16:42:57', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (185, 47, '≤8.0', '水分', 1, 'Y', 1, '0', '', '2025-12-19 16:43:00', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (186, 47, '≥75.0(猪),≥65.0(鸭)', '粗蛋白', 1, 'Y', 2, '0', '', '2025-12-19 16:43:00', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (187, 47, '≤10.0(猪),≤15.0(鸭)', '粗灰分', 1, 'Y', 3, '0', '', '2025-12-19 16:43:00', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (188, 47, '≤35.0', 'VBN', 1, 'Y', 4, '0', '', '2025-12-19 16:43:00', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (189, 47, '≥15.0(猪),≥10.0(鸭)', 'IgG', 1, 'Y', 5, '0', '', '2025-12-19 16:43:00', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (190, 48, '≤10.0', '水分', 1, 'Y', 1, '0', '', '2025-12-19 16:43:02', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (191, 48, '≥90.0', '粗蛋白', 1, 'Y', 2, '0', '', '2025-12-19 16:43:02', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (192, 48, '≤5.0', '粗灰分', 1, 'Y', 3, '0', '', '2025-12-19 16:43:02', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (193, 48, '≤1.0', '粗脂肪', 1, 'Y', 4, '0', '', '2025-12-19 16:43:02', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (194, 48, '≤1.0', '酸价', 1, 'Y', 5, '0', '', '2025-12-19 16:43:02', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (195, 48, '≤50.0', 'VBN', 1, 'Y', 6, '0', '', '2025-12-19 16:43:02', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (196, 48, '≥90.0', 'TP/CP', 1, 'Y', 7, '0', '', '2025-12-19 16:43:02', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (197, 50, '≥19', '总磷', 1, 'Y', 1, '0', '', '2025-12-19 16:43:33', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (198, 50, '≥28', '钙', 1, 'Y', 2, '0', '', '2025-12-19 16:43:33', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (199, 50, '≥14', '枸溶磷', 1, 'Y', 3, '0', '', '2025-12-19 16:43:33', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (200, 51, '≥36.0', '钙', 1, 'Y', 1, '0', '', '2025-12-19 16:43:34', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (201, 51, '≤400.0', '氟', 1, 'Y', 2, '0', '', '2025-12-19 16:43:34', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (202, 51, '≤10.0', '铅', 1, 'Y', 3, '0', '', '2025-12-19 16:43:34', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (203, 51, '≤2.0', '砷', 1, 'Y', 4, '0', '', '2025-12-19 16:43:35', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (204, 52, '≥100.0,≥90.0', '吸氨值', 1, 'Y', 1, '0', '', '2025-12-19 16:43:36', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (205, 52, '≤14.0,≤16.0', '干燥失重', 1, 'Y', 2, '0', '', '2025-12-19 16:43:36', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (206, 53, '≤12.0', '水分', 1, 'Y', 1, '0', '', '2025-12-19 16:43:38', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (207, 53, '≥55.0', '腐植酸:', 1, 'Y', 2, '0', '', '2025-12-19 16:43:38', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (208, 55, '≥16.5,≥19.0,≥21.0', '总磷', 1, 'Y', 1, '0', '', '2025-12-19 16:44:12', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (209, 55, '≥14.0,≥16.0,≥18.0', '枸溶磷', 1, 'Y', 2, '0', '', '2025-12-19 16:44:12', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (210, 55, '≥8.0,≥10.0', '水溶磷', 1, 'Y', 3, '0', '', '2025-12-19 16:44:12', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (211, 55, '≥20.0,≥15.0,≥14.0', '钙', 1, 'Y', 4, '0', '', '2025-12-19 16:44:12', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (212, 56, '≥22.0', '总磷', 1, 'Y', 1, '0', '', '2025-12-19 16:44:14', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (213, 56, '≥20.0', '水溶磷', 1, 'Y', 2, '0', '', '2025-12-19 16:44:14', '', NULL);
INSERT INTO `nht_product_parameters` (`id`, `product_id`, `options`, `param_name`, `param_type`, `required`, `sort`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (214, 56, '≥13.0', '钙', 1, 'Y', 3, '0', '', '2025-12-19 16:44:14', '', NULL);
COMMIT;

-- ----------------------------
-- Table structure for QRTZ_BLOB_TRIGGERS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_BLOB_TRIGGERS`;
CREATE TABLE `QRTZ_BLOB_TRIGGERS` (
  `sched_name` varchar(120) COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `blob_data` blob COMMENT '存放持久化Trigger对象',
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`),
  CONSTRAINT `qrtz_blob_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `QRTZ_TRIGGERS` (`sched_name`, `trigger_name`, `trigger_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Blob类型的触发器表';

-- ----------------------------
-- Records of QRTZ_BLOB_TRIGGERS
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for QRTZ_CALENDARS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_CALENDARS`;
CREATE TABLE `QRTZ_CALENDARS` (
  `sched_name` varchar(120) COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `calendar_name` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT '日历名称',
  `calendar` blob NOT NULL COMMENT '存放持久化calendar对象',
  PRIMARY KEY (`sched_name`,`calendar_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='日历信息表';

-- ----------------------------
-- Records of QRTZ_CALENDARS
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for QRTZ_CRON_TRIGGERS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_CRON_TRIGGERS`;
CREATE TABLE `QRTZ_CRON_TRIGGERS` (
  `sched_name` varchar(120) COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `cron_expression` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'cron表达式',
  `time_zone_id` varchar(80) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '时区',
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`),
  CONSTRAINT `qrtz_cron_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `QRTZ_TRIGGERS` (`sched_name`, `trigger_name`, `trigger_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Cron类型的触发器表';

-- ----------------------------
-- Records of QRTZ_CRON_TRIGGERS
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for QRTZ_FIRED_TRIGGERS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_FIRED_TRIGGERS`;
CREATE TABLE `QRTZ_FIRED_TRIGGERS` (
  `sched_name` varchar(120) COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `entry_id` varchar(95) COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度器实例id',
  `trigger_name` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `instance_name` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度器实例名',
  `fired_time` bigint NOT NULL COMMENT '触发的时间',
  `sched_time` bigint NOT NULL COMMENT '定时器制定的时间',
  `priority` int NOT NULL COMMENT '优先级',
  `state` varchar(16) COLLATE utf8mb4_general_ci NOT NULL COMMENT '状态',
  `job_name` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '任务名称',
  `job_group` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '任务组名',
  `is_nonconcurrent` varchar(1) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '是否并发',
  `requests_recovery` varchar(1) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '是否接受恢复执行',
  PRIMARY KEY (`sched_name`,`entry_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='已触发的触发器表';

-- ----------------------------
-- Records of QRTZ_FIRED_TRIGGERS
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for QRTZ_JOB_DETAILS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_JOB_DETAILS`;
CREATE TABLE `QRTZ_JOB_DETAILS` (
  `sched_name` varchar(120) COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `job_name` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务名称',
  `job_group` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务组名',
  `description` varchar(250) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '相关介绍',
  `job_class_name` varchar(250) COLLATE utf8mb4_general_ci NOT NULL COMMENT '执行任务类名称',
  `is_durable` varchar(1) COLLATE utf8mb4_general_ci NOT NULL COMMENT '是否持久化',
  `is_nonconcurrent` varchar(1) COLLATE utf8mb4_general_ci NOT NULL COMMENT '是否并发',
  `is_update_data` varchar(1) COLLATE utf8mb4_general_ci NOT NULL COMMENT '是否更新数据',
  `requests_recovery` varchar(1) COLLATE utf8mb4_general_ci NOT NULL COMMENT '是否接受恢复执行',
  `job_data` blob COMMENT '存放持久化job对象',
  PRIMARY KEY (`sched_name`,`job_name`,`job_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='任务详细信息表';

-- ----------------------------
-- Records of QRTZ_JOB_DETAILS
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for QRTZ_LOCKS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_LOCKS`;
CREATE TABLE `QRTZ_LOCKS` (
  `sched_name` varchar(120) COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `lock_name` varchar(40) COLLATE utf8mb4_general_ci NOT NULL COMMENT '悲观锁名称',
  PRIMARY KEY (`sched_name`,`lock_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='存储的悲观锁信息表';

-- ----------------------------
-- Records of QRTZ_LOCKS
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for QRTZ_PAUSED_TRIGGER_GRPS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_PAUSED_TRIGGER_GRPS`;
CREATE TABLE `QRTZ_PAUSED_TRIGGER_GRPS` (
  `sched_name` varchar(120) COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `trigger_group` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  PRIMARY KEY (`sched_name`,`trigger_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='暂停的触发器表';

-- ----------------------------
-- Records of QRTZ_PAUSED_TRIGGER_GRPS
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for QRTZ_SCHEDULER_STATE
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_SCHEDULER_STATE`;
CREATE TABLE `QRTZ_SCHEDULER_STATE` (
  `sched_name` varchar(120) COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `instance_name` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT '实例名称',
  `last_checkin_time` bigint NOT NULL COMMENT '上次检查时间',
  `checkin_interval` bigint NOT NULL COMMENT '检查间隔时间',
  PRIMARY KEY (`sched_name`,`instance_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='调度器状态表';

-- ----------------------------
-- Records of QRTZ_SCHEDULER_STATE
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for QRTZ_SIMPLE_TRIGGERS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_SIMPLE_TRIGGERS`;
CREATE TABLE `QRTZ_SIMPLE_TRIGGERS` (
  `sched_name` varchar(120) COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `repeat_count` bigint NOT NULL COMMENT '重复的次数统计',
  `repeat_interval` bigint NOT NULL COMMENT '重复的间隔时间',
  `times_triggered` bigint NOT NULL COMMENT '已经触发的次数',
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`),
  CONSTRAINT `qrtz_simple_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `QRTZ_TRIGGERS` (`sched_name`, `trigger_name`, `trigger_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='简单触发器的信息表';

-- ----------------------------
-- Records of QRTZ_SIMPLE_TRIGGERS
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for QRTZ_SIMPROP_TRIGGERS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_SIMPROP_TRIGGERS`;
CREATE TABLE `QRTZ_SIMPROP_TRIGGERS` (
  `sched_name` varchar(120) COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `str_prop_1` varchar(512) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'String类型的trigger的第一个参数',
  `str_prop_2` varchar(512) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'String类型的trigger的第二个参数',
  `str_prop_3` varchar(512) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'String类型的trigger的第三个参数',
  `int_prop_1` int DEFAULT NULL COMMENT 'int类型的trigger的第一个参数',
  `int_prop_2` int DEFAULT NULL COMMENT 'int类型的trigger的第二个参数',
  `long_prop_1` bigint DEFAULT NULL COMMENT 'long类型的trigger的第一个参数',
  `long_prop_2` bigint DEFAULT NULL COMMENT 'long类型的trigger的第二个参数',
  `dec_prop_1` decimal(13,4) DEFAULT NULL COMMENT 'decimal类型的trigger的第一个参数',
  `dec_prop_2` decimal(13,4) DEFAULT NULL COMMENT 'decimal类型的trigger的第二个参数',
  `bool_prop_1` varchar(1) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Boolean类型的trigger的第一个参数',
  `bool_prop_2` varchar(1) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Boolean类型的trigger的第二个参数',
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`),
  CONSTRAINT `qrtz_simprop_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `QRTZ_TRIGGERS` (`sched_name`, `trigger_name`, `trigger_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='同步机制的行锁表';

-- ----------------------------
-- Records of QRTZ_SIMPROP_TRIGGERS
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for QRTZ_TRIGGERS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_TRIGGERS`;
CREATE TABLE `QRTZ_TRIGGERS` (
  `sched_name` varchar(120) COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT '触发器的名字',
  `trigger_group` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT '触发器所属组的名字',
  `job_name` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_job_details表job_name的外键',
  `job_group` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_job_details表job_group的外键',
  `description` varchar(250) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '相关介绍',
  `next_fire_time` bigint DEFAULT NULL COMMENT '上一次触发时间（毫秒）',
  `prev_fire_time` bigint DEFAULT NULL COMMENT '下一次触发时间（默认为-1表示不触发）',
  `priority` int DEFAULT NULL COMMENT '优先级',
  `trigger_state` varchar(16) COLLATE utf8mb4_general_ci NOT NULL COMMENT '触发器状态',
  `trigger_type` varchar(8) COLLATE utf8mb4_general_ci NOT NULL COMMENT '触发器的类型',
  `start_time` bigint NOT NULL COMMENT '开始时间',
  `end_time` bigint DEFAULT NULL COMMENT '结束时间',
  `calendar_name` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '日程表名称',
  `misfire_instr` smallint DEFAULT NULL COMMENT '补偿执行的策略',
  `job_data` blob COMMENT '存放持久化job对象',
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`),
  KEY `sched_name` (`sched_name`,`job_name`,`job_group`),
  CONSTRAINT `qrtz_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `job_name`, `job_group`) REFERENCES `QRTZ_JOB_DETAILS` (`sched_name`, `job_name`, `job_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='触发器详细信息表';

-- ----------------------------
-- Records of QRTZ_TRIGGERS
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config` (
  `config_id` int NOT NULL AUTO_INCREMENT COMMENT '参数主键',
  `config_name` varchar(100) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '参数名称',
  `config_key` varchar(100) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '参数键名',
  `config_value` varchar(500) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '参数键值',
  `config_type` char(1) COLLATE utf8mb4_general_ci DEFAULT 'N' COMMENT '系统内置（Y是 N否）',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`config_id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='参数配置表';

-- ----------------------------
-- Records of sys_config
-- ----------------------------
BEGIN;
INSERT INTO `sys_config` (`config_id`, `config_name`, `config_key`, `config_value`, `config_type`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1, '主框架页-默认皮肤样式名称', 'sys.index.skinName', 'skin-blue', 'Y', 'admin', '2025-12-09 17:04:37', '', NULL, '蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow');
INSERT INTO `sys_config` (`config_id`, `config_name`, `config_key`, `config_value`, `config_type`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2, '用户管理-账号初始密码', 'sys.user.initPassword', '123456', 'Y', 'admin', '2025-12-09 17:04:37', '', NULL, '初始化密码 123456');
INSERT INTO `sys_config` (`config_id`, `config_name`, `config_key`, `config_value`, `config_type`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3, '主框架页-侧边栏主题', 'sys.index.sideTheme', 'theme-dark', 'Y', 'admin', '2025-12-09 17:04:37', '', NULL, '深色主题theme-dark，浅色主题theme-light');
INSERT INTO `sys_config` (`config_id`, `config_name`, `config_key`, `config_value`, `config_type`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4, '账号自助-验证码开关', 'sys.account.captchaEnabled', 'true', 'Y', 'admin', '2025-12-09 17:04:37', '', NULL, '是否开启验证码功能（true开启，false关闭）');
INSERT INTO `sys_config` (`config_id`, `config_name`, `config_key`, `config_value`, `config_type`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5, '账号自助-是否开启用户注册功能', 'sys.account.registerUser', 'true', 'Y', 'admin', '2025-12-09 17:04:37', 'admin', '2025-12-13 17:51:55', '是否开启注册用户功能（true开启，false关闭）');
INSERT INTO `sys_config` (`config_id`, `config_name`, `config_key`, `config_value`, `config_type`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (6, '用户登录-黑名单列表', 'sys.login.blackIPList', '', 'Y', 'admin', '2025-12-09 17:04:37', '', NULL, '设置登录IP黑名单限制，多个匹配项以;分隔，支持匹配（*通配、网段）');
INSERT INTO `sys_config` (`config_id`, `config_name`, `config_key`, `config_value`, `config_type`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (7, '用户管理-初始密码修改策略', 'sys.account.initPasswordModify', '1', 'Y', 'admin', '2025-12-09 17:04:37', '', NULL, '0：初始密码修改策略关闭，没有任何提示，1：提醒用户，如果未修改初始密码，则在登录时就会提醒修改密码对话框');
INSERT INTO `sys_config` (`config_id`, `config_name`, `config_key`, `config_value`, `config_type`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (8, '用户管理-账号密码更新周期', 'sys.account.passwordValidateDays', '0', 'Y', 'admin', '2025-12-09 17:04:37', '', NULL, '密码更新周期（填写数字，数据初始化值为0不限制，若修改必须为大于0小于365的正整数），如果超过这个周期登录系统时，则在登录时就会提醒修改密码对话框');
COMMIT;

-- ----------------------------
-- Table structure for sys_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept` (
  `dept_id` bigint NOT NULL AUTO_INCREMENT COMMENT '部门id',
  `parent_id` bigint DEFAULT '0' COMMENT '父部门id',
  `ancestors` varchar(50) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '祖级列表',
  `dept_name` varchar(30) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '部门名称',
  `order_num` int DEFAULT '0' COMMENT '显示顺序',
  `leader` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '负责人',
  `phone` varchar(11) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '联系电话',
  `email` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '邮箱',
  `status` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '部门状态（0正常 1停用）',
  `del_flag` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`dept_id`)
) ENGINE=InnoDB AUTO_INCREMENT=200 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='部门表';

-- ----------------------------
-- Records of sys_dept
-- ----------------------------
BEGIN;
INSERT INTO `sys_dept` (`dept_id`, `parent_id`, `ancestors`, `dept_name`, `order_num`, `leader`, `phone`, `email`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (100, 0, '0', '若依科技', 0, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2025-12-09 17:04:37', '', NULL);
INSERT INTO `sys_dept` (`dept_id`, `parent_id`, `ancestors`, `dept_name`, `order_num`, `leader`, `phone`, `email`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (101, 100, '0,100', '深圳总公司', 1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2025-12-09 17:04:37', '', NULL);
INSERT INTO `sys_dept` (`dept_id`, `parent_id`, `ancestors`, `dept_name`, `order_num`, `leader`, `phone`, `email`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (102, 100, '0,100', '长沙分公司', 2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2025-12-09 17:04:37', '', NULL);
INSERT INTO `sys_dept` (`dept_id`, `parent_id`, `ancestors`, `dept_name`, `order_num`, `leader`, `phone`, `email`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (103, 101, '0,100,101', '研发部门', 1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2025-12-09 17:04:37', '', NULL);
INSERT INTO `sys_dept` (`dept_id`, `parent_id`, `ancestors`, `dept_name`, `order_num`, `leader`, `phone`, `email`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (104, 101, '0,100,101', '市场部门', 2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2025-12-09 17:04:37', '', NULL);
INSERT INTO `sys_dept` (`dept_id`, `parent_id`, `ancestors`, `dept_name`, `order_num`, `leader`, `phone`, `email`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (105, 101, '0,100,101', '测试部门', 3, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2025-12-09 17:04:37', '', NULL);
INSERT INTO `sys_dept` (`dept_id`, `parent_id`, `ancestors`, `dept_name`, `order_num`, `leader`, `phone`, `email`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (106, 101, '0,100,101', '财务部门', 4, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2025-12-09 17:04:37', '', NULL);
INSERT INTO `sys_dept` (`dept_id`, `parent_id`, `ancestors`, `dept_name`, `order_num`, `leader`, `phone`, `email`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (107, 101, '0,100,101', '运维部门', 5, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2025-12-09 17:04:37', '', NULL);
INSERT INTO `sys_dept` (`dept_id`, `parent_id`, `ancestors`, `dept_name`, `order_num`, `leader`, `phone`, `email`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (108, 102, '0,100,102', '市场部门', 1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2025-12-09 17:04:37', '', NULL);
INSERT INTO `sys_dept` (`dept_id`, `parent_id`, `ancestors`, `dept_name`, `order_num`, `leader`, `phone`, `email`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (109, 102, '0,100,102', '财务部门', 2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2025-12-09 17:04:37', '', NULL);
COMMIT;

-- ----------------------------
-- Table structure for sys_dict_data
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data` (
  `dict_code` bigint NOT NULL AUTO_INCREMENT COMMENT '字典编码',
  `dict_sort` int DEFAULT '0' COMMENT '字典排序',
  `dict_label` varchar(100) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '字典标签',
  `dict_value` varchar(100) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '字典键值',
  `dict_type` varchar(100) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '字典类型',
  `css_class` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '样式属性（其他样式扩展）',
  `list_class` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '表格回显样式',
  `is_default` char(1) COLLATE utf8mb4_general_ci DEFAULT 'N' COMMENT '是否默认（Y是 N否）',
  `status` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dict_code`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='字典数据表';

-- ----------------------------
-- Records of sys_dict_data
-- ----------------------------
BEGIN;
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1, 1, '男', '0', 'sys_user_sex', '', '', 'Y', '0', 'admin', '2025-12-09 17:04:37', '', NULL, '性别男');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2, 2, '女', '1', 'sys_user_sex', '', '', 'N', '0', 'admin', '2025-12-09 17:04:37', '', NULL, '性别女');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3, 3, '未知', '2', 'sys_user_sex', '', '', 'N', '0', 'admin', '2025-12-09 17:04:37', '', NULL, '性别未知');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4, 1, '显示', '0', 'sys_show_hide', '', 'primary', 'Y', '0', 'admin', '2025-12-09 17:04:37', '', NULL, '显示菜单');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5, 2, '隐藏', '1', 'sys_show_hide', '', 'danger', 'N', '0', 'admin', '2025-12-09 17:04:37', '', NULL, '隐藏菜单');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (6, 1, '正常', '0', 'sys_normal_disable', '', 'primary', 'Y', '0', 'admin', '2025-12-09 17:04:37', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (7, 2, '停用', '1', 'sys_normal_disable', '', 'danger', 'N', '0', 'admin', '2025-12-09 17:04:37', '', NULL, '停用状态');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (8, 1, '正常', '0', 'sys_job_status', '', 'primary', 'Y', '0', 'admin', '2025-12-09 17:04:37', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (9, 2, '暂停', '1', 'sys_job_status', '', 'danger', 'N', '0', 'admin', '2025-12-09 17:04:37', '', NULL, '停用状态');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (10, 1, '默认', 'DEFAULT', 'sys_job_group', '', '', 'Y', '0', 'admin', '2025-12-09 17:04:37', '', NULL, '默认分组');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (11, 2, '系统', 'SYSTEM', 'sys_job_group', '', '', 'N', '0', 'admin', '2025-12-09 17:04:37', '', NULL, '系统分组');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (12, 1, '是', 'Y', 'sys_yes_no', '', 'primary', 'Y', '0', 'admin', '2025-12-09 17:04:37', '', NULL, '系统默认是');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (13, 2, '否', 'N', 'sys_yes_no', '', 'danger', 'N', '0', 'admin', '2025-12-09 17:04:37', '', NULL, '系统默认否');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (14, 1, '通知', '1', 'sys_notice_type', '', 'warning', 'Y', '0', 'admin', '2025-12-09 17:04:37', '', NULL, '通知');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (15, 2, '公告', '2', 'sys_notice_type', '', 'success', 'N', '0', 'admin', '2025-12-09 17:04:37', '', NULL, '公告');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (16, 1, '正常', '0', 'sys_notice_status', '', 'primary', 'Y', '0', 'admin', '2025-12-09 17:04:37', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (17, 2, '关闭', '1', 'sys_notice_status', '', 'danger', 'N', '0', 'admin', '2025-12-09 17:04:37', '', NULL, '关闭状态');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (18, 99, '其他', '0', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2025-12-09 17:04:37', '', NULL, '其他操作');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (19, 1, '新增', '1', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2025-12-09 17:04:37', '', NULL, '新增操作');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (20, 2, '修改', '2', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2025-12-09 17:04:37', '', NULL, '修改操作');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (21, 3, '删除', '3', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2025-12-09 17:04:37', '', NULL, '删除操作');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (22, 4, '授权', '4', 'sys_oper_type', '', 'primary', 'N', '0', 'admin', '2025-12-09 17:04:37', '', NULL, '授权操作');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (23, 5, '导出', '5', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2025-12-09 17:04:37', '', NULL, '导出操作');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (24, 6, '导入', '6', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2025-12-09 17:04:37', '', NULL, '导入操作');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (25, 7, '强退', '7', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2025-12-09 17:04:37', '', NULL, '强退操作');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (26, 8, '生成代码', '8', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2025-12-09 17:04:37', '', NULL, '生成操作');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (27, 9, '清空数据', '9', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2025-12-09 17:04:37', '', NULL, '清空操作');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (28, 1, '成功', '0', 'sys_common_status', '', 'primary', 'N', '0', 'admin', '2025-12-09 17:04:37', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (29, 2, '失败', '1', 'sys_common_status', '', 'danger', 'N', '0', 'admin', '2025-12-09 17:04:37', '', NULL, '停用状态');
COMMIT;

-- ----------------------------
-- Table structure for sys_dict_type
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type` (
  `dict_id` bigint NOT NULL AUTO_INCREMENT COMMENT '字典主键',
  `dict_name` varchar(100) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '字典名称',
  `dict_type` varchar(100) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '字典类型',
  `status` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dict_id`),
  UNIQUE KEY `dict_type` (`dict_type`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='字典类型表';

-- ----------------------------
-- Records of sys_dict_type
-- ----------------------------
BEGIN;
INSERT INTO `sys_dict_type` (`dict_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1, '用户性别', 'sys_user_sex', '0', 'admin', '2025-12-09 17:04:37', '', NULL, '用户性别列表');
INSERT INTO `sys_dict_type` (`dict_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2, '菜单状态', 'sys_show_hide', '0', 'admin', '2025-12-09 17:04:37', '', NULL, '菜单状态列表');
INSERT INTO `sys_dict_type` (`dict_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3, '系统开关', 'sys_normal_disable', '0', 'admin', '2025-12-09 17:04:37', '', NULL, '系统开关列表');
INSERT INTO `sys_dict_type` (`dict_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4, '任务状态', 'sys_job_status', '0', 'admin', '2025-12-09 17:04:37', '', NULL, '任务状态列表');
INSERT INTO `sys_dict_type` (`dict_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5, '任务分组', 'sys_job_group', '0', 'admin', '2025-12-09 17:04:37', '', NULL, '任务分组列表');
INSERT INTO `sys_dict_type` (`dict_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (6, '系统是否', 'sys_yes_no', '0', 'admin', '2025-12-09 17:04:37', '', NULL, '系统是否列表');
INSERT INTO `sys_dict_type` (`dict_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (7, '通知类型', 'sys_notice_type', '0', 'admin', '2025-12-09 17:04:37', '', NULL, '通知类型列表');
INSERT INTO `sys_dict_type` (`dict_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (8, '通知状态', 'sys_notice_status', '0', 'admin', '2025-12-09 17:04:37', '', NULL, '通知状态列表');
INSERT INTO `sys_dict_type` (`dict_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (9, '操作类型', 'sys_oper_type', '0', 'admin', '2025-12-09 17:04:37', '', NULL, '操作类型列表');
INSERT INTO `sys_dict_type` (`dict_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (10, '系统状态', 'sys_common_status', '0', 'admin', '2025-12-09 17:04:37', '', NULL, '登录状态列表');
COMMIT;

-- ----------------------------
-- Table structure for sys_job
-- ----------------------------
DROP TABLE IF EXISTS `sys_job`;
CREATE TABLE `sys_job` (
  `job_id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `job_name` varchar(64) COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '任务名称',
  `job_group` varchar(64) COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'DEFAULT' COMMENT '任务组名',
  `invoke_target` varchar(500) COLLATE utf8mb4_general_ci NOT NULL COMMENT '调用目标字符串',
  `cron_expression` varchar(255) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT 'cron执行表达式',
  `misfire_policy` varchar(20) COLLATE utf8mb4_general_ci DEFAULT '3' COMMENT '计划执行错误策略（1立即执行 2执行一次 3放弃执行）',
  `concurrent` char(1) COLLATE utf8mb4_general_ci DEFAULT '1' COMMENT '是否并发执行（0允许 1禁止）',
  `status` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '状态（0正常 1暂停）',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注信息',
  PRIMARY KEY (`job_id`,`job_name`,`job_group`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='定时任务调度表';

-- ----------------------------
-- Records of sys_job
-- ----------------------------
BEGIN;
INSERT INTO `sys_job` (`job_id`, `job_name`, `job_group`, `invoke_target`, `cron_expression`, `misfire_policy`, `concurrent`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1, '系统默认（无参）', 'DEFAULT', 'ryTask.ryNoParams', '0/10 * * * * ?', '3', '1', '1', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_job` (`job_id`, `job_name`, `job_group`, `invoke_target`, `cron_expression`, `misfire_policy`, `concurrent`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2, '系统默认（有参）', 'DEFAULT', 'ryTask.ryParams(\'ry\')', '0/15 * * * * ?', '3', '1', '1', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_job` (`job_id`, `job_name`, `job_group`, `invoke_target`, `cron_expression`, `misfire_policy`, `concurrent`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3, '系统默认（多参）', 'DEFAULT', 'ryTask.ryMultipleParams(\'ry\', true, 2000L, 316.50D, 100)', '0/20 * * * * ?', '3', '1', '1', 'admin', '2025-12-09 17:04:37', '', NULL, '');
COMMIT;

-- ----------------------------
-- Table structure for sys_job_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_job_log`;
CREATE TABLE `sys_job_log` (
  `job_log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务日志ID',
  `job_name` varchar(64) COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务名称',
  `job_group` varchar(64) COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务组名',
  `invoke_target` varchar(500) COLLATE utf8mb4_general_ci NOT NULL COMMENT '调用目标字符串',
  `job_message` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '日志信息',
  `status` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '执行状态（0正常 1失败）',
  `exception_info` varchar(2000) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '异常信息',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`job_log_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='定时任务调度日志表';

-- ----------------------------
-- Records of sys_job_log
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for sys_logininfor
-- ----------------------------
DROP TABLE IF EXISTS `sys_logininfor`;
CREATE TABLE `sys_logininfor` (
  `info_id` bigint NOT NULL AUTO_INCREMENT COMMENT '访问ID',
  `user_name` varchar(50) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '用户账号',
  `ipaddr` varchar(128) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '登录IP地址',
  `login_location` varchar(255) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '登录地点',
  `browser` varchar(50) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '浏览器类型',
  `os` varchar(50) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '操作系统',
  `status` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '登录状态（0成功 1失败）',
  `msg` varchar(255) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '提示消息',
  `login_time` datetime DEFAULT NULL COMMENT '访问时间',
  PRIMARY KEY (`info_id`),
  KEY `idx_sys_logininfor_s` (`status`),
  KEY `idx_sys_logininfor_lt` (`login_time`)
) ENGINE=InnoDB AUTO_INCREMENT=132 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统访问记录';

-- ----------------------------
-- Records of sys_logininfor
-- ----------------------------
BEGIN;
INSERT INTO `sys_logininfor` (`info_id`, `user_name`, `ipaddr`, `login_location`, `browser`, `os`, `status`, `msg`, `login_time`) VALUES (100, 'admin', '127.0.0.1', '内网IP', 'Chrome 142', 'Mac OS >=10.15.7', '0', '登录成功', '2025-12-09 17:31:34');
INSERT INTO `sys_logininfor` (`info_id`, `user_name`, `ipaddr`, `login_location`, `browser`, `os`, `status`, `msg`, `login_time`) VALUES (101, 'admin', '127.0.0.1', '内网IP', 'Chrome 142', 'Mac OS >=10.15.7', '0', '登录成功', '2025-12-09 17:40:18');
INSERT INTO `sys_logininfor` (`info_id`, `user_name`, `ipaddr`, `login_location`, `browser`, `os`, `status`, `msg`, `login_time`) VALUES (102, 'admin', '127.0.0.1', '内网IP', 'Chrome 143', 'Mac OS >=10.15.7', '0', '登录成功', '2025-12-13 17:17:34');
INSERT INTO `sys_logininfor` (`info_id`, `user_name`, `ipaddr`, `login_location`, `browser`, `os`, `status`, `msg`, `login_time`) VALUES (103, 'admin', '127.0.0.1', '内网IP', 'Chrome 143', 'Mac OS >=10.15.7', '0', '退出成功', '2025-12-13 17:45:07');
INSERT INTO `sys_logininfor` (`info_id`, `user_name`, `ipaddr`, `login_location`, `browser`, `os`, `status`, `msg`, `login_time`) VALUES (104, 'admin', '127.0.0.1', '内网IP', 'Chrome 143', 'Mac OS >=10.15.7', '0', '登录成功', '2025-12-13 17:51:37');
INSERT INTO `sys_logininfor` (`info_id`, `user_name`, `ipaddr`, `login_location`, `browser`, `os`, `status`, `msg`, `login_time`) VALUES (105, 'admin', '127.0.0.1', '内网IP', 'Chrome 143', 'Mac OS >=10.15.7', '0', '退出成功', '2025-12-13 17:51:59');
INSERT INTO `sys_logininfor` (`info_id`, `user_name`, `ipaddr`, `login_location`, `browser`, `os`, `status`, `msg`, `login_time`) VALUES (106, '17638510561', '127.0.0.1', '内网IP', 'Chrome 143', 'Mac OS >=10.15.7', '0', '注册成功', '2025-12-13 17:55:04');
INSERT INTO `sys_logininfor` (`info_id`, `user_name`, `ipaddr`, `login_location`, `browser`, `os`, `status`, `msg`, `login_time`) VALUES (107, '17638510561', '127.0.0.1', '内网IP', 'Chrome 143', 'Mac OS >=10.15.7', '0', '登录成功', '2025-12-13 17:55:14');
INSERT INTO `sys_logininfor` (`info_id`, `user_name`, `ipaddr`, `login_location`, `browser`, `os`, `status`, `msg`, `login_time`) VALUES (108, '17638510561', '127.0.0.1', '内网IP', 'Chrome 143', 'Mac OS >=10.15.7', '0', '退出成功', '2025-12-13 17:55:28');
INSERT INTO `sys_logininfor` (`info_id`, `user_name`, `ipaddr`, `login_location`, `browser`, `os`, `status`, `msg`, `login_time`) VALUES (109, 'admin', '127.0.0.1', '内网IP', 'Chrome 143', 'Mac OS >=10.15.7', '0', '登录成功', '2025-12-13 17:55:35');
INSERT INTO `sys_logininfor` (`info_id`, `user_name`, `ipaddr`, `login_location`, `browser`, `os`, `status`, `msg`, `login_time`) VALUES (110, 'admin', '127.0.0.1', '内网IP', 'Chrome 143', 'Mac OS >=10.15.7', '0', '退出成功', '2025-12-13 18:14:58');
INSERT INTO `sys_logininfor` (`info_id`, `user_name`, `ipaddr`, `login_location`, `browser`, `os`, `status`, `msg`, `login_time`) VALUES (111, '17638510561', '127.0.0.1', '内网IP', 'Chrome 143', 'Mac OS >=10.15.7', '0', '注册成功', '2025-12-13 18:15:24');
INSERT INTO `sys_logininfor` (`info_id`, `user_name`, `ipaddr`, `login_location`, `browser`, `os`, `status`, `msg`, `login_time`) VALUES (112, '17638510561', '127.0.0.1', '内网IP', 'Chrome 143', 'Mac OS >=10.15.7', '0', '登录成功', '2025-12-13 18:15:30');
INSERT INTO `sys_logininfor` (`info_id`, `user_name`, `ipaddr`, `login_location`, `browser`, `os`, `status`, `msg`, `login_time`) VALUES (113, '17638510561', '127.0.0.1', '内网IP', 'Chrome 143', 'Mac OS >=10.15.7', '0', '退出成功', '2025-12-13 18:17:30');
INSERT INTO `sys_logininfor` (`info_id`, `user_name`, `ipaddr`, `login_location`, `browser`, `os`, `status`, `msg`, `login_time`) VALUES (114, 'admin', '127.0.0.1', '内网IP', 'Chrome 143', 'Mac OS >=10.15.7', '0', '登录成功', '2025-12-13 18:17:35');
INSERT INTO `sys_logininfor` (`info_id`, `user_name`, `ipaddr`, `login_location`, `browser`, `os`, `status`, `msg`, `login_time`) VALUES (115, 'admin', '127.0.0.1', '内网IP', 'Chrome 143', 'Mac OS >=10.15.7', '0', '退出成功', '2025-12-13 18:19:57');
INSERT INTO `sys_logininfor` (`info_id`, `user_name`, `ipaddr`, `login_location`, `browser`, `os`, `status`, `msg`, `login_time`) VALUES (116, '17638510562', '127.0.0.1', '内网IP', 'Chrome 143', 'Mac OS >=10.15.7', '0', '注册成功', '2025-12-13 18:20:47');
INSERT INTO `sys_logininfor` (`info_id`, `user_name`, `ipaddr`, `login_location`, `browser`, `os`, `status`, `msg`, `login_time`) VALUES (117, '17638510562', '127.0.0.1', '内网IP', 'Chrome 143', 'Mac OS >=10.15.7', '0', '登录成功', '2025-12-13 18:20:56');
INSERT INTO `sys_logininfor` (`info_id`, `user_name`, `ipaddr`, `login_location`, `browser`, `os`, `status`, `msg`, `login_time`) VALUES (118, '17638510562', '127.0.0.1', '内网IP', 'Chrome 143', 'Mac OS >=10.15.7', '0', '退出成功', '2025-12-13 18:21:55');
INSERT INTO `sys_logininfor` (`info_id`, `user_name`, `ipaddr`, `login_location`, `browser`, `os`, `status`, `msg`, `login_time`) VALUES (119, 'admin', '127.0.0.1', '内网IP', 'Chrome 143', 'Mac OS >=10.15.7', '1', '用户不存在/密码错误', '2025-12-13 18:22:01');
INSERT INTO `sys_logininfor` (`info_id`, `user_name`, `ipaddr`, `login_location`, `browser`, `os`, `status`, `msg`, `login_time`) VALUES (120, 'admin', '127.0.0.1', '内网IP', 'Chrome 143', 'Mac OS >=10.15.7', '1', '用户不存在/密码错误', '2025-12-13 18:22:11');
INSERT INTO `sys_logininfor` (`info_id`, `user_name`, `ipaddr`, `login_location`, `browser`, `os`, `status`, `msg`, `login_time`) VALUES (121, 'admin', '127.0.0.1', '内网IP', 'Chrome 143', 'Mac OS >=10.15.7', '0', '登录成功', '2025-12-13 18:22:18');
INSERT INTO `sys_logininfor` (`info_id`, `user_name`, `ipaddr`, `login_location`, `browser`, `os`, `status`, `msg`, `login_time`) VALUES (122, '17638510561', '127.0.0.1', '内网IP', 'Chrome 143', 'Mac OS >=10.15.7', '0', '登录成功', '2025-12-13 20:39:51');
INSERT INTO `sys_logininfor` (`info_id`, `user_name`, `ipaddr`, `login_location`, `browser`, `os`, `status`, `msg`, `login_time`) VALUES (123, '17638510561', '127.0.0.1', '内网IP', 'Chrome 143', 'Mac OS >=10.15.7', '0', '退出成功', '2025-12-13 20:42:36');
INSERT INTO `sys_logininfor` (`info_id`, `user_name`, `ipaddr`, `login_location`, `browser`, `os`, `status`, `msg`, `login_time`) VALUES (124, 'admin', '127.0.0.1', '内网IP', 'Chrome 143', 'Mac OS >=10.15.7', '0', '登录成功', '2025-12-13 20:42:41');
INSERT INTO `sys_logininfor` (`info_id`, `user_name`, `ipaddr`, `login_location`, `browser`, `os`, `status`, `msg`, `login_time`) VALUES (125, 'admin', '127.0.0.1', '内网IP', 'Chrome 143', 'Mac OS >=10.15.7', '0', '登录成功', '2025-12-14 17:09:02');
INSERT INTO `sys_logininfor` (`info_id`, `user_name`, `ipaddr`, `login_location`, `browser`, `os`, `status`, `msg`, `login_time`) VALUES (126, 'admin', '127.0.0.1', '内网IP', 'Chrome 143', 'Mac OS >=10.15.7', '0', '登录成功', '2025-12-18 09:29:52');
INSERT INTO `sys_logininfor` (`info_id`, `user_name`, `ipaddr`, `login_location`, `browser`, `os`, `status`, `msg`, `login_time`) VALUES (127, 'admin', '127.0.0.1', '内网IP', 'Chrome 143', 'Mac OS >=10.15.7', '0', '登录成功', '2025-12-19 15:38:02');
INSERT INTO `sys_logininfor` (`info_id`, `user_name`, `ipaddr`, `login_location`, `browser`, `os`, `status`, `msg`, `login_time`) VALUES (128, 'admin', '127.0.0.1', '内网IP', 'Chrome 143', 'Mac OS >=10.15.7', '0', '登录成功', '2025-12-19 16:35:52');
INSERT INTO `sys_logininfor` (`info_id`, `user_name`, `ipaddr`, `login_location`, `browser`, `os`, `status`, `msg`, `login_time`) VALUES (129, 'admin', '127.0.0.1', '内网IP', 'Chrome 143', 'Mac OS >=10.15.7', '0', '登录成功', '2025-12-19 20:21:49');
INSERT INTO `sys_logininfor` (`info_id`, `user_name`, `ipaddr`, `login_location`, `browser`, `os`, `status`, `msg`, `login_time`) VALUES (130, 'admin', '127.0.0.1', '内网IP', 'Chrome 143', 'Mac OS >=10.15.7', '0', '退出成功', '2025-12-19 20:33:43');
INSERT INTO `sys_logininfor` (`info_id`, `user_name`, `ipaddr`, `login_location`, `browser`, `os`, `status`, `msg`, `login_time`) VALUES (131, 'admin', '127.0.0.1', '内网IP', 'Chrome 143', 'Mac OS >=10.15.7', '0', '登录成功', '2025-12-19 20:34:24');
COMMIT;

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
  `menu_id` bigint NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `menu_name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '菜单名称',
  `parent_id` bigint DEFAULT '0' COMMENT '父菜单ID',
  `order_num` int DEFAULT '0' COMMENT '显示顺序',
  `path` varchar(200) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '路由地址',
  `component` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '组件路径',
  `query` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '路由参数',
  `route_name` varchar(50) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '路由名称',
  `is_frame` int DEFAULT '1' COMMENT '是否为外链（0是 1否）',
  `is_cache` int DEFAULT '0' COMMENT '是否缓存（0缓存 1不缓存）',
  `menu_type` char(1) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '菜单类型（M目录 C菜单 F按钮）',
  `visible` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '菜单状态（0显示 1隐藏）',
  `status` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '菜单状态（0正常 1停用）',
  `perms` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(100) COLLATE utf8mb4_general_ci DEFAULT '#' COMMENT '菜单图标',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`menu_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2022 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='菜单权限表';

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
BEGIN;
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1, '系统管理', 0, 9995, 'system', NULL, '', '', 1, 0, 'M', '0', '0', '', 'system', 'admin', '2025-12-09 17:04:37', 'admin', '2025-12-13 17:22:05', '系统管理目录');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2, '系统监控', 0, 9996, 'monitor', NULL, '', '', 1, 0, 'M', '0', '0', '', 'monitor', 'admin', '2025-12-09 17:04:37', 'admin', '2025-12-13 17:22:12', '系统监控目录');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3, '系统工具', 0, 9997, 'tool', NULL, '', '', 1, 0, 'M', '0', '0', '', 'tool', 'admin', '2025-12-09 17:04:37', 'admin', '2025-12-13 17:22:16', '系统工具目录');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (100, '用户管理', 1, 1, 'user', 'system/user/index', '', '', 1, 0, 'C', '0', '0', 'system:user:list', 'user', 'admin', '2025-12-09 17:04:37', '', NULL, '用户管理菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (101, '角色管理', 1, 2, 'role', 'system/role/index', '', '', 1, 0, 'C', '0', '0', 'system:role:list', 'peoples', 'admin', '2025-12-09 17:04:37', '', NULL, '角色管理菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (102, '菜单管理', 1, 3, 'menu', 'system/menu/index', '', '', 1, 0, 'C', '0', '0', 'system:menu:list', 'tree-table', 'admin', '2025-12-09 17:04:37', '', NULL, '菜单管理菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (103, '部门管理', 1, 4, 'dept', 'system/dept/index', '', '', 1, 0, 'C', '0', '0', 'system:dept:list', 'tree', 'admin', '2025-12-09 17:04:37', '', NULL, '部门管理菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (104, '岗位管理', 1, 5, 'post', 'system/post/index', '', '', 1, 0, 'C', '0', '0', 'system:post:list', 'post', 'admin', '2025-12-09 17:04:37', '', NULL, '岗位管理菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (105, '字典管理', 1, 6, 'dict', 'system/dict/index', '', '', 1, 0, 'C', '0', '0', 'system:dict:list', 'dict', 'admin', '2025-12-09 17:04:37', '', NULL, '字典管理菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (106, '参数设置', 1, 7, 'config', 'system/config/index', '', '', 1, 0, 'C', '0', '0', 'system:config:list', 'edit', 'admin', '2025-12-09 17:04:37', '', NULL, '参数设置菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (107, '通知公告', 1, 8, 'notice', 'system/notice/index', '', '', 1, 0, 'C', '0', '0', 'system:notice:list', 'message', 'admin', '2025-12-09 17:04:37', '', NULL, '通知公告菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (108, '日志管理', 1, 9, 'log', '', '', '', 1, 0, 'M', '0', '0', '', 'log', 'admin', '2025-12-09 17:04:37', '', NULL, '日志管理菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (109, '在线用户', 2, 1, 'online', 'monitor/online/index', '', '', 1, 0, 'C', '0', '0', 'monitor:online:list', 'online', 'admin', '2025-12-09 17:04:37', '', NULL, '在线用户菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (110, '定时任务', 2, 2, 'job', 'monitor/job/index', '', '', 1, 0, 'C', '0', '0', 'monitor:job:list', 'job', 'admin', '2025-12-09 17:04:37', '', NULL, '定时任务菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (111, '数据监控', 2, 3, 'druid', 'monitor/druid/index', '', '', 1, 0, 'C', '0', '0', 'monitor:druid:list', 'druid', 'admin', '2025-12-09 17:04:37', '', NULL, '数据监控菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (112, '服务监控', 2, 4, 'server', 'monitor/server/index', '', '', 1, 0, 'C', '0', '0', 'monitor:server:list', 'server', 'admin', '2025-12-09 17:04:37', '', NULL, '服务监控菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (113, '缓存监控', 2, 5, 'cache', 'monitor/cache/index', '', '', 1, 0, 'C', '0', '0', 'monitor:cache:list', 'redis', 'admin', '2025-12-09 17:04:37', '', NULL, '缓存监控菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (114, '缓存列表', 2, 6, 'cacheList', 'monitor/cache/list', '', '', 1, 0, 'C', '0', '0', 'monitor:cache:list', 'redis-list', 'admin', '2025-12-09 17:04:37', '', NULL, '缓存列表菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (115, '表单构建', 3, 1, 'build', 'tool/build/index', '', '', 1, 0, 'C', '0', '0', 'tool:build:list', 'build', 'admin', '2025-12-09 17:04:37', '', NULL, '表单构建菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (116, '代码生成', 3, 2, 'gen', 'tool/gen/index', '', '', 1, 0, 'C', '0', '0', 'tool:gen:list', 'code', 'admin', '2025-12-09 17:04:37', '', NULL, '代码生成菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (117, '系统接口', 3, 3, 'swagger', 'tool/swagger/index', '', '', 1, 0, 'C', '0', '0', 'tool:swagger:list', 'swagger', 'admin', '2025-12-09 17:04:37', '', NULL, '系统接口菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (500, '操作日志', 108, 1, 'operlog', 'monitor/operlog/index', '', '', 1, 0, 'C', '0', '0', 'monitor:operlog:list', 'form', 'admin', '2025-12-09 17:04:37', '', NULL, '操作日志菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (501, '登录日志', 108, 2, 'logininfor', 'monitor/logininfor/index', '', '', 1, 0, 'C', '0', '0', 'monitor:logininfor:list', 'logininfor', 'admin', '2025-12-09 17:04:37', '', NULL, '登录日志菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1000, '用户查询', 100, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:query', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1001, '用户新增', 100, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:add', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1002, '用户修改', 100, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:edit', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1003, '用户删除', 100, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:remove', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1004, '用户导出', 100, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:export', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1005, '用户导入', 100, 6, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:import', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1006, '重置密码', 100, 7, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:resetPwd', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1007, '角色查询', 101, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:query', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1008, '角色新增', 101, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:add', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1009, '角色修改', 101, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:edit', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1010, '角色删除', 101, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:remove', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1011, '角色导出', 101, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:export', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1012, '菜单查询', 102, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:query', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1013, '菜单新增', 102, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:add', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1014, '菜单修改', 102, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:edit', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1015, '菜单删除', 102, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:remove', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1016, '部门查询', 103, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:query', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1017, '部门新增', 103, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:add', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1018, '部门修改', 103, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:edit', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1019, '部门删除', 103, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:remove', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1020, '岗位查询', 104, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:query', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1021, '岗位新增', 104, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:add', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1022, '岗位修改', 104, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:edit', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1023, '岗位删除', 104, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:remove', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1024, '岗位导出', 104, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:export', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1025, '字典查询', 105, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:query', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1026, '字典新增', 105, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:add', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1027, '字典修改', 105, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:edit', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1028, '字典删除', 105, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:remove', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1029, '字典导出', 105, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:export', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1030, '参数查询', 106, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:query', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1031, '参数新增', 106, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:add', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1032, '参数修改', 106, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:edit', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1033, '参数删除', 106, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:remove', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1034, '参数导出', 106, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:export', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1035, '公告查询', 107, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:query', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1036, '公告新增', 107, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:add', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1037, '公告修改', 107, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:edit', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1038, '公告删除', 107, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:remove', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1039, '操作查询', 500, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:query', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1040, '操作删除', 500, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:remove', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1041, '日志导出', 500, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:export', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1042, '登录查询', 501, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:query', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1043, '登录删除', 501, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:remove', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1044, '日志导出', 501, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:export', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1045, '账户解锁', 501, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:unlock', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1046, '在线查询', 109, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:online:query', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1047, '批量强退', 109, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:online:batchLogout', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1048, '单条强退', 109, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:online:forceLogout', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1049, '任务查询', 110, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:query', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1050, '任务新增', 110, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:add', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1051, '任务修改', 110, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:edit', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1052, '任务删除', 110, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:remove', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1053, '状态修改', 110, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:changeStatus', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1054, '任务导出', 110, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:export', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1055, '生成查询', 116, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:query', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1056, '生成修改', 116, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:edit', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1057, '生成删除', 116, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:remove', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1058, '导入代码', 116, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:import', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1059, '预览代码', 116, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:preview', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1060, '生成代码', 116, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:code', '#', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2000, '基础管理', 0, 20, 'base', NULL, NULL, '', 1, 0, 'M', '0', '0', '', 'list', 'admin', '2025-12-13 17:22:58', 'admin', '2025-12-13 17:23:12', '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2001, '收发地址', 2000, 1, 'address', 'nht/address/index', NULL, '', 1, 0, 'C', '0', '0', 'nht:address:list', 'cascader', 'admin', '2025-12-13 17:33:36', 'admin', '2025-12-13 18:19:49', '地址菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2002, '地址查询', 2001, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'nht:address:query', '#', 'admin', '2025-12-13 17:33:36', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2003, '地址新增', 2001, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'nht:address:add', '#', 'admin', '2025-12-13 17:33:36', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2004, '地址修改', 2001, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'nht:address:edit', '#', 'admin', '2025-12-13 17:33:36', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2005, '地址删除', 2001, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'nht:address:remove', '#', 'admin', '2025-12-13 17:33:36', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2006, '地址导出', 2001, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'nht:address:export', '#', 'admin', '2025-12-13 17:33:36', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2007, '我的采购需求', 0, 30, 'buyingLeads', NULL, NULL, '', 1, 0, 'C', '0', '0', '', 'shopping', 'admin', '2025-12-13 17:59:04', 'admin', '2025-12-13 17:59:22', '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2008, '找货源（供应商）', 0, 40, 'findGoods', NULL, NULL, '', 1, 0, 'C', '0', '0', NULL, 'server', 'admin', '2025-12-13 18:01:32', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2009, '全域地图', 0, 50, 'maps', NULL, NULL, '', 1, 0, 'C', '0', '0', NULL, 'international', 'admin', '2025-12-13 18:02:52', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2010, '合同管理', 0, 60, 'contract', NULL, NULL, '', 1, 0, 'C', '0', '0', NULL, 'form', 'admin', '2025-12-13 18:03:30', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2011, '积分钱包', 0, 70, 'wallet', NULL, NULL, '', 1, 0, 'C', '0', '0', NULL, 'money', 'admin', '2025-12-13 18:04:27', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2012, '账户设置', 0, 80, 'account', NULL, NULL, '', 1, 0, 'C', '0', '0', NULL, 'system', 'admin', '2025-12-13 18:05:31', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2013, '我的供应发布', 0, 30, 'publish', NULL, NULL, '', 1, 0, 'C', '0', '0', NULL, 'server', 'admin', '2025-12-13 18:06:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2014, '找订单（采购商）', 0, 40, 'findOrder', NULL, NULL, '', 1, 0, 'C', '0', '0', NULL, 'shopping', 'admin', '2025-12-13 18:07:22', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2016, '产品分类', 2000, 1, 'product', 'nht/product/index', NULL, '', 1, 0, 'C', '0', '0', 'nht:product:list', 'cascader', 'admin', '2025-12-18 09:31:20', 'admin', '2025-12-19 20:43:23', '产品菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2017, '产品查询', 2016, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'nht:product:query', '#', 'admin', '2025-12-18 09:31:20', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2018, '产品新增', 2016, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'nht:product:add', '#', 'admin', '2025-12-18 09:31:20', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2019, '产品修改', 2016, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'nht:product:edit', '#', 'admin', '2025-12-18 09:31:20', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2020, '产品删除', 2016, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'nht:product:remove', '#', 'admin', '2025-12-18 09:31:20', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2021, '产品导出', 2016, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'nht:product:export', '#', 'admin', '2025-12-18 09:31:20', '', NULL, '');
COMMIT;

-- ----------------------------
-- Table structure for sys_notice
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice` (
  `notice_id` int NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `notice_title` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '公告标题',
  `notice_type` char(1) COLLATE utf8mb4_general_ci NOT NULL COMMENT '公告类型（1通知 2公告）',
  `notice_content` longblob COMMENT '公告内容',
  `status` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '公告状态（0正常 1关闭）',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`notice_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='通知公告表';

-- ----------------------------
-- Records of sys_notice
-- ----------------------------
BEGIN;
INSERT INTO `sys_notice` (`notice_id`, `notice_title`, `notice_type`, `notice_content`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1, '温馨提醒：2018-07-01 若依新版本发布啦', '2', 0xE696B0E78988E69CACE58685E5AEB9, '0', 'admin', '2025-12-09 17:04:38', '', NULL, '管理员');
INSERT INTO `sys_notice` (`notice_id`, `notice_title`, `notice_type`, `notice_content`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2, '维护通知：2018-07-01 若依系统凌晨维护', '1', 0xE7BBB4E68AA4E58685E5AEB9, '0', 'admin', '2025-12-09 17:04:38', '', NULL, '管理员');
COMMIT;

-- ----------------------------
-- Table structure for sys_oper_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_oper_log`;
CREATE TABLE `sys_oper_log` (
  `oper_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志主键',
  `title` varchar(50) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '模块标题',
  `business_type` int DEFAULT '0' COMMENT '业务类型（0其它 1新增 2修改 3删除）',
  `method` varchar(200) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '方法名称',
  `request_method` varchar(10) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '请求方式',
  `operator_type` int DEFAULT '0' COMMENT '操作类别（0其它 1后台用户 2手机端用户）',
  `oper_name` varchar(50) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '操作人员',
  `dept_name` varchar(50) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '部门名称',
  `oper_url` varchar(255) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '请求URL',
  `oper_ip` varchar(128) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '主机地址',
  `oper_location` varchar(255) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '操作地点',
  `oper_param` varchar(2000) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '请求参数',
  `json_result` varchar(2000) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '返回参数',
  `status` int DEFAULT '0' COMMENT '操作状态（0正常 1异常）',
  `error_msg` varchar(2000) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '错误消息',
  `oper_time` datetime DEFAULT NULL COMMENT '操作时间',
  `cost_time` bigint DEFAULT '0' COMMENT '消耗时间',
  PRIMARY KEY (`oper_id`),
  KEY `idx_sys_oper_log_bt` (`business_type`),
  KEY `idx_sys_oper_log_s` (`status`),
  KEY `idx_sys_oper_log_ot` (`oper_time`)
) ENGINE=InnoDB AUTO_INCREMENT=172 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='操作日志记录';

-- ----------------------------
-- Records of sys_oper_log
-- ----------------------------
BEGIN;
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (100, '菜单管理', 3, 'com.ruoyi.web.controller.system.SysMenuController.remove()', 'DELETE', 1, 'admin', '研发部门', '/system/menu/4', '127.0.0.1', '内网IP', '4 ', '{\"msg\":\"菜单已分配,不允许删除\",\"code\":601}', 0, NULL, '2025-12-09 17:47:36', 31);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (101, '角色管理', 2, 'com.ruoyi.web.controller.system.SysRoleController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/role', '127.0.0.1', '内网IP', '{\"admin\":false,\"createTime\":\"2025-12-09 17:04:37\",\"dataScope\":\"2\",\"delFlag\":\"0\",\"deptCheckStrictly\":true,\"flag\":false,\"menuCheckStrictly\":true,\"menuIds\":[1,100,1000,1001,1002,1003,1004,1005,1006,101,1007,1008,1009,1010,1011,102,1012,1013,1014,1015,103,1016,1017,1018,1019,104,1020,1021,1022,1023,1024,105,1025,1026,1027,1028,1029,106,1030,1031,1032,1033,1034,107,1035,1036,1037,1038,108,500,1039,1040,1041,501,1042,1043,1044,1045,2,109,1046,1047,1048,110,1049,1050,1051,1052,1053,1054,111,112,113,114,3,115,116,1055,1056,1057,1058,1059,1060,117],\"params\":{},\"remark\":\"普通角色\",\"roleId\":2,\"roleKey\":\"common\",\"roleName\":\"普通角色\",\"roleSort\":2,\"status\":\"0\",\"updateBy\":\"admin\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-09 17:47:45', 86);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (102, '菜单管理', 3, 'com.ruoyi.web.controller.system.SysMenuController.remove()', 'DELETE', 1, 'admin', '研发部门', '/system/menu/4', '127.0.0.1', '内网IP', '4 ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-09 17:47:49', 14);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (103, '代码生成', 6, 'com.ruoyi.generator.controller.GenController.importTableSave()', 'POST', 1, 'admin', '研发部门', '/tool/gen/importTable', '127.0.0.1', '内网IP', '{\"tables\":\"nht_address\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-13 17:20:16', 128);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (104, '代码生成', 2, 'com.ruoyi.generator.controller.GenController.editSave()', 'PUT', 1, 'admin', '研发部门', '/tool/gen', '127.0.0.1', '内网IP', '{\"businessName\":\"address\",\"className\":\"NhtAddress\",\"columns\":[{\"capJavaField\":\"Id\",\"columnComment\":\"id\",\"columnId\":1,\"columnName\":\"id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2025-12-13 17:20:16\",\"dictType\":\"\",\"edit\":false,\"htmlType\":\"input\",\"increment\":true,\"insert\":true,\"isIncrement\":\"1\",\"isInsert\":\"1\",\"isPk\":\"1\",\"isRequired\":\"0\",\"javaField\":\"id\",\"javaType\":\"Long\",\"list\":false,\"params\":{},\"pk\":true,\"query\":false,\"queryType\":\"EQ\",\"required\":false,\"sort\":1,\"superColumn\":false,\"tableId\":1,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"UserId\",\"columnComment\":\"用户ID\",\"columnId\":2,\"columnName\":\"user_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2025-12-13 17:20:16\",\"dictType\":\"\",\"edit\":false,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"0\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"0\",\"isPk\":\"0\",\"isQuery\":\"0\",\"isRequired\":\"1\",\"javaField\":\"userId\",\"javaType\":\"Long\",\"list\":false,\"params\":{},\"pk\":false,\"query\":false,\"queryType\":\"EQ\",\"required\":true,\"sort\":2,\"superColumn\":false,\"tableId\":1,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"Name\",\"columnComment\":\"地址别名\",\"columnId\":3,\"columnName\":\"name\",\"columnType\":\"varchar(30)\",\"createBy\":\"admin\",\"createTime\":\"2025-12-13 17:20:16\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"1\",\"javaField\":\"name\",\"javaType\":\"String\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"LIKE\",\"required\":true,\"sort\":3,\"superColumn\":false,\"tableId\":1,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"Address\",\"columnComment\":\"详细地址\",\"columnId\":4,\"columnName\":\"address\",\"columnType\":\"varchar(255)\",\"createBy\":\"admin\",\"createTime\":\"2025-12-13 17:20:16\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"1\",\"javaField\":\"address\",\"javaType\":\"St', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-13 17:21:49', 44);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (105, '菜单管理', 2, 'com.ruoyi.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{\"children\":[],\"createTime\":\"2025-12-09 17:04:37\",\"icon\":\"system\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":1,\"menuName\":\"系统管理\",\"menuType\":\"M\",\"orderNum\":9995,\"params\":{},\"parentId\":0,\"path\":\"system\",\"perms\":\"\",\"query\":\"\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"0\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-13 17:22:05', 15);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (106, '菜单管理', 2, 'com.ruoyi.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{\"children\":[],\"createTime\":\"2025-12-09 17:04:37\",\"icon\":\"monitor\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2,\"menuName\":\"系统监控\",\"menuType\":\"M\",\"orderNum\":9996,\"params\":{},\"parentId\":0,\"path\":\"monitor\",\"perms\":\"\",\"query\":\"\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"0\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-13 17:22:12', 7);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (107, '菜单管理', 2, 'com.ruoyi.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{\"children\":[],\"createTime\":\"2025-12-09 17:04:37\",\"icon\":\"tool\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":3,\"menuName\":\"系统工具\",\"menuType\":\"M\",\"orderNum\":9997,\"params\":{},\"parentId\":0,\"path\":\"tool\",\"perms\":\"\",\"query\":\"\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"0\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-13 17:22:16', 7);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (108, '菜单管理', 1, 'com.ruoyi.web.controller.system.SysMenuController.add()', 'POST', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{\"children\":[],\"createBy\":\"admin\",\"icon\":\"list\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuName\":\"基础管理\",\"menuType\":\"M\",\"orderNum\":20,\"params\":{},\"parentId\":0,\"path\":\"/base\",\"status\":\"0\",\"visible\":\"0\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-13 17:22:58', 7);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (109, '菜单管理', 2, 'com.ruoyi.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{\"children\":[],\"createTime\":\"2025-12-13 17:22:58\",\"icon\":\"list\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2000,\"menuName\":\"基础管理\",\"menuType\":\"M\",\"orderNum\":20,\"params\":{},\"parentId\":0,\"path\":\"base\",\"perms\":\"\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"0\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-13 17:23:12', 8);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (110, '代码生成', 2, 'com.ruoyi.generator.controller.GenController.editSave()', 'PUT', 1, 'admin', '研发部门', '/tool/gen', '127.0.0.1', '内网IP', '{\"businessName\":\"address\",\"className\":\"NhtAddress\",\"columns\":[{\"capJavaField\":\"Id\",\"columnComment\":\"id\",\"columnId\":1,\"columnName\":\"id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2025-12-13 17:20:16\",\"dictType\":\"\",\"edit\":false,\"htmlType\":\"input\",\"increment\":true,\"insert\":true,\"isIncrement\":\"1\",\"isInsert\":\"1\",\"isPk\":\"1\",\"isRequired\":\"0\",\"javaField\":\"id\",\"javaType\":\"Long\",\"list\":false,\"params\":{},\"pk\":true,\"query\":false,\"queryType\":\"EQ\",\"required\":false,\"sort\":1,\"superColumn\":false,\"tableId\":1,\"updateBy\":\"\",\"updateTime\":\"2025-12-13 17:21:49\",\"usableColumn\":false},{\"capJavaField\":\"UserId\",\"columnComment\":\"用户ID\",\"columnId\":2,\"columnName\":\"user_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2025-12-13 17:20:16\",\"dictType\":\"\",\"edit\":false,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"0\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"0\",\"isPk\":\"0\",\"isQuery\":\"0\",\"isRequired\":\"1\",\"javaField\":\"userId\",\"javaType\":\"Long\",\"list\":false,\"params\":{},\"pk\":false,\"query\":false,\"queryType\":\"EQ\",\"required\":true,\"sort\":2,\"superColumn\":false,\"tableId\":1,\"updateBy\":\"\",\"updateTime\":\"2025-12-13 17:21:49\",\"usableColumn\":false},{\"capJavaField\":\"Name\",\"columnComment\":\"地址别名\",\"columnId\":3,\"columnName\":\"name\",\"columnType\":\"varchar(30)\",\"createBy\":\"admin\",\"createTime\":\"2025-12-13 17:20:16\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"1\",\"javaField\":\"name\",\"javaType\":\"String\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"LIKE\",\"required\":true,\"sort\":3,\"superColumn\":false,\"tableId\":1,\"updateBy\":\"\",\"updateTime\":\"2025-12-13 17:21:49\",\"usableColumn\":false},{\"capJavaField\":\"Address\",\"columnComment\":\"详细地址\",\"columnId\":4,\"columnName\":\"address\",\"columnType\":\"varchar(255)\",\"createBy\":\"admin\",\"createTime\":\"2025-12-13 17:20:16\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-13 17:23:25', 40);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (111, '代码生成', 8, 'com.ruoyi.generator.controller.GenController.batchGenCode()', 'GET', 1, 'admin', '研发部门', '/tool/gen/batchGenCode', '127.0.0.1', '内网IP', '{\"tables\":\"nht_address\"}', NULL, 0, NULL, '2025-12-13 17:31:23', 287);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (112, '代码生成', 2, 'com.ruoyi.generator.controller.GenController.synchDb()', 'GET', 1, 'admin', '研发部门', '/tool/gen/synchDb/nht_address', '127.0.0.1', '内网IP', '{}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-13 17:36:54', 147);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (113, '代码生成', 2, 'com.ruoyi.generator.controller.GenController.editSave()', 'PUT', 1, 'admin', '研发部门', '/tool/gen', '127.0.0.1', '内网IP', '{\"businessName\":\"address\",\"className\":\"NhtAddress\",\"columns\":[{\"capJavaField\":\"Id\",\"columnComment\":\"id\",\"columnId\":1,\"columnName\":\"id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2025-12-13 17:20:16\",\"dictType\":\"\",\"edit\":false,\"htmlType\":\"input\",\"increment\":true,\"insert\":true,\"isIncrement\":\"1\",\"isInsert\":\"1\",\"isPk\":\"1\",\"isRequired\":\"0\",\"javaField\":\"id\",\"javaType\":\"Long\",\"list\":false,\"params\":{},\"pk\":true,\"query\":false,\"queryType\":\"EQ\",\"required\":false,\"sort\":1,\"superColumn\":false,\"tableId\":1,\"updateBy\":\"\",\"updateTime\":\"2025-12-13 17:36:54\",\"usableColumn\":false},{\"capJavaField\":\"UserId\",\"columnComment\":\"用户ID\",\"columnId\":2,\"columnName\":\"user_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2025-12-13 17:20:16\",\"dictType\":\"\",\"edit\":false,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"0\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"0\",\"isPk\":\"0\",\"isQuery\":\"0\",\"isRequired\":\"1\",\"javaField\":\"userId\",\"javaType\":\"Long\",\"list\":false,\"params\":{},\"pk\":false,\"query\":false,\"queryType\":\"EQ\",\"required\":true,\"sort\":2,\"superColumn\":false,\"tableId\":1,\"updateBy\":\"\",\"updateTime\":\"2025-12-13 17:36:54\",\"usableColumn\":false},{\"capJavaField\":\"Name\",\"columnComment\":\"地址别名\",\"columnId\":3,\"columnName\":\"name\",\"columnType\":\"varchar(30)\",\"createBy\":\"admin\",\"createTime\":\"2025-12-13 17:20:16\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"1\",\"javaField\":\"name\",\"javaType\":\"String\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"LIKE\",\"required\":true,\"sort\":3,\"superColumn\":false,\"tableId\":1,\"updateBy\":\"\",\"updateTime\":\"2025-12-13 17:36:54\",\"usableColumn\":false},{\"capJavaField\":\"Address\",\"columnComment\":\"详细地址\",\"columnId\":4,\"columnName\":\"address\",\"columnType\":\"varchar(255)\",\"createBy\":\"admin\",\"createTime\":\"2025-12-13 17:20:16\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-13 17:37:46', 59);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (114, '代码生成', 8, 'com.ruoyi.generator.controller.GenController.batchGenCode()', 'GET', 1, 'admin', '研发部门', '/tool/gen/batchGenCode', '127.0.0.1', '内网IP', '{\"tables\":\"nht_address\"}', NULL, 0, NULL, '2025-12-13 17:38:02', 63);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (115, '参数管理', 2, 'com.ruoyi.web.controller.system.SysConfigController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/config', '127.0.0.1', '内网IP', '{\"configId\":5,\"configKey\":\"sys.account.registerUser\",\"configName\":\"账号自助-是否开启用户注册功能\",\"configType\":\"Y\",\"configValue\":\"true\",\"createBy\":\"admin\",\"createTime\":\"2025-12-09 17:04:37\",\"params\":{},\"remark\":\"是否开启注册用户功能（true开启，false关闭）\",\"updateBy\":\"admin\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-13 17:51:55', 22);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (116, '菜单管理', 2, 'com.ruoyi.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{\"children\":[],\"component\":\"nht/address/index\",\"createTime\":\"2025-12-13 17:33:36\",\"icon\":\"#\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2001,\"menuName\":\"收发地址\",\"menuType\":\"C\",\"orderNum\":1,\"params\":{},\"parentId\":2000,\"path\":\"address\",\"perms\":\"nht:address:list\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"0\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-13 17:56:23', 47);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (117, '菜单管理', 1, 'com.ruoyi.web.controller.system.SysMenuController.add()', 'POST', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{\"children\":[],\"createBy\":\"admin\",\"icon\":\"shopping\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuName\":\"我的采购需求\",\"menuType\":\"C\",\"orderNum\":20,\"params\":{},\"parentId\":0,\"path\":\"buyingLeads\",\"status\":\"0\",\"visible\":\"0\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-13 17:59:04', 20);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (118, '菜单管理', 2, 'com.ruoyi.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{\"children\":[],\"createTime\":\"2025-12-13 17:59:04\",\"icon\":\"shopping\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2007,\"menuName\":\"我的采购需求\",\"menuType\":\"C\",\"orderNum\":30,\"params\":{},\"parentId\":0,\"path\":\"buyingLeads\",\"perms\":\"\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"0\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-13 17:59:22', 10);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (119, '菜单管理', 1, 'com.ruoyi.web.controller.system.SysMenuController.add()', 'POST', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{\"children\":[],\"createBy\":\"admin\",\"icon\":\"server\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuName\":\"找货源（供应商）\",\"menuType\":\"C\",\"orderNum\":40,\"params\":{},\"parentId\":0,\"path\":\"findGoods\",\"status\":\"0\",\"visible\":\"0\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-13 18:01:32', 38);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (120, '菜单管理', 1, 'com.ruoyi.web.controller.system.SysMenuController.add()', 'POST', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{\"children\":[],\"createBy\":\"admin\",\"icon\":\"international\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuName\":\"全域地图\",\"menuType\":\"C\",\"orderNum\":50,\"params\":{},\"parentId\":0,\"path\":\"maps\",\"status\":\"0\",\"visible\":\"0\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-13 18:02:52', 14);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (121, '菜单管理', 1, 'com.ruoyi.web.controller.system.SysMenuController.add()', 'POST', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{\"children\":[],\"createBy\":\"admin\",\"icon\":\"form\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuName\":\"合同管理\",\"menuType\":\"C\",\"orderNum\":60,\"params\":{},\"parentId\":0,\"path\":\"contract\",\"status\":\"0\",\"visible\":\"0\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-13 18:03:30', 11);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (122, '菜单管理', 1, 'com.ruoyi.web.controller.system.SysMenuController.add()', 'POST', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{\"children\":[],\"createBy\":\"admin\",\"icon\":\"money\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuName\":\"积分钱包\",\"menuType\":\"C\",\"orderNum\":70,\"params\":{},\"parentId\":0,\"path\":\"wallet\",\"status\":\"0\",\"visible\":\"0\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-13 18:04:27', 9);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (123, '菜单管理', 1, 'com.ruoyi.web.controller.system.SysMenuController.add()', 'POST', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{\"children\":[],\"createBy\":\"admin\",\"icon\":\"system\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuName\":\"账户设置\",\"menuType\":\"C\",\"orderNum\":80,\"params\":{},\"parentId\":0,\"path\":\"account\",\"status\":\"0\",\"visible\":\"0\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-13 18:05:31', 9);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (124, '菜单管理', 1, 'com.ruoyi.web.controller.system.SysMenuController.add()', 'POST', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{\"children\":[],\"createBy\":\"admin\",\"icon\":\"server\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuName\":\"我的供应发布\",\"menuType\":\"C\",\"orderNum\":30,\"params\":{},\"parentId\":0,\"path\":\"publish\",\"status\":\"0\",\"visible\":\"0\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-13 18:06:37', 26);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (125, '菜单管理', 1, 'com.ruoyi.web.controller.system.SysMenuController.add()', 'POST', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{\"children\":[],\"createBy\":\"admin\",\"icon\":\"shopping\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuName\":\"找订单（采购商）\",\"menuType\":\"C\",\"orderNum\":40,\"params\":{},\"parentId\":0,\"path\":\"findOrder\",\"status\":\"0\",\"visible\":\"0\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-13 18:07:22', 8);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (126, '角色管理', 2, 'com.ruoyi.web.controller.system.SysRoleController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/role', '127.0.0.1', '内网IP', '{\"admin\":false,\"createTime\":\"2025-12-09 17:04:37\",\"dataScope\":\"2\",\"delFlag\":\"0\",\"deptCheckStrictly\":true,\"flag\":false,\"menuCheckStrictly\":true,\"menuIds\":[2007,2008,2009,2010,2011,2012],\"params\":{},\"remark\":\"普通角色\",\"roleId\":2,\"roleKey\":\"CG_USER\",\"roleName\":\"采购方角色\",\"roleSort\":2,\"status\":\"0\",\"updateBy\":\"admin\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-13 18:08:16', 106);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (127, '角色管理', 1, 'com.ruoyi.web.controller.system.SysRoleController.add()', 'POST', 1, 'admin', '研发部门', '/system/role', '127.0.0.1', '内网IP', '{\"admin\":false,\"createBy\":\"admin\",\"deptCheckStrictly\":true,\"deptIds\":[],\"flag\":false,\"menuCheckStrictly\":true,\"menuIds\":[2000,2001,2002,2003,2004,2005,2006,2013,2014,2009,2010,2011,2012],\"params\":{},\"roleId\":100,\"roleKey\":\"GY_USER\",\"roleName\":\"供应方角色\",\"roleSort\":3,\"status\":\"0\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-13 18:08:35', 21);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (128, '角色管理', 2, 'com.ruoyi.web.controller.system.SysRoleController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/role', '127.0.0.1', '内网IP', '{\"admin\":false,\"createTime\":\"2025-12-09 17:04:37\",\"dataScope\":\"2\",\"delFlag\":\"0\",\"deptCheckStrictly\":true,\"flag\":false,\"menuCheckStrictly\":true,\"menuIds\":[2000,2001,2002,2003,2004,2005,2006,2007,2008,2009,2010,2011,2012],\"params\":{},\"remark\":\"普通角色\",\"roleId\":2,\"roleKey\":\"CG_USER\",\"roleName\":\"采购方角色\",\"roleSort\":2,\"status\":\"0\",\"updateBy\":\"admin\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-13 18:08:40', 19);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (129, '用户管理', 3, 'com.ruoyi.web.controller.system.SysUserController.remove()', 'DELETE', 1, 'admin', '研发部门', '/system/user/100', '127.0.0.1', '内网IP', '[100] ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-13 18:14:50', 90);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (130, '菜单管理', 1, 'com.ruoyi.web.controller.system.SysMenuController.add()', 'POST', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{\"children\":[],\"createBy\":\"admin\",\"icon\":\"cascader\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuName\":\"产品管理\",\"menuType\":\"C\",\"orderNum\":20,\"params\":{},\"parentId\":2000,\"path\":\"product\",\"status\":\"0\",\"visible\":\"0\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-13 18:18:05', 18);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (131, '菜单管理', 2, 'com.ruoyi.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{\"children\":[],\"component\":\"nht/address/index\",\"createTime\":\"2025-12-13 17:33:36\",\"icon\":\"cascader\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2001,\"menuName\":\"收发地址\",\"menuType\":\"C\",\"orderNum\":1,\"params\":{},\"parentId\":2000,\"path\":\"address\",\"perms\":\"nht:address:list\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"0\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-13 18:19:49', 22);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (132, '角色管理', 2, 'com.ruoyi.web.controller.system.SysRoleController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/role', '127.0.0.1', '内网IP', '{\"admin\":false,\"createTime\":\"2025-12-09 17:04:37\",\"dataScope\":\"2\",\"delFlag\":\"0\",\"deptCheckStrictly\":true,\"flag\":false,\"menuCheckStrictly\":true,\"menuIds\":[2000,2001,2002,2003,2004,2005,2006,2015,2007,2008,2009,2010,2011,2012],\"params\":{},\"remark\":\"普通角色\",\"roleId\":2,\"roleKey\":\"CG_USER\",\"roleName\":\"采购方角色\",\"roleSort\":2,\"status\":\"0\",\"updateBy\":\"admin\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-13 18:22:31', 28);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (133, '角色管理', 2, 'com.ruoyi.web.controller.system.SysRoleController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/role', '127.0.0.1', '内网IP', '{\"admin\":false,\"createTime\":\"2025-12-13 18:08:35\",\"dataScope\":\"1\",\"delFlag\":\"0\",\"deptCheckStrictly\":true,\"flag\":false,\"menuCheckStrictly\":true,\"menuIds\":[2000,2001,2002,2003,2004,2005,2006,2015,2013,2014,2009,2010,2011,2012],\"params\":{},\"roleId\":100,\"roleKey\":\"GY_USER\",\"roleName\":\"供应方角色\",\"roleSort\":3,\"status\":\"0\",\"updateBy\":\"admin\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-13 18:22:36', 22);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (134, '代码生成', 6, 'com.ruoyi.generator.controller.GenController.importTableSave()', 'POST', 1, 'admin', '研发部门', '/tool/gen/importTable', '127.0.0.1', '内网IP', '{\"tables\":\"nht_product,nht_product_parameters\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-14 17:20:16', 169);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (135, '代码生成', 2, 'com.ruoyi.generator.controller.GenController.editSave()', 'PUT', 1, 'admin', '研发部门', '/tool/gen', '127.0.0.1', '内网IP', '{\"businessName\":\"product\",\"className\":\"NhtProduct\",\"columns\":[{\"capJavaField\":\"Id\",\"columnComment\":\"id\",\"columnId\":16,\"columnName\":\"id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2025-12-14 17:20:16\",\"dictType\":\"\",\"edit\":false,\"htmlType\":\"input\",\"increment\":true,\"insert\":true,\"isIncrement\":\"1\",\"isInsert\":\"1\",\"isPk\":\"1\",\"isRequired\":\"0\",\"javaField\":\"id\",\"javaType\":\"Long\",\"list\":false,\"params\":{},\"pk\":true,\"query\":false,\"queryType\":\"EQ\",\"required\":false,\"sort\":1,\"superColumn\":false,\"tableId\":2,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"UserId\",\"columnComment\":\"用户id\",\"columnId\":17,\"columnName\":\"user_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2025-12-14 17:20:16\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"0\",\"javaField\":\"userId\",\"javaType\":\"Long\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"EQ\",\"required\":false,\"sort\":2,\"superColumn\":false,\"tableId\":2,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"ParentId\",\"columnComment\":\"父id\",\"columnId\":18,\"columnName\":\"parent_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2025-12-14 17:20:16\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"0\",\"javaField\":\"parentId\",\"javaType\":\"Long\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"EQ\",\"required\":false,\"sort\":3,\"superColumn\":true,\"tableId\":2,\"updateBy\":\"\",\"usableColumn\":true},{\"capJavaField\":\"Ancestors\",\"columnComment\":\"祖级列表\",\"columnId\":19,\"columnName\":\"ancestors\",\"columnType\":\"varchar(50)\",\"createBy\":\"admin\",\"createTime\":\"2025-12-14 17:20:16\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"0\",\"javaField\":\"ancestors\",\"java', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-14 17:21:55', 94);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (136, '代码生成', 8, 'com.ruoyi.generator.controller.GenController.batchGenCode()', 'GET', 1, 'admin', '研发部门', '/tool/gen/batchGenCode', '127.0.0.1', '内网IP', '{\"tables\":\"nht_product,nht_product_parameters\"}', NULL, 0, NULL, '2025-12-14 17:22:05', 690);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (137, '代码生成', 2, 'com.ruoyi.generator.controller.GenController.editSave()', 'PUT', 1, 'admin', '研发部门', '/tool/gen', '127.0.0.1', '内网IP', '{\"businessName\":\"parameters\",\"className\":\"NhtProductParameters\",\"columns\":[{\"capJavaField\":\"Id\",\"columnComment\":\"id\",\"columnId\":27,\"columnName\":\"id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2025-12-14 17:20:16\",\"dictType\":\"\",\"edit\":false,\"htmlType\":\"input\",\"increment\":true,\"insert\":true,\"isIncrement\":\"1\",\"isInsert\":\"1\",\"isPk\":\"1\",\"isRequired\":\"0\",\"javaField\":\"id\",\"javaType\":\"Long\",\"list\":false,\"params\":{},\"pk\":true,\"query\":false,\"queryType\":\"EQ\",\"required\":false,\"sort\":1,\"superColumn\":false,\"tableId\":3,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"ProductId\",\"columnComment\":\"产品id\",\"columnId\":28,\"columnName\":\"product_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2025-12-14 17:20:16\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"1\",\"javaField\":\"productId\",\"javaType\":\"Long\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"EQ\",\"required\":true,\"sort\":2,\"superColumn\":false,\"tableId\":3,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"Options\",\"columnComment\":\"选择值\",\"columnId\":29,\"columnName\":\"options\",\"columnType\":\"varchar(255)\",\"createBy\":\"admin\",\"createTime\":\"2025-12-14 17:20:16\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"0\",\"javaField\":\"options\",\"javaType\":\"String\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"EQ\",\"required\":false,\"sort\":3,\"superColumn\":false,\"tableId\":3,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"ParamName\",\"columnComment\":\"参数名称\",\"columnId\":30,\"columnName\":\"param_name\",\"columnType\":\"varchar(50)\",\"createBy\":\"admin\",\"createTime\":\"2025-12-14 17:20:16\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"0\",\"', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-14 17:22:25', 42);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (138, '代码生成', 8, 'com.ruoyi.generator.controller.GenController.batchGenCode()', 'GET', 1, 'admin', '研发部门', '/tool/gen/batchGenCode', '127.0.0.1', '内网IP', '{\"tables\":\"nht_product,nht_product_parameters\"}', NULL, 0, NULL, '2025-12-14 17:22:41', 188);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (139, '菜单管理', 2, 'com.ruoyi.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{\"children\":[],\"component\":\"nht/product/index\",\"createTime\":\"2025-12-18 09:31:20\",\"icon\":\"cascader\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2016,\"menuName\":\"产品管理\",\"menuType\":\"C\",\"orderNum\":1,\"params\":{},\"parentId\":2000,\"path\":\"product\",\"perms\":\"nht:product:list\",\"routeName\":\"\",\"status\":\"0\",\"visible\":\"0\"} ', '{\"msg\":\"修改菜单\'产品管理\'失败，菜单名称已存在\",\"code\":500}', 0, NULL, '2025-12-18 09:31:51', 25);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (140, '菜单管理', 2, 'com.ruoyi.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{\"children\":[],\"component\":\"nht/product/index\",\"createTime\":\"2025-12-18 09:31:20\",\"icon\":\"cascader\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2016,\"menuName\":\"产品管理1\",\"menuType\":\"C\",\"orderNum\":1,\"params\":{},\"parentId\":2000,\"path\":\"product\",\"perms\":\"nht:product:list\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"0\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-18 09:31:55', 23);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (141, '菜单管理', 3, 'com.ruoyi.web.controller.system.SysMenuController.remove()', 'DELETE', 1, 'admin', '研发部门', '/system/menu/2015', '127.0.0.1', '内网IP', '2015 ', '{\"msg\":\"菜单已分配,不允许删除\",\"code\":601}', 0, NULL, '2025-12-18 09:31:59', 22);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (142, '角色管理', 2, 'com.ruoyi.web.controller.system.SysRoleController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/role', '127.0.0.1', '内网IP', '{\"admin\":false,\"createTime\":\"2025-12-09 17:04:37\",\"dataScope\":\"2\",\"delFlag\":\"0\",\"deptCheckStrictly\":true,\"flag\":false,\"menuCheckStrictly\":true,\"menuIds\":[2000,2001,2002,2003,2004,2005,2006,2016,2017,2018,2019,2020,2021,2007,2008,2009,2010,2011,2012],\"params\":{},\"remark\":\"普通角色\",\"roleId\":2,\"roleKey\":\"CG_USER\",\"roleName\":\"采购方角色\",\"roleSort\":2,\"status\":\"0\",\"updateBy\":\"admin\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-18 09:32:09', 94);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (143, '角色管理', 2, 'com.ruoyi.web.controller.system.SysRoleController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/role', '127.0.0.1', '内网IP', '{\"admin\":false,\"createTime\":\"2025-12-13 18:08:35\",\"dataScope\":\"1\",\"delFlag\":\"0\",\"deptCheckStrictly\":true,\"flag\":false,\"menuCheckStrictly\":true,\"menuIds\":[2000,2001,2002,2003,2004,2005,2006,2016,2017,2018,2019,2020,2021,2013,2014,2009,2010,2011,2012],\"params\":{},\"roleId\":100,\"roleKey\":\"GY_USER\",\"roleName\":\"供应方角色\",\"roleSort\":3,\"status\":\"0\",\"updateBy\":\"admin\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-18 09:32:17', 28);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (144, '菜单管理', 3, 'com.ruoyi.web.controller.system.SysMenuController.remove()', 'DELETE', 1, 'admin', '研发部门', '/system/menu/2015', '127.0.0.1', '内网IP', '2015 ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-18 09:32:23', 10);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (145, '菜单管理', 2, 'com.ruoyi.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{\"children\":[],\"component\":\"nht/product/index\",\"createTime\":\"2025-12-18 09:31:20\",\"icon\":\"cascader\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2016,\"menuName\":\"产品管理\",\"menuType\":\"C\",\"orderNum\":1,\"params\":{},\"parentId\":2000,\"path\":\"product\",\"perms\":\"nht:product:list\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"0\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-18 09:32:27', 10);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (146, '产品', 1, 'com.ruoyi.web.controller.nth.NhtProductController.add()', 'POST', 1, 'admin', '研发部门', '/nht/product', '127.0.0.1', '内网IP', '{\"children\":[],\"createTime\":\"2025-12-18 09:37:55\",\"id\":200,\"params\":{},\"parentId\":0,\"productName\":\"谷物及其加工产品\",\"status\":\"0\",\"userId\":1} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-18 09:37:55', 49);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (147, '产品', 1, 'com.ruoyi.web.controller.nth.NhtProductController.add()', 'POST', 1, 'admin', '研发部门', '/nht/product', '127.0.0.1', '内网IP', '{\"children\":[],\"createTime\":\"2025-12-18 09:38:10\",\"id\":201,\"params\":{},\"parentId\":0,\"productName\":\"油料籽实及其加工产品\",\"status\":\"0\",\"userId\":1} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-18 09:38:10', 4);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (148, '产品', 1, 'com.ruoyi.web.controller.nth.NhtProductController.add()', 'POST', 1, 'admin', '研发部门', '/nht/product', '127.0.0.1', '内网IP', '{\"children\":[],\"createTime\":\"2025-12-18 09:39:22\",\"id\":202,\"params\":{},\"parentId\":0,\"productName\":\"油脂类\",\"status\":\"0\",\"userId\":1} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-18 09:39:22', 29);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (149, '产品', 1, 'com.ruoyi.web.controller.nth.NhtProductController.add()', 'POST', 1, 'admin', '研发部门', '/nht/product', '127.0.0.1', '内网IP', '{\"children\":[],\"createTime\":\"2025-12-18 09:39:29\",\"id\":203,\"params\":{},\"parentId\":0,\"productName\":\"微生物发酵产品及副产品\",\"status\":\"0\",\"userId\":1} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-18 09:39:29', 13);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (150, '产品', 1, 'com.ruoyi.web.controller.nth.NhtProductController.add()', 'POST', 1, 'admin', '研发部门', '/nht/product', '127.0.0.1', '内网IP', '{\"children\":[],\"createTime\":\"2025-12-18 09:39:36\",\"id\":204,\"params\":{},\"parentId\":0,\"productName\":\"动物产品及其副产品\",\"status\":\"0\",\"userId\":1} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-18 09:39:36', 5);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (151, '产品', 1, 'com.ruoyi.web.controller.nth.NhtProductController.add()', 'POST', 1, 'admin', '研发部门', '/nht/product', '127.0.0.1', '内网IP', '{\"children\":[],\"createTime\":\"2025-12-18 09:39:43\",\"id\":205,\"params\":{},\"parentId\":0,\"productName\":\"矿物质\",\"status\":\"0\",\"userId\":1} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-18 09:39:43', 5);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (152, '产品', 1, 'com.ruoyi.web.controller.nth.NhtProductController.add()', 'POST', 1, 'admin', '研发部门', '/nht/product', '127.0.0.1', '内网IP', '{\"children\":[],\"createTime\":\"2025-12-18 09:40:04\",\"id\":206,\"params\":{},\"parentId\":200,\"productName\":\"玉米\",\"status\":\"0\",\"userId\":1} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-18 09:40:04', 7);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (153, '产品', 1, 'com.ruoyi.web.controller.nth.NhtProductController.add()', 'POST', 1, 'admin', '研发部门', '/nht/product', '127.0.0.1', '内网IP', '{\"children\":[],\"createTime\":\"2025-12-18 09:42:05\",\"id\":207,\"params\":{},\"parentId\":0,\"productName\":\"添加剂\",\"status\":\"0\",\"userId\":1} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-18 09:42:05', 17);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (154, '产品属性', 1, 'com.ruoyi.web.controller.nth.NhtProductParametersController.add()', 'POST', 1, 'admin', '研发部门', '/nht/parameters', '127.0.0.1', '内网IP', '{\"createTime\":\"2025-12-18 10:25:52\",\"id\":200,\"params\":{},\"productId\":206} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-18 10:25:52', 92);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (155, '产品属性', 3, 'com.ruoyi.web.controller.nth.NhtProductParametersController.remove()', 'DELETE', 1, 'admin', '研发部门', '/nht/parameters/200', '127.0.0.1', '内网IP', '[200] ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-18 10:25:56', 30);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (156, '产品属性', 1, 'com.ruoyi.web.controller.nth.NhtProductParametersController.add()', 'POST', 1, 'admin', '研发部门', '/nht/parameters', '127.0.0.1', '内网IP', '{\"createTime\":\"2025-12-18 10:35:35\",\"id\":201,\"options\":\"11,22,33\",\"paramName\":\"测试\",\"params\":{},\"productId\":206,\"required\":\"N\",\"sort\":1} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-18 10:35:35', 56);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (157, '产品属性', 2, 'com.ruoyi.web.controller.nth.NhtProductParametersController.edit()', 'PUT', 1, 'admin', '研发部门', '/nht/parameters', '127.0.0.1', '内网IP', '{\"createBy\":\"\",\"createTime\":\"2025-12-18 10:35:35\",\"delFlag\":\"0\",\"id\":201,\"options\":\"11,444,55,66,77\",\"paramName\":\"测试\",\"params\":{},\"productId\":206,\"required\":\"N\",\"sort\":1,\"updateBy\":\"\",\"updateTime\":\"2025-12-18 10:36:30\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-18 10:36:30', 25);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (158, '产品属性', 2, 'com.ruoyi.web.controller.nth.NhtProductParametersController.edit()', 'PUT', 1, 'admin', '研发部门', '/nht/parameters', '127.0.0.1', '内网IP', '{\"createBy\":\"\",\"createTime\":\"2025-12-18 10:35:35\",\"delFlag\":\"0\",\"id\":201,\"options\":\"11,444,66,77,22,33,44,555,666\",\"paramName\":\"测试\",\"params\":{},\"productId\":206,\"required\":\"N\",\"sort\":1,\"updateBy\":\"\",\"updateTime\":\"2025-12-18 10:38:03\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-18 10:38:03', 9);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (159, '产品属性', 2, 'com.ruoyi.web.controller.nth.NhtProductParametersController.edit()', 'PUT', 1, 'admin', '研发部门', '/nht/parameters', '127.0.0.1', '内网IP', '{\"createBy\":\"\",\"createTime\":\"2025-12-18 10:35:35\",\"delFlag\":\"0\",\"id\":201,\"options\":\"11,444,66,77,22,33,44,555,666,999,999999,77777,555555,77777777\",\"paramName\":\"测试\",\"params\":{},\"productId\":206,\"required\":\"N\",\"sort\":1,\"updateBy\":\"\",\"updateTime\":\"2025-12-18 10:38:28\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-18 10:38:28', 8);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (160, '产品属性', 2, 'com.ruoyi.web.controller.nth.NhtProductParametersController.edit()', 'PUT', 1, 'admin', '研发部门', '/nht/parameters', '127.0.0.1', '内网IP', '{\"createBy\":\"\",\"createTime\":\"2025-12-18 10:35:35\",\"delFlag\":\"0\",\"id\":201,\"options\":\"11,444,66,77,22,33,44,555,666,999,999999,77777,555555,77777777\",\"paramName\":\"测试\",\"params\":{},\"productId\":206,\"required\":\"Y\",\"sort\":1,\"updateBy\":\"\",\"updateTime\":\"2025-12-18 10:44:15\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-18 10:44:15', 16);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (161, '产品属性', 2, 'com.ruoyi.web.controller.nth.NhtProductParametersController.edit()', 'PUT', 1, 'admin', '研发部门', '/nht/parameters', '127.0.0.1', '内网IP', '{\"createBy\":\"\",\"createTime\":\"2025-12-18 10:35:35\",\"delFlag\":\"0\",\"id\":201,\"options\":\"11,444,66,77,22,33,44,555,666,999,999999,77777,555555,77777777\",\"paramName\":\"测试\",\"params\":{},\"productId\":206,\"required\":\"N\",\"sort\":1,\"updateBy\":\"\",\"updateTime\":\"2025-12-18 10:44:19\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-18 10:44:19', 7);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (162, '产品属性', 1, 'com.ruoyi.web.controller.nth.NhtProductParametersController.add()', 'POST', 1, 'admin', '研发部门', '/nht/parameters', '127.0.0.1', '内网IP', '{\"options\":\"ccc,ssss,csss\",\"paramName\":\"测试\",\"params\":{},\"productId\":206,\"required\":\"N\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-18 10:54:29', 199);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (163, '代码生成', 6, 'com.ruoyi.generator.controller.GenController.importTableSave()', 'POST', 1, 'admin', '研发部门', '/tool/gen/importTable', '127.0.0.1', '内网IP', '{\"tables\":\"nht_product_base_offer\"}', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-19 15:38:38', 158);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (164, '代码生成', 2, 'com.ruoyi.generator.controller.GenController.editSave()', 'PUT', 1, 'admin', '研发部门', '/tool/gen', '127.0.0.1', '内网IP', '{\"businessName\":\"bascOffer\",\"className\":\"NhtProductBaseOffer\",\"columns\":[{\"capJavaField\":\"Id\",\"columnComment\":\"id\",\"columnId\":39,\"columnName\":\"id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2025-12-19 15:38:38\",\"dictType\":\"\",\"edit\":false,\"htmlType\":\"input\",\"increment\":true,\"insert\":true,\"isIncrement\":\"1\",\"isInsert\":\"1\",\"isPk\":\"1\",\"isRequired\":\"0\",\"javaField\":\"id\",\"javaType\":\"Long\",\"list\":false,\"params\":{},\"pk\":true,\"query\":false,\"queryType\":\"EQ\",\"required\":false,\"sort\":1,\"superColumn\":false,\"tableId\":4,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"ProductId\",\"columnComment\":\"产品id\",\"columnId\":40,\"columnName\":\"product_id\",\"columnType\":\"bigint\",\"createBy\":\"admin\",\"createTime\":\"2025-12-19 15:38:38\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"1\",\"javaField\":\"productId\",\"javaType\":\"Long\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"EQ\",\"required\":true,\"sort\":2,\"superColumn\":false,\"tableId\":4,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"Year\",\"columnComment\":\"年份\",\"columnId\":41,\"columnName\":\"year\",\"columnType\":\"varchar(20)\",\"createBy\":\"admin\",\"createTime\":\"2025-12-19 15:38:38\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"0\",\"javaField\":\"year\",\"javaType\":\"String\",\"list\":true,\"params\":{},\"pk\":false,\"query\":true,\"queryType\":\"EQ\",\"required\":false,\"sort\":3,\"superColumn\":false,\"tableId\":4,\"updateBy\":\"\",\"usableColumn\":false},{\"capJavaField\":\"ContractName\",\"columnComment\":\"合约名称\",\"columnId\":42,\"columnName\":\"contract_name\",\"columnType\":\"varchar(255)\",\"createBy\":\"admin\",\"createTime\":\"2025-12-19 15:38:38\",\"dictType\":\"\",\"edit\":true,\"htmlType\":\"input\",\"increment\":false,\"insert\":true,\"isEdit\":\"1\",\"isIncrement\":\"0\",\"isInsert\":\"1\",\"isList\":\"1\",\"isPk\":\"0\",\"isQuery\":\"1\",\"isRequired\":\"0\",\"javaFi', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-19 15:39:18', 56);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (165, '代码生成', 8, 'com.ruoyi.generator.controller.GenController.batchGenCode()', 'GET', 1, 'admin', '研发部门', '/tool/gen/batchGenCode', '127.0.0.1', '内网IP', '{\"tables\":\"nht_product_base_offer\"}', NULL, 0, NULL, '2025-12-19 15:39:21', 550);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (166, '产品基础报价', 1, 'com.ruoyi.web.controller.nth.NhtProductBaseOfferController.add()', 'POST', 1, 'admin', '研发部门', '/nht/bascOffer', '127.0.0.1', '内网IP', '{\"basePrice\":1111,\"contractName\":\"2021\",\"createTime\":\"2025-12-19 15:49:27\",\"id\":202,\"params\":{},\"productId\":206} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-19 15:49:27', 187);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (167, '产品基础报价', 3, 'com.ruoyi.web.controller.nth.NhtProductBaseOfferController.remove()', 'DELETE', 1, 'admin', '研发部门', '/nht/bascOffer/202', '127.0.0.1', '内网IP', '[202] ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-19 15:49:52', 55);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (168, '产品属性', 2, 'com.ruoyi.web.controller.nth.NhtProductParametersController.edit()', 'PUT', 1, 'admin', '研发部门', '/nht/parameters', '127.0.0.1', '内网IP', '{\"createBy\":\"\",\"createTime\":\"2025-12-19 16:34:58\",\"delFlag\":\"0\",\"id\":13,\"options\":\"<14,<14.5,<15\",\"paramName\":\"水分\",\"paramType\":1,\"params\":{},\"productId\":4,\"required\":\"Y\",\"sort\":1,\"updateBy\":\"\",\"updateTime\":\"2025-12-19 20:22:37\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-19 20:22:37', 37);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (169, '产品属性', 2, 'com.ruoyi.web.controller.nth.NhtProductParametersController.edit()', 'PUT', 1, 'admin', '研发部门', '/nht/parameters', '127.0.0.1', '内网IP', '{\"createBy\":\"\",\"createTime\":\"2025-12-19 16:34:54\",\"delFlag\":\"0\",\"id\":1,\"options\":\"≥720,≥700,≥690,11\",\"paramName\":\"容重(g/L)\",\"paramType\":1,\"params\":{},\"productId\":2,\"required\":\"Y\",\"sort\":1,\"updateBy\":\"\",\"updateTime\":\"2025-12-19 20:25:48\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-19 20:25:48', 13);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (170, '产品属性', 2, 'com.ruoyi.web.controller.nth.NhtProductParametersController.edit()', 'PUT', 1, 'admin', '研发部门', '/nht/parameters', '127.0.0.1', '内网IP', '{\"createBy\":\"\",\"createTime\":\"2025-12-19 16:34:54\",\"delFlag\":\"0\",\"id\":1,\"options\":\"≥720,≥700,≥690\",\"paramName\":\"容重(g/L)\",\"paramType\":1,\"params\":{},\"productId\":2,\"required\":\"Y\",\"sort\":1,\"updateBy\":\"\",\"updateTime\":\"2025-12-19 20:25:54\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-19 20:25:54', 8);
INSERT INTO `sys_oper_log` (`oper_id`, `title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `dept_name`, `oper_url`, `oper_ip`, `oper_location`, `oper_param`, `json_result`, `status`, `error_msg`, `oper_time`, `cost_time`) VALUES (171, '菜单管理', 2, 'com.ruoyi.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{\"children\":[],\"component\":\"nht/product/index\",\"createTime\":\"2025-12-18 09:31:20\",\"icon\":\"cascader\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2016,\"menuName\":\"产品分类\",\"menuType\":\"C\",\"orderNum\":1,\"params\":{},\"parentId\":2000,\"path\":\"product\",\"perms\":\"nht:product:list\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"0\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2025-12-19 20:43:23', 39);
COMMIT;

-- ----------------------------
-- Table structure for sys_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_post`;
CREATE TABLE `sys_post` (
  `post_id` bigint NOT NULL AUTO_INCREMENT COMMENT '岗位ID',
  `post_code` varchar(64) COLLATE utf8mb4_general_ci NOT NULL COMMENT '岗位编码',
  `post_name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '岗位名称',
  `post_sort` int NOT NULL COMMENT '显示顺序',
  `status` char(1) COLLATE utf8mb4_general_ci NOT NULL COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`post_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='岗位信息表';

-- ----------------------------
-- Records of sys_post
-- ----------------------------
BEGIN;
INSERT INTO `sys_post` (`post_id`, `post_code`, `post_name`, `post_sort`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1, 'ceo', '董事长', 1, '0', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_post` (`post_id`, `post_code`, `post_name`, `post_sort`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2, 'se', '项目经理', 2, '0', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_post` (`post_id`, `post_code`, `post_name`, `post_sort`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3, 'hr', '人力资源', 3, '0', 'admin', '2025-12-09 17:04:37', '', NULL, '');
INSERT INTO `sys_post` (`post_id`, `post_code`, `post_name`, `post_sort`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4, 'user', '普通员工', 4, '0', 'admin', '2025-12-09 17:04:37', '', NULL, '');
COMMIT;

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `role_id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(30) COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色名称',
  `role_key` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色权限字符串',
  `role_sort` int NOT NULL COMMENT '显示顺序',
  `data_scope` char(1) COLLATE utf8mb4_general_ci DEFAULT '1' COMMENT '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）',
  `menu_check_strictly` tinyint(1) DEFAULT '1' COMMENT '菜单树选择项是否关联显示',
  `dept_check_strictly` tinyint(1) DEFAULT '1' COMMENT '部门树选择项是否关联显示',
  `status` char(1) COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色状态（0正常 1停用）',
  `del_flag` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=101 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色信息表';

-- ----------------------------
-- Records of sys_role
-- ----------------------------
BEGIN;
INSERT INTO `sys_role` (`role_id`, `role_name`, `role_key`, `role_sort`, `data_scope`, `menu_check_strictly`, `dept_check_strictly`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1, '超级管理员', 'admin', 1, '1', 1, 1, '0', '0', 'admin', '2025-12-09 17:04:37', '', NULL, '超级管理员');
INSERT INTO `sys_role` (`role_id`, `role_name`, `role_key`, `role_sort`, `data_scope`, `menu_check_strictly`, `dept_check_strictly`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2, '采购方角色', 'CG_USER', 2, '2', 1, 1, '0', '0', 'admin', '2025-12-09 17:04:37', 'admin', '2025-12-18 09:32:09', '普通角色');
INSERT INTO `sys_role` (`role_id`, `role_name`, `role_key`, `role_sort`, `data_scope`, `menu_check_strictly`, `dept_check_strictly`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (100, '供应方角色', 'GY_USER', 3, '1', 1, 1, '0', '0', 'admin', '2025-12-13 18:08:35', 'admin', '2025-12-18 09:32:17', NULL);
COMMIT;

-- ----------------------------
-- Table structure for sys_role_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_dept`;
CREATE TABLE `sys_role_dept` (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `dept_id` bigint NOT NULL COMMENT '部门ID',
  PRIMARY KEY (`role_id`,`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色和部门关联表';

-- ----------------------------
-- Records of sys_role_dept
-- ----------------------------
BEGIN;
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`) VALUES (2, 100);
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`) VALUES (2, 101);
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`) VALUES (2, 105);
COMMIT;

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`role_id`,`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色和菜单关联表';

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
BEGIN;
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 2000);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 2001);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 2002);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 2003);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 2004);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 2005);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 2006);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 2007);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 2008);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 2009);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 2010);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 2011);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 2012);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 2016);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 2017);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 2018);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 2019);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 2020);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 2021);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (100, 2000);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (100, 2001);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (100, 2002);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (100, 2003);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (100, 2004);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (100, 2005);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (100, 2006);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (100, 2009);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (100, 2010);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (100, 2011);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (100, 2012);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (100, 2013);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (100, 2014);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (100, 2016);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (100, 2017);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (100, 2018);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (100, 2019);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (100, 2020);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (100, 2021);
COMMIT;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `dept_id` bigint DEFAULT NULL COMMENT '部门ID',
  `user_name` varchar(30) COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户账号',
  `nick_name` varchar(30) COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户昵称',
  `user_type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT 'SYS_USER' COMMENT '用户类型（SYS_USER系统用户  CG_USER 采购方  GY_USER供应方）',
  `email` varchar(50) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '用户邮箱',
  `phonenumber` varchar(11) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '手机号码',
  `sex` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '用户性别（0男 1女 2未知）',
  `avatar` varchar(100) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '头像地址',
  `password` varchar(100) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '密码',
  `status` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '账号状态（0正常 1停用）',
  `del_flag` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `login_ip` varchar(128) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '最后登录IP',
  `login_date` datetime DEFAULT NULL COMMENT '最后登录时间',
  `pwd_update_date` datetime DEFAULT NULL COMMENT '密码最后更新时间',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=103 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户信息表';

-- ----------------------------
-- Records of sys_user
-- ----------------------------
BEGIN;
INSERT INTO `sys_user` (`user_id`, `dept_id`, `user_name`, `nick_name`, `user_type`, `email`, `phonenumber`, `sex`, `avatar`, `password`, `status`, `del_flag`, `login_ip`, `login_date`, `pwd_update_date`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1, 103, 'admin', '若依', 'SYS_USER', 'ry@163.com', '15888888888', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', '2025-12-19 20:34:25', '2025-12-09 17:04:37', 'admin', '2025-12-09 17:04:37', '', NULL, '管理员');
INSERT INTO `sys_user` (`user_id`, `dept_id`, `user_name`, `nick_name`, `user_type`, `email`, `phonenumber`, `sex`, `avatar`, `password`, `status`, `del_flag`, `login_ip`, `login_date`, `pwd_update_date`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2, 105, 'ry', '若依', 'SYS_USER', 'ry@qq.com', '15666666666', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', '2025-12-09 17:04:37', '2025-12-09 17:04:37', 'admin', '2025-12-09 17:04:37', '', NULL, '测试员');
INSERT INTO `sys_user` (`user_id`, `dept_id`, `user_name`, `nick_name`, `user_type`, `email`, `phonenumber`, `sex`, `avatar`, `password`, `status`, `del_flag`, `login_ip`, `login_date`, `pwd_update_date`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (100, NULL, '17638510561', '测试公司', 'CG_USER', '', '17638510561', '0', '', '$2a$10$VgfR3gC7RJ.BmEyopghkIOwNTujetXugI2IJTSgj/tO.6fVFwCwrq', '0', '2', '127.0.0.1', '2025-12-13 17:55:14', '2025-12-13 17:55:02', '', '2025-12-13 17:55:01', '', NULL, NULL);
INSERT INTO `sys_user` (`user_id`, `dept_id`, `user_name`, `nick_name`, `user_type`, `email`, `phonenumber`, `sex`, `avatar`, `password`, `status`, `del_flag`, `login_ip`, `login_date`, `pwd_update_date`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (101, NULL, '17638510561', '测试公司', 'CG_USER', '', '17638510561', '0', '', '$2a$10$ocWd7L7eKqQb0/uWJsD0SekCASVFb/LFMEou6Yho3kLJgn/yIxakK', '0', '0', '127.0.0.1', '2025-12-13 20:39:52', '2025-12-13 18:15:24', '', '2025-12-13 18:15:24', '', NULL, NULL);
INSERT INTO `sys_user` (`user_id`, `dept_id`, `user_name`, `nick_name`, `user_type`, `email`, `phonenumber`, `sex`, `avatar`, `password`, `status`, `del_flag`, `login_ip`, `login_date`, `pwd_update_date`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (102, NULL, '17638510562', '测试公司', 'GY_USER', '', '17638510562', '0', '', '$2a$10$IMd2FpLdaacotjAwIUeJXuTsfPw.C.Yoqw3aSxxU5dd.OdI0GiYce', '0', '0', '127.0.0.1', '2025-12-13 18:20:56', '2025-12-13 18:20:47', '', '2025-12-13 18:20:47', '', NULL, NULL);
COMMIT;

-- ----------------------------
-- Table structure for sys_user_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_post`;
CREATE TABLE `sys_user_post` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `post_id` bigint NOT NULL COMMENT '岗位ID',
  PRIMARY KEY (`user_id`,`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户与岗位关联表';

-- ----------------------------
-- Records of sys_user_post
-- ----------------------------
BEGIN;
INSERT INTO `sys_user_post` (`user_id`, `post_id`) VALUES (1, 1);
INSERT INTO `sys_user_post` (`user_id`, `post_id`) VALUES (2, 2);
COMMIT;

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户和角色关联表';

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
BEGIN;
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 1);
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (2, 2);
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (101, 2);
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (102, 100);
COMMIT;

-- ============================================================
-- AgriMatch (农汇通) - Core Tables (追加，不影响现有 Ruoyi 表)
-- 说明：
-- 1) 本库已存在 Ruoyi 的 `sys_user`，这里通过 ALTER 增补农汇通所需字段（不重建、不覆盖）。
-- 2) 业务核心表统一采用：`is_deleted`(逻辑删除)、`create_time`、`update_time`。
-- ============================================================

-- ----------------------------
-- Extend Table: sys_user (增补字段)
-- ----------------------------
-- 添加字段（如果字段已存在会报错，但可以忽略继续执行）
ALTER TABLE `sys_user`
  ADD COLUMN `wechat` varchar(50) DEFAULT NULL COMMENT '微信号',
  ADD COLUMN `company_id` bigint DEFAULT NULL COMMENT '公司ID（关联 bus_company.id）',
  ADD COLUMN `is_buyer` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否采购商（0否 1是）',
  ADD COLUMN `is_seller` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否供应商（0否 1是）',
  ADD COLUMN `pay_info_json` json DEFAULT NULL COMMENT '收付款信息（银行卡/三方账户）JSON',
  ADD COLUMN `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除（0否 1是）';

-- ----------------------------
-- Table structure for bus_company
-- ----------------------------
DROP TABLE IF EXISTS `bus_company`;
CREATE TABLE `bus_company` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '公司ID',
  `company_name` varchar(128) NOT NULL COMMENT '公司名称',
  `license_no` varchar(64) DEFAULT NULL COMMENT '营业执照号/统一社会信用代码',
  `license_img_url` varchar(255) DEFAULT NULL COMMENT '营业执照/资质图片URL',
  `contacts` varchar(50) DEFAULT NULL COMMENT '联系人',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `wechat` varchar(50) DEFAULT NULL COMMENT '微信号',
  `province` varchar(50) DEFAULT NULL COMMENT '省',
  `city` varchar(50) DEFAULT NULL COMMENT '市',
  `district` varchar(50) DEFAULT NULL COMMENT '区县',
  `address` varchar(255) DEFAULT NULL COMMENT '公司地址',
  `lat` decimal(10,7) DEFAULT NULL COMMENT '公司默认纬度',
  `lng` decimal(10,7) DEFAULT NULL COMMENT '公司默认经度',
  `locations_json` json DEFAULT NULL COMMENT '多发货点/多收货点坐标JSON',
  `bank_info_json` json DEFAULT NULL COMMENT '公司收付款信息JSON（可选）',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除（0否 1是）',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_bus_company_name` (`company_name`),
  KEY `idx_bus_company_phone` (`phone`),
  KEY `idx_bus_company_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='公司资质及位置坐标表';

-- ----------------------------
-- Table structure for bus_requirement
-- ----------------------------
DROP TABLE IF EXISTS `bus_requirement`;
CREATE TABLE `bus_requirement` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '需求ID',
  `company_id` bigint NOT NULL COMMENT '采购方公司ID（bus_company.id）',
  `user_id` bigint NOT NULL COMMENT '发布用户ID（sys_user.user_id）',
  `category_name` varchar(64) NOT NULL COMMENT '品类名称（支持自定义）',
  `quantity` decimal(18,3) DEFAULT NULL COMMENT '数量（吨/件等，单位由 params_json 说明）',
  `packaging` varchar(20) DEFAULT NULL COMMENT '包装（散装/袋装等）',
  `payment_method` varchar(20) DEFAULT NULL COMMENT '付款方式（现款/账期等）',
  `params_json` json DEFAULT NULL COMMENT '动态属性JSON（指标/模板字段等）',
  `purchase_lat` decimal(10,7) DEFAULT NULL COMMENT '采购点纬度',
  `purchase_lng` decimal(10,7) DEFAULT NULL COMMENT '采购点经度',
  `purchase_address` varchar(255) DEFAULT NULL COMMENT '采购点地址',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0发布 1成交 2下架）',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除（0否 1是）',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_req_company` (`company_id`),
  KEY `idx_req_user` (`user_id`),
  KEY `idx_req_category` (`category_name`),
  KEY `idx_req_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='采购需求表';

-- ----------------------------
-- Table structure for bus_supply
-- ----------------------------
DROP TABLE IF EXISTS `bus_supply`;
CREATE TABLE `bus_supply` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '供应ID',
  `company_id` bigint NOT NULL COMMENT '供应方公司ID（bus_company.id）',
  `user_id` bigint NOT NULL COMMENT '发布用户ID（sys_user.user_id）',
  `category_name` varchar(64) NOT NULL COMMENT '品类名称',
  `ex_factory_price` decimal(18,2) NOT NULL COMMENT '出厂价',
  `ship_lat` decimal(10,7) DEFAULT NULL COMMENT '发货点纬度',
  `ship_lng` decimal(10,7) DEFAULT NULL COMMENT '发货点经度',
  `ship_address` varchar(255) DEFAULT NULL COMMENT '发货点地址',
  `delivery_mode` varchar(20) DEFAULT NULL COMMENT '交付方式（自提/到厂等）',
  `price_rules_json` json DEFAULT NULL COMMENT '多组价格/交付规则JSON',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除（0否 1是）',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_supply_company` (`company_id`),
  KEY `idx_supply_user` (`user_id`),
  KEY `idx_supply_category` (`category_name`),
  KEY `idx_supply_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='供应发布表';

-- ----------------------------
-- Table structure for bus_contract_template
-- ----------------------------
DROP TABLE IF EXISTS `bus_contract_template`;
CREATE TABLE `bus_contract_template` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `owner_user_id` bigint NOT NULL COMMENT '所属用户ID（sys_user.user_id）',
  `owner_company_id` bigint DEFAULT NULL COMMENT '所属公司ID（bus_company.id）',
  `product_type` varchar(64) NOT NULL COMMENT '产品类型（豆粕/钢材等）',
  `template_name` varchar(128) NOT NULL COMMENT '模板名称',
  `template_content` longtext COMMENT '模板内容（可存HTML/占位符文本）',
  `file_url` varchar(255) DEFAULT NULL COMMENT '模板文件URL（可选）',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除（0否 1是）',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_tpl_owner_user` (`owner_user_id`),
  KEY `idx_tpl_product_type` (`product_type`),
  KEY `idx_tpl_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='合同模板表';

-- ----------------------------
-- Table structure for bus_order_eval
-- ----------------------------
DROP TABLE IF EXISTS `bus_order_eval`;
CREATE TABLE `bus_order_eval` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `requirement_id` bigint DEFAULT NULL COMMENT '需求ID（bus_requirement.id）',
  `supply_id` bigint DEFAULT NULL COMMENT '供应ID（bus_supply.id）',
  `from_user_id` bigint NOT NULL COMMENT '评价人用户ID（sys_user.user_id）',
  `to_company_id` bigint NOT NULL COMMENT '被评价公司ID（bus_company.id）',
  `stars` tinyint NOT NULL COMMENT '星级（1-5）',
  `comment` varchar(500) DEFAULT NULL COMMENT '文字评价',
  `images_json` json DEFAULT NULL COMMENT '图片/附件JSON（可选）',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除（0否 1是）',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_eval_to_company` (`to_company_id`),
  KEY `idx_eval_from_user` (`from_user_id`),
  KEY `idx_eval_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='评价记录表';

SET FOREIGN_KEY_CHECKS = 1;
