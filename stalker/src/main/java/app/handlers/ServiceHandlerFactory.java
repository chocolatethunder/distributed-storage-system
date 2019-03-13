package app.handlers;

import app.RequestType;

import java.net.Socket;

/**
 *This is the ServiceHandler Factory class that return a ServiceHandler Runnable depending on the requestype
 */
public class ServiceHandlerFactory {

    /**
     * Factory method to create Service Handler Thread for specific request type
     * @param requestType
     * @param socket
     * @param fileName
     * @return
     */
    public static Runnable getServiceHandler(RequestType requestType, Socket socket, String fileName) {
        switch (requestType) {
            case DOWNLOAD:
                return new DownloadServiceHandler(socket, fileName);
            case UPLOAD:
                return new UploadServiceHandler(socket, fileName);
            case DELETE:
                return new DeleteServiceHandler(socket, fileName);
            case LIST:
                return new FileListServiceHandler(socket);
            default:
                //
               // throw new Exception("Wrong request type");
                return null;
        }

    }
}
