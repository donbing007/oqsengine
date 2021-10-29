package com.xforceplus.ultraman.oqsengine.meta.dto;

import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import java.util.List;

/**
 * Created by justin.xu on 07/2021.
 *
 * @since 1.8
 */
public class ServerMetricsInfo {
    private List<ClientWatches> clientWatches;

    public ServerMetricsInfo(
        List<ClientWatches> clientWatches) {
        this.clientWatches = clientWatches;
    }

    public List<ClientWatches> getClientWatches() {
        return clientWatches;
    }

    public void setClientWatches(
        List<ClientWatches> clientWatches) {
        this.clientWatches = clientWatches;
    }

    /**
     * 客户端监视器.
     */
    public static class ClientWatches {
        private String clientId;
        private List<WatchElement> watches;
        private long lastHeartBeat;

        public ClientWatches(String clientId,
                             List<WatchElement> watches, long lastHeartBeat) {
            this.clientId = clientId;
            this.watches = watches;
            this.lastHeartBeat = lastHeartBeat;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public List<WatchElement> getWatches() {
            return watches;
        }

        public void setWatches(List<WatchElement> watches) {
            this.watches = watches;
        }

        public long getLastHeartBeat() {
            return lastHeartBeat;
        }

        public void setLastHeartBeat(long lastHeartBeat) {
            this.lastHeartBeat = lastHeartBeat;
        }
    }
}
