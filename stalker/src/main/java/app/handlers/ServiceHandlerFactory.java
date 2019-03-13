package app.handlers;

import app.RequestType;
import app.chunk_utils.IndexFile;

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
     * @param index
     * @return
     */
    public static Runnable getServiceHandler(RequestType requestType, Socket socket, String fileName, IndexFile index) {
        switch (requestType) {
            case DOWNLOAD:
                return new DownloadServiceHandler(socket, fileName, index);
            case UPLOAD:
                return new UploadServiceHandler(socket, fileName, index);
            case DELETE:
                return new DeleteServiceHandler(socket, fileName, index);
            case LIST:
                return new FileListServiceHandler(socket, index);
            default:
                //
               // throw new Exception("Wrong request type");
                return null;
        }

    }
}
