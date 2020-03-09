
// Generated by Akka gRPC. DO NOT EDIT.
package com.xforceplus.ultraman.oqsengine.sdk;

import akka.actor.ActorSystem;
import akka.grpc.Codec;
import akka.grpc.Codecs;
import akka.grpc.javadsl.*;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.japi.Function;
import akka.stream.Materializer;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static com.xforceplus.ultraman.oqsengine.sdk.EntityService.Serializers.*;


public class EntityServicePowerApiHandlerFactory {

  private static final CompletionStage<HttpResponse> notFound = CompletableFuture.completedFuture(
    HttpResponse.create().withStatus(StatusCodes.NOT_FOUND));

  /**
   * Creates a `HttpRequest` to `HttpResponse` handler that can be used in for example `Http().bindAndHandleAsync`
   * for the generated partial function handler and ends with `StatusCodes.NotFound` if the request is not matching.
   *
   * Use `akka.grpc.scaladsl.ServiceHandler.concatOrNotFound` with `EntityServiceHandler.partial` when combining
   * several services.
   */
  public static Function<HttpRequest, CompletionStage<HttpResponse>> create(EntityServicePowerApi implementation, Materializer mat, ActorSystem system) {
    return create(implementation, EntityService.name, mat, system);
  }

  /**
   * Creates a `HttpRequest` to `HttpResponse` handler that can be used in for example `Http().bindAndHandleAsync`
   * for the generated partial function handler and ends with `StatusCodes.NotFound` if the request is not matching.
   *
   * Use `akka.grpc.scaladsl.ServiceHandler.concatOrNotFound` with `EntityServiceHandler.partial` when combining
   * several services.
   */
  public static Function<HttpRequest, CompletionStage<HttpResponse>> create(EntityServicePowerApi implementation, Materializer mat, Function<ActorSystem, Function<Throwable, io.grpc.Status>> eHandler, ActorSystem system) {
    return create(implementation, EntityService.name, mat, eHandler, system);
  }

  /**
   * Creates a `HttpRequest` to `HttpResponse` handler that can be used in for example `Http().bindAndHandleAsync`
   * for the generated partial function handler and ends with `StatusCodes.NotFound` if the request is not matching.
   *
   * Use `akka.grpc.scaladsl.ServiceHandler.concatOrNotFound` with `EntityServiceHandler.partial` when combining
   * several services.
   *
   * Registering a gRPC service under a custom prefix is not widely supported and strongly discouraged by the specification.
   */
  public static Function<HttpRequest, CompletionStage<HttpResponse>> create(EntityServicePowerApi implementation, String prefix, Materializer mat, ActorSystem system) {
    return partial(implementation, prefix, mat, GrpcExceptionHandler.defaultMapper(), system);
  }

  /**
   * Creates a `HttpRequest` to `HttpResponse` handler that can be used in for example `Http().bindAndHandleAsync`
   * for the generated partial function handler and ends with `StatusCodes.NotFound` if the request is not matching.
   *
   * Use `akka.grpc.scaladsl.ServiceHandler.concatOrNotFound` with `EntityServiceHandler.partial` when combining
   * several services.
   *
   * Registering a gRPC service under a custom prefix is not widely supported and strongly discouraged by the specification.
   */
  public static Function<HttpRequest, CompletionStage<HttpResponse>> create(EntityServicePowerApi implementation, String prefix, Materializer mat, Function<ActorSystem, Function<Throwable, io.grpc.Status>> eHandler, ActorSystem system) {
    return partial(implementation, prefix, mat, eHandler, system);
  }

  /**
   * Creates a `HttpRequest` to `HttpResponse` handler that can be used in for example
   * `Http.get(system).bindAndHandleAsync`. It ends with `StatusCodes.NotFound` if the request is not matching.
   *
   * Use `akka.grpc.javadsl.ServiceHandler.concatOrNotFound` when combining several services.
   */
  public static Function<HttpRequest, CompletionStage<HttpResponse>> partial(EntityServicePowerApi implementation, String prefix, Materializer mat, ActorSystem system) {
    return partial(implementation, prefix, mat, GrpcExceptionHandler.defaultMapper(), system);
  }

