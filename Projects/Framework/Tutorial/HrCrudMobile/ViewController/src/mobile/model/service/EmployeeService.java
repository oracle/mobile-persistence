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

import mobile.model.Employee;


public class EmployeeService extends EntityCRUDService {

    public EmployeeService() {
    }

    protected Class getEntityClass() {
        return Employee.class;
    }

    protected String getEntityListName() {
        return "employee";
    }

    public Employee[] getEmployee() {
        List dataObjectList = getEntityList();

        Employee[] dataObjects = (Employee[]) dataObjectList.toArray(new Employee[dataObjectList.size()]);
        return dataObjects;
    }

    /**
     * This method is automatically called when using the Create operation on the employee collection
     * in the data control palette. It gets a new employee instance as argument and adds this instance to the
     * employee list.
     * Do NOT drag and drop this method from the data control palette, use the Create operation instead to ensure
     * that iterator binding and employee list stay in sync.
     * @param index
     * @param employee
     */
    public void addEmployee(int index, Employee employee) {
        addEntity(index, employee);
    }

    /**
     * This method is automatically called when using the Delete operation on the employee collection
     * in the data control palette. It removes the employee instance passed in from the employee list, deletes the
     * corresponding row from the database (if persisted) and calls the configured remove method on the remote
     * persistence manager.
     * Do NOT drag and drop this method from the data control palette, use the Delete operation instead to ensure
     * that iterator binding and employee list stay in sync.
     * @param employee
     */
    public void removeEmployee(Employee employee) {
        removeEntity(employee);
    }

    /**
     * Inserts or updates a employee using the configured persistence managers.
     * The insert or update is determined by calling isNewEntity on the employee instance.
     * @param employee
     */
    public void saveEmployee(Employee employee) {
        super.mergeEntity(employee);
    }

    /**
     * Retrieves all employee instances using the configured persistence managers and populates the employee list
     * with the result.
     * When this method is called for the first time, and a remote persistence manager is configured,
     * the data is fetched remotely and the local DB is populated with the results.
     */
    public void findAllEmployee() {
        super.findAll();
    }

    /**
     * Retrieves all employee instances using the findAll method on the remote persistence manager
     * and populates the employee list
     */
    public void findAllEmployeeRemote() {
        super.doRemoteFindAll();
    }

    /**
     * Retrieves the employee instances that match the searchValue filter using the configured persistence
     * managers and populates the employee list with the result.
     * By default, the search value is applied to all string attributes using a "startsWith" operator.
     * To customize the attributes on which the searchValue is applied, you can override method getQuickSearchAttributeNames.
     * If a find method is configured against the remote persistence manager, then this method will also
     * call this method.
     * @param searchValue
     */
    public void findEmployee(String searchValue) {
        super.find(searchValue);
    }


    /**
     * Synchronizes all pending data sync actions using the remote persistence manager
     * @param inBackground
     */
    public void synchronizeEmployee(Boolean inBackground) {
        super.synchronize(inBackground);
    }

    /**
     * Resets the values of the employee instance to the values as stored in the SQLite database. This method
     * will do nothing when the employee is not persisted to the database.
     * @param employee
     */
    public void resetEmployee(Employee employee) {
        super.resetEntity(employee);
    }

    /**
     * Returns true when there are pending employee data sync actions. Returns false if there are no such actions.
     */
    public boolean getHasEmployeeDataSynchActions() {
        return getDataSynchManager().getHasDataSynchActions();
    }
}


