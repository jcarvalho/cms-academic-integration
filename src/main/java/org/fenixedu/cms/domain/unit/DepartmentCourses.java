package org.fenixedu.cms.domain.unit;

import net.sourceforge.fenixedu.domain.Department;
import net.sourceforge.fenixedu.domain.Employee;
import net.sourceforge.fenixedu.domain.Person;
import net.sourceforge.fenixedu.domain.organizationalStructure.DepartmentUnit;
import net.sourceforge.fenixedu.domain.organizationalStructure.ScientificAreaUnit;
import org.fenixedu.cms.domain.Page;
import org.fenixedu.cms.domain.component.ComponentType;
import org.fenixedu.cms.rendering.TemplateContext;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.security.Authenticate;

import java.util.List;
import java.util.Optional;

@ComponentType(name = "departmentCourses", description = "Courses of a Department")
public class DepartmentCourses extends UnitSiteComponent {

        @Override public void handle(Page page, TemplateContext componentContext, TemplateContext globalContext) {
            if(unit(page) instanceof DepartmentUnit) {
                DepartmentUnit departmentUnit = (DepartmentUnit) unit(page);
                globalContext.put("scientificAreaUnits", getScientificAreaUnits(departmentUnit));
                globalContext.put("department", departmentUnit.getDepartment());
                globalContext.put("departmentUnit", departmentUnit);
            }
        }

        public List<ScientificAreaUnit> getScientificAreaUnits(DepartmentUnit unit) {
            DepartmentUnit departmentUnit = Optional.ofNullable(unit).orElseGet(() -> getPersonDepartmentUnit());
            return (departmentUnit != null) ? departmentUnit.getScientificAreaUnits() : null;
        }

        public DepartmentUnit getPersonDepartmentUnit() {
            final User user = Authenticate.getUser();
            final Person person = user == null ? null : Person.userToPerson.apply(user);
            final Employee employee = person == null ? null : person.getEmployee();
            final Department department = employee == null ? null : employee.getCurrentDepartmentWorkingPlace();
            return department == null ? null : department.getDepartmentUnit();
        }
}
