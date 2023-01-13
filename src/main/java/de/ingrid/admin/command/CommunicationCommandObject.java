/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 - 2023 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.admin.command;

public class CommunicationCommandObject {

    private String _proxyServiceUrl;
    private String _iBusProxyServiceUrl;

    private Integer _port;
    private String _ip;
    
    private Boolean _isConnected;

    public String getProxyServiceUrl() {
        return _proxyServiceUrl;
    }

    public void setProxyServiceUrl(final String proxyServiceUrl) {
        _proxyServiceUrl = proxyServiceUrl;
    }

    public String getBusProxyServiceUrl() {
        return _iBusProxyServiceUrl;
    }

    public void setBusProxyServiceUrl(final String busProxyServiceUrl) {
        _iBusProxyServiceUrl = busProxyServiceUrl;
    }

    public Integer getPort() {
        return _port;
    }

    public void setPort(final Integer port) {
        _port = port;
    }

    public String getIp() {
        return _ip;
    }

    public void setIp(final String ip) {
        _ip = ip;
    }

    public boolean getIsConnected() {
        return _isConnected;
    }

    public void setIsConnected( boolean isConnected ) {
        this._isConnected = isConnected;
    }
}
