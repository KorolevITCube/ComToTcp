package nlmk.com;

public class RequestWrapper {
    private final byte[] request;
    private final TcpController controllerLink;

    public byte[] getRequest() {
        return request;
    }

    public TcpController getControllerLink() {
        return controllerLink;
    }

    public RequestWrapper(byte[] request, TcpController controllerLink) {
        this.request = request;
        this.controllerLink = controllerLink;
    }
}