  /**
   * Creates a `HttpRequest` to `HttpResponse` handler that can be used in for example
   * `Http.get(system).bindAndHandleAsync`. It ends with `StatusCodes.NotFound` if the request is not matching.
   *
   * Use `akka.grpc.javadsl.ServiceHandler.concatOrNotFound` when combining several services.
   */
  public static Function<HttpRequest, CompletionStage<HttpResponse>> partial(EntityServicePowerApi implementation, String prefix, Materializer mat, Function<ActorSystem, Function<Throwable, io.grpc.Status>> eHandler, ActorSystem system) {
    return (req -> {
      Iterator<String> segments = req.getUri().pathSegments().iterator();
      if (segments.hasNext() && segments.next().equals(prefix) && segments.hasNext()) {
        String method = segments.next();
        if (segments.hasNext()) return notFound; // we don't allow any random `/prefix/Method/anything/here
        else return handle(req, method, implementation, mat, eHandler, system).exceptionally(e -> GrpcExceptionHandler.standard(e, eHandler, system));
      } else {
        return notFound;
      }
    });
  }

    public String getServiceName() {
      return EntityService.name;
    }

  private static CompletionStage<HttpResponse> handle(HttpRequest request, String method, EntityServicePowerApi implementation, Materializer mat, Function<ActorSystem, Function<Throwable, io.grpc.Status>> eHandler, ActorSystem system) {
    Codec responseCodec = Codecs.negotiate(request);
    Metadata metadata = new MetadataImpl(request.getHeaders());
    switch(method) {

      case "begin":
        return GrpcMarshalling.unmarshal(request, TransactionUpSerializer, mat)
          .thenCompose(e -> implementation.begin(e, metadata))
          .thenApply(e -> GrpcMarshalling.marshal(e, OperationResultSerializer, mat, responseCodec, system, package$.MODULE$.scalaAnonymousPartialFunction(eHandler)));

      case "build":
        return GrpcMarshalling.unmarshal(request, EntityUpSerializer, mat)
          .thenCompose(e -> implementation.build(e, metadata))
          .thenApply(e -> GrpcMarshalling.marshal(e, OperationResultSerializer, mat, responseCodec, system, package$.MODULE$.scalaAnonymousPartialFunction(eHandler)));

      case "replace":
        return GrpcMarshalling.unmarshal(request, EntityUpSerializer, mat)
          .thenCompose(e -> implementation.replace(e, metadata))
          .thenApply(e -> GrpcMarshalling.marshal(e, OperationResultSerializer, mat, responseCodec, system, package$.MODULE$.scalaAnonymousPartialFunction(eHandler)));

      case "remove":
        return GrpcMarshalling.unmarshal(request, EntityUpSerializer, mat)
          .thenCompose(e -> implementation.remove(e, metadata))
          .thenApply(e -> GrpcMarshalling.marshal(e, OperationResultSerializer, mat, responseCodec, system, package$.MODULE$.scalaAnonymousPartialFunction(eHandler)));

      case "selectOne":
        return GrpcMarshalling.unmarshal(request, EntityUpSerializer, mat)
          .thenCompose(e -> implementation.selectOne(e, metadata))
          .thenApply(e -> GrpcMarshalling.marshal(e, OperationResultSerializer, mat, responseCodec, system, package$.MODULE$.scalaAnonymousPartialFunction(eHandler)));

      case "selectByConditions":
        return GrpcMarshalling.unmarshal(request, SelectByConditionSerializer, mat)
          .thenCompose(e -> implementation.selectByConditions(e, metadata))
          .thenApply(e -> GrpcMarshalling.marshal(e, OperationResultSerializer, mat, responseCodec, system, package$.MODULE$.scalaAnonymousPartialFunction(eHandler)));

      case "commit":
        return GrpcMarshalling.unmarshal(request, TransactionUpSerializer, mat)
          .thenCompose(e -> implementation.commit(e, metadata))
          .thenApply(e -> GrpcMarshalling.marshal(e, OperationResultSerializer, mat, responseCodec, system, package$.MODULE$.scalaAnonymousPartialFunction(eHandler)));

      case "rollBack":
        return GrpcMarshalling.unmarshal(request, TransactionUpSerializer, mat)
          .thenCompose(e -> implementation.rollBack(e, metadata))
          .thenApply(e -> GrpcMarshalling.marshal(e, OperationResultSerializer, mat, responseCodec, system, package$.MODULE$.scalaAnonymousPartialFunction(eHandler)));

      default:
        CompletableFuture<HttpResponse> result = new CompletableFuture<>();
        result.completeExceptionally(new UnsupportedOperationException("Not implemented: " + method));
        return result;
    }
  }
}
