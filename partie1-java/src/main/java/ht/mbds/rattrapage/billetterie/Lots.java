package ht.mbds.rattrapage.billetterie;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Lots {

    private Lots() {
    }

    public static <T> List<List<T>> decouper(List<? extends T> elements, int taille) {
        if (taille <= 0) {
            throw new IllegalArgumentException("La taille doit etre strictement positive");
        }
        List<List<T>> lots = new ArrayList<>();
        for (int debut = 0; debut < elements.size(); debut += taille) {
            int fin = Math.min(debut + taille, elements.size());
            // Copie defensive : les lots ne dependent plus de la liste source
            lots.add(Collections.unmodifiableList(new ArrayList<T>(elements.subList(debut, fin))));
        }
        return Collections.unmodifiableList(lots);
    }
}
