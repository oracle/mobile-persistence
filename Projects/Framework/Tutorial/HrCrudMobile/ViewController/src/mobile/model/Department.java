package mobile.model;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import oracle.ateam.sample.mobile.persistence.util.EntityUtils;
import oracle.ateam.sample.mobile.persistence.service.ValueHolder;
import oracle.ateam.sample.mobile.persistence.service.ValueHolderInterface;
import oracle.ateam.sample.mobile.persistence.model.Entity;

import java.math.BigDecimal;

public class Department extends Entity {

    private BigDecimal departmentId;
    private String departmentName;
    private BigDecimal locationId;
    private BigDecimal managerId;

    private List employeesList = createIndirectList("employeesList");

    private ValueHolderInterface managerHolder = createValueHolder("manager");
    private transient Employee manager;

    public BigDecimal getDepartmentId() {
        return this.departmentId;
    }

    public void setDepartmentId(BigDecimal departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return this.departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public BigDecimal getLocationId() {
        return this.locationId;
    }

    public void setLocationId(BigDecimal locationId) {
        this.locationId = locationId;
    }

    public BigDecimal getManagerId() {
        return this.managerId;
    }

    public void setManagerId(BigDecimal managerId) {
        this.managerId = managerId;
    }


    public void setEmployeesList(List employeesList) {
        this.employeesList = employeesList;
    }

    /**
     * This method is called when entity instance is recreated from persisted JSON string in DataSynchAction
     */
    public void setEmployeesList(Employee[] employeesList) {
        this.employeesList = Arrays.asList(employeesList);
    }

    public List getEmployeesList() {
        return this.employeesList;
    }

    public Employee[] getEmployees() {
        List dataObjectList = getEmployeesList();

        return (Employee[]) dataObjectList.toArray(new Employee[dataObjectList.size()]);
    }

    public void addEmployee(int index, Employee employee) {
        employee.setIsNewEntity(true);
        EntityUtils.generatePrimaryKeyValue(employee, 1);
        employee.setDepartmentId(getDepartmentId());
        getEmployeesList().add(index, employee);
    }

    public void removeEmployee(Employee employee) {
        getEmployeesList().remove(employee);
    }


    public Employee getManager() {
        return (Employee) this.managerHolder.getValue();
    }

    protected ValueHolderInterface getManagerHolder() {
        return this.managerHolder;
    }

    public void setManager(Employee manager) {
        this.managerHolder.setValue(manager);
    }

    protected void setManagerHolder(ValueHolderInterface manager) {
        this.managerHolder = manager;
    }


}
