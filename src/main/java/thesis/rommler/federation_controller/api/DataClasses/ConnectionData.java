package thesis.rommler.federation_controller.api.DataClasses;

public class ConnectionData {

    public String requester_ip;
    public int requester_REST_port;
    public int socket_port;

    public ConnectionData(String requester_ip, int requester_REST_port, int socket_port){
        this.requester_ip = requester_ip;
        this.requester_REST_port = requester_REST_port;
        this.socket_port = socket_port;
    }
}
