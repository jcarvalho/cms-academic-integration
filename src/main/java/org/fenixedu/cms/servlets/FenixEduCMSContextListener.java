package org.fenixedu.cms.servlets;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import net.sourceforge.fenixedu.domain.Summary;

import org.fenixedu.bennu.signals.Signal;
import org.fenixedu.cms.domain.SummaryListener;

@WebListener
public class FenixEduCMSContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent event) {
        Signal.registerWithTransaction(Summary.CREATED_SIGNAL, new SummaryListener.Create());
        Signal.registerWithTransaction(Summary.DELETED_SIGNAL, new SummaryListener.Delete());
        Signal.registerWithTransaction(Summary.EDITED_SIGNAL, new SummaryListener.Edit());

    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
    }
}
