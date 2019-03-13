package app.handlers;

import app.RequestType;

import java.net.Socket;

/**
 *
 */
public class ServiceHandlerFactory {

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
