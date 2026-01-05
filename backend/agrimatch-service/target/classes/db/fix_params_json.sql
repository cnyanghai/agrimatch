-- 修复旧格式 params_json 数据，添加参数名称
-- 执行前请先备份数据

-- ==================== 供应表 (bus_supply) ====================

-- 更新 id=1 的玉米数据（旧格式 -> 新格式）
UPDATE bus_supply 
SET params_json = JSON_SET(
    params_json,
    '$.params', JSON_OBJECT(
        '1', JSON_OBJECT('name', '容重(g/L)', 'value', JSON_UNQUOTE(JSON_EXTRACT(params_json, '$.params."1"'))),
        '2', JSON_OBJECT('name', '水分', 'value', JSON_UNQUOTE(JSON_EXTRACT(params_json, '$.params."2"'))),
        '3', JSON_OBJECT('name', '不完善粒', 'value', JSON_UNQUOTE(JSON_EXTRACT(params_json, '$.params."3"'))),
        '4', JSON_OBJECT('name', '生霉粒', 'value', JSON_UNQUOTE(JSON_EXTRACT(params_json, '$.params."4"')))
    )
)
WHERE id = 1;

-- ==================== 需求表 (bus_requirement) ====================

-- 更新 id=1 的特殊格式数据（直接键值对 -> 新格式）
UPDATE bus_requirement 
SET params_json = '{"params":{"1":{"name":"水分","value":"≤14%"},"2":{"name":"霉变","value":"≤2%"}},"custom":{}}'
WHERE id = 1;

-- 更新 id=2 的玉米数据
UPDATE bus_requirement 
SET params_json = JSON_SET(
    params_json,
    '$.params', JSON_OBJECT(
        '1', JSON_OBJECT('name', '容重(g/L)', 'value', JSON_UNQUOTE(JSON_EXTRACT(params_json, '$.params."1"'))),
        '2', JSON_OBJECT('name', '水分', 'value', JSON_UNQUOTE(JSON_EXTRACT(params_json, '$.params."2"'))),
        '3', JSON_OBJECT('name', '不完善粒', 'value', JSON_UNQUOTE(JSON_EXTRACT(params_json, '$.params."3"'))),
        '4', JSON_OBJECT('name', '生霉粒', 'value', JSON_UNQUOTE(JSON_EXTRACT(params_json, '$.params."4"'))),
        '5', JSON_OBJECT('name', '粗蛋白', 'value', JSON_UNQUOTE(JSON_EXTRACT(params_json, '$.params."5"'))),
        '6', JSON_OBJECT('name', '杂质', 'value', JSON_UNQUOTE(JSON_EXTRACT(params_json, '$.params."6"')))
    )
)
WHERE id = 2;

-- 更新 id=4 的玉米数据
UPDATE bus_requirement 
SET params_json = JSON_SET(
    params_json,
    '$.params', JSON_OBJECT(
        '1', JSON_OBJECT('name', '容重(g/L)', 'value', JSON_UNQUOTE(JSON_EXTRACT(params_json, '$.params."1"'))),
        '2', JSON_OBJECT('name', '水分', 'value', JSON_UNQUOTE(JSON_EXTRACT(params_json, '$.params."2"'))),
        '3', JSON_OBJECT('name', '不完善粒', 'value', JSON_UNQUOTE(JSON_EXTRACT(params_json, '$.params."3"'))),
        '4', JSON_OBJECT('name', '生霉粒', 'value', JSON_UNQUOTE(JSON_EXTRACT(params_json, '$.params."4"'))),
        '5', JSON_OBJECT('name', '粗蛋白', 'value', JSON_UNQUOTE(JSON_EXTRACT(params_json, '$.params."5"'))),
        '6', JSON_OBJECT('name', '杂质', 'value', JSON_UNQUOTE(JSON_EXTRACT(params_json, '$.params."6"')))
    )
)
WHERE id = 4;

