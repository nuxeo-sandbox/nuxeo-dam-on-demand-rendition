package org.nuxeo.labs.dam.rendition.ondemand.worker;

import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.blobholder.SimpleBlobHolder;
import org.nuxeo.ecm.core.convert.api.ConversionService;
import org.nuxeo.ecm.core.io.download.DownloadService;
import org.nuxeo.ecm.core.transientstore.api.TransientStore;
import org.nuxeo.ecm.core.work.AbstractWork;
import org.nuxeo.labs.dam.rendition.ondemand.TransientStoreHelper;
import org.nuxeo.runtime.api.Framework;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class OnDemandVideoRenditionWorker extends AbstractWork {

    public static final String ONDEMAND_VIDEO_CONVERSION = "ondemandVideoConversion";

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
        closeSession();

        setStatus("Transcoding");
        ConversionService conversionService = Framework.getService(ConversionService.class);
        BlobHolder blobHolder = conversionService.convert(
                properties.get("format"),
                new SimpleBlobHolder(originalBlob),
                getParameters(properties));

        TransientStore ts = TransientStoreHelper.getTransientStore();
        if (!ts.exists(transientStoreKey)) {
            throw new NuxeoException("On Demand TransientStore entry can not be null");
        }
        ts.putBlobs(transientStoreKey,Collections.singletonList(blobHolder.getBlob()));
        ts.putParameter(transientStoreKey, DownloadService.TRANSIENT_STORE_PARAM_PROGRESS, 100);
        ts.setCompleted(transientStoreKey, true);

        startTransaction();
        setStatus("Done");
    }

    @Override
    public String getCategory() {
        return ONDEMAND_VIDEO_CONVERSION;
    }

    @Override
    public String getTitle() {
        return this.id;
    }

    protected Map<String,Serializable> getParameters(Properties properties) {
        Map<String,Serializable> result = new HashMap<>();
        for (Map.Entry<String,String> entry : properties.entrySet()) {
            result.put(entry.getKey(),entry.getValue());
        }
        return result;
    }


}
