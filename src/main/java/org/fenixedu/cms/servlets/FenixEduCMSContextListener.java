package org.fenixedu.cms.servlets;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import net.sourceforge.fenixedu.domain.ExecutionCourse;
import net.sourceforge.fenixedu.domain.Summary;

import org.fenixedu.bennu.cms.domain.Post;
import org.fenixedu.bennu.signals.DomainObjectEvent;
import org.fenixedu.bennu.signals.Signal;
import org.fenixedu.cms.domain.executionCourse.ExecutionCourseListener;
import org.fenixedu.cms.domain.executionCourse.SummaryListener;

@WebListener
public class FenixEduCMSContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        Signal.register(Summary.CREATED_SIGNAL, (DomainObjectEvent<Summary> event) -> {
            SummaryListener.updatePost(new Post(), event.getInstance());
        });
        Signal.register(Summary.DELETED_SIGNAL, (DomainObjectEvent<Summary> event) -> {
            event.getInstance().getPost().delete();
        });
        Signal.register(Summary.EDITED_SIGNAL, (DomainObjectEvent<Summary> event) -> {
            SummaryListener.updatePost(event.getInstance().getPost(), event.getInstance());
        });

        Signal.register(ExecutionCourse.CREATED_SIGNAL, (DomainObjectEvent<ExecutionCourse> event) -> {
            ExecutionCourseListener.create(event.getInstance());
        });
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
    }
}
