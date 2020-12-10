
// Generated by Akka gRPC. DO NOT EDIT.
package com.xforceplus.ultraman.oqsengine.sdk;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import akka.japi.Function;
import akka.http.javadsl.model.*;
import akka.actor.ActorSystem;
import akka.actor.ClassicActorSystemProvider;
import akka.stream.Materializer;
import akka.stream.SystemMaterializer;

import akka.grpc.Trailers;
import akka.grpc.javadsl.GrpcMarshalling;
import akka.grpc.javadsl.GrpcExceptionHandler;
import akka.grpc.javadsl.package$;

import akka.grpc.javadsl.Metadata;
import akka.grpc.javadsl.MetadataBuilder;

import static com.xforceplus.ultraman.oqsengine.sdk.EntityRebuildService.Serializers.*;


/*
 * Generated by Akka gRPC. DO NOT EDIT.
 */
public class EntityRebuildServicePowerApiHandlerFactory {

    private static final CompletionStage<HttpResponse> notFound = CompletableFuture.completedFuture(
      HttpResponse.create().withStatus(StatusCodes.NOT_FOUND));

    private static final CompletionStage<HttpResponse> unsupportedMediaType = CompletableFuture.completedFuture(
      HttpResponse.create().withStatus(StatusCodes.UNSUPPORTED_MEDIA_TYPE));

    /**
     * Creates a `HttpRequest` to `HttpResponse` handler that can be used in for example `Http().bindAndHandleAsync`
     * for the generated partial function handler and ends with `StatusCodes.NotFound` if the request is not matching.
     *
     * Use {@link akka.grpc.javadsl.ServiceHandler#concatOrNotFound} with {@link EntityRebuildServiceHandler#partial} when combining
     * several services.
     */
    public static Function<HttpRequest, CompletionStage<HttpResponse>> create(EntityRebuildServicePowerApi implementation, ClassicActorSystemProvider system) {
      return create(implementation, EntityRebuildService.name, system);
    }

    /**
     * Creates a `HttpRequest` to `HttpResponse` handler that can be used in for example `Http().bindAndHandleAsync`
     * for the generated partial function handler and ends with `StatusCodes.NotFound` if the request is not matching.
     *
     * Use {@link akka.grpc.javadsl.ServiceHandler#concatOrNotFound} with {@link EntityRebuildServiceHandler#partial} when combining
     * several services.
     */
    public static Function<HttpRequest, CompletionStage<HttpResponse>> create(EntityRebuildServicePowerApi implementation, Function<ActorSystem, Function<Throwable, Trailers>> eHandler, ClassicActorSystemProvider system) {
      return create(implementation, EntityRebuildService.name, eHandler, system);
    }

    /**
     * Creates a `HttpRequest` to `HttpResponse` handler that can be used in for example `Http().bindAndHandleAsync`
     * for the generated partial function handler and ends with `StatusCodes.NotFound` if the request is not matching.
     *
     * Use {@link akka.grpc.javadsl.ServiceHandler#concatOrNotFound} with {@link EntityRebuildServiceHandler#partial} when combining
     * several services.
     *
     * Registering a gRPC service under a custom prefix is not widely supported and strongly discouraged by the specification.
     */
    public static Function<HttpRequest, CompletionStage<HttpResponse>> create(EntityRebuildServicePowerApi implementation, String prefix, ClassicActorSystemProvider system) {
      return partial(implementation, prefix, SystemMaterializer.get(system).materializer(), GrpcExceptionHandler.defaultMapper(), system);
    }

    /**
     * Creates a `HttpRequest` to `HttpResponse` handler that can be used in for example `Http().bindAndHandleAsync`
     * for the generated partial function handler and ends with `StatusCodes.NotFound` if the request is not matching.
     *
     * Use {@link akka.grpc.javadsl.ServiceHandler#concatOrNotFound} with {@link EntityRebuildServiceHandler#partial} when combining
     * several services.
     *
     * Registering a gRPC service under a custom prefix is not widely supported and strongly discouraged by the specification.
     */
    public static Function<HttpRequest, CompletionStage<HttpResponse>> create(EntityRebuildServicePowerApi implementation, String prefix, Function<ActorSystem, Function<Throwable, Trailers>> eHandler, ClassicActorSystemProvider system) {
      return partial(implementation, prefix, SystemMaterializer.get(system).materializer(), eHandler, system);
    }

