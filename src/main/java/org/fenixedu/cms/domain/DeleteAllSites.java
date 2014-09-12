package org.fenixedu.cms.domain;

import org.fenixedu.bennu.scheduler.custom.CustomTask;

import pt.ist.fenixframework.Atomic;

public class DeleteAllSites extends CustomTask {

    @Override
    public void runTask() throws Exception {
        MigrationUtil.deleteAllSites();
    }

    @Override
    public Atomic.TxMode getTxMode() {
        return Atomic.TxMode.READ;
    }
}