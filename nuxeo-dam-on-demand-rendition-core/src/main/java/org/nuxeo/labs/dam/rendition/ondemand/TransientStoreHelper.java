package org.nuxeo.labs.dam.rendition.ondemand;

import org.apache.commons.codec.digest.DigestUtils;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.transientstore.api.TransientStore;
import org.nuxeo.ecm.core.transientstore.api.TransientStoreService;
import org.nuxeo.runtime.api.Framework;

import java.util.Map;

public class TransientStoreHelper {

    public static final String CACHE_NAME = "onDemandRenditions";

    public static TransientStore getTransientStore() {
        TransientStoreService tss = Framework.getService(TransientStoreService.class);
        return tss.getStore(CACHE_NAME);
    }

    public static String buildTransientStoreKey(DocumentModel doc, Properties properties) {
        StringBuffer sb = new StringBuffer();
        sb.append(doc.getId());
        sb.append("::");
        Blob blob = (Blob) doc.getPropertyValue("file:content");
        if (blob != null && blob.getDigest() != null) {
            sb.append(blob.getDigest());
            sb.append("::");
        }
        for(Map.Entry<String,String> entry: properties.entrySet()) {
            sb.append(entry.getKey()+entry.getValue());
            sb.append("::");
        }
        // Rendered documents might differ according to current user
        sb.append(doc.getCoreSession().getPrincipal().getName());
        return DigestUtils.md5Hex(sb.toString());
    }

}
