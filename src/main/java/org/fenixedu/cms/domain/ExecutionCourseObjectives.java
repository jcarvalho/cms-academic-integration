package org.fenixedu.cms.domain;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import net.sourceforge.fenixedu.domain.CurricularCourse;
import net.sourceforge.fenixedu.domain.Curriculum;
import net.sourceforge.fenixedu.domain.ExecutionCourse;
import net.sourceforge.fenixedu.domain.ExecutionSemester;

import org.fenixedu.bennu.cms.domain.ComponentType;
import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.domain.Site;
import org.fenixedu.bennu.cms.rendering.TemplateContext;

@ComponentType(type = "objectives", name = "Objectives", description = "Objectives for an Execution Course")
public class ExecutionCourseObjectives extends ExecutionCourseObjectives_Base {
    
    @Override
    public void handle(Page page, HttpServletRequest req, TemplateContext local, TemplateContext global) {
        ExecutionCourse executionCourse = executionCourse(page.getSite());
        global.put("executionPeriod", executionCourse.getExecutionPeriod());
        global.put("competenceCourseBeans", getCompetenceCourseBeans(executionCourse));
        global.put("curriculumByCurricularCourse", curriculumsByCurricularCourses(executionCourse));
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

    private List<CompetenceCourseBean> getCompetenceCourseBeans(ExecutionCourse executionCourse) {
        return executionCourse
                .getCurricularCoursesIndexedByCompetenceCourse()
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().isApproved())
                .map(entry -> new CompetenceCourseBean(entry.getKey(), entry.getValue(), executionCourse.getExecutionPeriod()))
                .collect(Collectors.toList());
    }
    

    private ExecutionSemester executionPeriod(ExecutionCourse executionCourse) {
        return executionCourse.getExecutionPeriod();
    }

    public ExecutionCourse executionCourse(Site site) {
        return site instanceof ExecutionCourseSite ? ((ExecutionCourseSite) site).getExecutionCourse() : null;
    }

}
