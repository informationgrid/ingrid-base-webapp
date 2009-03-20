package de.ingrid.admin;

public class CommunicationCommandObject {

    private String _proxyServiceUrl;
    private String _iBusProxyServiceUrl;
    private int _port;
    private String _ip;

    public String getProxyServiceUrl() {
        return _proxyServiceUrl;
    }

    public void setProxyServiceUrl(String proxyServiceUrl) {
        _proxyServiceUrl = proxyServiceUrl;
    }

    public String getBusProxyServiceUrl() {
        return _iBusProxyServiceUrl;
    }

    public void setBusProxyServiceUrl(String busProxyServiceUrl) {
        _iBusProxyServiceUrl = busProxyServiceUrl;
    }

    public int getPort() {
        return _port;
    }

    public void setPort(int port) {
        _port = port;
    }

    public String getIp() {
        return _ip;
    }

    public void setIp(String ip) {
        _ip = ip;
    }

}
