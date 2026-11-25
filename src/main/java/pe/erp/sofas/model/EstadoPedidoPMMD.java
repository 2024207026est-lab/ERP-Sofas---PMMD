package pe.erp.sofas.model;



public enum EstadoPedidoPMMD {
    
    REGISTRADO("Registrado", "EDIT"),
    
    COTIZADO("Cotizado", "REQUEST_QUOTE"),
    
    CONFIRMADO("Confirmado", "VERIFIED"),
    
    EN_PRODUCCION("En producción", "BUILD"),
    
    LISTO("Listo para entrega", "ARCHIVE"),
    
    ENTREGADO("Entregado", "LOCAL_SHIPPING"),
    
    CANCELADO("Cancelado", "CANCEL");

    private final String nombre;

    private final String icono;

    EstadoPedidoPMMD(String nombre, String icono) {
        this.nombre = nombre;
        this.icono = icono;
    }

    public String nombre() {
        return nombre;
    }

    public String icono() {
        return icono;
    }

    public boolean cerrado() {
        return this == ENTREGADO || this == CANCELADO;
    }

    public EstadoPedidoPMMD siguiente() {
        return switch (this) {
            case REGISTRADO -> COTIZADO;
            case COTIZADO -> CONFIRMADO;
            case CONFIRMADO -> EN_PRODUCCION;
            case EN_PRODUCCION -> LISTO;
            case LISTO, ENTREGADO -> ENTREGADO;
            case CANCELADO -> CANCELADO;
        };
    }



    public static EstadoPedidoPMMD[] flujo() {
        return new EstadoPedidoPMMD[]{REGISTRADO, COTIZADO, CONFIRMADO, EN_PRODUCCION, LISTO, ENTREGADO};
    }

    @Override
    public String toString() {
        return nombre;
    }
}
