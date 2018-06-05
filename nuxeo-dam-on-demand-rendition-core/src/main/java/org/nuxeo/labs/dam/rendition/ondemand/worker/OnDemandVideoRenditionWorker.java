package org.nuxeo.labs.dam.rendition.ondemand.worker;

import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.io.download.DownloadService;
import org.nuxeo.ecm.core.transientstore.api.TransientStore;
import org.nuxeo.ecm.core.work.AbstractWork;
import org.nuxeo.labs.dam.rendition.ondemand.TransientStoreHelper;

import java.util.Collections;

import static org.nuxeo.ecm.platform.video.service.VideoConversionWork.CATEGORY_VIDEO_CONVERSION;

public class OnDemandVideoRenditionWorker extends AbstractWork {

    protected String transientStoreKey;
    protected String username;
    protected Properties properties;

    public OnDemandVideoRenditionWorker(DocumentModel doc, String transientStoreKey,
                                        String username, Properties properties) {
        super(doc.getRepositoryName() + ':' + doc.getId() +':' + username + "onDemandVideoRendition");
        setDocument(doc.getRepositoryName(), doc.getId());
        this.transientStoreKey = transientStoreKey;
        this.username = username;
        this.properties = properties;
    }

    @Override
    public void work() {
        setProgress(Progress.PROGRESS_INDETERMINATE);
        openSystemSession();
        DocumentModel doc = session.getDocument(new IdRef(docId));
        Blob originalBlob = (Blob) doc.getPropertyValue("file:content");
        commitOrRollbackTransaction();

        setStatus("Transcoding");
        //todo

        TransientStore ts = TransientStoreHelper.getTransientStore();
        if (!ts.exists(transientStoreKey)) {
            throw new NuxeoException("On Demand TransientStore entry can not be null");
        }
        ts.putBlobs(transientStoreKey,Collections.singletonList(originalBlob));
        ts.putParameter(transientStoreKey, DownloadService.TRANSIENT_STORE_PARAM_PROGRESS, 100);
        ts.setCompleted(transientStoreKey, true);

        startTransaction();
        setStatus("Done");
    }

    @Override
    public String getCategory() {
        return CATEGORY_VIDEO_CONVERSION;
    }

    @Override
    public String getTitle() {
        return this.id;
    }

}
