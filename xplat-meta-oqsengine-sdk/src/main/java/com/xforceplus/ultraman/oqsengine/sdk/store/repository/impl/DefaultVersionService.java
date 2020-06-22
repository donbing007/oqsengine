package com.xforceplus.ultraman.oqsengine.sdk.store.repository.impl;

import com.xforceplus.ultraman.oqsengine.sdk.event.MetadataModuleVersionMissingEvent;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.VersionService;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.apache.metamodel.UpdateableDataContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * default version service
 * TODO using read write lock
 */
public class DefaultVersionService implements VersionService {

    private Logger logger = LoggerFactory.getLogger(VersionService.class);

    /**
     * set long is not thread-safe
     */
    private Map<Long, LinkedList<VersionedModule>> currentVersionForModule = new HashMap<>();

    private Map<Long, String> currentModuleVersionMap = new HashMap<>();

    /**
     * boNode  --> [moduleId:Long - version:String]
     */
    private Map<BoNode, LinkedList<Tuple2<Long, String>>> boModuleMapping = new HashMap<>();

    //RingDC a DC context in RingBuffer
    private RingDCHolder dc;

    private int versionSize;

    private ApplicationEventPublisher publisher;

    private ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    public DefaultVersionService(int versionSize, ApplicationEventPublisher eventPublisher) {
        this.versionSize = versionSize;
        this.publisher = eventPublisher;
    }

