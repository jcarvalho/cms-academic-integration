package org.fenixedu.cms.domain;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import net.sourceforge.fenixedu.domain.ExecutionCourse;
import net.sourceforge.fenixedu.domain.LessonPlanning;
import net.sourceforge.fenixedu.domain.ShiftType;

import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.rendering.TemplateContext;

public class ExecutionCourseLessonsPlanning extends ExecutionCourseLessonsPlanning_Base {

    @Override
    public void handle(Page page, HttpServletRequest req, TemplateContext componentContext, TemplateContext globalContext) {
        ExecutionCourse executionCourse = ((ExecutionCourseSite) page.getSite()).getExecutionCourse();
        Map<ShiftType, List<LessonPlanning>> lessonPlanningsMap = new TreeMap<ShiftType, List<LessonPlanning>>();
        if (executionCourse.getSite().getLessonPlanningAvailable() != null
                && executionCourse.getSite().getLessonPlanningAvailable()) {
            for (ShiftType shiftType : executionCourse.getShiftTypes()) {
                List<LessonPlanning> lessonPlanningsOrderedByOrder = executionCourse.getLessonPlanningsOrderedByOrder(shiftType);
                if (!lessonPlanningsOrderedByOrder.isEmpty()) {
                    lessonPlanningsMap.put(shiftType, lessonPlanningsOrderedByOrder);
                }
            }
        }
        globalContext.put("lessonPlanningsMap", lessonPlanningsMap);
    }
    
    
}
