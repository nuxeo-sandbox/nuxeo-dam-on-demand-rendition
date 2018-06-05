package org.nuxeo.labs.dam.rendition.ondemand;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.AsyncBlob;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.core.transientstore.api.TransientStore;
import org.nuxeo.ecm.core.work.api.WorkManager;
import org.nuxeo.labs.dam.rendition.ondemand.operation.VideoOnDemandRendition;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.transaction.TransactionHelper;

import javax.inject.Inject;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RunWith(FeaturesRunner.class)
@Features(AutomationFeature.class)
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
@Deploy({
        "nuxeo-dam-on-demand-rendition-core",
        "org.nuxeo.ecm.platform.tag",
        "org.nuxeo.ecm.platform.video.api",
        "org.nuxeo.ecm.platform.video.core",
        "org.nuxeo.ecm.platform.video.convert"
})
public class TestVideoOnDemandRendition {

    @Inject
    protected CoreSession session;

    @Inject
    protected AutomationService automationService;

    @Inject
    protected WorkManager workManager;

    @Test
    public void testOperation() throws OperationException, InterruptedException {

        File file = new File(getClass().getResource("/files/nuxeo.3gp").getPath());
        DocumentModel video = session.createDocumentModel(
                session.getRootDocument().getPathAsString(),"video","Video");
        video.setPropertyValue("file:content",new FileBlob(file));
        video = session.createDocument(video);

        TransactionHelper.commitOrRollbackTransaction();
        TransactionHelper.startTransaction();

        OperationContext ctx = new OperationContext(session);
        ctx.setInput(video);
        Map<String, Object> params = new HashMap<>();
        params.put("properties", new Properties());
        AsyncBlob blob = (AsyncBlob) automationService.run(ctx, VideoOnDemandRendition.ID, params);

        Assert.assertNotNull(blob);

        workManager.awaitCompletion(60,TimeUnit.SECONDS);

        TransientStore store = TransientStoreHelper.getTransientStore();

        Assert.assertTrue(store.isCompleted(blob.getKey()));

    }
}
