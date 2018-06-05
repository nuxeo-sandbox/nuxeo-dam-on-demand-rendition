package org.nuxeo.labs.dam.rendition.ondemand;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.blobholder.SimpleBlobHolder;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.convert.api.ConversionService;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import javax.inject.Inject;
import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

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
public class TestVideoConverter {

    @Inject
    protected ConversionService conversionService;

    @Test
    public void testConverter() {
        Blob blob = new FileBlob(new File(getClass().getResource("/files/nuxeo.3gp").getPath()));
        Map<String,Serializable> params = new HashMap<>();
        params.put("format","ondemandConvertToMP4");
        params.put("width","320");
        params.put("height","240");
        BlobHolder blobHolder = conversionService.convert(
                "ondemandConvertToMP4",new SimpleBlobHolder(blob),params);
    }
}