    /**
     * Creates a `HttpRequest` to `HttpResponse` handler that can be used in for example
     * `Http.get(system).bindAndHandleAsync`. It ends with `StatusCodes.NotFound` if the request is not matching.
     *
     * Use {@link akka.grpc.javadsl.ServiceHandler#concatOrNotFound} when combining several services.
     */
    public static Function<HttpRequest, CompletionStage<HttpResponse>> partial(EntityRebuildServicePowerApi implementation, String prefix, ClassicActorSystemProvider system) {
      return partial(implementation, prefix, SystemMaterializer.get(system).materializer(), GrpcExceptionHandler.defaultMapper(), system);
    }

    /**
     * Creates a `HttpRequest` to `HttpResponse` handler that can be used in for example
     * `Http.get(system).bindAndHandleAsync`. It ends with `StatusCodes.NotFound` if the request is not matching.
     *
     * Use {@link akka.grpc.javadsl.ServiceHandler#concatOrNotFound} when combining several services.
     */
    public static Function<HttpRequest, CompletionStage<HttpResponse>> partial(EntityRebuildServicePowerApi implementation, String prefix, Materializer mat, Function<ActorSystem, Function<Throwable, Trailers>> eHandler, ClassicActorSystemProvider system) {
      return (req -> {
        Iterator<String> segments = req.getUri().pathSegments().iterator();
        if (segments.hasNext() && segments.next().equals(prefix) && segments.hasNext()) {
          String method = segments.next();
          if (segments.hasNext()) return notFound; // we don't allow any random `/prefix/Method/anything/here
          else return handle(req, method, implementation, mat, eHandler, system);
        } else {
          return notFound;
        }
      });
    }

    public String getServiceName() {
      return EntityRebuildService.name;
    }

    private static CompletionStage<HttpResponse> handle(HttpRequest request, String method, EntityRebuildServicePowerApi implementation, Materializer mat, Function<ActorSystem, Function<Throwable, Trailers>> eHandler, ClassicActorSystemProvider system) {
      return GrpcMarshalling.negotiated(request, (reader, writer) -> {
        final CompletionStage<HttpResponse> response;
        Metadata metadata = MetadataBuilder.fromHeaders(request.getHeaders());
        switch(method) {
          
          case "rebuildIndex":
            response = GrpcMarshalling.unmarshal(request.entity().getDataBytes(), RebuildRequestSerializer, mat, reader)
              .thenCompose(e -> implementation.rebuildIndex(e, metadata))
              .thenApply(e -> GrpcMarshalling.marshal(e, RebuildTaskInfoSerializer, writer, system, eHandler));
            break;
          
          case "showProgress":
            response = GrpcMarshalling.unmarshal(request.entity().getDataBytes(), ShowTaskSerializer, mat, reader)
              .thenApply(e -> implementation.showProgress(e, metadata))
              .thenApply(e -> GrpcMarshalling.marshalStream(e, RebuildTaskInfoSerializer, writer, system, eHandler));
            break;
          
          case "listActiveTasks":
            response = GrpcMarshalling.unmarshal(request.entity().getDataBytes(), QueryPageSerializer, mat, reader)
              .thenApply(e -> implementation.listActiveTasks(e, metadata))
              .thenApply(e -> GrpcMarshalling.marshalStream(e, RebuildTaskInfoSerializer, writer, system, eHandler));
            break;
          
          case "getActiveTask":
            response = GrpcMarshalling.unmarshal(request.entity().getDataBytes(), EntityUpSerializer, mat, reader)
              .thenCompose(e -> implementation.getActiveTask(e, metadata))
              .thenApply(e -> GrpcMarshalling.marshal(e, RebuildTaskInfoSerializer, writer, system, eHandler));
            break;
          
          case "listAllTasks":
            response = GrpcMarshalling.unmarshal(request.entity().getDataBytes(), QueryPageSerializer, mat, reader)
              .thenApply(e -> implementation.listAllTasks(e, metadata))
              .thenApply(e -> GrpcMarshalling.marshalStream(e, RebuildTaskInfoSerializer, writer, system, eHandler));
            break;
          
          case "cancelTask":
            response = GrpcMarshalling.unmarshal(request.entity().getDataBytes(), ShowTaskSerializer, mat, reader)
              .thenCompose(e -> implementation.cancelTask(e, metadata))
              .thenApply(e -> GrpcMarshalling.marshal(e, RebuildTaskInfoSerializer, writer, system, eHandler));
            break;
          
          default:
            CompletableFuture<HttpResponse> result = new CompletableFuture<>();
            result.completeExceptionally(new UnsupportedOperationException("Not implemented: " + method));
            response = result;
        }
        return response.exceptionally(e -> GrpcExceptionHandler.standard(e, eHandler, writer, system));
      })
      .orElseGet(() -> unsupportedMediaType);
    }
  }