-- 更新 id=5 的玉米数据
UPDATE bus_requirement 
SET params_json = JSON_SET(
    params_json,
    '$.params', JSON_OBJECT(
        '1', JSON_OBJECT('name', '容重(g/L)', 'value', JSON_UNQUOTE(JSON_EXTRACT(params_json, '$.params."1"'))),
        '2', JSON_OBJECT('name', '水分', 'value', JSON_UNQUOTE(JSON_EXTRACT(params_json, '$.params."2"'))),
        '3', JSON_OBJECT('name', '不完善粒', 'value', JSON_UNQUOTE(JSON_EXTRACT(params_json, '$.params."3"'))),
        '4', JSON_OBJECT('name', '生霉粒', 'value', JSON_UNQUOTE(JSON_EXTRACT(params_json, '$.params."4"'))),
        '5', JSON_OBJECT('name', '粗蛋白', 'value', JSON_UNQUOTE(JSON_EXTRACT(params_json, '$.params."5"'))),
        '6', JSON_OBJECT('name', '杂质', 'value', JSON_UNQUOTE(JSON_EXTRACT(params_json, '$.params."6"')))
    )
)
WHERE id = 5;

-- 更新 id=6 的玉米数据
UPDATE bus_requirement 
SET params_json = JSON_SET(
    params_json,
    '$.params', JSON_OBJECT(
        '1', JSON_OBJECT('name', '容重(g/L)', 'value', JSON_UNQUOTE(JSON_EXTRACT(params_json, '$.params."1"'))),
        '2', JSON_OBJECT('name', '水分', 'value', JSON_UNQUOTE(JSON_EXTRACT(params_json, '$.params."2"'))),
        '3', JSON_OBJECT('name', '不完善粒', 'value', JSON_UNQUOTE(JSON_EXTRACT(params_json, '$.params."3"'))),
        '4', JSON_OBJECT('name', '生霉粒', 'value', JSON_UNQUOTE(JSON_EXTRACT(params_json, '$.params."4"'))),
        '5', JSON_OBJECT('name', '粗蛋白', 'value', JSON_UNQUOTE(JSON_EXTRACT(params_json, '$.params."5"'))),
        '6', JSON_OBJECT('name', '杂质', 'value', JSON_UNQUOTE(JSON_EXTRACT(params_json, '$.params."6"')))
    )
)
WHERE id = 6;

-- 更新 id=7 的豆粕数据
UPDATE bus_requirement 
SET params_json = JSON_SET(
    params_json,
    '$.params', JSON_OBJECT(
        '76', JSON_OBJECT('name', '水分', 'value', JSON_UNQUOTE(JSON_EXTRACT(params_json, '$.params."76"'))),
        '77', JSON_OBJECT('name', '粗蛋白', 'value', JSON_UNQUOTE(JSON_EXTRACT(params_json, '$.params."77"'))),
        '78', JSON_OBJECT('name', '粗灰分', 'value', JSON_UNQUOTE(JSON_EXTRACT(params_json, '$.params."78"'))),
        '79', JSON_OBJECT('name', 'KOH溶解度', 'value', JSON_UNQUOTE(JSON_EXTRACT(params_json, '$.params."79"')))
    )
)
WHERE id = 7;

-- 更新 id=8 的玉米数据
UPDATE bus_requirement 
SET params_json = JSON_SET(
    params_json,
    '$.params', JSON_OBJECT(
        '1', JSON_OBJECT('name', '容重(g/L)', 'value', JSON_UNQUOTE(JSON_EXTRACT(params_json, '$.params."1"'))),
        '2', JSON_OBJECT('name', '水分', 'value', JSON_UNQUOTE(JSON_EXTRACT(params_json, '$.params."2"'))),
        '3', JSON_OBJECT('name', '不完善粒', 'value', JSON_UNQUOTE(JSON_EXTRACT(params_json, '$.params."3"'))),
        '4', JSON_OBJECT('name', '生霉粒', 'value', JSON_UNQUOTE(JSON_EXTRACT(params_json, '$.params."4"'))),
        '5', JSON_OBJECT('name', '粗蛋白', 'value', JSON_UNQUOTE(JSON_EXTRACT(params_json, '$.params."5"'))),
        '6', JSON_OBJECT('name', '杂质', 'value', JSON_UNQUOTE(JSON_EXTRACT(params_json, '$.params."6"')))
    )
)
WHERE id = 8;

-- ==================== 验证结果 ====================
-- SELECT id, category_name, params_json FROM bus_supply WHERE id = 1;
-- SELECT id, category_name, params_json FROM bus_requirement WHERE id IN (1,2,4,5,6,7,8);

