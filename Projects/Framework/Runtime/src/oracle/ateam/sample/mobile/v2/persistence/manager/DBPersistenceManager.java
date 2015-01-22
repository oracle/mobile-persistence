 /*******************************************************************************
  Copyright © 2015, Oracle and/or its affiliates. All rights reserved.
  
  $revision_history$
  08-jan-2015   Steven Davelaar
  1.0           initial creation
 ******************************************************************************/
 package oracle.ateam.sample.mobile.v2.persistence.manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.math.BigDecimal;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.framework.exception.AdfException;

import oracle.ateam.sample.mobile.util.ADFMobileLogger;
import oracle.ateam.sample.mobile.util.MessageUtils;
import oracle.ateam.sample.mobile.v2.persistence.cache.EntityCache;
import oracle.ateam.sample.mobile.v2.persistence.db.BindParamInfo;
import oracle.ateam.sample.mobile.v2.persistence.db.DBConnectionFactory;
import oracle.ateam.sample.mobile.v2.persistence.metadata.AttributeMapping;
import oracle.ateam.sample.mobile.v2.persistence.metadata.AttributeMappingDirect;
import oracle.ateam.sample.mobile.v2.persistence.metadata.AttributeMappingOneToMany;
import oracle.ateam.sample.mobile.v2.persistence.metadata.AttributeMappingOneToOne;
import oracle.ateam.sample.mobile.v2.persistence.metadata.ClassMappingDescriptor;
import oracle.ateam.sample.mobile.v2.persistence.metadata.ObjectPersistenceMapping;
import oracle.ateam.sample.mobile.v2.persistence.metadata.PersistenceConfig;
import oracle.ateam.sample.mobile.v2.persistence.model.Entity;
import oracle.ateam.sample.mobile.v2.persistence.util.EntityUtils;


/**
 * Implementation of PersistenceManager interface that provides basic CRUD operations against
 * on-device SQLite database.
 * Provides a set of convenience methods to ease creation and execution of custom SQL statements.
 *
 */
