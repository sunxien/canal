package com.alibaba.otter.canal.client.adapter.rdb.support;

import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.DbType;
import com.alibaba.otter.canal.client.adapter.rdb.config.MappingConfig;
import com.alibaba.otter.canal.client.adapter.support.Util;

public class SyncUtil {
    private static final Logger logger  = LoggerFactory.getLogger(SyncUtil.class);

    public static Map<String, String> getColumnsMap(MappingConfig.DbMapping dbMapping, Map<String, Object> data) {
        return getColumnsMap(dbMapping, data.keySet());
    }

    public static Map<String, String> getColumnsMap(MappingConfig.DbMapping dbMapping, Collection<String> columns) {
        Map<String, String> columnsMap;
        if (dbMapping.getMapAll()) {
            if (dbMapping.getAllMapColumns() != null) {
                return dbMapping.getAllMapColumns();
            }
            columnsMap = new LinkedHashMap<>();
            for (String srcColumn : columns) {
                boolean flag = true;
                if (dbMapping.getTargetColumns() != null) {
                    for (Map.Entry<String, String> entry : dbMapping.getTargetColumns().entrySet()) {
                        if (srcColumn.equals(entry.getValue())) {
                            columnsMap.put(entry.getKey(), srcColumn);
                            flag = false;
                            break;
                        }
                    }
                }
                if (flag) {
                    columnsMap.put(srcColumn, srcColumn);
                }
            }
            dbMapping.setAllMapColumns(columnsMap);
        } else {
            columnsMap = dbMapping.getTargetColumns();
        }
        return columnsMap;
    }

