//package com.xforceplus.ultraman.oqsengine.sdk.ext.stream;
//
//import akka.stream.Attributes;
//import akka.stream.Inlet;
//import akka.stream.SinkShape;
//import akka.stream.stage.AbstractInHandler;
//import akka.stream.stage.GraphStage;
//import akka.stream.stage.GraphStageLogic;
//import akka.util.ByteString;
//
//public class TowerStorageSink extends GraphStage<SinkShape<ByteString>> {
//
//    public final Inlet<ByteString> in = Inlet.<ByteString>create("tower.in");
//
//    private final SinkShape<ByteString> shape = SinkShape.of(in);
//
//    @Override
//    public GraphStageLogic createLogic(Attributes inheritedAttributes) throws Exception, Exception {
//        return new GraphStageLogic(shape()) {
//
//            // This requests one element at the Sink startup.
//            @Override
//            public void preStart() {
//                pull(in);
//            }
//
//            {
//                setHandler(
//                        in,
//                        new AbstractInHandler() {
//                            @Override
//                            public void onPush() throws Exception {
//                                ByteString element = grab(in);
//                                element.writeToOutputStream();
//                                pull(in);
//                            }
//                        });
//            }
//        };
//    }
//
//    @Override
//    public SinkShape<ByteString> shape() {
//        return shape;
//    }
//}
