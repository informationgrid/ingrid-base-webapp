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
