package org.fenixedu.cms.domain.scientificArea;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toCollection;

import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import net.sourceforge.fenixedu.domain.Teacher;
import net.sourceforge.fenixedu.domain.organizationalStructure.Unit;
import net.sourceforge.fenixedu.domain.personnelSection.contracts.ProfessionalCategory;

import org.fenixedu.cms.domain.Page;
import org.fenixedu.cms.domain.component.ComponentType;
import org.fenixedu.cms.domain.unit.UnitSiteComponent;
import org.fenixedu.cms.rendering.TemplateContext;
import org.joda.time.YearMonthDay;

@SuppressWarnings("deprecation")
@ComponentType(description = "Shows the teachers of the site's Scientific Area", name = "Scientific Area Teachers")
public class ScientificAreaTeachers extends UnitSiteComponent {

    @Override
    public void handle(Page page, TemplateContext componentContext, TemplateContext globalContext) {
        Unit unit = unit(page);

        Map<ProfessionalCategory, SortedSet<Teacher>> teachers =
                unit.getDepartmentUnit()
                        .getDepartment()
                        .getAllTeachers(new YearMonthDay(), new YearMonthDay().plusDays(1))
                        .stream()
                        .filter(teacher -> teacher.getCurrentSectionOrScientificArea() == unit && teacher.getCategory() != null)
                        .collect(
                                groupingBy(Teacher::getCategory, TreeMap::new, toCollection(() -> new TreeSet<Teacher>(
                                        Teacher.TEACHER_COMPARATOR_BY_CATEGORY_AND_NUMBER))));

        globalContext.put("scientificAreaTeachers", teachers);
        globalContext.put("categories", teachers.keySet());
    }
}