public class DBPersistenceManager
  extends AbstractPersistenceManager
{
  private static ADFMobileLogger sLog = ADFMobileLogger.createLogger(DBPersistenceManager.class);

  public static final String SQL_SELECT_KEYWORD = "SELECT ";
  public static final String SQL_UPDATE_KEYWORD = "UPDATE ";
  public static final String SQL_INSERT_KEYWORD = "INSERT INTO ";
  public static final String SQL_DELETE_KEYWORD = "DELETE FROM ";
  public static final String SQL_WHERE_KEYWORD = " WHERE ";
  public static final String SQL_FROM_KEYWORD = " FROM ";
  public static final String SQL_SET_KEYWORD = " SET ";
  public static final String SQL_AND_OPERATOR = " AND ";
  public static final String SQL_OR_OPERATOR = " OR ";
  public static final String SQL_LIKE_OPERATOR = " LIKE ";

  public DBPersistenceManager()
  {
    super();
  }
  
  public Object getMaxValue(Class entityClass, String attrName)
  {
    ClassMappingDescriptor descriptor = ClassMappingDescriptor.getInstance(entityClass);
    AttributeMapping mapping = descriptor.findAttributeMappingByName(attrName);
    if (mapping!=null)
    {
       String tableName = descriptor.getTableName();
       String columnName = mapping.getColumnName();
       return getMaxColumnValue(tableName, columnName);
    }
    return null;
  }

  public Object getMaxColumnValue(String tableName, String columnName) {
      String sql = SQL_SELECT_KEYWORD+" max("+columnName+") "+SQL_FROM_KEYWORD+" "+tableName;
      ResultSet set = executeSqlSelect(sql, null);
        try {
            set.first();
            Object value = set.getObject(1);
            return value;
        } catch (SQLException e) {
            throw new AdfException(e);                
        }
    }

  /**
   * Executes SQL SELECT statement with optionally a list of bind parameters 
   * The number and sequence of bind parameters passed in should match the
   * number and sequence of bind parameter references in the SQL statement.
   * @param sql
   * @param bindParamInfos
   * @return
   */
  public ResultSet executeSqlSelect(String sql, List<BindParamInfo> bindParamInfos)
  {
    sLog.fine("Executing SQL statement "+sql);
    PreparedStatement statement = null;
    try
    {
      Connection c = DBConnectionFactory.getConnection();
      c.setAutoCommit(false);
      statement = c.prepareStatement(sql);
      if (bindParamInfos != null && bindParamInfos.size() > 0)
      {
        // there are bind params that need to be set
        setSqlBindParams(bindParamInfos, statement, false);
      }
      return statement.executeQuery();
    }
    catch (Exception e)
    {
      sLog.severe("Error executing SQL statement "+sql+": "+e.getLocalizedMessage());
      throw new AdfException(e);
    }
    finally
    {
      DBConnectionFactory.releaseConnection();
    }
  }

  /**
   * Executes a SQL INSERT, UPDATE or DELETE statement, with optionally a set
   * of bind parameters. 
   * The number and sequence of bind parameters passed in should match the
   * number and sequence of bind parameter references in the SQL statement.
   * The doCommit paarmeter determines whether a commit is executed after the statement.
   * @param sql
   * @param bindParamInfos
   * @param doCommit
   */
  public void executeSqlDml(String sql, List<BindParamInfo> bindParamInfos, boolean doCommit)
  {
    PreparedStatement statement = null;
    try
    {
      Connection c = DBConnectionFactory.getConnection();
      c.setAutoCommit(doCommit);
      statement = c.prepareStatement(sql);
      boolean isUpdate = sql.toUpperCase().startsWith(SQL_UPDATE_KEYWORD);
      boolean isDelete = sql.toUpperCase().startsWith(SQL_DELETE_KEYWORD);
      setSqlBindParams(bindParamInfos, statement, isUpdate || isDelete);
      statement.execute();
      // reset so user does not accidently commit when doing select statement with own code
      c.setAutoCommit(false);
    }
    catch (Exception e)
    {
      sLog.severe("Error executing SQL statement "+sql+": "+e.getLocalizedMessage());
      throw new AdfException(e);
    }
    finally
    {
      if (statement != null)
      {
        try
        {
          statement.close();
        }
        catch (Exception e)
        {
          sLog.severe("Error closing SQL statement "+sql+": "+e.getLocalizedMessage());
          //
        }
      }
      DBConnectionFactory.releaseConnection();
    }
  }

  /**
   * Persist the entity using SQL INSERT or UPDATE Statement.
   * To determine whether an insert or update needs to take place, the
   * isNewEntity method on the entity passed in is called.
   *
   * @param entity
   * @param doCommit
   */
  public void mergeEntity(Entity entity, boolean doCommit)
  {
    boolean doInsert = entity.getIsNewEntity();
    if (doInsert)
    {
      insertEntity(entity, doCommit);
    }
    else
    {
      updateEntity(entity, doCommit);
    }
  }

  /**
   * Persists the entity using SQL INSERT Statement
   * @param entity
   * @param doCommit
   */
  public void insertEntity(Entity entity, boolean doCommit)
  {
    if (isEntityExixtsInDB(entity))
    {
      MessageUtils.handleError(entity.getClass().getName()+ " with this key already exists");
    }
    List<BindParamInfo> bindParamInfos = getBindParamInfos(entity,false,true);
    insertRow(bindParamInfos, doCommit);
    mergeChildren(entity,doCommit);
  }

  /**
   * Inserts a row in SQL database using bindPramInfos passed in
   * @param bindParamInfos
   * @param doCommit
   */
  public void insertRow(List<BindParamInfo> bindParamInfos, boolean doCommit)
  {
    if (bindParamInfos.size() == 0)
    {
      // noting to insert
      return;
    }
    StringBuffer sql = getSqlInsertIntoPart(bindParamInfos);
    sql.append(getSqlInsertValuesPart(bindParamInfos));
    executeSqlDml(sql.toString(), bindParamInfos, doCommit);
  }

  /**
   * Inserts or updates a row in SQL database using bindPramInfos passed in.
   * If the primary key bindParamInfo is missing no row will be inserted nor updated.
   * To determine wherther the row is inserted or updated, method isRowExistsInDB is
   * called.
   * @param bindParamInfos
   * @param doCommit
   */
  public void mergeRow(List<BindParamInfo> bindParamInfos, boolean doCommit)
  {
    if (bindParamInfos.size() == 0)
    {
      // noting to insert or update
      return;
    }
    // get the primary key bind info
    List<BindParamInfo> primaryKeyBindParamInfos = new ArrayList();
    for (BindParamInfo bpInfo : bindParamInfos)
    {
      if (bpInfo.isPrimaryKey())
      {
        primaryKeyBindParamInfos.add(bpInfo);
      }
    }
    if (primaryKeyBindParamInfos.size()>0)
    {
      if (isRowExistsInDB(primaryKeyBindParamInfos))   
      {
        updateRow(bindParamInfos, doCommit);
      }
      else
      {
        insertRow(bindParamInfos, doCommit);
      }
    }
  }

  /**
   * Deletes all rows in the table that maps to the entityClass.
   * Also clears all entity instances from the cache
   * @param entityClass
   */
  public void deleteAllRows(Class entityClass)
  {
    ClassMappingDescriptor descriptor = ClassMappingDescriptor.getInstance(entityClass);
    if (descriptor.isPersisted())
    {
      deleteAllRows(descriptor.getTableName(), true);
    }
    // Remove all instances of the entity we are dealing with from the cache
    // to prevent obsolete instances in the cache
    EntityCache.getInstance().clear(entityClass);
  }

  /**
   * Deletes all rows in specified table
   * @param tableName
   * @param doCommit
   */
  public void deleteAllRows(String tableName, boolean doCommit)
  {
    StringBuffer sql = new StringBuffer();
    sql.append(SQL_DELETE_KEYWORD);
    sql.append(tableName);
    executeSqlDml(sql.toString(), new ArrayList<BindParamInfo>(), doCommit);
  }

  /**
   * Delete the row that matches the entity in SQL database 
   * @param entity
   * @param doCommit
   */
  public void removeEntity(Entity entity, boolean doCommit)
  {
    List<BindParamInfo> primaryKeyBindParamInfos = getPrimaryKeyBindParamInfo(entity);
    BindParamInfo firstPkValue = primaryKeyBindParamInfos.get(0);
    StringBuffer sql = new StringBuffer(SQL_DELETE_KEYWORD);
    sql.append(firstPkValue.getTableName());
    sql = constructWhereClause(sql, primaryKeyBindParamInfos);
    // need to pas primary key bind param info as only one in list to set
    // the bind param.
    executeSqlDml(sql.toString(), primaryKeyBindParamInfos, doCommit);
  }


  /**
   * Persist the entity using SQL UPDATE Statement
   * @param entity
   * @param doCommit
   */
  public void updateEntity(Entity entity, boolean doCommit)
  {
    List<BindParamInfo> bindParamInfos = getBindParamInfos(entity,false,true);
    updateRow(bindParamInfos, doCommit);
    mergeChildren(entity,doCommit);
  }

  /**
   * Updates a row SQL database using bindPramInfos passed in
   * The columns that will be updated are based on the list of 
   * bind parameters passed in. For each bind parameter, the associated column is updated
   * except for the primary key. The primary key bind parameter is used to construct
   * the WHERE clause
   * @param bindParamInfos
   * @param doCommit
   */
  public void updateRow(List<BindParamInfo> bindParamInfos, boolean doCommit)
  {
    if (bindParamInfos.size() == 0)
    {
      // noting to update
      return;
    }
    // if all bindParamInfos are pk bindings then there is nothing to update
    boolean update =false;
    for (BindParamInfo bpInfo : bindParamInfos)
    {
      if (!bpInfo.isPrimaryKey())
      {
        update = true;
        break;
      }
    }
    if (update)
    {
      StringBuffer sql = getSqlUpdateStatement(bindParamInfos);
      executeSqlDml(sql.toString(), bindParamInfos, doCommit);      
    }
  }

  /**
   * Helper method to create the INSERT INTO part of the SQL statement. 
   * For each BindParamInfo in the list, a column is added to the INSERT statement.
   * @param bindParamInfos
   * @return
   */
  public StringBuffer getSqlInsertIntoPart(List<BindParamInfo> bindParamInfos)
  {
    BindParamInfo firstValue = bindParamInfos.get(0);
    StringBuffer insertSQL = new StringBuffer(SQL_INSERT_KEYWORD);
    insertSQL.append(firstValue.getTableName());
    insertSQL.append(" (");
    insertSQL.append(getSqlColumnNamesCommaSeparated(bindParamInfos));
    insertSQL.append(") ");
    return insertSQL;
  }

  /**
   * Returns SQL statement of the form
   * <pre>
   *  UPDATE TABLE_NAME
   *  SET COLUMN_NAME1=?, COLUMN_NAME2=?
   *  WHERE PK_COLUMN_NAME=?
   * </pre>
   * For each BindParamInfo in the list, a column is added to the UPDATE statement, except
   * for the primary key BindParamInfo which is used to construct the WHERE clause.
   * @param bindParamInfos
   * @return
   */
  public StringBuffer getSqlUpdateStatement(List<BindParamInfo> bindParamInfos)
  {
    BindParamInfo firstValue = bindParamInfos.get(0);
    StringBuffer sql = new StringBuffer(SQL_UPDATE_KEYWORD);
    sql.append(firstValue.getTableName());
    sql.append(SQL_SET_KEYWORD);
    boolean first = true;
    List<BindParamInfo> primaryKeyValues = new ArrayList<BindParamInfo>();
    for (BindParamInfo bindParamInfo : bindParamInfos)
    {
      if (bindParamInfo.getColumnName()==null)
      {
        continue;
      }
      if (bindParamInfo.isPrimaryKey())
      {
        // skip the pk
        primaryKeyValues.add(bindParamInfo);
        continue;
      }
      if (!first)
      {
        sql.append(",");
      }
      else
      {
        first = false;
      }
      sql.append(bindParamInfo.getColumnName());
      sql.append(bindParamInfo.getOperator());
      sql.append("?");
    }
    // append where clause to update correct row
    sql = constructWhereClause(sql, primaryKeyValues);
    return sql;
  }


  /**
   * Sets the values of bind parameters on the SQL statement
   * @param bindParamInfos
   * @param statement
   * @throws java.sql.SQLException
   */
  public void setSqlBindParams(List<BindParamInfo> bindParamInfos, PreparedStatement statement, boolean pkValueAsLast)
    throws java.sql.SQLException
  { 
    int countPkBindParams = 0;
    for (BindParamInfo bindParamInfo : bindParamInfos)
    {
      if (bindParamInfo.isPrimaryKey())
      {
        countPkBindParams++;
      }
    }
    int pkBindParamsProcessed = 0;
    int bindVarCounter = 1;
    for (BindParamInfo bindParamInfo : bindParamInfos)
    {
      Object value = bindParamInfo.getValue();
      int bindpos = bindVarCounter;
      if (pkValueAsLast && bindParamInfo.isPrimaryKey())
      {
        // PK value is last bind var in where clause!
        bindpos = bindParamInfos.size()-countPkBindParams+pkBindParamsProcessed+1;
        pkBindParamsProcessed++;
      }
      else
      {
        // normal bind var for insert/update value, increase counter for next usage
        bindVarCounter++;
      }
      if (value != null)
      {
        if (value instanceof Date)
        {
          Date dateValue = (Date)value;
          // dates need to be saved to SQLIte as timestamp to not loose time part
          statement.setTimestamp(bindpos, new Timestamp(dateValue.getTime()));
        }
        else if (value instanceof Timestamp)
        {
          statement.setTimestamp(bindpos, (Timestamp) value);
        }
        else
        {
          statement.setObject(bindpos, value);          
        }
      }
      else
      {
        statement.setNull(bindpos, bindParamInfo.getSqlType());
      }
    }
  }

  /**
   * Returns the VALUES(?,?,?) part of SQL INSERT statement.
   * The number of question matrks added is equal to the size
   * of the list of bindParamInfos.
   * @param bindParamInfos
   * @return
   */
  public String getSqlInsertValuesPart(List<BindParamInfo> bindParamInfos)
  {
    StringBuffer valuesPart = new StringBuffer();
    valuesPart.append("VALUES (");
    boolean first = true;
    for (BindParamInfo bpInfo : bindParamInfos)
    {
      if (bpInfo.getColumnName()==null)
      {
        continue;
      }
      if (first)
      {
        first = false;
      }
      else
      {
        valuesPart.append(",");        
      }
      valuesPart.append("?");
    }
    valuesPart.append(")");
    return valuesPart.toString();
  }

  /**
   * Helper method that returns comma-separated list of SQL column names that can 
   * be used in INSERT or SELECT statement
   * @param bindParamInfos
   * @return
   */
  public String getSqlColumnNamesCommaSeparated(List<BindParamInfo> bindParamInfos)
  {
    StringBuffer columnNames = new StringBuffer();
    boolean first = true;
    for (BindParamInfo bindParamInfo : bindParamInfos)
    {
      if (bindParamInfo.getColumnName()==null)
      {
        continue;
      }
      if (first)
      {
        first = false;
      }
      else
      {
        columnNames.append(",");        
      }
      columnNames.append(bindParamInfo.getColumnName());        
    }
    return columnNames.toString();
  }

  /**
   * Cheks whether there is a corresponding row in the database for the entity passed in.
   * If no row is found, this method returns true, otherwise it returns false.
   * The actual check is delegated to method isRowExistsInDB.
   * @param entity
   * @return
   */
  public boolean isEntityExixtsInDB(Entity entity)
  {
    List<BindParamInfo> primaryKeyValues = getPrimaryKeyBindParamInfo(entity);
    return isRowExistsInDB(primaryKeyValues);
  }

  /**
   * Cheks whether there is a corresponding row in the database for the  
   * BindParamInfo passed in. If no row is found, this method returns true, otherwise 
   * it returns false;
   * @param entity
   * @return
   */
  public boolean isRowExistsInDB(BindParamInfo bindParamInfo)
  {
    List<BindParamInfo> bindParamInfos = new ArrayList<BindParamInfo>();
    bindParamInfos.add(bindParamInfo);
    return isRowExistsInDB(bindParamInfos);
  }

  /**
   * Cheks whether there is a corresponding row in the database for the 
   * BindParamInfos passed in. If no row is found, this method returns true, otherwise 
   * it returns false;
   * @param entity
   * @return
   */
  public boolean isRowExistsInDB(List<BindParamInfo> bindParamInfos)
  {
    BindParamInfo firstBindParamInfo = bindParamInfos.get(0);
    StringBuffer sql = new StringBuffer(SQL_SELECT_KEYWORD);
    sql.append("1");
    sql.append(SQL_FROM_KEYWORD);
    sql.append(firstBindParamInfo.getTableName());
    constructWhereClause(sql, bindParamInfos);
    // need to pas primary key bind param info as only one in list to set
    // the bind param.
    ResultSet set = executeSqlSelect(sql.toString(), bindParamInfos);
    boolean rowFound = false;
    try
    {
      rowFound = set.next();
    }
    catch (SQLException e)
    {
      throw new AdfException(e);
    }
    finally
    {
      closeResultSet(set);
    }
    return rowFound;
  }

  public void closeResultSet(ResultSet set)
  {
    if (set != null)
    {
      try
      {
//        set.getStatement().close();
        set.close();
      }
      catch (SQLException e)
      {
        sLog.severe("Error closing SQL statement: "+e.getLocalizedMessage());
      }
    }
  }

  public List<Entity> findAll(Class entityClass)
  {
    return findAll(entityClass.getName());
  }

  /**
   * Finds an entity instance by ket. If the entity is present in entity cache, the cached
   * instance is returned and no database query is performed.
   * @param entityClass
   * @param key
   * @return
   */
  public Entity findByKey(Class entityClass, Object[] key)
  {
    return findByKey(entityClass,key,true);
  }

  /**
   * Finds an entity instance by ket. If the entity is present in entity cache and param checkEntityCache
   * is true, the cached instance is returned and no database query is performed. If checkEntityCache is false,
   * a database query is performed, and the row values are applied to the entity instance in the cache, or
   * when the entity is not present in the cache, to a new entity instance.
   * @param entityClass
   * @param key
   * @param checkEntityCache
   * @return
   */
  public Entity findByKey(Class entityClass, Object[] key, boolean checkEntityCache)
  {
    // first call super to check the cache
    Entity entity = checkEntityCache ? super.findByKey(entityClass, key) : null;
    if (entity == null)
    {
      sLog.fine("Entity with key "+key+" not found in entity cache, now checking database");
      List<BindParamInfo> keyBindParamInfos = getPrimaryKeyBindParamInfo(entityClass);
      for (int i = 0; i < keyBindParamInfos.size(); i++)
      {
        BindParamInfo keyValue = keyBindParamInfos.get(i);
        if (key.length>i)
        {
          keyValue.setValue(key[i]);          
        }
      }
      ObjectPersistenceMapping persMapping = ObjectPersistenceMapping.getInstance();
      ClassMappingDescriptor descriptor = persMapping.findClassMappingDescriptor(entityClass.getName());
      StringBuffer sql = getSqlSelectFromPart(descriptor);
      constructWhereClause(sql, keyBindParamInfos);      
      ResultSet set = executeSqlSelect(sql.toString(), keyBindParamInfos);

      List<Entity> entities = createEntitiesFromResultSet(set, descriptor.getAttributeMappings());
      if (entities.size() > 0)
      {
        entity = entities.get(0);
        sLog.fine("Entity with key "+key+" found in database");
      }
      else
      {
        sLog.fine("Entity with key "+key+" NOT found in database");        
      }
    }
    return entity;
  }

  /**
   * Executes a SELECT statement on the table mapped to this entity, returning all columns
   * without a WHERE clause. For each row in the result set an entity instance of the specified type
   * is created, and the list returned is a list of these instances.
   * @param entityClass
   * @return
   */
  public List<Entity> findAll(String entityClass)
  {
    return find(entityClass, null);
  }


  /**
   * Converts string value to BigDecimal. Returns null if conversion fails.
   * @param value
   * @return
   */
   public BigDecimal getBigDecimalValue(String value)
   {
    try
    {
      BigDecimal bd = new BigDecimal(value);
      return bd;
    }
    catch (Exception e)
    {
    }
    return null;
   }

  /**
   * Returns a set of rows by applying the searchValue to applicable columns in the table.
   * The where clause is constructed by looping over all direct or one-to-one attribute mappings, checking whether
   * the mapping attribute is of the proper type (String or number when the searchValue is a numeric value), and if so, 
   * the column is added to the where clause. If the column is of a string type, 
   * the LIKE opertaor is used and the searchValue is suffixed with the wildcard character "%". 
   * If the column is numeric, the = operator is used.
   * 
   * @param entityClass
   * @param searchValue
   * @return List of matching entity instances
   */
  public List<Entity> find(Class entityClass, String searchValue)
  {
    return find(entityClass, searchValue, null);
  }

  /**
   * Returns a set of rows by applying the searchValue to applicable columns in the table.
   * The where clause is constructed by looping over all direct or one-to-one attribute mappings, checking whether
   * the mapping attribute is of the proper type (String or number when the searchValue is a numeric value), and in the list
   * of attrNames (when specified), and if so, the column is added to the where clause. If the column is of a string type, 
   * the LIKE opertaor is used and the searchValue is suffixed with the wildcard character "%". 
   * If the column is numeric, the = operator is used.
   * 
   * @param entityClass
   * @param searchValue
   * @param attrNamesToSearch list of attribute names to search on. If empty or null, all attributes with matching type 
   * will be searched on
   * @return List of matching entity instances
   */
  public List<Entity> find(Class entityClass, String searchValue, List<String> attrNamesToSearch)
  {
    ObjectPersistenceMapping persMapping = ObjectPersistenceMapping.getInstance();
    ClassMappingDescriptor descriptor = persMapping.findClassMappingDescriptor(entityClass.getName());
    StringBuffer sql = getSqlSelectFromPart(descriptor);

    List<BindParamInfo> bindParamInfos = new ArrayList<BindParamInfo>();
    List<AttributeMappingDirect> attributeMappings = descriptor.getAttributeMappingsDirect();
    BigDecimal numericSearchValue = getBigDecimalValue(searchValue);
    boolean isNumeric = numericSearchValue!=null;
    for (AttributeMappingDirect mapping : attributeMappings)
    {
      if (attrNamesToSearch!=null && !attrNamesToSearch.contains(mapping.getAttributeName()))
      {
        // skip this attr
        continue;
      }
      BindParamInfo bindParamInfo = constructBindParamInfo(entityClass, mapping);
      // check the SQL type to determine whether we can search on it, and the operator
      int sqlType = bindParamInfo.getSqlType();
      if (sqlType==Types.CHAR || sqlType==Types.CLOB || sqlType==Types.VARCHAR)
      {
        bindParamInfo.setValue(searchValue.toUpperCase()+"%");
        bindParamInfo.setOperator(SQL_LIKE_OPERATOR);
        bindParamInfo.setCaseInsensitive(true);
        bindParamInfos.add(bindParamInfo);            
      }
      else if (isNumeric
              && (sqlType==Types.BIGINT ||sqlType==Types.DECIMAL || sqlType==Types.DOUBLE || sqlType==Types.FLOAT
                  || sqlType==Types.INTEGER || sqlType==Types.NUMERIC || sqlType==Types.SMALLINT) )
      {
        bindParamInfo.setValue(numericSearchValue);
        // no need to set operator, default operator is already "="
        bindParamInfos.add(bindParamInfo);              
      }
    }    
    sql = constructWhereClause(sql, bindParamInfos, SQL_OR_OPERATOR);
    sql = constructOrderByClause(sql, descriptor);
    ResultSet set = executeSqlSelect(sql.toString(), bindParamInfos);
    return createEntitiesFromResultSet(set, descriptor.getAttributeMappings());
  }

  /**
   * Executes a SELECT statement on the table mapped to the entity class, returning all columns.
   * The WHERE clause is build based on the list BindParamInfo instances passed in. When multiple
   * bindParamInfos are passed in the WHERE clause conditions are chained using the AND operator.
   * For each row in the result set an entity instance of the specified type
   * is created, and the list returned is a list of these instances.
   * @param entityClass
   * @return
   */
  public List<Entity> find(String entityClass, List<BindParamInfo> bindParamInfos)
  {
    ObjectPersistenceMapping persMapping = ObjectPersistenceMapping.getInstance();
    ClassMappingDescriptor descriptor = persMapping.findClassMappingDescriptor(entityClass);
    StringBuffer sql = getSqlSelectFromPart(descriptor);
    sql = constructWhereClause(sql, bindParamInfos);
    sql = constructOrderByClause(sql, descriptor);
    ResultSet set = executeSqlSelect(sql.toString(), bindParamInfos);
    return createEntitiesFromResultSet(set, descriptor.getAttributeMappings());
  }

  /**
   * Add SQL WHERE clause to SQL statement passed in. For the BindParamInfo passed in
   * a column is added with the value specified as bind parameter ("=?")
   * @param sql
   * @param bindParamInfo
   * @return
   */
  public StringBuffer constructWhereClause(StringBuffer sql, BindParamInfo bindParamInfo)
  {
    List<BindParamInfo> bindParamInfos = new ArrayList<BindParamInfo>();
    bindParamInfos.add(bindParamInfo);
    return constructWhereClause(sql, bindParamInfos);
  }

  /**
   * Add SQL WHERE clause to SQL statement passed in. For each BindParamInfo a column is added
   * with the value specified as bind parameter ("=?"). When multiple bindParamInfos are passed in, the
   * AND operator is used to construct the where clause.
   * @param sql
   * @param bindParamInfo
   * @return
   */
  public StringBuffer constructWhereClause(StringBuffer sql, List<BindParamInfo> bindParamInfos)
  {
    return constructWhereClause(sql, bindParamInfos, SQL_AND_OPERATOR);
  }

  /**
   * Add SQL WHERE clause to SQL statement passed in. For each BindParamInfo a column is added
   * with the value specified as bind parameter
   * @param sql
   * @param bindParamInfos
   * @return
   */
  public StringBuffer constructWhereClause(StringBuffer sql, List<BindParamInfo> bindParamInfos, String operator)
  {
    if (bindParamInfos == null || bindParamInfos.size() == 0)
    {
      return sql;
    }
    sql.append(SQL_WHERE_KEYWORD);
    for (int i = 0; i < bindParamInfos.size(); i++)
    {
      if (i > 0)
      {
        sql.append(operator);
      }
      BindParamInfo bp = bindParamInfos.get(i);
      if (bp.isCaseInsensitive())
      {
        sql.append("UPPER(");        
        sql.append(bp.getColumnName());                
        sql.append(")");        
      }
      else
      {
        sql.append(bp.getColumnName());        
      }
      sql.append(bp.getOperator());
      sql.append("?");
    }
    return sql;
  }

  /**
   * Helper method to create SELECT .. FROM part of SQL statement
   * The columns added to the SELECT clause are all columns that are mapped to
   * an attribute in the ClassMappingDescriptor
   * @param descriptor
   * @return
   */
  public StringBuffer getSqlSelectFromPart(ClassMappingDescriptor descriptor)
  {
    StringBuffer sql = new StringBuffer(SQL_SELECT_KEYWORD);
    List<AttributeMappingDirect> attributeMappings = descriptor.getAttributeMappingsDirect();
    boolean first = true;
    for (AttributeMappingDirect mapping : attributeMappings)
    {
      if (mapping.getColumnName()==null)
      {
        continue;
      }
      if (first)
      {
        first = false;
      }
      else
      {
        sql.append(",");        
      }
      sql.append(mapping.getColumnName());
    }
    sql.append(SQL_FROM_KEYWORD);
    sql.append(descriptor.getTableName());
    return sql;
  }

  /**
   * Create a list of entity instances based on the SQL ResultSet and the attribute mappings
   * of the entity class.
   * @param resultSet
   * @param attributeMappings
   * @return
   */
  public List<Entity> createEntitiesFromResultSet(ResultSet resultSet, List<AttributeMapping> attributeMappings)
  {
    List<Entity> entities = new ArrayList<Entity>();
    try
    {
      ClassMappingDescriptor classDescriptor = attributeMappings.get(0).getClassMappingDescriptor();
      List<AttributeMapping> keyMappings = classDescriptor.getPrimaryKeyAttributeMappings();
      Class entityClass = classDescriptor.getClazz();
      while (resultSet.next())
      {
        Object[] keyValue = getAttributeValuesFromResultSet(resultSet, keyMappings, entityClass);
        Entity entity = EntityCache.getInstance().findByUID(classDescriptor.getClazz(), keyValue);
        boolean entityInCache = entity != null;
        if (entityInCache)
        {
          // we now update existing instances in cache so we can also process return payload from
          // write operation
          entities.add(entity);
//          continue;
        }
        else
        {
          entity = EntityUtils.getNewEntityInstance(classDescriptor.getClazz());
          entities.add(entity);
        }  
        List<AttributeMapping> selectMappings = classDescriptor.getAttributeMappings();
        for (AttributeMapping mapping : selectMappings)
        {
          // first handle onen to many mapping: does not have column value in result set
          if (mapping.isOneToManyMapping())
          {
            // indirect list is now created inside entity class when declaring member
            // so it also works correctly when creatng a new entity instance in UI

            // Only when entity was already in cache, we reset the attribute, so it will be
            // queried from local DB again upon first access.
// Not needed anymore, the ws call now directly populates the child list, so no need to refresh when
// already in cache            
//            if (entityInCache)
//            {
//              AttributeMappingOneToMany oneToManyMapping = (AttributeMappingOneToMany) mapping;
//              List indirectList = new IndirectList(entity, oneToManyMapping);
//              entity.setAttributeValue(mapping.getAttributeName(), indirectList);              
//            }
            continue;
          }
          if (mapping.isDirectMapping() && mapping.isPersisted())
          {
            Object value = getAttributeValueFromResultSet(resultSet, mapping, entityClass);
            entity.setAttributeValue(mapping.getAttributeName(), value);
          }
          else if (mapping.isOneToOneMapping())
          {
            // value holder is now created inside entity class when declaring member
            // so it also works correctly when creating a new entity instance in UI
//            Object value = getColumnValueFromResultSet(resultSet, mapping, entityClass);
//            AttributeMappingOneToOne oneToOneMapping = (AttributeMappingOneToOne) mapping;
//            ValueHolderInterface vh = new ValueHolder(oneToOneMapping, value);
//            entity.setAttributeValue(mapping.getAttributeName(), vh);
          }
        }
        if (!entityInCache)
        {
          // Need to add to cache after attrs set otherwise PK value is not yet known.
          EntityCache.getInstance().addEntity(entity);          
        }
      }
    }
    catch (SQLException e)
    {
      throw new AdfException(e);
    }
    return entities;
  }

  /**
   * Convert the column value returned by a SELECT statement to the Java Type expected by the corresponding
   * entity attribute
   * @param resultSet
   * @param mapping
   * @param entityClass
   * @return
   */
  public Object getAttributeValueFromResultSet(ResultSet resultSet, AttributeMapping mapping, Class entityClass)
  {
    Object sqlValue = getColumnValueFromResultSet(resultSet, mapping, entityClass);
    return EntityUtils.convertColumnValueToAttributeTypeIfNeeded(entityClass, mapping.getAttributeName(), sqlValue);      
  }

  public Object[] getAttributeValuesFromResultSet(ResultSet resultSet, List<AttributeMapping> mappings, Class entityClass)
  {
    Object[] values = new Object[mappings.size()];
    for (int i = 0; i < mappings.size(); i++)
    {
      values[i] = getAttributeValueFromResultSet(resultSet,mappings.get(i),entityClass);      
    }
    return values;
  }

  /**
   * Get the column value returned by a SELECT statement for a specific entity attribute
   * @param resultSet
   * @param mapping
   * @param entityClass
   * @return
   */
  public Object getColumnValueFromResultSet(ResultSet resultSet, AttributeMapping mapping, Class entityClass)
  {
    // calling getObject on Oracle DB returns correct type, however, on SQLLite is alywas return String
    // so we need to proper get method
    String attrName = mapping.getAttributeName();
    Class javaType = EntityUtils.getJavaType(entityClass, attrName);
    Object value = null;
    try
    {
      if (resultSet.getObject(mapping.getColumnName()) == null)
      {
        return value;
      }
      else if (javaType == String.class)
      {
        value = resultSet.getString(mapping.getColumnName());
      }
      else if (javaType == java.util.Date.class || javaType == java.sql.Date.class)
      {
        // dates are saved to SQLIte as timestamp to not loose time part
        Timestamp ts = resultSet.getTimestamp(mapping.getColumnName());
//        value = resultSet.getDate(mapping.getColumnName());
        value = new java.sql.Date(ts.getTime());
      }
      else if (javaType == Time.class || javaType == Timestamp.class)
      {
        value = resultSet.getTime(mapping.getColumnName());
      }
      else if (javaType == Integer.class)
      {
        value = new Integer(resultSet.getInt(mapping.getColumnName()));
      }
      else if (javaType == Long.class)
      {
        value = new Long(resultSet.getLong(mapping.getColumnName()));
      }
      else if (javaType == Float.class)
      {
        value = new Float(resultSet.getFloat(mapping.getColumnName()));
      }
      else if (javaType == Double.class)
      {
        value = new Float(resultSet.getDouble(mapping.getColumnName()));
      }
      else if (javaType == Double.class)
      {
        value = new Float(resultSet.getDouble(mapping.getColumnName()));
      }
      else if (javaType == Clob.class)
      {
        value = resultSet.getClob(mapping.getColumnName());
      }
      else if (javaType == Blob.class)
      {
        value = resultSet.getBlob(mapping.getColumnName());
      }
      else if (javaType == Short.class)
      {
        value = new Short(resultSet.getShort(mapping.getColumnName()));
      }
      else
      {
        value = resultSet.getObject(mapping.getColumnName());
      }
    }
    catch (SQLException e)
    {
      throw new AdfException("Error getting SQL resultSet value for column " + mapping.getColumnName() + ": " +
                             e.getLocalizedMessage(), AdfException.ERROR);
    }
    return value;
  }


  /**
   * Helper method that checks whether the SQLite database already exists on the mobile device.
   * The checked is performed by retrieving the database file name and checking whether the file already exists.
   * You will typically call this method from the start method in your application Lifecycle listener class, or you
   * can use or extend the InitDBLifeCycleListener included in this framework that already calls this method.
   * The database connection details, and the script to create the database should be specified in
   * mobile-persistence-config.properties file that must be stored in the META-INF directory of your
   * ApplicationController project.
   * In this file you can also specify whether the database should be encrypted. If you set db.encrypt=true,
   * you also need to specify the property db.password.prefname which contains the value of the preference you need to
   * create in your app that stores the password used for encryption. Optionally you can also specify db.encryption.type
   * if you want to use encryption algorithm aes256 or rc4 rather than the default aes128.
   *
   * Here is an example of the entries that must be present in this file:
   * in the property file to make this InitDBLifeCycleListener class work correctly:
   * <pre>
   * db.name=HR.db
   * db.encrypt=false
   * db.password.prefname=HRPassword
   * persistence.mapping.xml=META-INF/tlMap.xml
   * ddl.script=META-INF/hr.sql
   * </pre>
   *
   * The database is created with PRAGMA auto_vacuum = FULL to ensure that the DB file will shrink in size when
   * rows are deleted from a table. See http://www.sqlite.org/pragma.html#pragma_auto_vacuum for more info.
   *
   * @see oracle.ateam.sample.mobile.lifecycle.InitDBLifeCycleListener
   */
  public void initDBIfNeeded()
  {
    File dbFile = new File(PersistenceConfig.getDatabaseFilePath());
    if (!dbFile.exists())
    {
      String ddlScript = PersistenceConfig.getDDLScript();
      Connection connection = null;
      try
      {
        connection = DBConnectionFactory.getConnection();
        // set PRAGMA auto_vacuum = FULL to ensure the DB file will shrink in size
        // when rows are deleted. See http://www.sqlite.org/pragma.html#pragma_auto_vacuum
        PreparedStatement stmt = connection.prepareStatement("PRAGMA auto_vacuum = FULL;");
        stmt.execute();
      }
      catch (Exception e)
      {
        throw new AdfException("Error creating the database with PRAGMA auto_vacuum=FULL: "+e.getMessage(), AdfException.ERROR);
      }
      finally
      {
        DBConnectionFactory.releaseConnection();
      }
      // check whether we need to encrypt the database
      // always encrypt now
      if (PersistenceConfig.encryptDatabase())
      {
        sLog.info("Encrypting SQLite database");
        String pwString = PersistenceConfig.getEncryptionType()+":"+PersistenceConfig.getDatabasePassword();
        try
        {
          AdfmfJavaUtilities.encryptDatabase(DBConnectionFactory.getConnection(), pwString);
        }
        catch (Exception e)
        {
          throw new AdfException("Error enrypting the database: "+e.getMessage(), AdfException.ERROR);
        }
        finally
        {
          DBConnectionFactory.releaseConnection();
        }
      }
      sLog.info("SQLite database does NOT exist on mobile device, now executing DDL script "+ddlScript);
      executeSqlScript(ddlScript, true);
    }
  }

  /**
   * Execute a SQL script
   * @param script
   * @param doCommit
   */
  public void executeSqlScript(String script, boolean doCommit)
  {
    try
    {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      InputStream is = cl.getResourceAsStream(script);
      if (is == null)
      {
        sLog.severe("Could not find SQL script "+script);
        throw new AdfException("Could not find SQL Script: " + script, AdfException.ERROR);
      }
      BufferedReader bReader = new BufferedReader(new InputStreamReader(is));
      List<String> stmts = new ArrayList<String>();
      String strstmt = "";
      String ln = bReader.readLine();
      while (ln != null)
      {
        if (ln.startsWith("REM") || ln.startsWith("COMMIT") || ln.startsWith("--"))
        {
          ln = bReader.readLine();
          continue;
        }
        strstmt = strstmt + ln;
        if (strstmt.endsWith(";"))
        {
          stmts.add(strstmt);
          strstmt = "";
          ln = bReader.readLine();
          continue;
        }
        ln = bReader.readLine();

      }
      Connection connection = DBConnectionFactory.getConnection();
      connection.setAutoCommit(false);
      for (int i = 0; i < stmts.size(); i++)
      {
        Statement pStmt = connection.createStatement();
        String sql = stmts.get(i);
        sLog.severe("Processing SQL script "+script+", executing statement "+sql);
        try
        {
          pStmt.executeUpdate(sql);
        }
        catch (Exception e)
        {
          sLog.severe("Processing SQL script "+script+", error executing statement "+sql+": "+e.getLocalizedMessage());
          throw new AdfException("Error while executing statement " + sql + " in script " + script + ": " +
                                 e.getLocalizedMessage(), AdfException.ERROR);
        }
      }
      if (doCommit)
      {
        connection.commit();
      }
    }
    catch (Exception e)
    {
      throw new AdfException(e);
    }
    finally
    {
      DBConnectionFactory.releaseConnection();
    }
  }

  /**
   *  Execute Comnmit statement on DB connection
   */
  public void commmit()
  {
    try
    {
      DBConnectionFactory.getConnection().commit();
    }
    catch (Exception e)
    {
      throw new AdfException("Error while trying to commit transaction: " + e.getLocalizedMessage(),
                             AdfException.ERROR);
    }
    finally
    {
      DBConnectionFactory.releaseConnection();
    }
  }

  /**
   *  Execute Rollback statement on DB connection
   */
  public void rollback()
  {
    try
    {
      DBConnectionFactory.getConnection().rollback();
    }
    catch (Exception e)
    {
      throw new AdfException("Error while trying to rollback transaction: " + e.getLocalizedMessage(),
                             AdfException.ERROR);
    }
    finally
    {
      DBConnectionFactory.releaseConnection();
    }
  }

  /**
     * This method sets order by clause as defined in &lt;order-by&gt; element in mapping XML file.
     * You can manually add this element as a child of the &lt;class-mapping-descriptor&gt; element
     * (you need to edit the file outside JDeveloper). Example:
     * <pre>
     *  &lt;order-by&gt;HIRE_DATE DEC, LAST_NAME&lt;/order-by&gt;
     * </pre>
     * @param sql
     * @param classMappingDescriptor
     * @return
     */
  public StringBuffer constructOrderByClause(StringBuffer sql, ClassMappingDescriptor classMappingDescriptor)
  {
    String orderBy = classMappingDescriptor.getOrderBy();
    if (orderBy!=null && !orderBy.equals(""))
    {
      sql.append(" ORDER BY ");
      sql.append(orderBy);
    }
    return sql;
  }

  /**
   * Insert or update rows for children as defined in one-to-many ammpings for parent entity
   * @param parentEntity
   * @param doCommit
   */
  public void mergeChildren(Entity parentEntity, boolean doCommit)
  {
    ObjectPersistenceMapping mapping = ObjectPersistenceMapping.getInstance();
    ClassMappingDescriptor descriptor = mapping.findClassMappingDescriptor(parentEntity.getClass().getName());
    List<AttributeMappingOneToMany> attributeMappings = descriptor.getAttributeMappingsOneToMany();
    for (AttributeMappingOneToMany attrMapping : attributeMappings)
    {
      // only merge children of the one-to-many mapping does not have a method accessor. When it
      // has a method accessor, it is not a true parent-child relationship and the parent merge method is not
      // able to process the child entity
      if (attrMapping.getAccessorMethod()==null)
      {
        List<Entity> children = (List<Entity>) parentEntity.getAttributeValue(attrMapping.getAttributeName());
        for (Entity child : children)
        {
          mergeEntity(child, doCommit);
        }        
      }
    }
  }

  public List<Entity> findAllInParent(Class entityClass, Entity parent, String accessorAttribute)
  {
    // first find the corresponding oneToManyMapping
    ClassMappingDescriptor parentDescriptor = ClassMappingDescriptor.getInstance(parent.getClass());
    List<AttributeMappingOneToMany> mappings = parentDescriptor.getAttributeMappingsOneToMany();
    AttributeMappingOneToMany oneToManyMapping = null;
    for (AttributeMappingOneToMany mapping : mappings)
    {
      if (mapping.getAttributeName().equals(accessorAttribute))
      {
        oneToManyMapping = mapping;
        break;
      }
    }
    if (oneToManyMapping==null)
    {
      MessageUtils.handleError("Cannot execute findAllInParent, no one-to-many mapping found between "+parent.getClass().getName()+" and "+entityClass.getName());
    }
    return findAllInParent(entityClass, parent, oneToManyMapping );
  }

  public List<Entity> findAllInParent(Class entityClass, Entity parent, AttributeMappingOneToMany oneToManyMapping)
  {
    ClassMappingDescriptor parentDescriptor = ClassMappingDescriptor.getInstance(parent.getClass());
    ClassMappingDescriptor referenceDescriptor = ClassMappingDescriptor.getInstance(entityClass);
    Map<String,String> columnMappings = oneToManyMapping.getColumnMappings();
    Iterator<String> sourceColumns = columnMappings.keySet().iterator();
    List<BindParamInfo> bindParamInfos = new ArrayList<BindParamInfo>();
    while (sourceColumns.hasNext())
    {
      String sourceColumn = sourceColumns.next();
      String targetColumn = columnMappings.get(sourceColumn);
      // lookup attribute mapping for the source column in referenceDescriptor
      AttributeMapping refAttrMapping = referenceDescriptor.findAttributeMappingByColumnName(sourceColumn);
      BindParamInfo bp = constructBindParamInfo(referenceDescriptor.getClazz(), refAttrMapping);
      // the value must be set to the value of the attribute that matches the targetColumn
      AttributeMapping baseAttrMapping =
        parentDescriptor.findAttributeMappingByColumnName(targetColumn);
      Object value = parent.getAttributeValue(baseAttrMapping.getAttributeName());
      bp.setValue(value);
      bindParamInfos.add(bp);
    }
    return find(entityClass.getName(), bindParamInfos);
  }

  public Entity getAsParent(Class entityClass, Entity child, String accessorAttribute)
  {
    ClassMappingDescriptor childDescriptor = ClassMappingDescriptor.getInstance(child.getClass());
    List<AttributeMappingOneToOne> mappings = childDescriptor.getAttributeMappingsOneToOne();
    AttributeMappingOneToOne oneToOneMapping = null;
    for (AttributeMappingOneToOne mapping : mappings)
    {
      if (mapping.getAttributeName().equals(accessorAttribute))
      {
        oneToOneMapping = mapping;
        break;
      }
    }
    if (oneToOneMapping==null)
    {
      MessageUtils.handleError("Cannot execute getAsParent, no one-to-one mapping found between "+child.getClass().getName()+" and "+entityClass.getName());
    }
    return getAsParent(entityClass, child, oneToOneMapping );
  }

  public Entity getAsParent(Class entityClass, Entity child, AttributeMappingOneToOne oneToOneMapping)
  {
    ClassMappingDescriptor childDescriptor = ClassMappingDescriptor.getInstance(child.getClass());
    ClassMappingDescriptor parentDescriptor = ClassMappingDescriptor.getInstance(entityClass);
    Map<String,String> columnMappings = oneToOneMapping.getColumnMappings();
    Iterator<String> sourceColumns = columnMappings.keySet().iterator();
    List<BindParamInfo> bindParamInfos = new ArrayList<BindParamInfo>();
    while (sourceColumns.hasNext())
    {
      String sourceColumn = sourceColumns.next();
      String targetColumn = columnMappings.get(sourceColumn);
      // lookup attribute mapping for the source column in childDescriptor to get the FK value
      AttributeMapping attrMapping = childDescriptor.findAttributeMappingByColumnName(sourceColumn);
      Object value = child.getAttributeValue(attrMapping.getAttributeName());
      if (value==null)
      {
        //foreign key attr is null, no parent entity to be found
        break;
      }
      // lookup corresponding attribute mapping in parentDescriptor and
      // create bind param info for parent entity
      AttributeMapping refAttrMapping = parentDescriptor.findAttributeMappingByColumnName(targetColumn);
      BindParamInfo bp = constructBindParamInfo(parentDescriptor.getClazz(), refAttrMapping);
      bp.setValue(value);
      bindParamInfos.add(bp);
    }
    if (bindParamInfos.size()>0)
    {
      List<Entity> entities= find(entityClass.getName(), bindParamInfos);      
      if (entities.size()>0)
      {
        return entities.get(0);
      }
      else
      {
        return null;
      }
    }
    return null;
  }


  /**
   * Resets entity attributes with latest values from database row
   * @param entity
   */
  public void resetEntity(Entity entity)
  {
    findByKey(entity.getClass(),EntityUtils.getEntityKey(entity),false);
  }

}
