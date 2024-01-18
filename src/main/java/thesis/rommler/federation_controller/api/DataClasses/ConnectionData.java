package thesis.rommler.federation_controller.api.DataClasses;

public class ConnectionData {

    public String requester_ip;
    public int requester_port;
    public int socket_port;

    public ConnectionData(String requester_ip, int requester_port, int socket_port){
        this.requester_ip = requester_ip;
        this.requester_port = requester_port;
        this.socket_port = socket_port;
    }
}
