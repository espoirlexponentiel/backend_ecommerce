package com.ecommerce.backend.entity;

public enum OrderStatus {
    EN_ATTENTE,
    VALIDEE,
    EN_PREPARATION,
    EXPEDIEE,
    LIVREE,
    ANNULEE;

    public static OrderStatus fromFlexibleString(String status) {
        if (status == null || status.isBlank()) {
            return EN_ATTENTE;
        }
        String s = status.trim().toUpperCase()
                .replace(" ", "_")
                .replace("É", "E")
                .replace("È", "E")
                .replace("Ê", "E")
                .replace("Ë", "E");

        if (s.contains("PREPAR")) return EN_PREPARATION;
        if (s.contains("EXPED")) return EXPEDIEE;
        if (s.contains("LIVR")) return LIVREE;
        if (s.contains("ANNUL")) return ANNULEE;
        if (s.contains("VALID")) return VALIDEE;
        if (s.contains("ATTENT")) return EN_ATTENTE;

        try {
            return OrderStatus.valueOf(s);
        } catch (IllegalArgumentException e) {
            return EN_ATTENTE;
        }
    }
}