    private <T> T read(Supplier<T> supplier) {
        rwLock.readLock().lock();
        try {
            return supplier.get();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    private void write(Supplier<Void> supplier) {
        rwLock.writeLock().lock();
        try {
            supplier.get();
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * synchronized to prevent concurrent init
     *
     * @param id
     * @param version
     * @return
     */
    @Override
    public synchronized UpdateableDataContext getVersionedDCForBoById(long id, String version) {

        List<Tuple2<Long, String>> versionedList = findById(id);

        if (versionedList == null) {
            /**
             * not init
             */
            return null;
        }

        Optional<Tuple2<Long, String>> first = versionedList
                .stream().filter(x -> x._2().equals(version)).findFirst();

        if (first.isPresent()) {
            return this.getVersionedDCForModule(first.get()._1(), version);
        } else {
            if (versionedList.size() < versionSize && publisher != null) {
                //maybe the new or old one is not arrived try to fetch one
                //TODO

                Long relatedModuleId = versionedList.get(0)._1();
                publisher.publishEvent(
                        new MetadataModuleVersionMissingEvent(relatedModuleId, version)
                );

                return getVersionedDCForBoInner(id, version);
            }
        }
        return null;
    }

    @Override
    public synchronized UpdateableDataContext getVersionedDCForBoByCode(String code, String version) {
        List<Tuple2<Long, String>> versionedList = findByCode(code);

        if (versionedList == null) {
            /**
             * not init
             */
            return null;
        }

        Optional<Tuple2<Long, String>> first = versionedList
                .stream().filter(x -> x._2().equals(version)).findFirst();

        if (first.isPresent()) {
            return this.getVersionedDCForModule(first.get()._1(), version);
        } else {
            if (versionedList.size() < versionSize && publisher != null) {

                Long relatedModuleId = versionedList.get(0)._1();
                publisher.publishEvent(new MetadataModuleVersionMissingEvent(relatedModuleId, version));

                return getVersionedDCForBoInner(code, version);
            }
        }
        return null;
    }

    private UpdateableDataContext getVersionedDCForBoInner(long id, String version) {

        List<Tuple2<Long, String>> versionedList = findById(id);

        if (versionedList == null) {
            return null;
        }

        Optional<Tuple2<Long, String>> first = versionedList
                .stream().filter(x -> x._2().equals(version)).findFirst();

        return first.map(longStringTuple2 -> this.getVersionedDCForModule(longStringTuple2._1(), version)).orElse(null);
    }

    private UpdateableDataContext getVersionedDCForBoInner(String code, String version) {

        List<Tuple2<Long, String>> versionedList = findByCode(code);

        if (versionedList == null) {
            return null;
        }

        Optional<Tuple2<Long, String>> first = versionedList
                .stream().filter(x -> x._2().equals(version)).findFirst();

        return first.map(longStringTuple2 -> this.getVersionedDCForModule(longStringTuple2._1(), version)).orElse(null);
    }

    @Override
    public synchronized UpdateableDataContext getCurrentVersionDCForBoById(Long id) {
        LinkedList<Tuple2<Long, String>> versionedList = findById(id);


        if (versionedList == null) {
            /**
             * not init
             */
            return null;
        }

        Tuple2<Long, String> last = versionedList.getLast();

        if (last != null) {
            return this.getVersionedDCForModule(last._1(), last._2());
        }

        return null;
    }

    @Override
    public synchronized UpdateableDataContext getCurrentVersionDCForBoByCode(String code) {

        logger.debug("select code {}" , code);
        LinkedList<Tuple2<Long, String>> versionedList = findByCode(code);
        if (versionedList == null) {
            /**
             * not init
             */
            logger.debug("current no such version {}" , code);
            return null;
        }

        Tuple2<Long, String> last = versionedList.getLast();

        if (last != null) {
            logger.debug("got last version {} for {}" , code, last._2());
            return this.getVersionedDCForModule(last._1(), last._2());
        }

        logger.debug("last version is empty {}" , code);
        return null;
    }

    @Override
    public synchronized UpdateableDataContext getVersionedDCForModule(long id, String version) {
        LinkedList<VersionedModule> versionedModules = currentVersionForModule.get(id);
        return versionedModules.stream().filter(x -> x.getVersion().equals(version))
                .findAny().map(x -> x.getRingDC().getDc()).orElse(null);
    }

    /**
     * save module to the current version
     *
     * @param id
     * @param version
     * @param boIds
     */
    @Override
    public synchronized void saveModule(long id, String version, List<BoNode> boIds) {

        String currentVersion = currentModuleVersionMap.get(id);

        if (currentVersion == null) {
            LinkedList<VersionedModule> list = new LinkedList<>();
            list.addLast(new VersionedModule(version, boIds, dc.getRoot(), System.currentTimeMillis()));
            currentVersionForModule.put(id, list);

            boIds.forEach(boNode -> {
                LinkedList<Tuple2<Long, String>> boList = new LinkedList<>();
                boList.addLast(Tuple.of(id, version));
                boModuleMapping.put(boNode, boList);
            });

        } else if (!currentVersion.equals(version)) {

            logger.debug("CurrentVersion is {}, Version is {}， VersionSize is {}", currentVersion, version, versionSize);


            LinkedList<VersionedModule> list = currentVersionForModule.get(id);
            //TODO fix the bug
            if (list.size() + 1 > versionSize) {
                logger.debug("version is overflow we need to roll it");
                list.removeLast();
            }

            RingDC last;
            if(list.size() > 0) {
                last = list.getLast().getRingDC();
            }else{
                last = dc.getRoot();
            }

            list.addLast(new VersionedModule(version, boIds, last.next(), System.currentTimeMillis()));

            boIds.forEach(boNode -> {

                //this bo may not exists in preview module
                //add new Bo
                LinkedList<Tuple2<Long, String>> boList = findById(boNode.getId());

                if (boList != null) {
                    if (boList.size() + 1 > versionSize) {
                        boList.removeFirst();
                    }
                } else {
                    //init new bo here
                    boList = new LinkedList<>();
                    boModuleMapping.put(boNode, boList);
                }

                boList.addLast(Tuple.of(id, version));
            });
        }

        currentModuleVersionMap.put(id, version);
    }

    @Override
    public synchronized void initVersionedDC(int versionSize, Supplier<UpdateableDataContext> dcSupplier) {
        if (versionSize < 0) {
            versionSize = 1;
        }

        if (versionSize == 1) {
            UpdateableDataContext root = dcSupplier.get();
            dc = new RingDCHolder(new RingDC(root));
        } else {
            UpdateableDataContext root = dcSupplier.get();
            dc = new RingDCHolder(new RingDC(root));

            IntStream.range(0, versionSize - 1)
                    .mapToObj(x -> dcSupplier.get())
                    .forEach(x -> dc.addNode(new RingDC(x)));
        }
    }

    @Override
    public synchronized Map<Long, String> getCurrentVersion() {
        return Collections.unmodifiableMap(this.currentModuleVersionMap);
    }

    private LinkedList<Tuple2<Long, String>> findById(long id) {
        return boModuleMapping.entrySet()
                .stream()
                .filter(x -> x.getKey().getId().equals(id))
                .findFirst().map(Map.Entry::getValue).orElse(null);
    }

    private LinkedList<Tuple2<Long, String>> findByCode(String code) {
        return boModuleMapping.entrySet()
                .stream()
                .filter(x -> x.getKey().getCode().equals(code))
                .findFirst().map(Map.Entry::getValue).orElse(null);
    }

    @Override
    public synchronized Map<BoNode, LinkedList<Tuple2<Long, String>>> getBoModuleMapping() {
        return Collections.unmodifiableMap(boModuleMapping);
    }
}
