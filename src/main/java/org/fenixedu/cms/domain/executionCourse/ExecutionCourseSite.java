package org.fenixedu.cms.domain.executionCourse;

import net.sourceforge.fenixedu.domain.ExecutionCourse;
import pt.ist.fenixframework.Atomic;

public class ExecutionCourseSite extends ExecutionCourseSite_Base {

    public ExecutionCourseSite(ExecutionCourse executionCourse) {
        setExecutionCourse(executionCourse);
    }

    @Override
    @Atomic
    public void delete() {
        setExecutionCourse(null);
        super.delete();
    }

}
