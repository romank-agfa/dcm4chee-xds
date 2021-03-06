/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2012
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4chee.xds2.conf;

import java.util.Map;

import org.dcm4che3.conf.api.generic.ConfigClass;
import org.dcm4che3.conf.api.generic.ConfigField;
import org.dcm4che3.conf.api.generic.ReflectiveConfig;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.DeviceExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 */
@ConfigClass(commonName = "XCARespondingGW", objectClass = "xcaRespondingGW", nodeName = "xcaRespondingGW")
public class XCARespondingGWCfg extends DeviceExtension {

    public static final Logger log = LoggerFactory.getLogger(XCARespondingGWCfg.class);

    private static final long serialVersionUID = -8258532093950989486L;

    private static final String DEFAULTID = "*";

    @ConfigField(name = "xdsApplicationName")
    private String applicationName;

    @ConfigField(name = "xdsHomeCommunityID")
    private String homeCommunityID;

    @ConfigField(name = "xdsSoapMsgLogDir")
    private String soapLogDir;

    @ConfigField(name = "xdsQueryUrl")
    private String queryUrl;

    @ConfigField(name = "xdsRetrieveUrl")
    private String retrieveUrl;

    @ConfigField(name = "xdsRegistry")
    private Device registry;

    @ConfigField(mapName = "xdsRepositories", mapKey = "xdsRepositoryUid", name = "xdsRepository", mapElementObjectClass = "xdsRepositoryByUid")
    private Map<String, Device> repositoryDeviceByUidMap;

    public String getRepositoryURL(String repositoryID) {
        try {
            return repositoryDeviceByUidMap.get(repositoryID).getDeviceExtensionNotNull(XdsRepository.class).getRetrieveUrl();
        } catch (Exception e) {

            try {
                String url = repositoryDeviceByUidMap.get(DEFAULTID).getDeviceExtensionNotNull(XdsRepository.class).getRetrieveUrl();
                log.warn("Using default repository for repository UID {}!", repositoryID);
                return url;
            } catch (Exception ee) {
                throw new RuntimeException("Cannot retrieve repository URL for repository UID " + repositoryID, e);
            }

        }
    }

    public String getRegistryURL() {
        try {
            return registry.getDeviceExtensionNotNull(XdsRegistry.class).getQueryUrl();
        } catch (Exception e) {
            throw new RuntimeException("Cannot retrieve registry URL", e);
        }
    }

    public String getQueryUrl() {
        return queryUrl;
    }

    public void setQueryUrl(String queryUrl) {
        this.queryUrl = queryUrl;
    }

    public String getRetrieveUrl() {
        return retrieveUrl;
    }

    public void setRetrieveUrl(String retrieveUrl) {
        this.retrieveUrl = retrieveUrl;
    }

    public Device getRegistry() {
        return registry;
    }

    public void setRegistry(Device registry) {
        this.registry = registry;
    }

    public Map<String, Device> getRepositoryDeviceByUidMap() {
        return repositoryDeviceByUidMap;
    }

    public void setRepositoryDeviceByUidMap(Map<String, Device> repositoryDeviceByUidMap) {
        this.repositoryDeviceByUidMap = repositoryDeviceByUidMap;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public final void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getHomeCommunityID() {
        return homeCommunityID;
    }

    public void setHomeCommunityID(String homeCommunityID) {
        this.homeCommunityID = homeCommunityID;
    }

    public String getSoapLogDir() {
        return soapLogDir;
    }

    public void setSoapLogDir(String soapLogDir) {
        this.soapLogDir = soapLogDir;
    }

    @Override
    public void reconfigure(DeviceExtension from) {
        XCARespondingGWCfg src = (XCARespondingGWCfg) from;
        ReflectiveConfig.reconfigure(src, this);
    }

}
