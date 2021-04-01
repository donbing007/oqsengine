package com.xforceplus.ultraman.oqsengine.boot.listener;

import com.xforceplus.ultraman.oqsengine.event.EventBus;
import com.xforceplus.ultraman.oqsengine.event.EventType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class EventListener {

    @Autowired
    private EventBus eventBus;

    @PostConstruct
    public void register(){
        eventBus.watch(EventType.ENTITY_BUILD, x -> {
            System.out.println("CREATE");
            System.out.println(x);
        });


        eventBus.watch(EventType.ENTITY_DELETE, x -> {
            System.out.println("DELETE");
            System.out.println(x);
        });

        eventBus.watch(EventType.ENTITY_REPLACE, x -> {
            System.out.println("REPLACE");
            System.out.println(x);
        });

        eventBus.watch(EventType.TX_PREPAREDNESS_COMMIT, x -> {
            System.out.println("COMMIT");
            System.out.println(x);
        });

        eventBus.watch(EventType.TX_BEGIN, x -> {
            System.out.println("BEGINE TX");
            System.out.println(x);
        });

        eventBus.watch(EventType.TX_COMMITED, x -> {
            System.out.println("COMMIT TX");
            System.out.println(x);
        });

        eventBus.watch(EventType.TX_ROLLBACKED, x -> {
            System.out.println("ROLLBACK TX");
            System.out.println(x);
        });
    }
}
