package org.fenixedu.cms.domain.department.componenets;

import net.sourceforge.fenixedu.domain.Department;
import net.sourceforge.fenixedu.domain.Teacher;
import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.domain.component.ComponentType;
import org.fenixedu.bennu.cms.rendering.TemplateContext;
import org.fenixedu.cms.domain.department.DepartmentSite;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ComponentType(name = "departmentTeachers", description = "Teachers information for a Department")
public class DepartmentTeachers extends DepartmentTeachers_Base {

    @Override public void handle(Page page, TemplateContext componentContext, TemplateContext globalContext) {
        Department department = ((DepartmentSite) page.getSite()).getDepartment();
        globalContext.put("department", department);

        globalContext.put("categories", mapTeachersToField(departmentTeachers(department), Teacher::getCategory));
        globalContext.put("categoryTeachers", teachersByField(departmentTeachers(department), Teacher::getCategory));
        globalContext.put("teachersWithoutCategory", teachersWithoutField(departmentTeachers(department), Teacher::getCategory));

        globalContext.put("areas", mapTeachersToField(departmentTeachers(department), Teacher::getCurrentSectionOrScientificArea));
        globalContext.put("areaTeachers", teachersByField(departmentTeachers(department), Teacher::getCurrentSectionOrScientificArea));
        globalContext.put("teachersWithoutArea", teachersWithoutField(departmentTeachers(department), Teacher::getCurrentSectionOrScientificArea));
    }

    private Stream<Teacher> departmentTeachers(Department department) {
        return department.getAllCurrentTeachers().stream().sorted(Teacher.TEACHER_COMPARATOR_BY_CATEGORY_AND_NUMBER);
    }

    private <T> Stream<T> mapTeachersToField(Stream<Teacher> teachers, Function<Teacher, T> teacherField) {
        return teachers.map(teacherField).filter(Objects::nonNull);
    }

    private <T> Stream<Teacher> teachersWithoutField(Stream<Teacher> teachers, Function<Teacher, T> teacherField) {
        return teachers.filter(teacher -> teacherField.apply(teacher) == null);
    }

    private <T> Stream<Teacher> teachersWithField(Stream<Teacher> teachers, Function<Teacher, T> teacherField) {
        return teachers.filter(teacher -> teacherField.apply(teacher) != null);
    }

    private <T> Map<T, List<Teacher>> teachersByField(Stream<Teacher> teachers, Function<Teacher, T> teacherField) {
        return teachersWithField(teachers, teacherField).collect(Collectors.groupingBy(teacherField));
    }

}
