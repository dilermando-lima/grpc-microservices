
import java.io.IOException;

import grpc.account.Account;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.util.TransmitStatusRuntimeExceptionInterceptor;
import repository.MemoryRepository;
import services.AccountReadService;
import services.AccountWriteService;

public class App {

    public static void main(String[] args) throws IOException, InterruptedException {

        var repository = new MemoryRepository<Account>();

        Server server = ServerBuilder.forPort(3009)
                .addService(new AccountWriteService(repository))
                .addService(new AccountReadService(repository))
                .intercept(TransmitStatusRuntimeExceptionInterceptor.instance())
                .build();
                
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                server.shutdown();
            }
        });
        server.awaitTermination();

    }

}
