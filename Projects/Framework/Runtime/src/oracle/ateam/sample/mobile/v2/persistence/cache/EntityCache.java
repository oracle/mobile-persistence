/*******************************************************************************
 Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 20-apr-2015   Steven Davelaar
 1.1           Bug in removeEntity causing NPE, entityCache variable was not initialized
 08-jan-2015   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.v2.persistence.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oracle.ateam.sample.mobile.v2.persistence.model.Entity;
import oracle.ateam.sample.mobile.v2.persistence.util.EntityUtils;
import oracle.ateam.sample.mobile.util.ADFMobileLogger;


/**
 * This classes caches all entities. Entities can be accessed by their key   
 */
public class EntityCache
{
  private static ADFMobileLogger sLog = ADFMobileLogger.createLogger(EntityCache.class);
//  private  Map<Class<? extends Entity>,Map<EntityKey,? extends Entity>> cache = new HashMap();
  private  Map<Class,Map> cache = new HashMap<Class,Map>();
  private static EntityCache instance;
  
  public EntityCache()
  {
    super();
  }
  
  /**
   * Returns singleton instance of the cache
   * @return
   */
  public static EntityCache getInstance()
  {
    if (instance==null)
    {
      instance = new EntityCache();
    }
    return instance;
  }
  
  /**
   * Adds an entity to the cache if no entity with same key does exists yet in the cache
   * @param entity
   */
  public <E extends Entity> void addEntity(E entity)
  {
     Class entityClass =entity.getClass();
     Map<EntityKey,E> entityCache = findOrCreateEntityCache(entityClass);
     entityCache.put(new EntityKey(EntityUtils.getEntityKey(entity)), entity);
  }

  /**
   * Addes a list of entities to the cache.
   * @param entities
   */
  public <E extends Entity> void addEntities(List<E> entities)
  {
    if (entities.size()>0)
    {
      Class entityClass = entities.get(0).getClass();
      Map<EntityKey,E> entityCache = findOrCreateEntityCache(entityClass);
      for (E entity : entities)
      {
        entityCache.put(new EntityKey(EntityUtils.getEntityKey(entity)), entity);
      }
    }
  }

  public <E extends Entity> void removeEntity(E entity)
  {
    Class cls = entity.getClass();
    Map<EntityCache.EntityKey, E> entityCache = findOrCreateEntityCache(cls);
    entityCache.remove(new EntityKey(EntityUtils.getEntityKey(entity)));
  }

  public <E extends Entity> Map<EntityKey,E> findOrCreateEntityCache(Class<E> entityClass)
  {
    Map<EntityKey,E> entityCache = (Map<EntityKey, E>) cache.get(entityClass);
     if (entityCache==null)
     {
       entityCache = new HashMap<EntityKey,E>();
       cache.put(entityClass, entityCache);
     }
     return entityCache;
  }

  public <E extends Entity> E findByUID(Class entityClass, Object[] uid)
  {
     Map entityCache = (Map) cache.get(entityClass);
     if (entityCache!=null)
     {
       return (E)entityCache.get(new EntityKey(uid));         
     }
     return null;
  }

  /**
   *  Clears complete cache for all entities
   */
  public void clearAll()
  {
    cache.clear();
  }

  /**
   * Clears all entries in the cache for this entity type
   * @param entityClass
   */
  public void clear(Class entityClass)
  {
    Map entityCache = cache.get(entityClass);
    if (entityCache!=null)
    {
      entityCache.clear();
    }
  }


  class EntityKey
  {
    private Object[] keyValues;
    
    EntityKey(Object[] keyValues)
    {
      this.keyValues = keyValues;
    }
    
    public Object[] getKeyValues()
    {
      return this.keyValues;
    }
    
    public int hashCode()
    {
      return 1;
    }

    public boolean equals(Object obj)
    {
      if (obj.getClass()!=this.getClass())
      {
        return false;
      }
      EntityKey compareEntityKey = (EntityKey) obj;
      return EntityUtils.compareKeys(getKeyValues(), compareEntityKey.getKeyValues());
    }
    
  }

}
