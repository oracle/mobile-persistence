/*******************************************************************************
 Copyright © 2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
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
  private Map<Class,Map<EntityKey,Entity>> cache = new HashMap<Class,Map<EntityKey,Entity>>();
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
  public void addEntity(Entity entity)
  {
     Map<EntityKey,Entity> entityCache = findOrCreateEntityCache(entity.getClass());
     entityCache.put(new EntityKey(EntityUtils.getEntityKey(entity)), entity);
  }

  /**
   * Addes a list of entities to the cache.
   * @param entities
   */
  public void addEntities(List<Entity> entities)
  {
    if (entities.size()>0)
    {
      Class entityClass = entities.get(0).getClass();
      Map<EntityKey,Entity> entityCache = findOrCreateEntityCache(entityClass);
      for (Entity entity : entities)
      {
        entityCache.put(new EntityKey(EntityUtils.getEntityKey(entity)), entity);
      }
    }
  }

  public void removeEntity(Entity entity)
  {
     Map<EntityKey,Entity> entityCache = findOrCreateEntityCache(entity.getClass());
     entityCache.remove(new EntityKey(EntityUtils.getEntityKey(entity)));
  }

  public Map<EntityKey,Entity> findOrCreateEntityCache(Class entityClass)
  {
     Map<EntityKey,Entity> entityCache = cache.get(entityClass);
     if (entityCache==null)
     {
       entityCache = new HashMap<EntityKey,Entity>();
       cache.put(entityClass, entityCache);
     }
     return entityCache;
  }

  public Entity findByUID(Class entityClass, Object[] uid)
  {
     Map entityCache = (Map) cache.get(entityClass);
     if (entityCache!=null)
     {
       return (Entity) entityCache.get(new EntityKey(uid));         
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
    Map<EntityKey,Entity> entityCache = cache.get(entityClass);
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
