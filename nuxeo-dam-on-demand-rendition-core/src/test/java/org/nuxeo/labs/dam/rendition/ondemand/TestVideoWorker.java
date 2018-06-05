package org.nuxeo.labs.dam.rendition.ondemand;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.labs.dam.rendition.ondemand.worker.OnDemandVideoRenditionWorker;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.transaction.TransactionHelper;

import javax.inject.Inject;
import java.io.File;
import java.io.Serializable;

@RunWith(FeaturesRunner.class)
@Features(PlatformFeature.class)
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
@Deploy({
        "nuxeo-dam-on-demand-rendition-core",
        "org.nuxeo.ecm.platform.tag",
        "org.nuxeo.ecm.platform.video.api",
        "org.nuxeo.ecm.platform.video.core",
        "org.nuxeo.ecm.platform.video.convert"
})
public class TestVideoWorker {

    @Inject
    protected CoreSession session;

    @Test(expected = NuxeoException.class)
    public void testWorker() {

        Blob blob = new FileBlob(new File(getClass().getResource("/files/nuxeo.3gp").getPath()));
        DocumentModel video = session.createDocumentModel(
                session.getRootDocument().getPathAsString(),"video","Video");
        video.setPropertyValue("file:content", (Serializable) blob);
        video = session.createDocument(video);

        TransactionHelper.commitOrRollbackTransaction();
        TransactionHelper.startTransaction();

        Properties properties = new Properties();
        properties.put("format","ondemandConvertToMP4");
        properties.put("width","320");
        properties.put("height","240");

        OnDemandVideoRenditionWorker worker = new OnDemandVideoRenditionWorker(
                video,"aaa",session.getPrincipal().getName(),properties);

        worker.work();

    }
}
