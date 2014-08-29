package org.fenixedu.cms.domain.department.componenets;

import net.sourceforge.fenixedu.domain.Degree;
import net.sourceforge.fenixedu.domain.Department;
import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.domain.component.ComponentType;
import org.fenixedu.bennu.cms.rendering.TemplateContext;
import org.fenixedu.cms.domain.department.DepartmentSite;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.partitioningBy;
@ComponentType(name = "departmentDegrees", description = "Degrees information for a Department")
public class DepartmentDegrees extends DepartmentDegrees_Base {

    @Override public void handle(Page page, TemplateContext componentContext, TemplateContext global) {
        Department department = ((DepartmentSite) page.getSite()).getDepartment();
        Map<Boolean, List<Degree>> departmentsByActivity = department.getDegreesSet().stream().collect(partitioningBy(Degree::isActive));

        global.put("department", department);
        global.put("activeDegrees", departmentsByActivity.get(true));
        global.put("inactiveDegrees", departmentsByActivity.get(false));
        global.put("degreesByType", department.getDegreesSet().stream().collect(groupingBy(Degree::getDegreeType)));
    }


}
