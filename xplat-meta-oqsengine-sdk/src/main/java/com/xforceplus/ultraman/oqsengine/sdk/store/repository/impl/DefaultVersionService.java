package com.xforceplus.ultraman.oqsengine.sdk.store.repository.impl;

import com.xforceplus.ultraman.oqsengine.sdk.event.MetadataModuleMissingEvent;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.VersionService;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.impl.BoNode;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.impl.RingDC;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.impl.RingDCHolder;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.impl.VersionedModule;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.apache.metamodel.UpdateableDataContext;
import org.springframework.context.ApplicationEventPublisher;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class DefaultVersionService implements VersionService {

    /**
     * set long is not thread-safe
     */
    private Map<Long, LinkedList<VersionedModule>> currentVersionForModule = new HashMap<>();

    private Map<Long, String> currentModuleVersionMap = new HashMap<>();

    private Map<BoNode, LinkedList<Tuple2<Long, String>>> boModuleMapping = new HashMap<>();

    private RingDCHolder dc;

    private int versionSize;

    private ApplicationEventPublisher publisher;

    public DefaultVersionService(int versionSize, ApplicationEventPublisher eventPublisher){
        this.versionSize = versionSize;
        this.publisher = eventPublisher;
    }

    /**
     * synchronized to prevent concurrent init
     * @param id
     * @param version
     * @return
     */
    @Override
    public synchronized UpdateableDataContext getVersionedDCForBoById(long id, String version) {

        List<Tuple2<Long, String>>  versionedList = findById(id);

        if(versionedList == null){
            /**
             * not init
             */
            return null;
        }

        Optional<Tuple2<Long, String>> first = versionedList
                .stream().filter(x -> x._2().equals(version)).findFirst();

        if(first.isPresent()){
            return this.getVersionedDCForModule(first.get()._1(), version);
        }else{
            if(versionedList.size() < versionSize && publisher != null){
                //maybe the new or old one is not arrived try to fetch one
                //TODO
                publisher.publishEvent(new MetadataModuleMissingEvent());

                return getVersionedDCForBoInner(id, version);
            }
        }
        return null;
    }

    @Override
    public UpdateableDataContext getVersionedDCForBoByCode(String code, String version) {
        List<Tuple2<Long, String>>  versionedList = findByCode(code);

        if(versionedList == null){
            /**
             * not init
             */
            return null;
        }

        Optional<Tuple2<Long, String>> first = versionedList
                .stream().filter(x -> x._2().equals(version)).findFirst();

        if(first.isPresent()){
            return this.getVersionedDCForModule(first.get()._1(), version);
        }else{
            if(versionedList.size() < versionSize && publisher != null){
                //maybe the new or old one is not arrived try to fetch one
                //TODO
                publisher.publishEvent(new MetadataModuleMissingEvent());

                return getVersionedDCForBoInner(code, version);
            }
        }
        return null;
    }

    private UpdateableDataContext getVersionedDCForBoInner(long id, String version){

        List<Tuple2<Long, String>>  versionedList = findById(id);

        if(versionedList == null){
            return null;
        }

        Optional<Tuple2<Long, String>> first = versionedList
                .stream().filter(x -> x._2().equals(version)).findFirst();

        return first.map(longStringTuple2 -> this.getVersionedDCForModule(longStringTuple2._1(), version)).orElse(null);
    }

    private UpdateableDataContext getVersionedDCForBoInner(String code, String version){

        List<Tuple2<Long, String>>  versionedList = findByCode(code);

        if(versionedList == null){
            return null;
        }

        Optional<Tuple2<Long, String>> first = versionedList
                .stream().filter(x -> x._2().equals(version)).findFirst();

        return first.map(longStringTuple2 -> this.getVersionedDCForModule(longStringTuple2._1(), version)).orElse(null);
    }

    @Override
    public UpdateableDataContext getCurrentVersionDCForBoById(Long id) {
        LinkedList<Tuple2<Long, String>>  versionedList = findById(id);


        if(versionedList == null){
            /**
             * not init
             */
            return null;
        }

        Tuple2<Long, String> last = versionedList.getLast();

        if(last != null){
            return this.getVersionedDCForModule(last._1(), last._2());
        }

        return null;
    }

    @Override
    public UpdateableDataContext getCurrentVersionDCForBoByCode(String code) {

        LinkedList<Tuple2<Long, String>>  versionedList = findByCode(code);
        if(versionedList == null){
            /**
             * not init
             */
            return null;
        }

        Tuple2<Long, String> last = versionedList.getLast();

        if(last != null){
            return this.getVersionedDCForModule(last._1(), last._2());
        }

        return null;
    }

    @Override
    public UpdateableDataContext getVersionedDCForModule(long id, String version) {
        LinkedList<VersionedModule> versionedModules = currentVersionForModule.get(id);
        return versionedModules.stream().filter(x -> x.getVersion().equals(version))
                .findAny().map(x -> x.getRingDC().getDc()).orElse(null);
    }

    /**
     * save module to the current version
     * @param id
     * @param version
     * @param boIds
     */
    @Override
    public synchronized void saveModule(long id, String version, List<BoNode> boIds) {

        String currentVersion = currentModuleVersionMap.get(id);

        if(currentVersion == null){
            LinkedList<VersionedModule> list = new LinkedList<>();
            list.addLast(new VersionedModule(version, boIds, dc.getRoot(), System.currentTimeMillis()));
            currentVersionForModule.put(id, list);

            boIds.forEach(boNode -> {
                LinkedList<Tuple2<Long, String>> boList = new LinkedList<>();
                boList.addLast(Tuple.of(id, version));
                boModuleMapping.put(boNode, boList);
            });

        } else if(! currentVersion.equals(version) ){
            LinkedList<VersionedModule> list = currentVersionForModule.get(id);
            if(list.size() + 1 > versionSize){
                list.removeFirst();
            }

            RingDC last = list.getLast().getRingDC();
            list.addLast(new VersionedModule(version, boIds, last.next(), System.currentTimeMillis()));

            boIds.forEach(boId -> {

                LinkedList<Tuple2<Long, String>> boList = findById(boId.getId());

                if(boList.size() + 1 > versionSize){
                    boList.removeFirst();
                }

                boList.addLast(Tuple.of(id, version));
            });
        }

        currentModuleVersionMap.put(id, version);
    }

    @Override
    public void initVersionedDC(int versionSize, Supplier<UpdateableDataContext> dcSupplier) {
        if(versionSize < 0 ){
            versionSize = 1;
        }

        if(versionSize == 1){
            UpdateableDataContext root = dcSupplier.get();
            dc = new RingDCHolder(new RingDC(root));
        }else{
            UpdateableDataContext root = dcSupplier.get();
            dc = new RingDCHolder(new RingDC(root));

            IntStream.range(0, versionSize - 1)
                    .mapToObj(x -> dcSupplier.get())
                    .forEach(x -> dc.addNode(new RingDC(x)));
        }
    }

    @Override
    public Map<Long, String> getCurrentVersion() {
        return Collections.unmodifiableMap(this.currentModuleVersionMap);
    }

    private LinkedList<Tuple2<Long, String>> findById(long id){
        return boModuleMapping.entrySet()
                .stream()
                .filter(x -> x.getKey().getId().equals(id))
                .findFirst().map(Map.Entry::getValue).orElse(null);
    }

    private LinkedList<Tuple2<Long, String>> findByCode(String code){
        return boModuleMapping.entrySet()
                .stream()
                .filter(x -> x.getKey().getCode().equals(code))
                .findFirst().map(Map.Entry::getValue).orElse(null);
    }
}
