/*******************************************************************************
 Copyright © 2014, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 07-jan-2014   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.persistence.manager;

import java.util.List;

import oracle.ateam.sample.mobile.persistence.model.Entity;

public interface RemotePersistenceManager extends PersistenceManager
{
  boolean isFindAllSupported(Class clazz);

  boolean isFindAllInParentSupported(Class clazz, String accessorAttribute);
  
  boolean isGetAsParentSupported(Class clazz, String accessorAttribute);

  boolean isFindSupported(Class clazz);

  boolean isCreateSupported(Class clazz);

  boolean isUpdateSupported(Class clazz);

  boolean isMergeSupported(Class clazz);

  boolean isRemoveSupported(Class clazz);
  
  List findAllInParent(Class childEntityClass, Entity parent, String accessorAttribute);

  Entity getAsParent(Class parentEntityClass, Entity child, String accessorAttribute);

  void getCanonical(Entity entity);

  void invokeCustomMethod(Entity entity, String methodName);

  void setLocalPersistenceManager(DBPersistenceManager dBPersistenceManager);

  DBPersistenceManager getLocalPersistenceManager();
}
