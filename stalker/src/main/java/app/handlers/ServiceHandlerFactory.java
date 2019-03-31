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
     * @param index
     * @return
     */
    public static Runnable getServiceHandler(TcpPacket request, Socket socket, IndexFile index) {

        MessageType requestType = request.getMessageType();
        Request toProcess = NetworkUtils.getPacketContents(request);
        switch (requestType) {
            case DOWNLOAD:
                return new DownloadServiceHandler(socket, toProcess, index);
            case UPLOAD:
                return new UploadServiceHandler(socket, toProcess, index);
            case DELETE:
                return new DeleteServiceHandler(socket, toProcess, index);
            case LIST:
                return new FileListServiceHandler(socket, index);
            default:
                //
               // throw new Exception("Wrong request type");
                return null;
        }

    }
}
