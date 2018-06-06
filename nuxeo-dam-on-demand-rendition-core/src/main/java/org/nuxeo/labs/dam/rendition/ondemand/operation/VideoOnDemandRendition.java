package org.nuxeo.labs.dam.rendition.ondemand.operation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.impl.blob.AsyncBlob;
import org.nuxeo.ecm.core.transientstore.api.TransientStore;
import org.nuxeo.ecm.core.work.api.Work;
import org.nuxeo.ecm.core.work.api.WorkManager;
import org.nuxeo.labs.dam.rendition.ondemand.TransientStoreHelper;
import org.nuxeo.labs.dam.rendition.ondemand.worker.OnDemandVideoRenditionWorker;
import org.nuxeo.runtime.api.Framework;

import java.util.Collections;

/**
 *
 */
@Operation(
        id=VideoOnDemandRendition.ID,
        category=Constants.CAT_CONVERSION,
        label="VideoOnDemandRendition",
        description="Request Video Rendition"
)
public class VideoOnDemandRendition {

    private static final Log log = LogFactory.getLog(VideoOnDemandRendition.class);

    public static final String ID = "VideoOnDemandRendition";

    public static final String WORKERID_KEY = "workerid";

    @Context
    protected CoreSession session;

    @Param(name = "properties")
    protected Properties properties;

    @OperationMethod
    public Blob run(DocumentModel doc) {
        // build the key
        String key = TransientStoreHelper.buildTransientStoreKey(doc, properties);

        TransientStore ts = TransientStoreHelper.getTransientStore();
        if (ts == null) {
            throw new NuxeoException("Unable to find Transient Store");
        }

        if (!ts.exists(key)) {
            log.trace("No rendition already initialized");
            Work work = new OnDemandVideoRenditionWorker(doc,key,session.getPrincipal().getName(),properties);
            ts.setCompleted(key, false);
            ts.putParameter(key, WORKERID_KEY, work.getId());
            Blob blob = new AsyncBlob(key);
            ts.putBlobs(key, Collections.singletonList(blob));
            Framework.getService(WorkManager.class).schedule(work, WorkManager.Scheduling.IF_NOT_SCHEDULED);
            return blob;
        } else {
            log.trace("Rendition already initialized");
            return new AsyncBlob(key);
        }
    }
}
