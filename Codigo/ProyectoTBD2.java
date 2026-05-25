package proyectotbd2;

public class ProyectoTBD2 {

    public static void main(String[] args) {
        DashboardFrame dashboard =new DashboardFrame();
        dashboard.setVisible(true);
        new LoginFrame(dashboard);
    }
}
    

