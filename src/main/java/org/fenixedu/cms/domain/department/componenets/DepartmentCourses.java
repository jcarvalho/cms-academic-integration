package org.fenixedu.cms.domain.department.componenets;

import net.sourceforge.fenixedu.dataTransferObject.commons.CurricularCourseByExecutionSemesterBean;
import net.sourceforge.fenixedu.domain.Department;
import net.sourceforge.fenixedu.domain.Employee;
import net.sourceforge.fenixedu.domain.Person;
import net.sourceforge.fenixedu.domain.organizationalStructure.DepartmentUnit;
import net.sourceforge.fenixedu.domain.organizationalStructure.ScientificAreaUnit;
import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.rendering.TemplateContext;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.cms.domain.department.DepartmentSite;

import java.util.List;

public class DepartmentCourses extends DepartmentCourses_Base {

    private CurricularCourseByExecutionSemesterBean personDepartment;

    @Override public void handle(Page page, TemplateContext componentContext, TemplateContext globalContext) {
        Department department = ((DepartmentSite) page.getSite()).getDepartment();

        getScientificAreaUnits(department.getDepartmentUnit());
        globalContext.put("department", department);
        globalContext.put("departmentUnit", department.getDepartmentUnit());
    }

    public List<ScientificAreaUnit> getScientificAreaUnits(DepartmentUnit unit) {
        DepartmentUnit departmentUnit = null;
        if (unit != null) {
            departmentUnit = unit;
        } else if (getPersonDepartment() != null) {
            departmentUnit = getPersonDepartment().getDepartmentUnit();
        }
        return (departmentUnit != null) ? departmentUnit.getScientificAreaUnits() : null;
    }

    public Department getPersonDepartment() {
        final User user = Authenticate.getUser();
        final Person person = user == null ? null : Person.userToPerson.apply(user);
        final Employee employee = person == null ? null : person.getEmployee();
        return employee == null ? null : employee.getCurrentDepartmentWorkingPlace();
    }

}
