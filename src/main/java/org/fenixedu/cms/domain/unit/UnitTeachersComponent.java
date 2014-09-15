package org.fenixedu.cms.domain.unit;

import net.sourceforge.fenixedu.domain.Teacher;
import net.sourceforge.fenixedu.domain.organizationalStructure.Unit;
import net.sourceforge.fenixedu.domain.personnelSection.contracts.ProfessionalCategory;
import org.fenixedu.cms.domain.Page;
import org.fenixedu.cms.domain.component.ComponentType;
import org.fenixedu.cms.rendering.TemplateContext;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toCollection;

@ComponentType(name = "departmentTeachers", description = "Teachers information for a Department")
public class UnitTeachersComponent extends UnitSiteComponent {

    @Override public void handle(Page page, TemplateContext componentContext, TemplateContext globalContext) {
        Unit unit = unit(page);
        globalContext.put("teachersByCategory", teachersByCategory(unit));
        globalContext.put("teachersByArea", teachersByArea(unit));
        globalContext.put("teachersWithoutArea", teachersWithoutArea(unit));
        globalContext.put("hasTeachersWithoutArea", teachersWithoutArea(unit).findAny().isPresent());

    }

    private Map<ProfessionalCategory, List<Teacher>> teachersByCategory(Unit unit) {
        return unitTeachers(unit).filter(teacher -> teacher.getCategory() != null).collect(groupingBy(Teacher::getCategory));
    }

    private Map<Unit, List<Teacher>> teachersByArea(Unit unit) {
         return unitTeachers(unit).filter(teacher -> teacher.getCurrentSectionOrScientificArea() != null)
                .collect(groupingBy(Teacher::getCurrentSectionOrScientificArea));
    }

    private Stream<Teacher> teachersWithoutArea(Unit unit) {
        return unitTeachers(unit).filter(teacher->teacher.getCurrentSectionOrScientificArea() == null);
    }

    private Stream<Teacher> unitTeachers(Unit unit) {
        return unit.getDepartment().getAllCurrentTeachers().stream().sorted(Teacher.TEACHER_COMPARATOR_BY_CATEGORY_AND_NUMBER);
    }

}