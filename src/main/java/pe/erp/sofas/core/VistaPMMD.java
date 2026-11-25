package pe.erp.sofas.core;



public interface VistaPMMD {
    

    
    void onShow(IntentPMMD intentPMMD);



    interface ConCiclo extends VistaPMMD {
        
        void onHide();
    }
}
