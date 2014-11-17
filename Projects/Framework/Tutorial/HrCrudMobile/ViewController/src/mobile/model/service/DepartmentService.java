/*******************************************************************************
 Copyright © 2014, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 10-nov-2014   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package mobile.model.service;


import java.util.ArrayList;

import java.util.List;

import oracle.ateam.sample.mobile.persistence.util.EntityUtils;
import oracle.ateam.sample.mobile.persistence.service.EntityCRUDService;

import mobile.model.Department;


public class DepartmentService extends EntityCRUDService {

    public DepartmentService() {
    }

    protected Class getEntityClass() {
        return Department.class;
    }

    protected String getEntityListName() {
        return "department";
    }

    public Department[] getDepartment() {
        List dataObjectList = getEntityList();

        Department[] dataObjects = (Department[]) dataObjectList.toArray(new Department[dataObjectList.size()]);
        return dataObjects;
    }

    /**
     * This method is automatically called when using the Create operation on the department collection
     * in the data control palette. It gets a new department instance as argument and adds this instance to the
     * department list.
     * Do NOT drag and drop this method from the data control palette, use the Create operation instead to ensure
     * that iterator binding and department list stay in sync.
     * @param index
     * @param department
     */
    public void addDepartment(int index, Department department) {
        addEntity(index, department);
    }

    /**
     * This method is automatically called when using the Delete operation on the department collection
     * in the data control palette. It removes the department instance passed in from the department list, deletes the
     * corresponding row from the database (if persisted) and calls the configured remove method on the remote
     * persistence manager.
     * Do NOT drag and drop this method from the data control palette, use the Delete operation instead to ensure
     * that iterator binding and department list stay in sync.
     * @param department
     */
    public void removeDepartment(Department department) {
        removeEntity(department);
    }

    /**
     * Inserts or updates a department using the configured persistence managers.
     * The insert or update is determined by calling isNewEntity on the department instance.
     * @param department
     */
    public void saveDepartment(Department department) {
        super.mergeEntity(department);
    }

    /**
     * Retrieves all department instances using the configured persistence managers and populates the department list
     * with the result.
     * When this method is called for the first time, and a remote persistence manager is configured,
     * the data is fetched remotely and the local DB is populated with the results.
     */
    public void findAllDepartment() {
        super.findAll();
    }

    /**
     * Retrieves all department instances using the findAll method on the remote persistence manager
     * and populates the department list
     */
    public void findAllDepartmentRemote() {
        super.doRemoteFindAll();
    }

    /**
     * Retrieves the department instances that match the searchValue filter using the configured persistence
     * managers and populates the department list with the result.
     * By default, the search value is applied to all string attributes using a "startsWith" operator.
     * To customize the attributes on which the searchValue is applied, you can override method getQuickSearchAttributeNames.
     * If a find method is configured against the remote persistence manager, then this method will also
     * call this method.
     * @param searchValue
     */
    public void findDepartment(String searchValue) {
        super.find(searchValue);
    }


    /**
     * Synchronizes all pending data sync actions using the remote persistence manager
     * @param inBackground
     */
    public void synchronizeDepartment(Boolean inBackground) {
        super.synchronize(inBackground);
    }

    /**
     * Resets the values of the department instance to the values as stored in the SQLite database. This method
     * will do nothing when the department is not persisted to the database.
     * @param department
     */
    public void resetDepartment(Department department) {
        super.resetEntity(department);
    }

    /**
     * Returns true when there are pending department data sync actions. Returns false if there are no such actions.
     */
    public boolean getHasDepartmentDataSynchActions() {
        return getDataSynchManager().getHasDataSynchActions();
    }
}


