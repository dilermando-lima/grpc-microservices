
import java.io.IOException;
import java.util.Objects;

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

        var port = System.getenv("ENV_PORT_APP");
        Objects.requireNonNull(port, "ENV_PORT_APP has not been found in environment variables");

        Server server = ServerBuilder.forPort(Integer.parseInt(port))
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
