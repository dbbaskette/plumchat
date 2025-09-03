package com.baskettecase.mcpserver.service;

import com.baskettecase.mcpserver.model.ColumnInfo;
import com.baskettecase.mcpserver.model.SchemaInfo;
import com.baskettecase.mcpserver.model.TableInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SchemaDiscoveryService {

    private static final Logger logger = LoggerFactory.getLogger(SchemaDiscoveryService.class);

    private final DataSource dataSource;

    public SchemaDiscoveryService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<SchemaInfo> getAllSchemas() throws SQLException {
        List<SchemaInfo> schemas = new ArrayList<>();
        
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            try (ResultSet rs = metaData.getSchemas()) {
                while (rs.next()) {
                    String schemaName = rs.getString("TABLE_SCHEM");
                    String owner = rs.getString("TABLE_CATALOG");
                    
                    // Skip system schemas for cleaner output
                    if (isUserSchema(schemaName)) {
                        List<String> tableNames = getTableNamesForSchema(metaData, schemaName);
                        schemas.add(new SchemaInfo(schemaName, owner, tableNames));
                    }
                }
            }
        }
        
        logger.info("Found {} user schemas", schemas.size());
        return schemas;
    }

    public List<TableInfo> getTablesInSchema(String schemaName) throws SQLException {
        List<TableInfo> tables = new ArrayList<>();
        
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            try (ResultSet rs = metaData.getTables(null, schemaName, "%", new String[]{"TABLE", "VIEW"})) {
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    String tableType = rs.getString("TABLE_TYPE");
                    String remarks = rs.getString("REMARKS");
                    
                    List<ColumnInfo> columns = getColumnsForTable(metaData, schemaName, tableName);
                    tables.add(new TableInfo(schemaName, tableName, tableType, remarks, columns));
                }
            }
        }
        
        logger.info("Found {} tables in schema '{}'", tables.size(), schemaName);
        return tables;
    }

    public TableInfo getTableInfo(String schemaName, String tableName) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            try (ResultSet rs = metaData.getTables(null, schemaName, tableName, new String[]{"TABLE", "VIEW"})) {
                if (rs.next()) {
                    String tableType = rs.getString("TABLE_TYPE");
                    String remarks = rs.getString("REMARKS");
                    
                    List<ColumnInfo> columns = getColumnsForTable(metaData, schemaName, tableName);
                    return new TableInfo(schemaName, tableName, tableType, remarks, columns);
                }
            }
        }
        
        throw new IllegalArgumentException("Table " + schemaName + "." + tableName + " not found");
    }

    private List<ColumnInfo> getColumnsForTable(DatabaseMetaData metaData, String schemaName, String tableName) throws SQLException {
        List<ColumnInfo> columns = new ArrayList<>();
        Set<String> primaryKeys = getPrimaryKeys(metaData, schemaName, tableName);
        Set<String> foreignKeys = getForeignKeys(metaData, schemaName, tableName);
        
        try (ResultSet rs = metaData.getColumns(null, schemaName, tableName, "%")) {
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                String dataType = rs.getString("TYPE_NAME");
                Integer columnSize = rs.getInt("COLUMN_SIZE");
                Integer decimalDigits = rs.getInt("DECIMAL_DIGITS");
                boolean nullable = rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable;
                String defaultValue = rs.getString("COLUMN_DEF");
                String remarks = rs.getString("REMARKS");
                boolean isPrimaryKey = primaryKeys.contains(columnName);
                boolean isForeignKey = foreignKeys.contains(columnName);
                
                columns.add(new ColumnInfo(
                    columnName, dataType, columnSize, decimalDigits, 
                    nullable, defaultValue, remarks, isPrimaryKey, isForeignKey
                ));
            }
        }
        
        return columns;
    }

    private Set<String> getPrimaryKeys(DatabaseMetaData metaData, String schemaName, String tableName) throws SQLException {
        Set<String> primaryKeys = new HashSet<>();
        
        try (ResultSet rs = metaData.getPrimaryKeys(null, schemaName, tableName)) {
            while (rs.next()) {
                primaryKeys.add(rs.getString("COLUMN_NAME"));
            }
        }
        
        return primaryKeys;
    }

    private Set<String> getForeignKeys(DatabaseMetaData metaData, String schemaName, String tableName) throws SQLException {
        Set<String> foreignKeys = new HashSet<>();
        
        try (ResultSet rs = metaData.getImportedKeys(null, schemaName, tableName)) {
            while (rs.next()) {
                foreignKeys.add(rs.getString("FKCOLUMN_NAME"));
            }
        }
        
        return foreignKeys;
    }

    private List<String> getTableNamesForSchema(DatabaseMetaData metaData, String schemaName) throws SQLException {
        List<String> tableNames = new ArrayList<>();
        
        try (ResultSet rs = metaData.getTables(null, schemaName, "%", new String[]{"TABLE", "VIEW"})) {
            while (rs.next()) {
                tableNames.add(rs.getString("TABLE_NAME"));
            }
        }
        
        return tableNames;
    }

    private boolean isUserSchema(String schemaName) {
        // Skip common system schemas
        if (schemaName == null) return false;
        
        String lowerSchema = schemaName.toLowerCase();
        return !lowerSchema.equals("information_schema") &&
               !lowerSchema.equals("pg_catalog") &&
               !lowerSchema.equals("pg_toast") &&
               !lowerSchema.startsWith("pg_temp") &&
               !lowerSchema.startsWith("pg_toast_temp");
    }
}