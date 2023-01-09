class DataPoint {
    private double valLivello;
    private long time;
    private String stato;
    private int percApertura;
    private String manMode ;

    public DataPoint(double valLivello, long time, String stato, int percApertura,String s) {
        this.valLivello = valLivello;
        this.time = time;
        this.stato = stato;
        this.percApertura=percApertura;
        this.manMode=s;
    }

    public double getValLivello() {
        return valLivello;
    }

    public long getTime() {
        return time;
    }

    public String getStato() {
        return stato;
    }

    public int getPercApertura() {
        return percApertura;
    }

    public String getManMode() {
        return manMode;
    }
}
