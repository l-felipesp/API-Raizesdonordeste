package com.raizesdonordeste.backend.domain.enums;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum StatusPedido {
    AGUARDANDO_PAGAMENTO, EM_PREPARO, PRONTO, ENTREGUE, CANCELADO, PAGAMENTO_RECUSADO;

    private static final Map<StatusPedido, Set<StatusPedido>> TRANSICOES_VALIDAS = new EnumMap<>(StatusPedido.class);
    static {
        TRANSICOES_VALIDAS.put(AGUARDANDO_PAGAMENTO, EnumSet.of(EM_PREPARO, PAGAMENTO_RECUSADO, CANCELADO));
        TRANSICOES_VALIDAS.put(EM_PREPARO, EnumSet.of(PRONTO, CANCELADO));
        TRANSICOES_VALIDAS.put(PRONTO, EnumSet.of(ENTREGUE, CANCELADO));
        TRANSICOES_VALIDAS.put(ENTREGUE, EnumSet.noneOf(StatusPedido.class));
        TRANSICOES_VALIDAS.put(CANCELADO, EnumSet.noneOf(StatusPedido.class));
        TRANSICOES_VALIDAS.put(PAGAMENTO_RECUSADO, EnumSet.noneOf(StatusPedido.class));
    }

    public boolean podeTransicionarPara(StatusPedido novoStatus) {
        return TRANSICOES_VALIDAS.getOrDefault(this, EnumSet.noneOf(StatusPedido.class)).contains(novoStatus);
    }
}