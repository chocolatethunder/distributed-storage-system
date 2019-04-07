package app.handlers;

import app.MessageType;
import app.Request;
import app.NetworkUtils;
import app.TcpPacket;
import app.chunk_utils.IndexFile;

import java.net.Socket;

/**
 *This is the ServiceHandler Factory class that return a ServiceHandler Runnable depending on the requestype
 */
public class ServiceHandlerFactory {

    /**
     * Factory method to create Service Handler Thread for specific request type
     * @param request
     * @param socket
     * @param socket
     * @return
     */
    public static Runnable getServiceHandler(TcpPacket request, Socket socket) {
        MessageType requestType = request.getMessageType();
        Request toProcess = null;
        if (requestType != MessageType.LIST){
            toProcess = NetworkUtils.getPacketContents(request);
        }
        switch (requestType) {
            case DOWNLOAD:
                return new DownloadServiceHandler(socket, toProcess);
            case UPLOAD:
                return new UploadServiceHandler(socket, toProcess);
            case DELETE:
                return new DeleteServiceHandler(socket, toProcess);
            case LIST:
                return new FileListServiceHandler(socket);
            default:
                //
               // throw new Exception("Wrong request type");
                return null;
        }

    }
}
