package oraclesoa;

public class MessageHandlerThread implements Thread {
    private GatewayMessageHandler gatewayMessageHandler;
    private ClientMessageHandler clientMessageHandler;
    private JmsMessageStore messageStore;
}
