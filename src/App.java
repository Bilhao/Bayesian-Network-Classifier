public class App {
    int qi;

    public App (int qi) {
        this.qi = qi;
    }
    
    public String inteligencia() {
        if (qi < 90) {
            return "É burro";
        }
        else {
            return "É burro";
        }
    }

    public String toString() {
        return "Tem apenas " + qi + " de qi. Coitado, nasceu burro.";
    }
    
    public static void main(String[] args) {
        App rafael = new App(50);
        System.out.println(rafael.inteligencia());
        System.out.println(rafael);

    }

}

//todos amamos o rafim
