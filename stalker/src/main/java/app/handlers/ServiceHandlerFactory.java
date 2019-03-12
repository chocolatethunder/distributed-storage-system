package app.handlers;

import app.RequestType;

import java.net.Socket;

/**
 *
 */
public class ServiceHandlerFactory {

    public static Runnable getServiceHandler(RequestType requestType, Socket socket) {
        switch (requestType) {
            case DOWNLOAD:
                return new DownloadServiceHandler(socket);
            case UPLOAD:
                return new UploadServiceHandler(socket);
            case DELETE:
                return new DeleteServiceHandler(socket);
            case LIST:
                return new FileListServiceHandler(socket);
            default:
                //
               // throw new Exception("Wrong request type");
                return null;
        }

    }
}
