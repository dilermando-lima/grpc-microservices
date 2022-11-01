package services;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import grpc.account.Account;
import grpc.account.ByIdRequest;
import grpc.account.CreateRequest;
import grpc.account.CreateResponse;
import grpc.account.Empty;
import grpc.account.UpdateRequest;
import grpc.account.WriteGrpc.WriteImplBase;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import repository.RepositoryInt;

public class AccountWriteService extends WriteImplBase {

    private RepositoryInt<Account> repository;

    public AccountWriteService(RepositoryInt<Account> repository) {
        this.repository = repository;
    }

    @Override
    public void accountCreate(CreateRequest request, StreamObserver<CreateResponse> responseObserver) {

        validAccountName.accept(request.getAccountName());
        validUserEmail.accept(request.getUserEmail());
        validUserName.accept(request.getUserName());

        var response = convertCreateRequestToCreateResponse.apply(request);

        repository.save(
                response.getAccountId(),
                converterCreateResponseToAccount.apply(response));

        responseObserver.onNext(response);
        responseObserver.onCompleted();

    }

    @Override
    public void accountRemove(ByIdRequest request, StreamObserver<Empty> responseObserver) {
        validAccountId.accept(request.getAccountId());

        var account = repository.getById(request.getAccountId());
        validAccountByIdNotFound.accept(account, request.getAccountId());

        repository.remove(request.getAccountId());

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void accountUpdate(UpdateRequest request, StreamObserver<Account> responseObserver) {
        validAccountId.accept(request.getAccountId());
        validAccountName.accept(request.getAccountName());

        var account = repository.getById(request.getAccountId());
        validAccountByIdNotFound.accept(account, request.getAccountId());

        injectUpdateRequestIntoAccount.accept(request, account);

        repository.save(account.getAccountId(), account);

        responseObserver.onNext(account);
        responseObserver.onCompleted();
    }

    private Function<CreateRequest, CreateResponse> convertCreateRequestToCreateResponse = newAccountRequest -> {
        String newId = UUID.randomUUID().toString();
        return CreateResponse
                .newBuilder()
                .setAccountId("C-" + newId)
                .setAccountName(newAccountRequest.getAccountName())
                .setDateTimeCreated(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .setUserEmail(newAccountRequest.getUserEmail())
                .setUserId("U-" + newId)
                .build();
    };

    private BiConsumer<UpdateRequest, Account> injectUpdateRequestIntoAccount = (updateRequest, account) -> account
            .toBuilder()
            .setAccountName(updateRequest.getAccountName())
            .setDateTimeLastUpdated(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
            .build();

    private Function<CreateResponse, Account> converterCreateResponseToAccount = createResponse -> Account
            .newBuilder()
            .setAccountId(createResponse.getAccountId())
            .setAccountName(createResponse.getAccountName())
            .setDateTimeCreated(createResponse.getDateTimeCreated())
            .setDateTimeLastUpdated(createResponse.getDateTimeCreated())
            .build();

    private BiConsumer<Object, String> validAccountByIdNotFound = (account, id) -> {
        if (account == null)
            throw Status.INVALID_ARGUMENT.withDescription("accountId %s has not been found".formatted(id))
                    .asRuntimeException();
    };

    private Consumer<String> validAccountName = name -> {
        if (name.isBlank())
            throw Status.INVALID_ARGUMENT.withDescription("accountName cannot be empty").asRuntimeException();
        if (name.length() > 50)
            throw Status.INVALID_ARGUMENT.withDescription("accountName must be less than 50 caract")
                    .asRuntimeException();
    };

    private Consumer<String> validUserEmail = email -> {
        if (email.isBlank())
            throw Status.INVALID_ARGUMENT.withDescription("userEmail cannot be empty").asRuntimeException();
        if (email.length() > 255)
            throw Status.INVALID_ARGUMENT.withDescription("userEmail must be less than 255 caract")
                    .asRuntimeException();
    };

    private Consumer<String> validUserName = name -> {
        if (name.isBlank())
            throw Status.INVALID_ARGUMENT.withDescription("userName cannot be empty").asRuntimeException();
        if (name.length() > 50)
            throw Status.INVALID_ARGUMENT.withDescription("userName must be less than 50 caract").asRuntimeException();
    };

    private Consumer<String> validAccountId = accountId -> {
        if (accountId.isBlank())
            throw Status.INVALID_ARGUMENT.withDescription("accountId cannot be empty").asRuntimeException();
    };

}