    /**
     * 设置 preparedStatement
     *
     * @param type sqlType
     * @param pstmt 需要设置的preparedStatement
     * @param value 值
     * @param i 索引号
     */
    public static void setPStmt(int type, PreparedStatement pstmt, Object value, int i) throws SQLException {
        switch (type) {
            case Types.BIT:
            case Types.BOOLEAN:
                if (value instanceof Boolean) {
                    pstmt.setBoolean(i, (Boolean) value);
                } else if (value instanceof String) {
                    boolean v = !value.equals("0");
                    pstmt.setBoolean(i, v);
                } else if (value instanceof Number) {
                    boolean v = ((Number) value).intValue() != 0;
                    pstmt.setBoolean(i, v);
                } else {
                    pstmt.setNull(i, type);
                }
                break;
            case Types.CHAR:
            case Types.NCHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
                if (value instanceof String) {
                    pstmt.setString(i, (String) value);
                } else if (value == null) {
                    pstmt.setNull(i, type);
                } else {
                    pstmt.setString(i, value.toString());
                }
                break;
            case Types.TINYINT:
                // 向上提升一级，处理unsigned情况
                if (value instanceof Number) {
                    pstmt.setShort(i, ((Number) value).shortValue());
                } else if (value instanceof String) {
                    pstmt.setShort(i, Short.parseShort((String) value));
                } else {
                    pstmt.setNull(i, type);
                }
                break;
            case Types.SMALLINT:
                if (value instanceof Number) {
                    pstmt.setInt(i, ((Number) value).intValue());
                } else if (value instanceof String) {
                    pstmt.setInt(i, Integer.parseInt((String) value));
                } else {
                    pstmt.setNull(i, type);
                }
                break;
            case Types.INTEGER:
                if (value instanceof Number) {
                    pstmt.setLong(i, ((Number) value).longValue());
                } else if (value instanceof String) {
                    pstmt.setLong(i, Long.parseLong((String) value));
                } else {
                    pstmt.setNull(i, type);
                }
                break;
            case Types.BIGINT:
                if (value instanceof Number) {
                    pstmt.setBigDecimal(i, new BigDecimal(value.toString()));
                } else if (value instanceof String) {
                    pstmt.setBigDecimal(i, new BigDecimal(value.toString()));
                } else {
                    pstmt.setNull(i, type);
                }
                break;
            case Types.DECIMAL:
            case Types.NUMERIC:
                if (value instanceof BigDecimal) {
                    pstmt.setBigDecimal(i, (BigDecimal) value);
                } else if (value instanceof Byte) {
                    pstmt.setInt(i, ((Byte) value).intValue());
                } else if (value instanceof Short) {
                    pstmt.setInt(i, ((Short) value).intValue());
                } else if (value instanceof Integer) {
                    pstmt.setInt(i, (Integer) value);
                } else if (value instanceof Long) {
                    pstmt.setLong(i, (Long) value);
                } else if (value instanceof Float) {
                    pstmt.setBigDecimal(i, new BigDecimal((float) value));
                } else if (value instanceof Double) {
                    pstmt.setBigDecimal(i, new BigDecimal((double) value));
                } else if (value != null) {
                    pstmt.setBigDecimal(i, new BigDecimal(value.toString()));
                } else {
                    pstmt.setNull(i, type);
                }
                break;
            case Types.REAL:
                if (value instanceof Number) {
                    pstmt.setFloat(i, ((Number) value).floatValue());
                } else if (value instanceof String) {
                    pstmt.setFloat(i, Float.parseFloat((String) value));
                } else {
                    pstmt.setNull(i, type);
                }
                break;
            case Types.FLOAT:
            case Types.DOUBLE:
                if (value instanceof Number) {
                    pstmt.setDouble(i, ((Number) value).doubleValue());
                } else if (value instanceof String) {
                    pstmt.setDouble(i, Double.parseDouble((String) value));
                } else {
                    pstmt.setNull(i, type);
                }
                break;
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
            case Types.BLOB:
                if (value instanceof Blob) {
                    pstmt.setBlob(i, (Blob) value);
                } else if (value instanceof byte[]) {
                    pstmt.setBytes(i, (byte[]) value);
                } else if (value instanceof String) {
                    pstmt.setBytes(i, ((String) value).getBytes(StandardCharsets.ISO_8859_1));
                } else {
                    pstmt.setNull(i, type);
                }
                break;
            case Types.CLOB:
                if (value instanceof Clob) {
                    pstmt.setClob(i, (Clob) value);
                } else if (value instanceof byte[]) {
                    pstmt.setBytes(i, (byte[]) value);
                } else if (value instanceof String) {
                    Reader clobReader = new StringReader((String) value);
                    pstmt.setCharacterStream(i, clobReader);
                } else {
                    pstmt.setNull(i, type);
                }
                break;
            case Types.DATE:
                if (value instanceof java.sql.Date) {
                    pstmt.setDate(i, (java.sql.Date) value);
                } else if (value instanceof java.util.Date) {
                    pstmt.setDate(i, new java.sql.Date(((java.util.Date) value).getTime()));
                } else if (value instanceof String) {
                    String v = (String) value;
                    if (!v.startsWith("0000-00-00")) {
                        java.util.Date date = Util.parseDate(v);
                        if (date != null) {
                            pstmt.setDate(i, new Date(date.getTime()));
                        } else {
                            pstmt.setNull(i, type);
                        }
                    } else {
                        pstmt.setObject(i, value);
                    }
                } else {
                    pstmt.setNull(i, type);
                }
                break;
            case Types.TIME:
                if (value instanceof java.sql.Time) {
                    pstmt.setTime(i, (java.sql.Time) value);
                } else if (value instanceof java.util.Date) {
                    pstmt.setTime(i, new java.sql.Time(((java.util.Date) value).getTime()));
                } else if (value instanceof String) {
                    String v = (String) value;
                    if(Util.isAccuracyOverSecond(v)) {
                        //the java.sql.time doesn't support for even millisecond, only setObject works here.
                        pstmt.setObject(i, v);
                    }else {
                        java.util.Date date = Util.parseDate(v);
                        if (date != null) {
                            pstmt.setTime(i, new Time(date.getTime()));
                        } else {
                            pstmt.setNull(i, type);
                        }
                    }
                } else {
                    pstmt.setNull(i, type);
                }
                break;
            case Types.TIMESTAMP:
                if (value instanceof java.sql.Timestamp) {
                    pstmt.setTimestamp(i, (java.sql.Timestamp) value);
                } else if (value instanceof java.util.Date) {
                    pstmt.setTimestamp(i, new java.sql.Timestamp(((java.util.Date) value).getTime()));
                } else if (value instanceof String) {
                    String v = (String) value;
                    if (!v.startsWith("0000-00-00")) {
                        if(Util.isAccuracyOverMillisecond(v)){
                            //convert to ISO-8601 standard format, with up to nanoseconds (9 digits) precision
                            LocalDateTime isoDatetime = Util.parseISOLocalDateTime(v);
                            if (isoDatetime != null) {
                                pstmt.setTimestamp(i, Timestamp.valueOf(isoDatetime));
                            } else {
                                //if can't convert, set to null
                                pstmt.setNull(i, type);
                            }
                        }else {
                            java.util.Date date = Util.parseDate(v);
                            if (date != null) {
                                pstmt.setTimestamp(i, new Timestamp(date.getTime()));
                            } else {
                                pstmt.setNull(i, type);
                            }
                        }
                    } else {
                        pstmt.setObject(i, value);
                    }
                } else {
                    pstmt.setNull(i, type);
                }
                break;
            default:
                pstmt.setObject(i, value, type);
        }
    }

    public static String getDbTableName(MappingConfig.DbMapping dbMapping, String dbType) {
        String result = "";
        String backtick = getBacktickByDbType(dbType);
        if (StringUtils.isNotEmpty(dbMapping.getTargetDb())) {
            result += (backtick + dbMapping.getTargetDb() + backtick + ".");
        }
        result += (backtick + dbMapping.getTargetTable() + backtick);
        return result;
    }

    public static String getSourceDbTableName(MappingConfig.DbMapping dbMapping, String dbType) {
        String result = "";
        String backtick = getBacktickByDbType(dbType);
        if (StringUtils.isNotEmpty(dbMapping.getDatabase())) {
            result += (backtick + dbMapping.getDatabase() + backtick + ".");
        }

        result += (backtick + dbMapping.getTable() + backtick);
        return result;
    }

    /**
     * 根据DbType返回反引号或空字符串
     *
     * @param dbTypeName DbType名称
     * @return 反引号或空字符串
     */
    public static String getBacktickByDbType(String dbTypeName) {
        DbType dbType = DbType.of(dbTypeName);
        if (dbType == null) {
            dbType = DbType.other;
        }

        // 只有当dbType为MySQL/MariaDB或OceanBase时返回反引号
        switch (dbType) {
            case mysql:
            case mariadb:
            case oceanbase:
                return "`";
        //  当dbType 为 postgresql时，返回双引号(避免表名为postgresql的关键字时，sql查询报错)
            case postgresql:
                return "\"";
            default:
                return "";
        }
    }
}
