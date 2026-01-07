package com.agrimatch.common.migration;

import com.agrimatch.chat.mapper.ChatMapper;
import com.agrimatch.contract.mapper.ContractMapper;
import com.agrimatch.supply.mapper.SupplyMapper;
import com.agrimatch.requirement.mapper.RequirementMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 参数 JSON 格式标准化迁移工具
 * 将各种格式的 params_json 统一为 {"参数名": "参数值"} 的扁平结构
 */
@Component
public class ParamsMigrationRunner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(ParamsMigrationRunner.class);

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public ParamsMigrationRunner(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("[Migration] 开始执行参数 JSON 标准化迁移...");
        
        // 1. 加载参数 ID 到名称的映射
        Map<String, String> idToName = loadParamIdToNameMap();
        log.info("[Migration] 已加载 {} 条参数映射关系", idToName.size());

        // 2. 迁移供应表
        migrateTable("bus_supply", "params_json", idToName);

        // 3. 迁移需求表
        migrateTable("bus_requirement", "params_json", idToName);

        // 4. 迁移合同表
        migrateTable("bus_contract", "params_json", idToName);

        // 5. 迁移聊天消息表（针对报价消息中的 dynamicParams）
        migrateChatMessages(idToName);

        log.info("[Migration] 参数 JSON 标准化迁移完成。");
    }

    private void migrateChatMessages(Map<String, String> idToName) {
        String sql = "SELECT id, payload_json FROM bus_chat_message WHERE msg_type = 'QUOTE' AND payload_json LIKE '%dynamicParams%'";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        int count = 0;

        for (Map<String, Object> row : rows) {
            Long id = ((Number) row.get("id")).longValue();
            String payloadJson = (String) row.get("payload_json");
            
            try {
                JsonNode root = objectMapper.readTree(payloadJson);
                if (root.has("fields") && root.get("fields").has("dynamicParams")) {
                    JsonNode dynamicParams = root.get("fields").get("dynamicParams");
                    String standardizedParams = standardize(objectMapper.writeValueAsString(dynamicParams), idToName);
                    
                    if (standardizedParams != null) {
                        ((ObjectNode) root.get("fields")).set("dynamicParams", objectMapper.readTree(standardizedParams));
                        jdbcTemplate.update("UPDATE bus_chat_message SET payload_json = ? WHERE id = ?", objectMapper.writeValueAsString(root), id);
                        count++;
                    }
                } else if (root.has("dynamicParams")) {
                    // 某些版本可能直接在根部
                    JsonNode dynamicParams = root.get("dynamicParams");
                    String standardizedParams = standardize(objectMapper.writeValueAsString(dynamicParams), idToName);
                    if (standardizedParams != null) {
                        ((ObjectNode) root).set("dynamicParams", objectMapper.readTree(standardizedParams));
                        jdbcTemplate.update("UPDATE bus_chat_message SET payload_json = ? WHERE id = ?", objectMapper.writeValueAsString(root), id);
                        count++;
                    }
                }
            } catch (Exception e) {
                log.error("[Migration] 迁移聊天消息 ID {} 失败: {}", id, e.getMessage());
            }
        }
        log.info("[Migration] 聊天消息迁移完成，更新 {} 条报价记录", count);
    }

    private Map<String, String> loadParamIdToNameMap() {
        Map<String, String> map = new HashMap<>();
        String sql = "SELECT id, param_name FROM nht_product_parameters";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        for (Map<String, Object> row : rows) {
            map.put(row.get("id").toString(), (String) row.get("param_name"));
        }
        return map;
    }

    private void migrateTable(String tableName, String columnName, Map<String, String> idToName) {
        String sql = "SELECT id, " + columnName + " FROM " + tableName + " WHERE " + columnName + " IS NOT NULL AND " + columnName + " != ''";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        int count = 0;

        for (Map<String, Object> row : rows) {
            Long id = ((Number) row.get("id")).longValue();
            String rawJson = (String) row.get(columnName);
            
            try {
                String standardized = standardize(rawJson, idToName);
                if (standardized != null && !standardized.equals(rawJson)) {
                    jdbcTemplate.update("UPDATE " + tableName + " SET " + columnName + " = ? WHERE id = ?", standardized, id);
                    count++;
                }
            } catch (Exception e) {
                log.error("[Migration] 迁移表 {} ID {} 失败: {}", tableName, id, e.getMessage());
            }
        }
        log.info("[Migration] 表 {} 迁移完成，更新 {} 条记录", tableName, count);
    }

    private String standardize(String rawJson, Map<String, String> idToName) throws Exception {
        if (rawJson == null || rawJson.trim().isEmpty() || rawJson.equals("{}")) return rawJson;

        JsonNode root = objectMapper.readTree(rawJson);
        ObjectNode result = objectMapper.createObjectNode();

        // 情况 1: {"params": {"1": {"name": "...", "value": "..."}, ...}, "custom": {...}}
        if (root.has("params") && root.get("params").isObject()) {
            JsonNode params = root.get("params");
            params.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                JsonNode val = entry.getValue();
                if (val.isObject() && val.has("name") && val.has("value")) {
                    result.put(val.get("name").asText(), val.get("value").asText());
                } else if (val.isTextual()) {
                    String name = idToName.getOrDefault(key, key);
                    result.put(name, val.asText());
                }
            });
            // 处理 custom
            if (root.has("custom") && root.get("custom").isObject()) {
                root.get("custom").fields().forEachRemaining(entry -> {
                    result.put(entry.getKey(), entry.getValue().asText());
                });
            }
        } 
        // 情况 2: 直接是 {"1": "值", "2": "值"}
        else if (root.isObject()) {
            // 检查是否包含嵌套的 params 或 custom，如果不包含，则认为是扁平的 ID-Value 或 Name-Value
            boolean hasNested = root.has("params") || root.has("custom");
            if (!hasNested) {
                root.fields().forEachRemaining(entry -> {
                    String key = entry.getKey();
                    String name = idToName.getOrDefault(key, key);
                    result.put(name, entry.getValue().asText());
                });
            } else {
                // 如果有嵌套但没走上面的逻辑，说明结构可能更复杂，或者是我们已经处理过的结构的一部分
                // 这里我们递归处理或者提取有用的部分
                if (root.has("params") && root.get("params").isObject()) {
                    return standardize(objectMapper.writeValueAsString(root), idToName);
                }
            }
        }
        // 情况 3: 数组格式 [{"label": "...", "value": "..."}, ...]
        else if (root.isArray()) {
            for (JsonNode item : root) {
                if (item.has("label") && item.has("value")) {
                    result.put(item.get("label").asText(), item.get("value").asText());
                } else if (item.has("name") && item.has("value")) {
                    result.put(item.get("name").asText(), item.get("value").asText());
                }
            }
        }

        if (result.isEmpty()) return rawJson;
        return objectMapper.writeValueAsString(result);
    }
}

