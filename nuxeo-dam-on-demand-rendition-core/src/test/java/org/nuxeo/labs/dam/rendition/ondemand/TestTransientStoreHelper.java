package org.nuxeo.labs.dam.rendition.ondemand;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import javax.inject.Inject;
import java.io.File;

@RunWith(FeaturesRunner.class)
@Features(PlatformFeature.class)
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
@Deploy({
        "nuxeo-dam-on-demand-rendition-core",
})
public class TestTransientStoreHelper {

    @Inject
    protected CoreSession session;

    @Test
    public void testKeyGeneration() {
        File file = new File(getClass().getResource("/files/nuxeo.3gp").getPath());
        DocumentModel doc = session.createDocumentModel(
                session.getRootDocument().getPathAsString(),"doc","File");
        doc.setPropertyValue("file:content",new FileBlob(file));
        doc = session.createDocument(doc);

        Properties properties = new Properties();
        properties.put("prop1","prop1");
        String key1 = TransientStoreHelper.buildTransientStoreKey(doc,properties);
        properties.put("prop2","prop2");
        String key2 = TransientStoreHelper.buildTransientStoreKey(doc,properties);

        Assert.assertNotEquals(key1,key2);

    }
}
