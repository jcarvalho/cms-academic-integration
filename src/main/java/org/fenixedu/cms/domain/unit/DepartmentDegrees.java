package org.fenixedu.cms.domain.unit;

import net.sourceforge.fenixedu.domain.Degree;
import net.sourceforge.fenixedu.domain.Department;
import org.fenixedu.cms.domain.Page;
import org.fenixedu.cms.domain.component.ComponentType;
import org.fenixedu.cms.rendering.TemplateContext;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toList;

@ComponentType(name = "departmentDegrees", description = "Degrees information for a Department")
public class DepartmentDegrees extends UnitSiteComponent {

    @Override public void handle(Page page, TemplateContext componentContext, TemplateContext global) {
        Department department = unit(page).getDepartment();
        Predicate<Degree> isActive = Degree::isActive;
        global.put("activeTypes", department.getDegreesSet().stream().filter(isActive).map(Degree::getDegreeType).collect(toList()));
        global.put("inactiveTypes",  department.getDegreesSet().stream().filter(isActive.negate()).map(Degree::getDegreeType).collect(toList()));
        global.put("degreesByType", department.getDegreesSet().stream().collect(groupingBy(Degree::getDegreeType)));
    }


}