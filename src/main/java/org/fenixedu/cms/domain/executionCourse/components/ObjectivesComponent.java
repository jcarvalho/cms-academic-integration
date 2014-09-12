package org.fenixedu.cms.domain.executionCourse.components;

import net.sourceforge.fenixedu.domain.CurricularCourse;
import net.sourceforge.fenixedu.domain.Curriculum;
import net.sourceforge.fenixedu.domain.ExecutionCourse;
import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.domain.component.ComponentType;
import org.fenixedu.bennu.cms.rendering.TemplateContext;
import org.fenixedu.cms.domain.executionCourse.CompetenceCourseBean;
import org.fenixedu.cms.domain.executionCourse.ExecutionCourseSite;

import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@ComponentType(name = "CompetenceCourse", description = "Competence Course information for an Execution Course")
public class ObjectivesComponent extends ObjectivesComponent_Base {
    
    @Override
    public void handle(Page page, TemplateContext componentContext, TemplateContext globalContext) {
        ExecutionCourse executionCourse = ((ExecutionCourseSite) page.getSite()).getExecutionCourse();
        globalContext.put("executionPeriod", executionCourse.getExecutionPeriod());
        globalContext.put("competenceCourseBeans", CompetenceCourseBean.approvedCompetenceCourses(executionCourse));
        globalContext.put("curriculumByCurricularCourse", curriculumsByCurricularCourses(executionCourse));
    }

    private Map<CurricularCourse, Curriculum> curriculumsByCurricularCourses(ExecutionCourse executionCourse) {
        Date end = executionCourse.getExecutionPeriod().getExecutionYear().getEndDate();
        return executionCourse
                .getCurricularCoursesSortedByDegreeAndCurricularCourseName()
                .stream()
                .filter(curricularCourse -> !curricularCourse.isBolonhaDegree())
                .collect(
                        Collectors.toMap(Function.identity(),
                                curricularCourse -> curricularCourse.findLatestCurriculumModifiedBefore(end)));
    }

}
