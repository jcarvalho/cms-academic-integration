package org.fenixedu.cms.domain.unit;

import net.sourceforge.fenixedu.domain.Employee;
import net.sourceforge.fenixedu.domain.organizationalStructure.Party;
import net.sourceforge.fenixedu.domain.organizationalStructure.Unit;
import net.sourceforge.fenixedu.domain.person.RoleType;
import org.apache.commons.beanutils.BeanComparator;
import org.fenixedu.cms.domain.Page;
import org.fenixedu.cms.domain.component.ComponentType;
import org.fenixedu.cms.rendering.TemplateContext;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ComponentType(name = "departmentEmployees", description = "Emplyees information for a Department")
public class EmployeesComponent extends UnitSiteComponent {
    private static final BeanComparator employeeComparator = new BeanComparator("person", Party.COMPARATOR_BY_NAME_AND_ID);

    @Override public void handle(Page page, TemplateContext componentContext, TemplateContext globalContext) {
        Map<Unit, List<Employee>> employeesMap = nonTeacherEmployeesByWorkingPlace(unit(page));
        globalContext.put("hasEmployeesNoArea", nonTeacherEmployeesWithoutWorkingPlace(unit(page)).findAny().isPresent());
        globalContext.put("employeesNoArea", nonTeacherEmployeesWithoutWorkingPlace(unit(page)));
        globalContext.put("employeesByArea", employeesMap);
    }

    private Map<Unit, List<Employee>> nonTeacherEmployeesByWorkingPlace(Unit unit) {
        return nonTeacherEmployeesWithWorkingPlace(unit).collect(Collectors.groupingBy(Employee::getCurrentWorkingPlace));
    }

    private Stream<Employee> nonTeacherEmployeesWithoutWorkingPlace(Unit unit) {
        return nonTeacherEmployees(unit).filter(employee->employee.getCurrentWorkingPlace()==null);
    }

    private Stream<Employee> nonTeacherEmployeesWithWorkingPlace(Unit unit) {
        return nonTeacherEmployees(unit).filter(employee->employee.getCurrentWorkingPlace()!=null);
    }

    private Stream<Employee> nonTeacherEmployees(Unit unit) {
        return unit.getAllCurrentNonTeacherEmployees().stream().filter(employee->!employee.getPerson().hasRole(RoleType.TEACHER)).sorted(employeeComparator);
    }
}