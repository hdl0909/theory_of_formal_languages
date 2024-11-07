import java.util.*;

public class NFA {
    Set<Integer> states;
    Set<String> alphabet;
    Map<String, Map<Integer, Set<Integer>>> transitions;
    Set<Integer> initialStates;
    Set<Integer> finalStates;

    public NFA(Set<Integer> states, Set<String> alphabet, Map<String, Map<Integer, Set<Integer>>> transitions,
               Set<Integer> initialStates, Set<Integer> finalStates) {
        this.states = states;
        this.alphabet = alphabet;
        this.transitions = transitions;
        this.initialStates = initialStates;
        this.finalStates = finalStates;
    }
}
