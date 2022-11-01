package services;

import java.util.function.BiConsumer;

import grpc.account.Account;
import grpc.account.ByIdRequest;
import grpc.account.ListRequest;
import grpc.account.ReadGrpc.ReadImplBase;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import repository.RepositoryInt;

public class AccountReadService extends ReadImplBase {

    private RepositoryInt<Account> repository;

    public AccountReadService(RepositoryInt<Account> repository) {
        this.repository = repository;
    }

    @Override
    public void accountGetById(ByIdRequest request, StreamObserver<Account> responseObserver) {
        var account = repository.getById(request.getAccountId());
        validAccountByIdNotFound.accept(account, request.getAccountId());
        responseObserver.onNext(account);
        responseObserver.onCompleted();
    }

    private BiConsumer<Object, String> validAccountByIdNotFound = (account, id) -> {
        if (account == null)
            throw Status.INVALID_ARGUMENT.withDescription("accountId %s has not been found".formatted(id))
                    .asRuntimeException();
    };

    @Override
    public void accountList(ListRequest request, StreamObserver<Account> responseObserver) {
        repository.list(request.getNumPage(), request.getPageSize())
                .forEach(responseObserver::onNext);
        responseObserver.onCompleted();
    }

}
